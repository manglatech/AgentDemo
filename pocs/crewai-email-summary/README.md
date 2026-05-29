# CrewAI Email Summary POC

**ID:** `crewai-email-summary`

Same flow as `email-summary`, implemented with [CrewAI](https://docs.crewai.com/) agents and tools (Python), orchestrated from the Spring Boot launcher.

## Agents

| CrewAI agent | Role |
|--------------|------|
| Content Provider | `get_source_content` tool → hardcoded text |
| Summary Analyst | Llama via Ollama → 2–3 sentence summary |
| Email Coordinator | `send_summary_email` tool → Azure Communication Services |

## Setup

**Requires Python 3.10–3.13** (CrewAI does not support 3.14 yet).

```bash
cd pocs/crewai-email-summary
# macOS Homebrew example:
/opt/homebrew/opt/python@3.12/bin/python3.12 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

The Spring launcher uses `.venv/bin/python` automatically when the venv exists.

Or run: `./setup.sh` from this directory (installs deps into `.venv`).

Ensure **Ollama** is running with your model (`ollama pull llama3.2`).

Azure settings are passed from Spring via environment variables (see `application.yml` / `application-local.yml`).

## Run standalone

```bash
export OLLAMA_BASE_URL=http://127.0.0.1:11434/v1
export OLLAMA_MODEL=llama3.2:latest
export AZURE_EMAIL_ENABLED=true
export AZURE_COMMUNICATION_CONNECTION_STRING="endpoint=...;accesskey=..."
export AZURE_EMAIL_SENDER="DoNotReply@....azurecomm.net"
export AZURE_EMAIL_RECIPIENT="you@example.com"
python run_crew.py
```
