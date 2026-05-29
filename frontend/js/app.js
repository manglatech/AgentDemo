let selectedPocId = null;

const pocListEl = document.getElementById("poc-list");
const runBtn = document.getElementById("run-btn");
const runTitle = document.getElementById("run-title");
const runDescription = document.getElementById("run-description");
const runStatus = document.getElementById("run-status");
const runResults = document.getElementById("run-results");

async function loadPocs() {
  const response = await fetch("/api/pocs");
  if (!response.ok) {
    pocListEl.innerHTML = "<p class='muted'>Failed to load POCs.</p>";
    return;
  }

  const pocs = await response.json();
  if (pocs.length === 0) {
    pocListEl.innerHTML = "<p class='muted'>No POCs registered.</p>";
    return;
  }

  pocListEl.innerHTML = "";
  pocs.forEach((poc) => {
    const card = document.createElement("button");
    card.type = "button";
    card.className = "poc-card";
    card.dataset.pocId = poc.id;
    card.innerHTML = `
      <h3>${escapeHtml(poc.name)}</h3>
      <p>${escapeHtml(poc.description)}</p>
      <div class="folder">${escapeHtml(poc.folder)}</div>
    `;
    card.addEventListener("click", () => selectPoc(poc, card));
    pocListEl.appendChild(card);
  });

  selectPoc(pocs[0], pocListEl.querySelector(".poc-card"));
}

function selectPoc(poc, cardEl) {
  selectedPocId = poc.id;
  document.querySelectorAll(".poc-card").forEach((el) => el.classList.remove("selected"));
  cardEl.classList.add("selected");
  runTitle.textContent = poc.name;
  runDescription.textContent = poc.description;
  runBtn.disabled = false;
  runResults.innerHTML = "";
  runStatus.classList.add("hidden");
}

runBtn.addEventListener("click", async () => {
  if (!selectedPocId) return;

  runBtn.disabled = true;
  runResults.innerHTML = "";
  runStatus.textContent = "Running POC… (Ollama / Azure may take a moment)";
  runStatus.className = "run-status running";

  try {
    const response = await fetch(`/api/pocs/${selectedPocId}/run`, { method: "POST" });
    const result = await response.json();

    if (!response.ok) {
      runStatus.textContent = result.message || "Run failed";
      runStatus.className = "run-status running";
      return;
    }

    runStatus.classList.add("hidden");
    renderResult(result);
  } catch (err) {
    runStatus.textContent = "Network error: " + err.message;
    runStatus.className = "run-status running";
  } finally {
    runBtn.disabled = false;
  }
});

function renderResult(result) {
  const banner = document.createElement("div");
  banner.className = `result-banner ${result.status}`;
  banner.textContent = `Status: ${result.status}`;
  if (result.errorMessage) {
    banner.textContent += ` — ${result.errorMessage}`;
  }
  runResults.appendChild(banner);

  (result.steps || []).forEach((step) => {
    const el = document.createElement("div");
    el.className = `step ${step.status}`;
    el.innerHTML = `
      <div class="step-header">
        <span class="step-agent">${escapeHtml(step.agent)}</span>
        <span class="step-status">${escapeHtml(step.status)}</span>
      </div>
      <pre class="step-detail">${escapeHtml(step.detail || "")}</pre>
    `;
    runResults.appendChild(el);
  });
}

function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

loadPocs();
