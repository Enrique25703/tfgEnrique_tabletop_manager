(function () {
  const popup = document.getElementById("popupCreador");
  const form40k = document.getElementById("form40k");
  const selectFaccion = document.getElementById("faccion");
  const selectEjercito = document.getElementById("ejercito");
  const plantillaEjercitos = document.getElementById("ejercitosPlantilla");

  if (!popup || !form40k || !selectFaccion || !selectEjercito || !plantillaEjercitos) {
    return;
  }

  function abrirPopup() {
    popup.classList.add("visible");
  }

  function cerrarPopup() {
    popup.classList.remove("visible");
  }

  function mostrarFormulario40k() {
    form40k.style.display = "block";
  }

  function mostrarErrorAos() {
    alert("Error al cargar Age of Sigmar 4º edicion.");
  }

  function actualizarEjercitos() {
    const faccionSeleccionada = selectFaccion.value;
    const opciones = plantillaEjercitos.querySelectorAll("option");

    selectEjercito.innerHTML = "";
    selectEjercito.appendChild(new Option("Selecciona un ejercito", ""));

    opciones.forEach(function (opcion) {
      if (opcion.dataset.faccion === faccionSeleccionada) {
        selectEjercito.appendChild(new Option(opcion.textContent, opcion.value));
      }
    });
  }

  document.getElementById("abrirPopupCreador").addEventListener("click", abrirPopup);
  document.getElementById("cerrarPopupCreador").addEventListener("click", cerrarPopup);
  document.getElementById("mostrarFormulario40k").addEventListener("click", mostrarFormulario40k);
  document.getElementById("mostrarErrorAos").addEventListener("click", mostrarErrorAos);
  selectFaccion.addEventListener("change", actualizarEjercitos);
})();
