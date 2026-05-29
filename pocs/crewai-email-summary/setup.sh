#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

pick_python() {
  for candidate in \
    "${PYTHON:-}" \
    "$(command -v python3.12 2>/dev/null || true)" \
    "/opt/homebrew/opt/python@3.12/bin/python3.12" \
    "/usr/local/opt/python@3.12/bin/python3.12" \
    "$(command -v python3.13 2>/dev/null || true)" \
    "$(command -v python3.11 2>/dev/null || true)"; do
    if [[ -n "$candidate" && -x "$candidate" ]]; then
      echo "$candidate"
      return 0
    fi
  done
  return 1
}

PY=$(pick_python) || {
  echo "CrewAI requires Python 3.10–3.13. Install one, e.g.: brew install python@3.12" >&2
  exit 1
}

ver=$("$PY" -c 'import sys; print(f"{sys.version_info.major}.{sys.version_info.minor}")')
major=${ver%%.*}
minor=${ver#*.}
if (( major != 3 || minor < 10 || minor > 13 )); then
  echo "Selected $PY ($ver) is not supported. Use Python 3.10–3.13." >&2
  exit 1
fi

echo "Using $PY ($ver)"
rm -rf .venv
"$PY" -m venv .venv
.venv/bin/pip install --upgrade pip
.venv/bin/pip install -r requirements.txt
echo "Done. Virtualenv: $(pwd)/.venv"
