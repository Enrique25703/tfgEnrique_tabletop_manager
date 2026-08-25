(function () {
  const popup = document.getElementById("popupCreador");
  const botonAbrir = document.getElementById("abrirPopupCreador");
  const botonCerrar = document.getElementById("cerrarPopupCreadorNuevo");
  const formulario = document.getElementById("formCrearLista");
  const campoFormato = document.getElementById("formatoJuegoSeleccionado");
  const campoEjercito = document.getElementById("ejercitoSeleccionado");
  const resumenJuego = document.getElementById("resumenJuegoSeleccionado");
  const selectorFaccion = document.getElementById("faccionSelector");
  const bloqueEjercito = document.getElementById("bloqueEjercito");
  const selectorEjercito = document.getElementById("ejercitoSelector");
  const campoNombre = document.getElementById("nombreListaNuevo");
  const opcionesJuego = document.querySelectorAll("[data-game-choice]");
  const plantilla40k = document.getElementById("ejercitosPlantilla");
  const plantillaAos = document.getElementById("ejercitosPlantillaAos");

  if (!popup || !botonAbrir || !botonCerrar || !formulario || !campoFormato || !campoEjercito
    || !resumenJuego || !selectorFaccion || !bloqueEjercito || !selectorEjercito || !campoNombre
    || !plantilla40k || !plantillaAos || opcionesJuego.length === 0) {
    return;
  }

  const juegos = {
    WH40K_11: {
      action: "/creador-listas-40k",
      label: "Warhammer 40.000",
      plantilla: plantilla40k
    },
    AOS_4: {
      action: "/creador-listas-aos",
      label: "Age of Sigmar",
      plantilla: plantillaAos
    }
  };

  let juegoSeleccionado = "";

  function obtenerMapaFacciones(plantilla) {
    const mapa = new Map();
    plantilla.querySelectorAll("option").forEach(function (opcion) {
      const faccion = (opcion.dataset.faccion || "").trim();
      const ejercito = (opcion.value || "").trim();
      if (!faccion || !ejercito) {
        return;
      }
      if (!mapa.has(faccion)) {
        mapa.set(faccion, []);
      }
      mapa.get(faccion).push(ejercito);
    });
    return mapa;
  }

  function reiniciarFormulario() {
    selectorFaccion.innerHTML = '<option value="">Selecciona una faccion</option>';
    selectorEjercito.innerHTML = '<option value="">Selecciona un ejercito</option>';
    selectorEjercito.value = "";
    selectorEjercito.required = false;
    bloqueEjercito.hidden = true;
    campoEjercito.value = "";
  }

  function actualizarResumen() {
    if (!juegoSeleccionado) {
      resumenJuego.textContent = "Selecciona un juego para continuar.";
      return;
    }
    resumenJuego.textContent = juegos[juegoSeleccionado].label + ": elige faccion, define el nombre y el limite de puntos.";
  }

  function cargarFacciones() {
    reiniciarFormulario();
    if (!juegoSeleccionado) {
      return;
    }

    const mapa = obtenerMapaFacciones(juegos[juegoSeleccionado].plantilla);
    Array.from(mapa.keys()).sort(function (a, b) {
      return a.localeCompare(b, "es");
    }).forEach(function (faccion) {
      selectorFaccion.appendChild(new Option(faccion, faccion));
    });
  }

  function actualizarEjercitos() {
    selectorEjercito.innerHTML = '<option value="">Selecciona un ejercito</option>';
    selectorEjercito.value = "";
    campoEjercito.value = "";
    selectorEjercito.required = false;
    bloqueEjercito.hidden = true;

    if (!juegoSeleccionado || !selectorFaccion.value) {
      return;
    }

    const ejercitos = obtenerMapaFacciones(juegos[juegoSeleccionado].plantilla).get(selectorFaccion.value) || [];
    if (ejercitos.length <= 1) {
      campoEjercito.value = ejercitos[0] || selectorFaccion.value;
      return;
    }

    ejercitos.forEach(function (ejercito) {
      selectorEjercito.appendChild(new Option(ejercito, ejercito));
    });
    bloqueEjercito.hidden = false;
    selectorEjercito.required = true;
  }

  function seleccionarJuego(codigo) {
    juegoSeleccionado = codigo;
    campoFormato.value = codigo;
    formulario.action = juegos[codigo].action;
    formulario.hidden = false;
    opcionesJuego.forEach(function (opcion) {
      opcion.classList.toggle("is-selected", opcion.dataset.gameChoice === codigo);
    });
    actualizarResumen();
    cargarFacciones();
    selectorFaccion.focus();
  }

  function abrirPopup() {
    popup.classList.add("visible");
  }

  function cerrarPopup() {
    popup.classList.remove("visible");
  }

  opcionesJuego.forEach(function (opcion) {
    opcion.addEventListener("click", function () {
      seleccionarJuego(opcion.dataset.gameChoice);
    });
  });

  botonAbrir.addEventListener("click", abrirPopup);
  botonCerrar.addEventListener("click", cerrarPopup);
  selectorFaccion.addEventListener("change", actualizarEjercitos);
  selectorEjercito.addEventListener("change", function () {
    campoEjercito.value = selectorEjercito.value;
  });

  formulario.addEventListener("submit", function (event) {
    if (!juegoSeleccionado) {
      event.preventDefault();
      return;
    }
    if (!selectorFaccion.value) {
      event.preventDefault();
      selectorFaccion.focus();
      return;
    }
    if (!campoEjercito.value) {
      event.preventDefault();
      if (!bloqueEjercito.hidden) {
        selectorEjercito.focus();
      }
      return;
    }
    if (!campoNombre.value.trim()) {
      event.preventDefault();
      campoNombre.focus();
    }
  });
})();
