(function () {
  const popup = document.getElementById("popupCreador");
  const form40k = document.getElementById("form40k");
  const formAos = document.getElementById("formAos");
  const selectFaccion = document.getElementById("faccion");
  const selectEjercito = document.getElementById("ejercito");
  const plantillaEjercitos = document.getElementById("ejercitosPlantilla");
  const botonCrear = document.getElementById("crearLista40k");
  const selectFaccionAos = document.getElementById("faccionAos");
  const selectEjercitoAos = document.getElementById("ejercitoAos");
  const plantillaEjercitosAos = document.getElementById("ejercitosPlantillaAos");
  const botonCrearAos = document.getElementById("crearListaAos");

  if (!popup || !form40k || !formAos || !selectFaccion || !selectEjercito || !plantillaEjercitos || !botonCrear
    || !selectFaccionAos || !selectEjercitoAos || !plantillaEjercitosAos || !botonCrearAos) {
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
    formAos.style.display = "none";
    actualizarEstadoFormulario();
  }

  function mostrarFormularioAos() {
    formAos.style.display = "block";
    form40k.style.display = "none";
    actualizarEstadoFormularioAos();
  }

  function actualizarEstadoFormulario() {
    const faccionSeleccionada = selectFaccion.value !== "";
    const ejercitoSeleccionado = selectEjercito.value !== "";

    selectEjercito.disabled = !faccionSeleccionada;
    botonCrear.toggleAttribute("aria-disabled", !(faccionSeleccionada && ejercitoSeleccionado));
  }

  function actualizarEstadoFormularioAos() {
    const faccionSeleccionada = selectFaccionAos.value !== "";
    const ejercitoSeleccionado = selectEjercitoAos.value !== "";

    selectEjercitoAos.disabled = !faccionSeleccionada;
    botonCrearAos.toggleAttribute("aria-disabled", !(faccionSeleccionada && ejercitoSeleccionado));
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

    selectEjercito.value = "";
    actualizarEstadoFormulario();
  }

  function actualizarEjercitosAos() {
    const faccionSeleccionada = selectFaccionAos.value;
    const opciones = plantillaEjercitosAos.querySelectorAll("option");

    selectEjercitoAos.innerHTML = "";
    selectEjercitoAos.appendChild(new Option("Selecciona un ejercito", ""));

    opciones.forEach(function (opcion) {
      if (opcion.dataset.faccion === faccionSeleccionada) {
        selectEjercitoAos.appendChild(new Option(opcion.textContent, opcion.value));
      }
    });

    selectEjercitoAos.value = "";
    actualizarEstadoFormularioAos();
  }

  document.getElementById("abrirPopupCreador").addEventListener("click", abrirPopup);
  document.getElementById("cerrarPopupCreador").addEventListener("click", cerrarPopup);
  document.getElementById("mostrarFormulario40k").addEventListener("click", mostrarFormulario40k);
  document.getElementById("mostrarFormularioAos").addEventListener("click", mostrarFormularioAos);
  selectFaccion.addEventListener("change", actualizarEjercitos);
  selectEjercito.addEventListener("change", actualizarEstadoFormulario);
  selectFaccionAos.addEventListener("change", actualizarEjercitosAos);
  selectEjercitoAos.addEventListener("change", actualizarEstadoFormularioAos);
  form40k.addEventListener("submit", function (event) {
    if (selectFaccion.value === "" || selectEjercito.value === "") {
      event.preventDefault();

      if (selectFaccion.value === "") {
        selectFaccion.focus();
        return;
      }

      selectEjercito.focus();
    }
  });
  formAos.addEventListener("submit", function (event) {
    if (selectFaccionAos.value === "" || selectEjercitoAos.value === "") {
      event.preventDefault();

      if (selectFaccionAos.value === "") {
        selectFaccionAos.focus();
        return;
      }

      selectEjercitoAos.focus();
    }
  });
  actualizarEstadoFormulario();
  actualizarEstadoFormularioAos();
})();
