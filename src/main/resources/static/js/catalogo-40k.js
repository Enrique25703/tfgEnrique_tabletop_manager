(function () {
  const form = document.getElementById("filtroCatalogoForm");
  const selectFaccion = document.getElementById("faccion");

  if (!form || !selectFaccion) {
    return;
  }

  selectFaccion.addEventListener("change", function () {
    form.submit();
  });
})();
