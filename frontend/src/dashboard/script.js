function handleClick(item) {
    window.location.href = `/dashboard/management/${item}?from=/dashboard`;
}

document
    .querySelector("#hutch")
    .addEventListener("click", () => handleClick("hutch"));
document
    .querySelector("#meadow")
    .addEventListener("click", () => handleClick("meadow"));
document
    .querySelector("#chicken-coop")
    .addEventListener("click", () => handleClick("chicken-coop"));
document
    .querySelector("#assets")
    .addEventListener("click", () => handleClick("assets"));
