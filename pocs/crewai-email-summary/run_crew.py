#!/usr/bin/env python3
"""CrewAI POC: content → Llama summary (Ollama) → Azure Email. Prints JSON to stdout."""

from __future__ import annotations

import json
import os
import sys
from typing import Any

os.environ.setdefault("CREWAI_TRACING_ENABLED", "false")

from crewai import Agent, Crew, LLM, Process, Task
from crewai.tools import tool

POC_ID = "crewai-email-summary"

HARDCODED_TEXT = """
Our team shipped a multi-region deployment for the payment service last quarter.
We migrated three legacy endpoints to a unified API gateway, cut p99 latency from
420ms to 180ms, and added circuit breakers on all downstream calls. Onboarding
for new merchants dropped from two weeks to four days after we automated KYC
checks. We still have open work on fraud-model retraining and PCI audit prep,
but customer-reported checkout failures fell by 37% since January.
""".strip()


def _build_llm() -> LLM:
    base_url = os.environ.get("OLLAMA_BASE_URL", "http://127.0.0.1:11434/v1")
    model = os.environ.get("OLLAMA_MODEL", "llama3.2:latest")
    return LLM(model=model, base_url=base_url, api_key="ollama")


@tool("get_source_content")
def get_source_content() -> str:
    """Return the hardcoded business update source text."""
    return HARDCODED_TEXT


@tool("send_summary_email")
def send_summary_email(summary: str) -> str:
    """Send the summary as a plain-text email via Azure Communication Services."""
    enabled = os.environ.get("AZURE_EMAIL_ENABLED", "false").lower() == "true"
    if not enabled:
        return "SKIPPED: Azure email disabled (AZURE_EMAIL_ENABLED != true)"

    connection_string = os.environ.get("AZURE_COMMUNICATION_CONNECTION_STRING", "")
    sender = os.environ.get("AZURE_EMAIL_SENDER", "")
    recipient = os.environ.get("AZURE_EMAIL_RECIPIENT", "")
    subject = os.environ.get("AZURE_EMAIL_SUBJECT", "Multi-Agent Demo Summary")

    if not connection_string:
        return "SKIPPED: AZURE_COMMUNICATION_CONNECTION_STRING not set"

    from azure.communication.email import EmailClient

    client = EmailClient.from_connection_string(connection_string)
    message = {
        "senderAddress": sender,
        "recipients": {"to": [{"address": recipient}]},
        "content": {"subject": subject, "plainText": summary},
    }
    poller = client.begin_send(message)
    result = poller.result()
    operation_id = getattr(result, "id", None) or str(result)
    return f"Operation id: {operation_id}"


def _task_output_detail(task: Task) -> str:
    output = getattr(task, "output", None)
    if output is None:
        return ""
    return str(getattr(output, "raw", None) or getattr(output, "json", None) or output)


def _step_status(detail: str, agent: str = "") -> str:
    normalized = detail.lower()
    if detail.startswith("SKIPPED:") or "skipped:" in normalized:
        return "SKIPPED"
    if detail.startswith("FAILED:"):
        return "FAILED"
    if agent == "EmailAgent" and (
        "azure_email_enabled" in normalized
        or "unable to send" in normalized
        or "not enabled" in normalized
    ):
        return "SKIPPED"
    return "SUCCESS"


def run_crew() -> dict[str, Any]:
    llm = _build_llm()

    content_agent = Agent(
        role="Content Provider",
        goal="Provide the source business update text exactly as given by the tool.",
        backstory="You supply fixed operational update text for downstream agents.",
        tools=[get_source_content],
        llm=llm,
        verbose=False,
        allow_delegation=False,
    )

    summary_agent = Agent(
        role="Summary Analyst",
        goal="Produce a concise 2-3 sentence summary focused on outcomes and metrics.",
        backstory="You distill long operational updates into executive summaries.",
        llm=llm,
        verbose=False,
        allow_delegation=False,
    )

    email_agent = Agent(
        role="Email Coordinator",
        goal="Deliver the final summary to stakeholders via Azure email.",
        backstory="You send transactional email using the send_summary_email tool.",
        tools=[send_summary_email],
        llm=llm,
        verbose=False,
        allow_delegation=False,
    )

    content_task = Task(
        description=(
            "Call the get_source_content tool and return the full source text "
            "without shortening or paraphrasing."
        ),
        expected_output="The complete hardcoded business update text.",
        agent=content_agent,
    )

    summary_task = Task(
        description=(
            "Using the source text from the previous task, write ONLY a new 2-3 sentence "
            "executive summary focused on outcomes and metrics. Do not repeat the full source text."
        ),
        expected_output="A short plain-text summary (2-3 sentences only).",
        agent=summary_agent,
        context=[content_task],
    )

    email_task = Task(
        description=(
            "Send the summary from the previous task using the send_summary_email tool. "
            "Pass only the summary text as the argument."
        ),
        expected_output="Confirmation from the email tool including operation id or skip reason.",
        agent=email_agent,
        context=[summary_task],
    )

    crew = Crew(
        agents=[content_agent, summary_agent, email_agent],
        tasks=[content_task, summary_task, email_task],
        process=Process.sequential,
        verbose=False,
    )

    crew.kickoff()

    content_detail = _task_output_detail(content_task) or HARDCODED_TEXT
    summary_detail = _task_output_detail(summary_task)
    email_detail = _task_output_detail(email_task)

    steps = [
        {
            "agent": "ContentAgent",
            "status": _step_status(content_detail, "ContentAgent"),
            "detail": content_detail,
        },
        {
            "agent": "SummaryAgent",
            "status": _step_status(summary_detail, "SummaryAgent"),
            "detail": summary_detail,
        },
        {
            "agent": "EmailAgent",
            "status": _step_status(email_detail, "EmailAgent"),
            "detail": email_detail,
        },
    ]

    email_status = steps[2]["status"]
    if email_status == "SUCCESS":
        overall = "SUCCESS"
        error = None
    elif email_status == "SKIPPED":
        overall = "PARTIAL"
        error = None
    elif email_status == "FAILED":
        overall = "PARTIAL"
        error = steps[2]["detail"]
    else:
        overall = "SUCCESS"
        error = None

    return {"pocId": POC_ID, "status": overall, "steps": steps, "errorMessage": error}


def main() -> None:
    try:
        result = run_crew()
        print(json.dumps(result, ensure_ascii=False))
    except Exception as exc:  # noqa: BLE001
        payload = {
            "pocId": POC_ID,
            "status": "FAILED",
            "steps": [{"agent": "CrewAI", "status": "FAILED", "detail": str(exc)}],
            "errorMessage": str(exc),
        }
        print(json.dumps(payload, ensure_ascii=False))
        sys.exit(1)


if __name__ == "__main__":
    main()
