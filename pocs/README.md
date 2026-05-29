# POCs

Each proof-of-concept lives in its own folder:

```
pocs/
  <poc-id>/          # Docs and metadata
src/main/java/com/agentdemo/pocs/<package>/   # Java implementation
```

## Add a new POC

1. Create `pocs/<poc-id>/README.md` describing the POC.
2. Add Java package `com.agentdemo.pocs.<package>` with agents/services.
3. Implement `com.agentdemo.platform.spi.Poc` as a `@Component`.
4. Add config under `pocs.<poc-id>` in `application.yml` if needed.
5. Restart the app — the POC appears automatically in the UI.

## Python / CrewAI POCs

Place Python code under `pocs/<poc-id>/` with a `requirements.txt` and entry script.
Implement `Poc` in Java to invoke the script (see `crewai-email-summary`).
