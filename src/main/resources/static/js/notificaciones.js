document.querySelectorAll(".notification-dialog-trigger").forEach((trigger) => {
  trigger.addEventListener("click", () => {
    const dialog = document.getElementById(trigger.dataset.dialogId);
    if (dialog) dialog.showModal();
  });
});

document.querySelectorAll(".notification-dialog").forEach((dialog) => {
  dialog.querySelectorAll("[data-close-notification-dialog]").forEach((button) => {
    button.addEventListener("click", () => dialog.close());
  });

  dialog.addEventListener("click", (event) => {
    const bounds = dialog.getBoundingClientRect();
    const inside = event.clientX >= bounds.left && event.clientX <= bounds.right
      && event.clientY >= bounds.top && event.clientY <= bounds.bottom;
    if (!inside) dialog.close();
  });
});
