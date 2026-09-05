(function () {
  const buscador = document.querySelector("[data-community-filter]");
  const filas = Array.from(document.querySelectorAll("[data-community-row]"));
  const mensajeVacio = document.querySelector("[data-community-filter-empty]");
  if (!buscador || filas.length === 0) return;

  function normalizar(valor) {
    return String(valor || "")
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "");
  }

  buscador.addEventListener("input", function () {
    const termino = normalizar(buscador.value.trim());
    let visibles = 0;
    filas.forEach(function (fila) {
      const visible = normalizar(fila.dataset.communityName).includes(termino);
      fila.hidden = !visible;
      if (visible) visibles += 1;
    });
    if (mensajeVacio) mensajeVacio.hidden = visibles !== 0;
  });
})();
