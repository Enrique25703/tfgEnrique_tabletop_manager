document.querySelectorAll("[data-community-image-selector]").forEach((selector) => {
  const input = selector.querySelector("[data-community-image-input]");
  const preview = selector.querySelector("[data-community-image-preview]");
  const label = selector.querySelector("[data-community-image-label]");
  const dialog = selector.querySelector("[data-community-image-dialog]");
  const choices = Array.from(selector.querySelectorAll(".community-image-choice"));
  let temporaryValue = input.value.trim();

  function canonical(value) {
    try {
      return decodeURIComponent(String(value || ""));
    } catch (error) {
      return String(value || "");
    }
  }

  function choiceFor(value) {
    return choices.find((choice) => canonical(choice.dataset.value) === canonical(value)) || choices[0];
  }

  function markChoice(value) {
    choices.forEach((choice) => choice.classList.toggle("selected", canonical(choice.dataset.value) === canonical(value)));
  }

  function applyValue(value) {
    const choice = choiceFor(value);
    input.value = choice.dataset.value || "";
    preview.src = choice.dataset.preview || "/images/default-community.svg";
    label.textContent = choice.dataset.label || "Imagen genérica";
    temporaryValue = input.value;
    markChoice(temporaryValue);
  }

  selector.querySelector("[data-open-community-images]").addEventListener("click", () => {
    temporaryValue = input.value.trim();
    markChoice(temporaryValue);
    dialog.showModal();
  });

  selector.querySelector("[data-clear-community-image]").addEventListener("click", () => applyValue(""));
  selector.querySelector("[data-close-community-images]").addEventListener("click", () => dialog.close());
  selector.querySelector("[data-confirm-community-image]").addEventListener("click", () => {
    applyValue(temporaryValue);
    dialog.close();
  });

  choices.forEach((choice) => {
    choice.addEventListener("click", () => {
      temporaryValue = choice.dataset.value || "";
      markChoice(temporaryValue);
    });
  });

  dialog.addEventListener("click", (event) => {
    const bounds = dialog.getBoundingClientRect();
    const inside = event.clientX >= bounds.left && event.clientX <= bounds.right
      && event.clientY >= bounds.top && event.clientY <= bounds.bottom;
    if (!inside) dialog.close();
  });

  applyValue(input.value.trim());
});
