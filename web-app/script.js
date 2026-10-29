
const API_URL = `${window.location.origin}/JobTrack/api/applications`;

let applications = [];

const table = document.getElementById("applicationTable");
const searchInput = document.getElementById("searchInput");
const statusFilter = document.getElementById("statusFilter");
const dialog = document.getElementById("applicationDialog");
const form = document.getElementById("applicationForm");
const dialogTitle = document.getElementById("dialogTitle");
const dialogSubtitle = document.getElementById("dialogSubtitle");
const saveButton = document.getElementById("saveButton");

function escapeHTML(value) {
  return String(value ?? "").replace(/[&<>"']/g, char => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  })[char]);
}

async function readResponse(response) {
  const responseText = await response.text();

  let result;

  try {
    result = JSON.parse(responseText);
  } catch {
    console.error("Server returned a non-JSON response:", responseText);

    throw new Error(
      `Server returned an unexpected response (HTTP ${response.status}). Check the Tomcat logs.`
    );
  }

  if (!response.ok) {
    throw new Error(
      result.error || `Request failed with HTTP ${response.status}.`
    );
  }

  return result;
}

async function loadApplications() {
  try {
    const response = await fetch(API_URL);
    const data = await readResponse(response);

    if (!Array.isArray(data)) {
      throw new Error(data.error || "Unexpected API response");
    }

    applications = data;
    renderApplications();
  } catch (error) {
    console.error("Unable to load applications:", error);

    table.innerHTML = `
      <tr>
        <td colspan="5">
          Unable to load applications. Check the server and database.
        </td>
      </tr>`;

    document.getElementById("resultCount").textContent =
      "Applications could not be loaded";
  }
}

function renderApplications() {
  const search = searchInput.value.trim().toLowerCase();
  const status = statusFilter.value;

  const filtered = applications.filter(app => {
    const matchesSearch =
      String(app.company ?? "").toLowerCase().includes(search) ||
      String(app.role ?? "").toLowerCase().includes(search);

    const matchesStatus =
      status === "All" || app.status === status;

    return matchesSearch && matchesStatus;
  });

  table.innerHTML = filtered.map(app => {
    const statusClass = String(app.status ?? "")
      .toLowerCase()
      .replace(/[^a-z0-9_-]/g, "");

    return `
      <tr>
        <td class="company-name">${escapeHTML(app.company)}</td>
        <td class="role-name">${escapeHTML(app.role)}</td>
        <td class="date-text">${escapeHTML(app.date)}</td>
        <td>
          <span class="status status-${statusClass}">
            ${escapeHTML(app.status)}
          </span>
        </td>
        <td>
          <button
            type="button"
            class="action-button"
            data-edit="${Number(app.id)}">
            Edit
          </button>
          <button
            type="button"
            class="action-button"
            data-delete="${Number(app.id)}">
            Delete
          </button>
        </td>
      </tr>`;
  }).join("");

  document.getElementById("resultCount").textContent =
    `Showing ${filtered.length} of ${applications.length} applications`;

  document.getElementById("totalCount").textContent =
    applications.length;

  document.getElementById("interviewCount").textContent =
    applications.filter(app => app.status === "Interview").length;

  document.getElementById("selectedCount").textContent =
    applications.filter(app => app.status === "Selected").length;

  document.getElementById("assessmentCount").textContent =
    applications.filter(app => app.status === "Assessment").length;
}

searchInput.addEventListener("input", renderApplications);
statusFilter.addEventListener("change", renderApplications);

// Open the dialog in Add mode.
document.getElementById("addButton").addEventListener("click", () => {
  form.reset();

  form.elements.id.value = "";
  form.elements.date.value = new Date().toISOString().slice(0, 10);

  dialogTitle.textContent = "Add application";
  dialogSubtitle.textContent = "Enter your job details.";
  saveButton.textContent = "Save application";

  dialog.showModal();
});

// Close the application dialog.
document.getElementById("closeDialog").addEventListener("click", () => {
  dialog.close();
});

// Edit or delete an application.
table.addEventListener("click", async event => {
  const editButton = event.target.closest("[data-edit]");

  if (editButton) {
    const id = Number(editButton.dataset.edit);
    const application = applications.find(app => Number(app.id) === id);

    if (!application) {
      alert("Application not found. Please refresh the page.");
      return;
    }

    form.reset();

    form.elements.id.value = application.id;
    form.elements.company.value = application.company ?? "";
    form.elements.role.value = application.role ?? "";
    form.elements.date.value = application.date ?? "";
    form.elements.status.value = application.status ?? "Applied";
    form.elements.description.value = application.description ?? "";

    dialogTitle.textContent = "Edit application";
    dialogSubtitle.textContent = "Update your job details.";
    saveButton.textContent = "Update application";

    dialog.showModal();
    return;
  }

  const deleteButton = event.target.closest("[data-delete]");

  if (!deleteButton) {
    return;
  }

  const id = deleteButton.dataset.delete;

  const confirmed = confirm(
    "Are you sure you want to delete this application? This action cannot be undone."
  );

  if (!confirmed) {
    return;
  }

  deleteButton.disabled = true;

  try {
    const response = await fetch(
      `${API_URL}?id=${encodeURIComponent(id)}`,
      { method: "DELETE" }
    );

    await readResponse(response);
    await loadApplications();

    alert("Application deleted successfully!");
  } catch (error) {
    console.error("Unable to delete application:", error);
    alert(error.message || "Unable to delete application. Please try again.");
    await loadApplications();
  }
});

// Add a new application or update an existing one.
form.addEventListener("submit", async event => {
  event.preventDefault();

  const submitButton = form.querySelector('[type="submit"]');

  if (submitButton) {
    submitButton.disabled = true;
  }

  const id = form.elements.id.value.trim();

  const application = {
    company: form.elements.company.value.trim(),
    role: form.elements.role.value.trim(),
    status: form.elements.status.value,
    date: form.elements.date.value,
    description: form.elements.description.value.trim()
  };

  const isEditing = id !== "";

  try {
    let response;

    if (isEditing) {
      // Send update fields in the URL so Java doPut()
      // can read them through request.getParameter().
      const updateParams = new URLSearchParams({
        id,
        ...application
      });

      response = await fetch(`${API_URL}?${updateParams.toString()}`, {
        method: "PUT"
      });
    } else {
      // New applications continue to use POST form data.
      const requestBody = new URLSearchParams(application);

      response = await fetch(API_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
        },
        body: requestBody.toString()
      });
    }

    await readResponse(response);
    await loadApplications();

    dialog.close();
    form.reset();

    alert(
      isEditing
        ? "Application updated successfully!"
        : "Application added successfully!"
    );
  } catch (error) {
    console.error(
      isEditing ? "Unable to update application:" : "Unable to add application:",
      error
    );

    alert(
      error.message ||
      (isEditing
        ? "Unable to update application. Please try again."
        : "Unable to save application. Please try again.")
    );
  } finally {
    if (submitButton) {
      submitButton.disabled = false;
    }
  }
});

loadApplications();
