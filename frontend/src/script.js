let usersData = [];

function displayUsers(users) {
  for (let i = 0; i < 10; i++) {
    if (i < users.length) {
      const user = users[i];

      if (!user) {
        document.getElementById(i + "-0").textContent = "";
        document.getElementById(i + "-1").textContent = "";
        document.getElementById(i + "-2").textContent = "";
        document.getElementById(i + "-3").textContent = "";
        document.getElementById(i + "-4").textContent = "";
        continue;
      }

      if (user.rang.current < 4) {
        document.getElementById(i + "-0").innerHTML =
          user.rang.current +
          ' <span class="material-symbols-outlined">crown</span>';
      } else {
        document.getElementById(i + "-0").innerHTML =
          user.rang.current +
          ' <span class="material-symbols-outlined">workspace_premium</span>';
      }
      document.getElementById(i + "-1").textContent = user.nom;
      document.getElementById(i + "-2").textContent = user.production;
      document.getElementById(i + "-3").textContent = user.capacite;
      document.getElementById(i + "-4").textContent = user.ecus;
    }
  }
}

function sortTable(column, order) {
  document.getElementById(column + "Button").innerHTML =
    order === -1
      ? '<span class="material-symbols-outlined">arrow_upward_alt</span>'
      : '<span class="material-symbols-outlined">arrow_downward_alt</span>';
  document.getElementById(column + "Button").setAttribute("active", "true");

  // Réinitialiser les autres boutons de tri
  const columns = ["rang", "nom", "production", "capacite", "ecus"];
  columns.forEach((col) => {
    if (col !== column) {
      document.getElementById(col + "Button").innerHTML =
        '<span class="material-symbols-outlined">unfold_more</span>';
      document.getElementById(col + "Button").setAttribute("active", "none");
    }
  });
  if (!Array.isArray(usersData) || usersData.length === 0) {
    return;
  }

  usersData.sort((a, b) => {
    const valA = column === "rang" ? a.rang.current : a[column];
    const valB = column === "rang" ? b.rang.current : b[column];
    if (valA < valB) {
      return order;
    }
    if (valA > valB) {
      return -order;
    }
    return 0;
  });
  displayUsers(usersData);
}

fetch("api/classement")
  .then((response) => {
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    return response.json();
  })
  .catch((error) => {
    console.error("Error fetching user data from api/classement:", error);
    return fetch("/frontend/src/fakeapi/users.json").then((response) => {
      if (!response.ok) {
        throw new Error(`Fallback HTTP ${response.status}`);
      }
      return response.json();
    });
  })
  .then((data) => {
    usersData = Array.isArray(data) ? data : [];
    displayUsers(usersData);
  })
  .catch((fallbackError) => {
    console.error("Error fetching fallback users.json:", fallbackError);
    usersData = [];
    displayUsers(usersData);
  });
