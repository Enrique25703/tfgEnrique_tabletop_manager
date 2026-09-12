(function () {
  const form = document.getElementById("filtroCatalogoForm");
  const selectFaccion = document.getElementById("faccion");
  const selectEjercito = document.getElementById("ejercito");

  if (!form || !selectFaccion) {
    return;
  }

  selectFaccion.addEventListener("change", function () {
    selectEjercito.value = "";
    form.requestSubmit();
  });

  const buscar = document.getElementById("buscarUnidad");
  const filtros = [
    {control: document.getElementById("ocultarLegends"), atributo: "legends"},
    {control: document.getElementById("ocultarAliados"), atributo: "aliado"},
    {control: document.getElementById("ocultarEstructuras"), atributo: "estructura"}
  ];
  if (!buscar || filtros.some(filtro => !filtro.control)) return;

  const filas = Array.from(document.querySelectorAll("[data-unidad]"));
  const enlaces = Array.from(document.querySelectorAll(".detalle-unidad"));
  const normalizar = texto => texto.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLocaleLowerCase("es");
  const nombres = filas.map(fila => normalizar(fila.dataset.nombre || ""));

  function aplicarFiltros(actualizarUrl = true) {
    const consulta = normalizar(buscar.value.trim());
    let visibles = 0;
    filas.forEach((fila, indice) => {
      fila.hidden = !nombres[indice].includes(consulta) || filtros.some(filtro =>
        filtro.control.checked && fila.dataset[filtro.atributo] === "true");
      if (!fila.hidden) visibles++;
    });
    const recuento = document.getElementById("recuentoUnidades");
    if (recuento) recuento.textContent = visibles + " de " + filas.length + " unidades";
    const vacio = document.getElementById("sinUnidadesFiltradas");
    if (vacio) vacio.hidden = visibles !== 0;

    const url = new URL(window.location.href);
    const valores = new Map([["buscarUnidad", buscar.value.trim()]]);
    filtros.forEach(filtro => valores.set(filtro.control.name, filtro.control.checked ? "true" : ""));
    valores.forEach((valor, nombre) => valor ? url.searchParams.set(nombre, valor) : url.searchParams.delete(nombre));
    if (actualizarUrl) window.history.replaceState(null, "", url);
    enlaces.forEach(enlace => {
      const destino = new URL(enlace.href);
      valores.forEach((valor, nombre) => valor ? destino.searchParams.set(nombre, valor) : destino.searchParams.delete(nombre));
      enlace.href = destino.href;
    });
  }

  buscar.addEventListener("input", () => aplicarFiltros());
  filtros.forEach(filtro => filtro.control.addEventListener("change", () => aplicarFiltros()));
  document.getElementById("limpiarFiltrosUnidades").addEventListener("click", () => {
    buscar.value = "";
    filtros.forEach(filtro => { filtro.control.checked = false; });
    aplicarFiltros();
    buscar.focus();
  });
  window.addEventListener("pageshow", () => aplicarFiltros(false));
  aplicarFiltros(false);
})();
