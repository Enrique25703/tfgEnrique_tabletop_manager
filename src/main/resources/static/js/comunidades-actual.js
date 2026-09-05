(function () {
  const tarjetas = Array.from(document.querySelectorAll("[data-event-target]"));
  const detalles = Array.from(document.querySelectorAll(".event-detail"));

  tarjetas.forEach(function (tarjeta) {
    tarjeta.addEventListener("click", function () {
      const objetivo = tarjeta.dataset.eventTarget;
      tarjetas.forEach(function (otraTarjeta) {
        const activa = otraTarjeta === tarjeta;
        otraTarjeta.classList.toggle("active", activa);
        otraTarjeta.setAttribute("aria-expanded", String(activa));
      });
      detalles.forEach(function (detalle) {
        const activo = detalle.id === objetivo;
        detalle.hidden = !activo;
        detalle.classList.toggle("active", activo);
      });
    });
  });

  const modal = document.getElementById("modalUbicacionEvento");
  const contenedorMapa = document.getElementById("mapaUbicacionEvento");
  const tituloMapa = document.getElementById("tituloMapaEvento");
  const direccionMapa = document.getElementById("direccionMapaEvento");
  const botonesUbicacion = Array.from(document.querySelectorAll(".location-map-button"));
  let mapa = null;
  let marcador = null;
  let ultimoBoton = null;

  function cerrarMapa() {
    if (!modal || modal.hidden) {
      return;
    }
    modal.hidden = true;
    modal.setAttribute("aria-hidden", "true");
    document.body.classList.remove("map-open");
    if (ultimoBoton) {
      ultimoBoton.focus();
    }
  }

  function prepararMapa(latitud, longitud, titulo, direccion) {
    if (typeof L === "undefined" || !contenedorMapa) {
      return;
    }
    if (!mapa) {
      mapa = L.map(contenedorMapa, { zoomControl: true });
      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        maxZoom: 19,
        attribution: "&copy; OpenStreetMap contributors"
      }).addTo(mapa);
    }
    if (marcador) {
      marcador.remove();
    }
    marcador = L.marker([latitud, longitud]).addTo(mapa);
    marcador.bindPopup("<strong>" + escaparHtml(titulo) + "</strong><br>" + escaparHtml(direccion)).openPopup();
    mapa.setView([latitud, longitud], 16);
    window.setTimeout(function () { mapa.invalidateSize(); }, 80);
  }

  function escaparHtml(texto) {
    const elemento = document.createElement("span");
    elemento.textContent = texto || "";
    return elemento.innerHTML;
  }

  botonesUbicacion.forEach(function (boton) {
    boton.addEventListener("click", function () {
      if (!modal) {
        return;
      }
      const latitud = Number.parseFloat(boton.dataset.mapLat || "");
      const longitud = Number.parseFloat(boton.dataset.mapLon || "");
      if (!Number.isFinite(latitud) || !Number.isFinite(longitud)) {
        return;
      }

      ultimoBoton = boton;
      tituloMapa.textContent = boton.dataset.mapTitle || "Ubicación del evento";
      direccionMapa.textContent = boton.dataset.mapAddress || "";
      modal.hidden = false;
      modal.setAttribute("aria-hidden", "false");
      document.body.classList.add("map-open");
      prepararMapa(latitud, longitud, tituloMapa.textContent, direccionMapa.textContent);
      const cerrar = modal.querySelector(".map-close");
      if (cerrar) {
        cerrar.focus();
      }
    });
  });

  document.querySelectorAll("[data-close-map]").forEach(function (boton) {
    boton.addEventListener("click", cerrarMapa);
  });

  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape") {
      cerrarMapa();
    }
  });

  const modalComunidad = document.getElementById("modalCrearComunidad");
  const abrirComunidad = document.getElementById("abrirCrearComunidad");

  function cerrarComunidad() {
    if (!modalComunidad || modalComunidad.hidden) return;
    modalComunidad.hidden = true;
    modalComunidad.setAttribute("aria-hidden", "true");
    document.body.classList.remove("map-open");
    if (abrirComunidad) abrirComunidad.focus();
  }

  if (modalComunidad && abrirComunidad) {
    abrirComunidad.addEventListener("click", function () {
      modalComunidad.hidden = false;
      modalComunidad.setAttribute("aria-hidden", "false");
      document.body.classList.add("map-open");
      const nombre = document.getElementById("nombreComunidad");
      if (nombre) nombre.focus();
    });
    modalComunidad.querySelectorAll("[data-close-community-modal]").forEach(function (boton) {
      boton.addEventListener("click", cerrarComunidad);
    });
    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape") cerrarComunidad();
    });
  }
})();
