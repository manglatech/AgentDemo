# Agent Demo

A Spring Boot **POC launcher** for experimenting with multi-agent workflows. Pick a proof-of-concept in the web UI, run it, and inspect step-by-step results.

Includes two email-summary demos with the same pipeline:

1. **Java multi-agent** — Spring components + Ollama + Azure Communication Services Email  
2. **CrewAI** — Python crew (agents, tasks, tools) invoked from Java

## Architecture

```text
Browser (frontend/)
    │
    ▼
REST API  GET /api/pocs  |  POST /api/pocs/{id}/run
    │
    ▼
PocRegistry → Poc implementations
    │
    ├── email-summary        (Java agents)
    └── crewai-email-summary (Python CrewAI script)
```

| Layer | Path |
|-------|------|
| UI | `frontend/` → served as static assets |
| Platform | `src/main/java/com/agentdemo/platform/` |
| POCs | `pocs/<id>/` (docs) + `com.agentdemo.pocs.*` (code) |

## Prerequisites

- **Java 21**
- **Maven 3.9+**
- **[Ollama](https://ollama.com/)** with a model pulled (e.g. `ollama pull llama3.2`)
- **Azure Communication Services** with Email domain linked ([connect domain guide](https://learn.microsoft.com/en-us/azure/communication-services/quickstarts/email/connect-email-communication-resource))
- **CrewAI POC only:** Python **3.10–3.13** and `./pocs/crewai-email-summary/setup.sh`

## Quick start

```bash
# 1. Start Ollama
ollama serve
ollama pull llama3.2

# 2. Configure secrets (optional, for email)
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# Edit application-local.yml with your Azure connection string

# 3. CrewAI POC setup (one time)
cd pocs/crewai-email-summary && ./setup.sh && cd ../..

# 4. Run the app
mvn spring-boot:run
```

Open **http://localhost:8080**, select a POC, and click **Run POC**.

## Included POCs

| ID | Name | Stack |
|----|------|--------|
| `email-summary` | Email Summary (Multi-Agent) | Java → Ollama → Azure Email |
| `crewai-email-summary` | Email Summary (CrewAI) | CrewAI + Ollama → Azure Email |

**Agent flow (both POCs):**

| Step | Agent | Action |
|------|--------|--------|
| 1 | Content | Return hardcoded business update text |
| 2 | Summary | Summarize with Llama via Ollama |
| 3 | Email | Send summary via Azure Email |

See `pocs/email-summary/README.md` and `pocs/crewai-email-summary/README.md` for details.

## Configuration

Main settings: `src/main/resources/application.yml`

Secrets: `src/main/resources/application-local.yml` (gitignored). Copy from `application-local.yml.example`.

```yaml
pocs:
  email-summary:
    ollama:
      base-url: http://127.0.0.1:11434
      model: llama3.2:latest
    azure:
      communication:
        email:
          enabled: true
          sender: "DoNotReply@<your-domain>.azurecomm.net"
          recipient: "you@example.com"
          subject: "Multi-Agent Demo Summary"
          # connection-string in application-local.yml
```

CrewAI uses the same Azure/Ollama settings under `pocs.crewai-email-summary`.

## REST API

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/pocs` | List registered POCs |
| `POST` | `/api/pocs/{id}/run` | Run a POC and return steps + status |

Example:

```bash
curl http://localhost:8080/api/pocs
curl -X POST http://localhost:8080/api/pocs/email-summary/run
```

## Add a new POC

1. Add `pocs/<poc-id>/README.md`
2. Implement `com.agentdemo.platform.spi.Poc` as a `@Component`
3. Add config under `pocs.<poc-id>` in `application.yml` if needed
4. Restart — the POC appears in the UI automatically

See `pocs/README.md` and `pocs/_template/README.md`.

## Project structure

```text
AgentDemo/
├── frontend/                 # POC launcher UI
├── pocs/
│   ├── email-summary/
│   ├── crewai-email-summary/ # Python + setup.sh
│   └── _template/
├── src/main/java/com/agentdemo/
│   ├── platform/             # API, registry, Poc SPI
│   └── pocs/                 # POC implementations
├── pom.xml
└── README.md
```

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Email skipped | Set `connection-string` in `application-local.yml` and `enabled: true` |
| `DomainNotLinked` | Link email domain to Communication Services in Azure Portal |
| Ollama timeout | Ensure `ollama serve` is running and model is pulled |
| CrewAI fails to start | Run `pocs/crewai-email-summary/setup.sh` (Python 3.10–3.13) |

## License

Internal POC / demo project.

Added CodeReview Branch: 
Password: 
