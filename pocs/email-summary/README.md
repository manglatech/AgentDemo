# Email Summary POC

**ID:** `email-summary`

Multi-agent flow:

1. **ContentAgent** — returns hardcoded business update text
2. **SummaryAgent** — summarizes via Llama (Ollama)
3. **EmailAgent** — sends summary through Azure Communication Services Email

## Code location

- `src/main/java/com/agentdemo/pocs/emailsummary/`
- Config: `pocs.email-summary.*` in `application.yml`

## Configuration

See `application-local.yml.example` for Azure connection string.
