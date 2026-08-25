(function () {
  const app = document.getElementById("creadorListaApp");
  if (!app) {
    return;
  }

  let contadorUnidad = 0;
  let unidadActiva = null;
  const formatoJuego = app.dataset.formatoJuego || "";
  const nombreLista = app.dataset.nombreLista || "";
  const faccion = app.dataset.faccion || "";
  const ejercito = app.dataset.ejercito || "";
  let limitePuntos = parseInt(app.dataset.limitePuntos || "2000", 10);
  const urlGuardado = app.dataset.urlGuardado || "/creador-listas-40k/guardar";
  const estadoFiltrosCatalogo = {
    legends: true,
    fortificaciones: true,
    aliadas: true,
    texto: ""
  };

  function parsearJsonSeguro(texto, valorPorDefecto) {
    if (!texto) {
      return valorPorDefecto;
    }
    try {
      return JSON.parse(texto);
    } catch (error) {
      return valorPorDefecto;
    }
  }

  function clonarDatos(datos) {
    return parsearJsonSeguro(JSON.stringify(datos), {});
  }

  function valorBooleanoDataset(valor) {
    return String(valor).toLowerCase() === "true";
  }

  function normalizarTextoFiltro(texto) {
    return String(texto || "")
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, " ");
  }

  function construirTextoUnidadCatalogo(boton) {
    return normalizarTextoFiltro(
      (boton.dataset.nombre || "") + " "
      + (boton.dataset.roles || "") + " "
      + (boton.dataset.armas || "") + " "
      + (boton.dataset.palabrasClaveFaccion || "") + " "
      + (boton.dataset.palabrasClave || "") + " "
      + (boton.textContent || "")
    );
  }

  function esUnidadLegendCatalogo(boton) {
    return valorBooleanoDataset(boton.dataset.esLegend)
      || construirTextoUnidadCatalogo(boton).includes("legends");
  }

  function esUnidadFortificacionCatalogo(boton) {
    const texto = construirTextoUnidadCatalogo(boton);
    return valorBooleanoDataset(boton.dataset.esFortificacion)
      || texto.includes("fortification")
      || texto.includes("fortificacion");
  }

  function esUnidadAliadaCatalogo(boton) {
    const texto = construirTextoUnidadCatalogo(boton);
    return valorBooleanoDataset(boton.dataset.esAliada)
      || texto.includes("imperial agents")
      || texto.includes("agents of the imperium")
      || texto.includes("agent of the imperium")
      || texto.includes("freeblade")
      || texto.includes("dreadblade")
      || texto.includes("chaos daemons")
      || texto.includes("chaos demons")
      || texto.includes("allied unit")
      || texto.includes("allies");
  }

  function debeOcultarBotonCatalogo(boton) {
    return (estadoFiltrosCatalogo.legends && esUnidadLegendCatalogo(boton))
      || (estadoFiltrosCatalogo.fortificaciones && esUnidadFortificacionCatalogo(boton))
      || (estadoFiltrosCatalogo.aliadas && esUnidadAliadaCatalogo(boton))
      || (estadoFiltrosCatalogo.texto && !construirTextoUnidadCatalogo(boton).includes(estadoFiltrosCatalogo.texto));
  }

  function actualizarEstadoCategoriasCatalogo() {
    document.querySelectorAll(".catalog-category").forEach(function (categoria) {
      const visibles = Array.from(categoria.querySelectorAll(".boton-catalogo-unidad")).some(function (boton) {
        return boton.style.display !== "none";
      });
      categoria.classList.toggle("catalog-category-hidden", !visibles);
    });
  }

  function aplicarFiltrosCatalogo() {
    document.querySelectorAll(".boton-catalogo-unidad").forEach(function (boton) {
      boton.style.display = debeOcultarBotonCatalogo(boton) ? "none" : "";
    });
    actualizarEstadoCategoriasCatalogo();
  }

  function inicializarFiltrosCatalogo() {
    const filtroLegends = document.getElementById("filtroLegends");
    const filtroFortificaciones = document.getElementById("filtroFortificaciones");
    const filtroAliadas = document.getElementById("filtroAliadas");
    const filtroTexto = document.getElementById("filtroTexto");
    if (!filtroLegends || !filtroFortificaciones || !filtroAliadas || !filtroTexto) {
      return;
    }

    const refrescarFiltros = function () {
      estadoFiltrosCatalogo.legends = !!filtroLegends.checked;
      estadoFiltrosCatalogo.fortificaciones = !!filtroFortificaciones.checked;
      estadoFiltrosCatalogo.aliadas = !!filtroAliadas.checked;
      estadoFiltrosCatalogo.texto = normalizarTextoFiltro(filtroTexto.value).trim();
      aplicarFiltrosCatalogo();
    };

    [filtroLegends, filtroFortificaciones, filtroAliadas].forEach(function (checkbox) {
      checkbox.addEventListener("change", refrescarFiltros);
    });
    filtroTexto.addEventListener("input", refrescarFiltros);

    refrescarFiltros();
  }

  function actualizarContadorPuntos() {
    const unidades = document.querySelectorAll(".unidad-en-lista");
    let total = 0;
    for (let i = 0; i < unidades.length; i++) {
      total += parseInt(unidades[i].dataset.puntosBase || "0", 10);
    }
    document.getElementById("contadorPuntos").textContent = total;
    validarLista();
  }

  function asegurarBloqueLimpio(idBloque) {
    const bloque = document.getElementById(idBloque);
    if (bloque.classList.contains("list-empty")) {
      bloque.textContent = "";
      bloque.classList.remove("list-empty");
    }
  }

  function revisarBloqueVacio(idBloque) {
    const bloque = document.getElementById(idBloque);
    if (bloque.children.length === 0) {
      bloque.textContent = "Sin unidades";
      bloque.classList.add("list-empty");
    }
  }

  function crearDatosUnidadDesdeBoton(boton) {
    return {
      nombre: boton.dataset.nombre || "",
      roles: boton.dataset.roles || "",
      puntos: boton.dataset.puntos || "",
      puntosBase: parseInt(boton.dataset.puntosBase || "0", 10),
      categoria: boton.dataset.categoria || "otros",
      equipamientoResumen: crearResumenEquipamiento(boton.dataset.armas || ""),
      configuracionEstado: crearEstadoConfiguracion(
        parsearJsonSeguro(boton.dataset.configuracionJson, { gruposMiniaturas: [], opcionesComposicion: [] })
      ),
      battleline: valorBooleanoDataset(boton.dataset.battleline),
      epicHero: valorBooleanoDataset(boton.dataset.epicHero),
      character: valorBooleanoDataset(boton.dataset.character),
      leader: valorBooleanoDataset(boton.dataset.leader),
      support: valorBooleanoDataset(boton.dataset.support),
      palabrasClave: boton.dataset.palabrasClave || "",
      compatibles: parsearJsonSeguro(boton.dataset.compatiblesJson, [])
    };
  }

  function crearDatosEspecial(tipo) {
    return {
      nombre: tipo === "destacamentos" ? "Destacamentos" : "Disposicion",
      roles: "Disposicion",
      puntos: "0",
      puntosBase: 0,
      categoria: "disposicion",
      tipoEspecial: tipo,
      configuracionEstado: { tipoEspecial: tipo, seleccionados: [] },
      equipamientoResumen: [],
      compatibles: []
    };
  }

  function crearResumenEquipamiento(texto) {
    if (!texto) {
      return [];
    }

    return texto
      .split(",")
      .map(function (parte) {
        return parte.trim();
      })
      .filter(function (parte) {
        return parte.length > 0 && parte !== "Sin armas registradas";
      });
  }

  function crearSeleccionInicial(grupoEquipamiento) {
    const opciones = grupoEquipamiento.opciones || [];
    const maximo = Math.max(grupoEquipamiento.maximo || 0, 0);
    const minimo = Math.max(grupoEquipamiento.minimo || 0, 0);
    if (opciones.length === 0 || maximo === 0) {
      return [];
    }

    const opcionPorDefecto = opciones.find(function (opcion) {
      return opcion.seleccionPorDefecto;
    });

    if (maximo <= 1 && opcionPorDefecto) {
      return [opcionPorDefecto.id];
    }

    const total = Math.min(opciones.length, Math.min(maximo, minimo));
    const seleccion = [];
    for (let indice = 0; indice < total; indice++) {
      seleccion.push(opciones[indice].id);
    }
    return seleccion;
  }

  function crearInstanciaModelo(modelo, indice) {
    const selecciones = {};
    const gruposEquipamiento = modelo.gruposEquipamiento || [];
    for (let i = 0; i < gruposEquipamiento.length; i++) {
      selecciones[gruposEquipamiento[i].id] = crearSeleccionInicial(gruposEquipamiento[i]);
    }
    return {
      id: modelo.id + "-instancia-" + indice,
      selecciones: selecciones
    };
  }

  function totalGrupoMiniaturas(grupo) {
    let total = 0;
    const modelos = grupo.modelos || [];
    for (let i = 0; i < modelos.length; i++) {
      total += (modelos[i].instancias || []).length;
    }
    const subgrupos = grupo.subgrupos || [];
    for (let indice = 0; indice < subgrupos.length; indice++) {
      total += totalGrupoMiniaturas(subgrupos[indice]);
    }
    return total;
  }

  function puedeAnadirInstancia(grupo, modelo) {
    const totalGrupo = totalGrupoMiniaturas(grupo);
    return totalGrupo < (grupo.maximo || 0) && (modelo.instancias || []).length < (modelo.maximo || 0);
  }

  function puedeQuitarInstancia(grupo, modelo) {
    const totalGrupo = totalGrupoMiniaturas(grupo);
    return (modelo.instancias || []).length > (modelo.minimo || 0) && (totalGrupo - 1) >= (grupo.minimo || 0);
  }

  function crearGruposMiniaturasEstado(gruposCatalogo) {
    const gruposMiniaturas = [];

    for (let indiceGrupo = 0; indiceGrupo < gruposCatalogo.length; indiceGrupo++) {
      const grupoCatalogo = gruposCatalogo[indiceGrupo];
      const grupoEstado = {
        id: grupoCatalogo.id || ("grupo-" + indiceGrupo),
        nombre: grupoCatalogo.nombre || "Miniaturas",
        minimo: grupoCatalogo.minimo || 0,
        maximo: grupoCatalogo.maximo || 0,
        modelos: [],
        subgrupos: []
      };

      const modelosCatalogo = grupoCatalogo.modelos || [];
      for (let indiceModelo = 0; indiceModelo < modelosCatalogo.length; indiceModelo++) {
        const modeloCatalogo = modelosCatalogo[indiceModelo];
        const modeloEstado = {
          id: modeloCatalogo.id || ("modelo-" + indiceModelo),
          nombre: modeloCatalogo.nombre || "Miniatura",
          minimo: modeloCatalogo.minimo || 0,
          maximo: modeloCatalogo.maximo || 0,
          equipamientoFijo: modeloCatalogo.equipamientoFijo || [],
          gruposEquipamiento: modeloCatalogo.gruposEquipamiento || [],
          instancias: []
        };

        for (let numeroInstancia = 0; numeroInstancia < modeloEstado.minimo; numeroInstancia++) {
          modeloEstado.instancias.push(crearInstanciaModelo(modeloEstado, numeroInstancia + 1));
        }

        grupoEstado.modelos.push(modeloEstado);
      }

      grupoEstado.subgrupos = crearGruposMiniaturasEstado(grupoCatalogo.subgrupos || []);

      while (totalGrupoMiniaturas(grupoEstado) < grupoEstado.minimo && grupoEstado.modelos.length > 0) {
        let relleno = false;
        for (let indiceModelo = 0; indiceModelo < grupoEstado.modelos.length; indiceModelo++) {
          const modelo = grupoEstado.modelos[indiceModelo];
          if (puedeAnadirInstancia(grupoEstado, modelo)) {
            modelo.instancias.push(crearInstanciaModelo(modelo, modelo.instancias.length + 1));
            relleno = true;
            if (totalGrupoMiniaturas(grupoEstado) >= grupoEstado.minimo) {
              break;
            }
          }
        }
        if (!relleno) {
          break;
        }
      }

      gruposMiniaturas.push(grupoEstado);
    }

    return gruposMiniaturas;
  }

  function crearEstadoConfiguracion(configuracionCatalogo) {
    const opcionesCatalogo = (configuracionCatalogo && configuracionCatalogo.opcionesComposicion) || [];
    const opcionesComposicion = [];

    for (let indice = 0; indice < opcionesCatalogo.length; indice++) {
      const opcion = opcionesCatalogo[indice];
      opcionesComposicion.push({
        id: opcion.id || ("composicion-" + indice),
        nombre: opcion.nombre || "Composicion",
        puntos: opcion.puntos || 0,
        seleccionPorDefecto: !!opcion.seleccionPorDefecto,
        gruposMiniaturas: crearGruposMiniaturasEstado(opcion.gruposMiniaturas || [])
      });
    }

    let opcionSeleccionadaId = "";
    for (let indice = 0; indice < opcionesComposicion.length; indice++) {
      if (opcionesComposicion[indice].seleccionPorDefecto) {
        opcionSeleccionadaId = opcionesComposicion[indice].id;
        break;
      }
    }
    if (!opcionSeleccionadaId && opcionesComposicion.length > 0) {
      opcionSeleccionadaId = opcionesComposicion[0].id;
    }

    return {
      opcionSeleccionadaId: opcionSeleccionadaId,
      opcionesComposicion: opcionesComposicion,
      gruposMiniaturas: crearGruposMiniaturasEstado(
        (configuracionCatalogo && configuracionCatalogo.gruposMiniaturas) || []
      )
    };
  }

  function obtenerOpcionComposicionActiva(configuracion) {
    const opciones = configuracion.opcionesComposicion || [];
    for (let indice = 0; indice < opciones.length; indice++) {
      if (opciones[indice].id === configuracion.opcionSeleccionadaId) {
        return opciones[indice];
      }
    }
    return opciones.length > 0 ? opciones[0] : null;
  }

  function obtenerGruposActivos(configuracion) {
    const opcionActiva = obtenerOpcionComposicionActiva(configuracion);
    return opcionActiva ? (opcionActiva.gruposMiniaturas || []) : (configuracion.gruposMiniaturas || []);
  }

  function obtenerPuntosActivos(contenedor, configuracion) {
    const opcionActiva = obtenerOpcionComposicionActiva(configuracion);
    if (opcionActiva && opcionActiva.puntos > 0) {
      return opcionActiva.puntos;
    }
    return parseInt(contenedor.dataset.puntosBaseOriginal || contenedor.dataset.puntosBase || "0", 10);
  }

  function actualizarPresentacionUnidad(contenedor) {
    const configuracion = parsearJsonSeguro(
      contenedor.dataset.configuracionEstado,
      { gruposMiniaturas: [], opcionesComposicion: [] }
    );
    const puntos = obtenerPuntosActivos(contenedor, configuracion);
    contenedor.dataset.puntosBase = String(puntos);

    const puntosTarjeta = contenedor.querySelector(".unit-points-value");
    if (puntosTarjeta) {
      puntosTarjeta.textContent = String(puntos);
    }

    if (unidadActiva === contenedor.dataset.identificador) {
      document.getElementById("detalleMeta").textContent = "Rol: " + (contenedor.dataset.roles || "Sin rol")
        + " | Puntos: " + puntos;
    }

    actualizarContadorPuntos();
  }

  function resolverImagenUnidad(datosUnidad, esWarlord) {
    const texto = normalizarTextoFiltro((datosUnidad.roles || "") + " " + (datosUnidad.palabrasClave || ""));
    if (esWarlord) return "/unitTypes40k/Warlord.png";
    if (datosUnidad.epicHero || texto.includes("epic hero")) return "/unitTypes40k/epic_character.png";
    if (datosUnidad.character || texto.includes("character") || texto.includes("personaje")) return "/unitTypes40k/character.png";
    if (texto.includes("titan")) return "/unitTypes40k/titan.png";
    if (texto.includes("transport")) return "/unitTypes40k/transport.png";
    if (texto.includes("vehicle") || texto.includes("vehiculo")) return "/unitTypes40k/vehicle.png";
    if (texto.includes("monster") || texto.includes("monstruo")) return "/unitTypes40k/monster.png";
    return "/unitTypes40k/basic.png";
  }

  function actualizarImagenUnidad(contenedor) {
    const imagen = contenedor.querySelector(".unit-type-image");
    if (imagen) {
      imagen.src = resolverImagenUnidad({
        roles: contenedor.dataset.roles,
        palabrasClave: contenedor.dataset.palabrasClave,
        epicHero: valorBooleanoDataset(contenedor.dataset.epicHero),
        character: valorBooleanoDataset(contenedor.dataset.character)
      }, valorBooleanoDataset(contenedor.dataset.warlord));
    }
  }

  function agregarUnidad(datosUnidad) {
    if (datosUnidad.tipoEspecial && document.querySelector('[data-tipo-especial="' + datosUnidad.tipoEspecial + '"]')) {
      return;
    }
    const heroeEpicoRepetido = datosUnidad.epicHero && Array.from(
      document.querySelectorAll(".unidad-en-lista")
    ).some(function (unidad) {
      return valorBooleanoDataset(unidad.dataset.epicHero)
        && unidad.dataset.nombre === datosUnidad.nombre;
    });
    if (heroeEpicoRepetido) {
      document.getElementById("estadoGuardado").textContent =
        datosUnidad.nombre + " es un héroe épico y solo puede incluirse una vez.";
      return;
    }
    document.getElementById("estadoGuardado").textContent = "";

    const idBloque = "bloque-" + datosUnidad.categoria;
    const bloque = document.getElementById(idBloque);
    asegurarBloqueLimpio(idBloque);

    contadorUnidad++;

    const contenedor = document.createElement("div");
    contenedor.className = "unidad-en-lista";
    contenedor.dataset.nombre = datosUnidad.nombre;
    contenedor.dataset.roles = datosUnidad.roles;
    contenedor.dataset.puntos = datosUnidad.puntos;
    contenedor.dataset.puntosBaseOriginal = String(datosUnidad.puntosBase);
    contenedor.dataset.puntosBase = String(datosUnidad.puntosBase);
    contenedor.dataset.identificador = "unidad-" + contadorUnidad;
    contenedor.dataset.categoria = datosUnidad.categoria;
    contenedor.dataset.tipoEspecial = datosUnidad.tipoEspecial || "";
    contenedor.dataset.notas = "";
    contenedor.dataset.battleline = String(!!datosUnidad.battleline);
    contenedor.dataset.epicHero = String(!!datosUnidad.epicHero);
    contenedor.dataset.character = String(!!datosUnidad.character);
    contenedor.dataset.leader = String(!!datosUnidad.leader);
    contenedor.dataset.support = String(!!datosUnidad.support);
    contenedor.dataset.palabrasClave = datosUnidad.palabrasClave || "";
    contenedor.dataset.compatibles = JSON.stringify(datosUnidad.compatibles || []);
    contenedor.dataset.warlord = "false";
    contenedor.dataset.equipamientoResumen = JSON.stringify(datosUnidad.equipamientoResumen || []);
    contenedor.dataset.configuracionEstado = JSON.stringify(
      datosUnidad.configuracionEstado || { gruposMiniaturas: [], opcionesComposicion: [] }
    );

    const imagen = document.createElement("img");
    imagen.className = "unit-type-image";
    imagen.alt = "";

    const botonSeleccion = document.createElement("button");
    botonSeleccion.type = "button";
    botonSeleccion.className = "boton-seleccionar-unidad unit-main-button";
    const nombreUnidad = document.createElement("span");
    nombreUnidad.className = "unit-name";
    nombreUnidad.textContent = datosUnidad.nombre;
    const rolUnidad = document.createElement("span");
    rolUnidad.className = "unit-role";
    rolUnidad.textContent = datosUnidad.roles || "Unidad";
    botonSeleccion.appendChild(nombreUnidad);
    botonSeleccion.appendChild(rolUnidad);
    botonSeleccion.addEventListener("click", function () {
      seleccionarUnidad(contenedor);
    });

    const botonDuplicar = document.createElement("button");
    botonDuplicar.type = "button";
    botonDuplicar.textContent = "Duplicar";
    botonDuplicar.addEventListener("click", function () {
      agregarUnidad({
        nombre: contenedor.dataset.nombre || "",
        roles: contenedor.dataset.roles || "",
        puntos: contenedor.dataset.puntos || "",
        puntosBase: parseInt(contenedor.dataset.puntosBaseOriginal || contenedor.dataset.puntosBase || "0", 10),
        categoria: contenedor.dataset.categoria || "",
        equipamientoResumen: parsearJsonSeguro(contenedor.dataset.equipamientoResumen, []),
        configuracionEstado: clonarDatos(
          parsearJsonSeguro(contenedor.dataset.configuracionEstado, { gruposMiniaturas: [], opcionesComposicion: [] })
        ),
        battleline: valorBooleanoDataset(contenedor.dataset.battleline),
        epicHero: valorBooleanoDataset(contenedor.dataset.epicHero),
        character: valorBooleanoDataset(contenedor.dataset.character),
        leader: valorBooleanoDataset(contenedor.dataset.leader),
        support: valorBooleanoDataset(contenedor.dataset.support),
        palabrasClave: contenedor.dataset.palabrasClave || "",
        compatibles: parsearJsonSeguro(contenedor.dataset.compatibles, [])
      });
    });
    botonDuplicar.disabled = !!datosUnidad.tipoEspecial;

    const botonEliminar = document.createElement("button");
    botonEliminar.type = "button";
    botonEliminar.textContent = "Eliminar";
    botonEliminar.addEventListener("click", function () {
      eliminarUnidad(contenedor, idBloque);
    });

    const puntosUnidad = document.createElement("span");
    puntosUnidad.className = "unit-points";
    puntosUnidad.innerHTML = '<span class="unit-points-value">' + datosUnidad.puntosBase + '</span> pts';
    const acciones = document.createElement("div");
    acciones.className = "unit-card-actions";
    acciones.appendChild(botonDuplicar);
    acciones.appendChild(botonEliminar);
    contenedor.appendChild(imagen);
    contenedor.appendChild(botonSeleccion);
    contenedor.appendChild(puntosUnidad);
    contenedor.appendChild(acciones);

    bloque.appendChild(contenedor);
    actualizarImagenUnidad(contenedor);
    actualizarPresentacionUnidad(contenedor);
  }

  function obtenerUnidadActiva() {
    if (!unidadActiva) {
      return null;
    }
    return document.querySelector('[data-identificador="' + unidadActiva + '"]');
  }

  function seleccionarUnidad(contenedor) {
    guardarCambiosUnidadActiva();
    document.querySelectorAll(".unidad-en-lista.is-active").forEach(function (unidad) {
      unidad.classList.remove("is-active");
    });
    unidadActiva = contenedor.dataset.identificador;
    contenedor.classList.add("is-active");
    document.getElementById("editorUnidad").classList.add("is-open");
    document.getElementById("panelVacio").style.display = "none";
    document.getElementById("panelDetalle").style.display = "block";
    document.getElementById("detalleNombre").textContent = contenedor.dataset.nombre;
    document.getElementById("notasUnidad").value = contenedor.dataset.notas || "";
    document.getElementById("unidadWarlord").checked = valorBooleanoDataset(contenedor.dataset.warlord);
    document.getElementById("unidadWarlord").disabled = !valorBooleanoDataset(contenedor.dataset.character);
    actualizarPresentacionUnidad(contenedor);
    renderizarConfiguracionUnidad(contenedor);
  }

  function eliminarUnidad(contenedor, idBloque) {
    if (unidadActiva === contenedor.dataset.identificador) {
      unidadActiva = null;
      document.getElementById("panelVacio").style.display = "block";
      document.getElementById("panelDetalle").style.display = "none";
      document.getElementById("configuracionUnidad").innerHTML = "";
      document.getElementById("editorUnidad").classList.remove("is-open");
    }

    contenedor.remove();
    revisarBloqueVacio(idBloque);
    actualizarContadorPuntos();
  }

  function guardarCambiosUnidadActiva() {
    const contenedor = obtenerUnidadActiva();
    if (!contenedor) {
      return;
    }
    contenedor.dataset.notas = document.getElementById("notasUnidad").value || "";
    contenedor.dataset.warlord = String(document.getElementById("unidadWarlord").checked);
    actualizarImagenUnidad(contenedor);
  }

  function actualizarEstadoConfiguracion(contenedor, configuracion) {
    contenedor.dataset.configuracionEstado = JSON.stringify(configuracion);
  }

  function obtenerEstadoEspecial(contenedor) {
    if (!contenedor) return { seleccionados: [] };
    return parsearJsonSeguro(contenedor.dataset.configuracionEstado, { seleccionados: [] });
  }

  function opcionesDestacamentos() {
    return Array.from(document.querySelectorAll("#datosDestacamentos option"));
  }

  function obtenerTotalDP(ids) {
    return opcionesDestacamentos().filter(function (opcion) {
      return ids.indexOf(opcion.value) >= 0;
    }).reduce(function (total, opcion) {
      return total + parseInt(opcion.dataset.puntosDp || "0", 10);
    }, 0);
  }

  function disposicionesDisponibles() {
    const destacamentos = document.querySelector('[data-tipo-especial="destacamentos"]');
    const seleccionados = obtenerEstadoEspecial(destacamentos).seleccionados;
    return opcionesDestacamentos().filter(function (opcion) {
      return seleccionados.indexOf(opcion.value) >= 0;
    }).flatMap(function (opcion) {
      return (opcion.dataset.disposiciones || "").split(" | ").filter(Boolean);
    }).filter(function (disposicion, indice, todas) {
      return todas.indexOf(disposicion) === indice;
    });
  }

  function renderizarConfiguracionEspecial(contenedor) {
    const bloque = document.createElement("div");
    bloque.className = "config-card";
    const estado = obtenerEstadoEspecial(contenedor);
    const esDestacamentos = contenedor.dataset.tipoEspecial === "destacamentos";
    const opciones = esDestacamentos ? opcionesDestacamentos() : disposicionesDisponibles();
    const titulo = document.createElement("p");
    titulo.className = "config-card-title";
    titulo.textContent = esDestacamentos ? "Destacamentos seleccionados" : "Disposición de la lista";
    bloque.appendChild(titulo);

    if (opciones.length === 0) {
      const aviso = document.createElement("p");
      aviso.textContent = esDestacamentos
        ? "El catálogo no contiene destacamentos para este ejército."
        : "Marca primero uno o más destacamentos.";
      bloque.appendChild(aviso);
      return bloque;
    }

    opciones.forEach(function (opcion) {
      const id = esDestacamentos ? opcion.value : opcion;
      const nombre = esDestacamentos
        ? opcion.dataset.nombre + " (" + (opcion.dataset.puntosDp || "0") + " DP)"
        : opcion;
      const fila = document.createElement("label");
      fila.className = "choice-row";
      const input = document.createElement("input");
      input.type = "checkbox";
      input.checked = estado.seleccionados.indexOf(id) >= 0;
      input.addEventListener("change", function () {
        const siguiente = obtenerEstadoEspecial(contenedor);
        if (input.checked) siguiente.seleccionados.push(id);
        else siguiente.seleccionados = siguiente.seleccionados.filter(function (valor) { return valor !== id; });
        if (esDestacamentos && obtenerTotalDP(siguiente.seleccionados) > 3) {
          input.checked = false;
          return;
        }
        actualizarEstadoConfiguracion(contenedor, siguiente);
        if (esDestacamentos) {
          const unidadDisposicion = document.querySelector('[data-tipo-especial="disposicion"]');
          if (unidadDisposicion) {
            const disponibles = disposicionesDisponibles();
            const estadoDisposicion = obtenerEstadoEspecial(unidadDisposicion);
            estadoDisposicion.seleccionados = estadoDisposicion.seleccionados.filter(function (valor) {
              return disponibles.indexOf(valor) >= 0;
            });
            actualizarEstadoConfiguracion(unidadDisposicion, estadoDisposicion);
          }
        }
        document.getElementById("contadorDP").textContent = String(obtenerTotalDP(obtenerEstadoEspecial(document.querySelector('[data-tipo-especial="destacamentos"]')).seleccionados));
        renderizarConfiguracionUnidad(contenedor);
        validarLista();
      });
      const texto = document.createElement("span");
      texto.textContent = nombre;
      fila.appendChild(input);
      fila.appendChild(texto);
      bloque.appendChild(fila);
    });
    return bloque;
  }

  function renderizarConfiguracionUnidad(contenedor) {
    const panel = document.getElementById("configuracionUnidad");
    panel.innerHTML = "";

    if (contenedor.dataset.tipoEspecial) {
      panel.appendChild(renderizarConfiguracionEspecial(contenedor));
      return;
    }

    const configuracion = parsearJsonSeguro(
      contenedor.dataset.configuracionEstado,
      { gruposMiniaturas: [], opcionesComposicion: [] }
    );

    if ((configuracion.opcionesComposicion || []).length > 0) {
      panel.appendChild(renderizarSelectorComposicion(contenedor, configuracion));
    }

    const grupos = obtenerGruposActivos(configuracion);
    if (grupos.length === 0) {
      const equipamientoResumen = parsearJsonSeguro(contenedor.dataset.equipamientoResumen, []);
      if (equipamientoResumen.length > 0) {
        panel.appendChild(renderizarListaEquipamiento("Equipamiento de la unidad", equipamientoResumen));
      } else {
        const texto = document.createElement("p");
        texto.textContent = "Esta unidad no expone equipamiento configurable en el catalogo cargado.";
        panel.appendChild(texto);
      }
      return;
    }

    for (let indiceGrupo = 0; indiceGrupo < grupos.length; indiceGrupo++) {
      panel.appendChild(renderizarGrupoMiniaturas(contenedor, grupos[indiceGrupo], [indiceGrupo], 0));
    }
  }

  function renderizarSelectorComposicion(contenedor, configuracion) {
    const bloque = document.createElement("div");
    bloque.className = "config-card";

    const titulo = document.createElement("p");
    titulo.className = "config-card-title";
    titulo.textContent = "Composicion de unidad";
    bloque.appendChild(titulo);

    const opciones = configuracion.opcionesComposicion || [];
    for (let indice = 0; indice < opciones.length; indice++) {
      const opcion = opciones[indice];
      const fila = document.createElement("label");
      fila.className = "choice-row";

      const radio = document.createElement("input");
      radio.type = "radio";
      radio.name = contenedor.dataset.identificador + "-composicion";
      radio.value = opcion.id;
      radio.checked = opcion.id === configuracion.opcionSeleccionadaId;
      radio.addEventListener("change", function () {
        cambiarComposicionUnidad(contenedor, opcion.id);
      });

      const texto = document.createElement("span");
      texto.textContent = opcion.nombre + " (" + opcion.puntos + " pts)";

      fila.appendChild(radio);
      fila.appendChild(texto);
      bloque.appendChild(fila);
    }

    return bloque;
  }

  function renderizarGrupoMiniaturas(contenedor, grupo, rutaGrupo, nivel) {
    const bloque = document.createElement("div");
    bloque.className = "config-card config-group nivel-" + nivel;

    const titulo = document.createElement("div");
    titulo.className = "group-header";

    const nombre = document.createElement("p");
    nombre.className = "config-card-title";
    nombre.textContent = grupo.nombre;

    const resumen = document.createElement("span");
    resumen.className = "group-count";
    resumen.textContent = totalGrupoMiniaturas(grupo) + "/" + (grupo.maximo || 0);

    titulo.appendChild(nombre);
    titulo.appendChild(resumen);
    bloque.appendChild(titulo);

    const modelos = grupo.modelos || [];
    for (let indiceModelo = 0; indiceModelo < modelos.length; indiceModelo++) {
      bloque.appendChild(renderizarModeloMiniaturas(contenedor, grupo, modelos[indiceModelo], rutaGrupo, indiceModelo));
    }

    const subgrupos = grupo.subgrupos || [];
    for (let indiceSubgrupo = 0; indiceSubgrupo < subgrupos.length; indiceSubgrupo++) {
      bloque.appendChild(
        renderizarGrupoMiniaturas(contenedor, subgrupos[indiceSubgrupo], rutaGrupo.concat(indiceSubgrupo), nivel + 1)
      );
    }

    return bloque;
  }

  function renderizarModeloMiniaturas(contenedor, grupo, modelo, rutaGrupo, indiceModelo) {
    const tarjeta = document.createElement("div");
    tarjeta.className = "model-card";

    const cabecera = document.createElement("div");
    cabecera.className = "model-header";

    const info = document.createElement("div");
    info.className = "model-info";

    const nombre = document.createElement("p");
    nombre.className = "model-name";
    nombre.textContent = modelo.nombre;

    const rango = document.createElement("p");
    rango.className = "model-range";
    rango.textContent = "Min " + (modelo.minimo || 0) + " · Max " + (modelo.maximo || 0);

    info.appendChild(nombre);
    info.appendChild(rango);

    const controles = document.createElement("div");
    controles.className = "count-controls";

    const botonMenos = document.createElement("button");
    botonMenos.type = "button";
    botonMenos.className = "count-button";
    botonMenos.textContent = "-";
    botonMenos.disabled = !puedeQuitarInstancia(grupo, modelo);
    botonMenos.addEventListener("click", function () {
      quitarInstanciaModelo(contenedor, rutaGrupo, indiceModelo);
    });

    const valor = document.createElement("span");
    valor.className = "count-value";
    valor.textContent = String((modelo.instancias || []).length);

    const botonMas = document.createElement("button");
    botonMas.type = "button";
    botonMas.className = "count-button";
    botonMas.textContent = "+";
    botonMas.disabled = !puedeAnadirInstancia(grupo, modelo);
    botonMas.addEventListener("click", function () {
      anadirInstanciaModelo(contenedor, rutaGrupo, indiceModelo);
    });

    controles.appendChild(botonMenos);
    controles.appendChild(valor);
    controles.appendChild(botonMas);

    cabecera.appendChild(info);
    cabecera.appendChild(controles);
    tarjeta.appendChild(cabecera);

    if ((modelo.instancias || []).length > 0) {
      tarjeta.appendChild(renderizarDetalleModelo(contenedor, modelo, rutaGrupo, indiceModelo));
    }

    return tarjeta;
  }

  function renderizarDetalleModelo(contenedor, modelo, rutaGrupo, indiceModelo) {
    const bloque = document.createElement("div");
    bloque.className = "model-detail";

    if ((modelo.equipamientoFijo || []).length > 0) {
      bloque.appendChild(renderizarListaEquipamiento("Equipamiento base", modelo.equipamientoFijo));
    }

    if ((modelo.gruposEquipamiento || []).length === 0) {
      return bloque;
    }

    const instancias = modelo.instancias || [];
    if (instancias.length === 1) {
      bloque.appendChild(
        renderizarEquipamientoInstancia(
          contenedor,
          modelo,
          instancias[0],
          rutaGrupo,
          indiceModelo,
          0,
          ""
        )
      );
      return bloque;
    }

    for (let indiceInstancia = 0; indiceInstancia < instancias.length; indiceInstancia++) {
      bloque.appendChild(
        renderizarEquipamientoInstancia(
          contenedor,
          modelo,
          instancias[indiceInstancia],
          rutaGrupo,
          indiceModelo,
          indiceInstancia,
          modelo.nombre + " " + (indiceInstancia + 1)
        )
      );
    }

    return bloque;
  }

  function renderizarEquipamientoInstancia(contenedor, modelo, instancia, rutaGrupo, indiceModelo, indiceInstancia, tituloInstancia) {
    const tarjeta = document.createElement("div");
    tarjeta.className = "instance-card";

    if (tituloInstancia) {
      const titulo = document.createElement("p");
      titulo.className = "instance-title";
      titulo.textContent = tituloInstancia;
      tarjeta.appendChild(titulo);
    }

    const gruposEquipamiento = modelo.gruposEquipamiento || [];
    for (let indiceGrupo = 0; indiceGrupo < gruposEquipamiento.length; indiceGrupo++) {
      tarjeta.appendChild(
        renderizarGrupoEquipamiento(
          contenedor,
          instancia,
          rutaGrupo,
          indiceModelo,
          indiceInstancia,
          gruposEquipamiento[indiceGrupo]
        )
      );
    }

    return tarjeta;
  }

  function renderizarGrupoEquipamiento(contenedor, instancia, rutaGrupo, indiceModelo, indiceInstancia, grupoEquipamiento) {
    const bloque = document.createElement("div");
    bloque.className = "choice-card";

    const titulo = document.createElement("div");
    titulo.className = "choice-header";

    const nombre = document.createElement("p");
    nombre.className = "choice-title";
    nombre.textContent = grupoEquipamiento.nombre;

    const seleccionActual = instancia.selecciones[grupoEquipamiento.id] || [];
    const resumen = document.createElement("span");
    resumen.className = "choice-count";
    resumen.textContent = seleccionActual.length + "/" + (grupoEquipamiento.maximo || 0);

    titulo.appendChild(nombre);
    titulo.appendChild(resumen);
    bloque.appendChild(titulo);

    if ((grupoEquipamiento.maximo || 0) <= 1 && (grupoEquipamiento.minimo || 0) === 0) {
      const filaNinguna = document.createElement("label");
      filaNinguna.className = "choice-row";

      const radioNinguno = document.createElement("input");
      radioNinguno.type = "radio";
      radioNinguno.name = contenedor.dataset.identificador + "-" + instancia.id + "-" + grupoEquipamiento.id;
      radioNinguno.value = "";
      radioNinguno.checked = seleccionActual.length === 0;
      radioNinguno.addEventListener("change", function () {
        cambiarSeleccionSimple(contenedor, rutaGrupo, indiceModelo, indiceInstancia, grupoEquipamiento.id, "");
      });

      const textoNinguno = document.createElement("span");
      textoNinguno.textContent = "Sin elegir";

      filaNinguna.appendChild(radioNinguno);
      filaNinguna.appendChild(textoNinguno);
      bloque.appendChild(filaNinguna);
    }

    const opciones = grupoEquipamiento.opciones || [];
    for (let indiceOpcion = 0; indiceOpcion < opciones.length; indiceOpcion++) {
      const opcion = opciones[indiceOpcion];
      const fila = document.createElement("label");
      fila.className = "choice-row";

      const input = document.createElement("input");
      input.name = contenedor.dataset.identificador + "-" + instancia.id + "-" + grupoEquipamiento.id;
      input.value = opcion.id;

      if ((grupoEquipamiento.maximo || 0) <= 1) {
        input.type = "radio";
        input.checked = seleccionActual.length > 0 && seleccionActual[0] === opcion.id;
        input.addEventListener("change", function () {
          cambiarSeleccionSimple(contenedor, rutaGrupo, indiceModelo, indiceInstancia, grupoEquipamiento.id, opcion.id);
        });
      } else {
        input.type = "checkbox";
        input.checked = seleccionActual.indexOf(opcion.id) >= 0;
        input.disabled = !input.checked && seleccionActual.length >= grupoEquipamiento.maximo;
        input.addEventListener("change", function () {
          cambiarSeleccionMultiple(
            contenedor,
            rutaGrupo,
            indiceModelo,
            indiceInstancia,
            grupoEquipamiento.id,
            opcion.id,
            input.checked
          );
        });
      }

      const texto = document.createElement("span");
      texto.textContent = opcion.nombre;

      fila.appendChild(input);
      fila.appendChild(texto);
      bloque.appendChild(fila);

      if (input.checked && (opcion.detalleEquipamiento || []).length > 0) {
        bloque.appendChild(renderizarListaEquipamiento("", opcion.detalleEquipamiento));
      }
    }

    return bloque;
  }

  function renderizarListaEquipamiento(titulo, elementos) {
    const bloque = document.createElement("div");
    bloque.className = "gear-list";

    if (titulo) {
      const nombre = document.createElement("p");
      nombre.className = "gear-title";
      nombre.textContent = titulo;
      bloque.appendChild(nombre);
    }

    const lista = document.createElement("div");
    lista.className = "gear-items";

    for (let indice = 0; indice < elementos.length; indice++) {
      const fila = document.createElement("label");
      fila.className = "gear-item";

      const check = document.createElement("input");
      check.type = "checkbox";
      check.checked = true;
      check.disabled = true;

      const texto = document.createElement("span");
      texto.textContent = elementos[indice];

      fila.appendChild(check);
      fila.appendChild(texto);
      lista.appendChild(fila);
    }

    bloque.appendChild(lista);
    return bloque;
  }

  function obtenerGrupoPorRuta(grupos, ruta) {
    let grupo = grupos[ruta[0]];
    for (let indice = 1; indice < ruta.length; indice++) {
      grupo = (grupo.subgrupos || [])[ruta[indice]];
    }
    return grupo;
  }

  function anadirInstanciaModelo(contenedor, rutaGrupo, indiceModelo) {
    const configuracion = parsearJsonSeguro(
      contenedor.dataset.configuracionEstado,
      { gruposMiniaturas: [], opcionesComposicion: [] }
    );
    const grupo = obtenerGrupoPorRuta(obtenerGruposActivos(configuracion), rutaGrupo);
    const modelo = grupo.modelos[indiceModelo];
    if (!puedeAnadirInstancia(grupo, modelo)) {
      return;
    }

    modelo.instancias.push(crearInstanciaModelo(modelo, modelo.instancias.length + 1));
    actualizarEstadoConfiguracion(contenedor, configuracion);
    actualizarPresentacionUnidad(contenedor);
    if (unidadActiva === contenedor.dataset.identificador) {
      renderizarConfiguracionUnidad(contenedor);
    }
  }

  function quitarInstanciaModelo(contenedor, rutaGrupo, indiceModelo) {
    const configuracion = parsearJsonSeguro(
      contenedor.dataset.configuracionEstado,
      { gruposMiniaturas: [], opcionesComposicion: [] }
    );
    const grupo = obtenerGrupoPorRuta(obtenerGruposActivos(configuracion), rutaGrupo);
    const modelo = grupo.modelos[indiceModelo];
    if (!puedeQuitarInstancia(grupo, modelo)) {
      return;
    }

    modelo.instancias.pop();
    actualizarEstadoConfiguracion(contenedor, configuracion);
    actualizarPresentacionUnidad(contenedor);
    if (unidadActiva === contenedor.dataset.identificador) {
      renderizarConfiguracionUnidad(contenedor);
    }
  }

  function cambiarSeleccionSimple(contenedor, rutaGrupo, indiceModelo, indiceInstancia, idGrupoEquipamiento, idOpcion) {
    const configuracion = parsearJsonSeguro(
      contenedor.dataset.configuracionEstado,
      { gruposMiniaturas: [], opcionesComposicion: [] }
    );
    const instancia = obtenerGrupoPorRuta(obtenerGruposActivos(configuracion), rutaGrupo).modelos[indiceModelo].instancias[indiceInstancia];
    instancia.selecciones[idGrupoEquipamiento] = idOpcion ? [idOpcion] : [];
    actualizarEstadoConfiguracion(contenedor, configuracion);
    if (unidadActiva === contenedor.dataset.identificador) {
      renderizarConfiguracionUnidad(contenedor);
    }
  }

  function cambiarSeleccionMultiple(contenedor, rutaGrupo, indiceModelo, indiceInstancia, idGrupoEquipamiento, idOpcion, activo) {
    const configuracion = parsearJsonSeguro(
      contenedor.dataset.configuracionEstado,
      { gruposMiniaturas: [], opcionesComposicion: [] }
    );
    const modelo = obtenerGrupoPorRuta(obtenerGruposActivos(configuracion), rutaGrupo).modelos[indiceModelo];
    const grupoEquipamiento = (modelo.gruposEquipamiento || []).find(function (grupo) {
      return grupo.id === idGrupoEquipamiento;
    });
    const instancia = modelo.instancias[indiceInstancia];
    const seleccionActual = instancia.selecciones[idGrupoEquipamiento] || [];

    if (activo) {
      if (seleccionActual.indexOf(idOpcion) < 0 && seleccionActual.length < grupoEquipamiento.maximo) {
        seleccionActual.push(idOpcion);
      }
    } else {
      const posicion = seleccionActual.indexOf(idOpcion);
      if (posicion >= 0 && (seleccionActual.length - 1) >= grupoEquipamiento.minimo) {
        seleccionActual.splice(posicion, 1);
      }
    }

    instancia.selecciones[idGrupoEquipamiento] = seleccionActual;
    actualizarEstadoConfiguracion(contenedor, configuracion);
    if (unidadActiva === contenedor.dataset.identificador) {
      renderizarConfiguracionUnidad(contenedor);
    }
  }

  function cambiarComposicionUnidad(contenedor, idComposicion) {
    const configuracion = parsearJsonSeguro(
      contenedor.dataset.configuracionEstado,
      { gruposMiniaturas: [], opcionesComposicion: [] }
    );
    configuracion.opcionSeleccionadaId = idComposicion;
    actualizarEstadoConfiguracion(contenedor, configuracion);
    actualizarPresentacionUnidad(contenedor);
    if (unidadActiva === contenedor.dataset.identificador) {
      renderizarConfiguracionUnidad(contenedor);
    }
  }

  function configuracionIncompleta(valor) {
    if (!valor || typeof valor !== "object") {
      return false;
    }
    if (Array.isArray(valor)) {
      return valor.some(configuracionIncompleta);
    }
    if (Array.isArray(valor.gruposEquipamiento) && Array.isArray(valor.instancias)) {
      for (let i = 0; i < valor.gruposEquipamiento.length; i++) {
        const grupo = valor.gruposEquipamiento[i];
        const minimo = parseInt(grupo.minimo || "0", 10);
        for (let j = 0; j < valor.instancias.length; j++) {
          const seleccion = (valor.instancias[j].selecciones || {})[grupo.id] || [];
          if (seleccion.length < minimo) {
            return true;
          }
        }
      }
    }
    return Object.keys(valor).some(function (clave) { return configuracionIncompleta(valor[clave]); });
  }

  function recopilarErroresComposicion(grupos, errores, contexto) {
    if (!Array.isArray(grupos)) {
      return;
    }

    grupos.forEach(function (grupo) {
      const nombreGrupo = grupo.nombre || "Grupo";
      const totalGrupo = totalGrupoMiniaturas(grupo);
      const minimoGrupo = Math.max(grupo.minimo || 0, 0);
      const maximoGrupo = Math.max(grupo.maximo || 0, 0);
      const contextoGrupo = contexto ? (contexto + " > " + nombreGrupo) : nombreGrupo;

      if (totalGrupo < minimoGrupo || (maximoGrupo > 0 && totalGrupo > maximoGrupo)) {
        errores.push(contextoGrupo + ": composicion invalida (" + totalGrupo + "/" + maximoGrupo + ").");
      }

      (grupo.modelos || []).forEach(function (modelo) {
        const totalModelo = (modelo.instancias || []).length;
        const minimoModelo = Math.max(modelo.minimo || 0, 0);
        const maximoModelo = Math.max(modelo.maximo || 0, 0);
        if (totalModelo < minimoModelo || (maximoModelo > 0 && totalModelo > maximoModelo)) {
          errores.push(
            contextoGrupo + " > " + (modelo.nombre || "Miniatura")
            + ": composicion invalida (" + totalModelo + "/" + maximoModelo + ")."
          );
        }
      });

      recopilarErroresComposicion(grupo.subgrupos || [], errores, contextoGrupo);
    });
  }

  function obtenerErroresConfiguracionUnidad(configuracion) {
    const errores = [];
    const gruposActivos = obtenerGruposActivos(configuracion);

    if (configuracionIncompleta(configuracion)) {
      errores.push("falta una seleccion obligatoria de equipamiento.");
    }

    recopilarErroresComposicion(gruposActivos, errores, "");
    return errores;
  }

  function validarLista() {
    const errores = [];
    const unidades = Array.from(document.querySelectorAll(".unidad-en-lista"));
    const puntos = parseInt(document.getElementById("contadorPuntos").textContent || "0", 10);
    if (unidades.length === 0) errores.push("Añade al menos una unidad.");
    const unidadDestacamentos = document.querySelector('[data-tipo-especial="destacamentos"]');
    const unidadDisposicion = document.querySelector('[data-tipo-especial="disposicion"]');
    if (!unidadDestacamentos) errores.push("Debes añadir Destacamentos.");
    if (!unidadDisposicion) errores.push("Debes añadir Disposición.");
    const estadoDestacamentos = obtenerEstadoEspecial(unidadDestacamentos);
    const estadoDisposicion = obtenerEstadoEspecial(unidadDisposicion);
    if (unidadDestacamentos && estadoDestacamentos.seleccionados.length === 0) errores.push("Debes marcar al menos un destacamento.");
    if (obtenerTotalDP(estadoDestacamentos.seleccionados) > 3) errores.push("Los destacamentos superan el máximo de 3 DP.");
    if (unidadDisposicion && estadoDisposicion.seleccionados.length === 0) errores.push("Debes seleccionar una disposición.");
    if (puntos > limitePuntos) errores.push("Se supera el límite de puntos por " + (puntos - limitePuntos) + ".");
    unidades.forEach(function (unidad) {
      const nombre = unidad.dataset.nombre || "";
      const configuracion = parsearJsonSeguro(
        unidad.dataset.configuracionEstado,
        { gruposMiniaturas: [], opcionesComposicion: [] }
      );
      obtenerErroresConfiguracionUnidad(configuracion).forEach(function (errorConfiguracion) {
        errores.push(nombre + ": " + errorConfiguracion);
      });
    });

    const heroesEpicos = {};
    unidades.filter(function (unidad) {
      return valorBooleanoDataset(unidad.dataset.epicHero);
    }).forEach(function (unidad) {
      const nombre = unidad.dataset.nombre || "Héroe épico";
      heroesEpicos[nombre] = (heroesEpicos[nombre] || 0) + 1;
    });
    Object.keys(heroesEpicos).forEach(function (nombre) {
      if (heroesEpicos[nombre] > 1) {
        errores.push(nombre + ": los héroes épicos no se pueden repetir.");
      }
    });

    const warlords = unidades.filter(function (unidad) { return valorBooleanoDataset(unidad.dataset.warlord); });
    if (warlords.length !== 1) errores.push("Debes designar exactamente un Warlord.");
    if (warlords.some(function (unidad) { return !valorBooleanoDataset(unidad.dataset.character); })) {
      errores.push("El Warlord debe ser un Character.");
    }

    const lista = document.getElementById("erroresValidacion");
    lista.innerHTML = "";
    errores.forEach(function (error) {
      const elemento = document.createElement("li");
      elemento.textContent = error;
      lista.appendChild(elemento);
    });

    const exceso = puntos > limitePuntos;
    const progreso = limitePuntos > 0 ? Math.min((puntos / limitePuntos) * 100, 100) : 0;
    const panel = document.getElementById("panelProgreso");
    const resumen = document.getElementById("resumenValidacion");
    document.getElementById("barraPuntos").style.width = progreso + "%";
    panel.classList.toggle("is-danger", exceso);
    panel.classList.toggle("is-warning", !exceso && errores.length > 0);
    resumen.classList.toggle("is-danger", exceso);
    resumen.classList.toggle("is-warning", !exceso && errores.length > 0);
    document.getElementById("estadoValidacion").textContent = exceso
      ? "Límite de puntos superado"
      : (errores.length ? "Lista incompleta" : "Lista válida");
    resumen.textContent = exceso
      ? "Reduce el ejército hasta " + limitePuntos + " puntos."
      : (errores.length ? "Hay " + errores.length + " problema(s) por resolver." : "La lista está preparada para guardarse.");
    return errores;
  }

  function construirPayloadLista() {
    guardarCambiosUnidadActiva();

    const unidades = document.querySelectorAll(".unidad-en-lista");
    const listaUnidades = [];
    for (let i = 0; i < unidades.length; i++) {
      listaUnidades.push({
        nombre: unidades[i].dataset.nombre || "",
        roles: unidades[i].dataset.roles || "",
        puntos: unidades[i].dataset.puntos || "",
        puntosBase: parseInt(unidades[i].dataset.puntosBase || "0", 10),
        categoria: unidades[i].dataset.categoria || "",
        tipoEspecial: unidades[i].dataset.tipoEspecial || "",
        notas: unidades[i].dataset.notas || "",
        configuracionMiniaturas: parsearJsonSeguro(
          unidades[i].dataset.configuracionEstado,
          { gruposMiniaturas: [], opcionesComposicion: [] }
        ),
        battleline: valorBooleanoDataset(unidades[i].dataset.battleline),
        epicHero: valorBooleanoDataset(unidades[i].dataset.epicHero),
        character: valorBooleanoDataset(unidades[i].dataset.character),
        warlord: valorBooleanoDataset(unidades[i].dataset.warlord),
        palabrasClave: unidades[i].dataset.palabrasClave || ""
      });
    }

    return {
      esquema: "2.0",
      formatoJuego: formatoJuego,
      nombreLista: nombreLista,
      faccion: faccion,
      ejercito: ejercito,
      limitePuntos: limitePuntos,
      puntosTotales: parseInt(document.getElementById("contadorPuntos").textContent || "0", 10),
      revisionCatalogo: "",
      unidades: listaUnidades
    };
  }

  async function guardarLista() {
    const estado = document.getElementById("estadoGuardado");
    const boton = document.getElementById("botonGuardarLista");
    const payload = construirPayloadLista();

    const errores = validarLista();
    if (errores.length > 0) {
      estado.textContent = "Corrige los errores antes de guardar.";
      return;
    }

    if (payload.unidades.length === 0) {
      estado.textContent = "Debes anadir al menos una unidad.";
      return;
    }

    estado.textContent = "Guardando...";
    boton.disabled = true;

    try {
      const datosFormulario = new URLSearchParams();
      datosFormulario.append("formatoJuego", formatoJuego);
      datosFormulario.append("nombreLista", payload.nombreLista);
      datosFormulario.append("faccion", faccion);
      datosFormulario.append("ejercito", ejercito);
      datosFormulario.append("limitePuntos", String(payload.limitePuntos));
      datosFormulario.append("puntosTotales", String(payload.puntosTotales));
      datosFormulario.append("revisionCatalogo", "");
      datosFormulario.append("datosListaJson", JSON.stringify(payload));

      const respuesta = await fetch(urlGuardado, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
        },
        body: datosFormulario.toString()
      });

      const resultado = await respuesta.text();
      if (!respuesta.ok || !resultado.startsWith("OK|")) {
        estado.textContent = resultado.startsWith("ERROR|")
          ? resultado.substring(6)
          : "No se ha podido guardar la lista.";
        return;
      }

      estado.textContent = "Lista guardada. Version " + resultado.substring(3) + ".";
      window.location.assign("/mis-listas");
    } catch (error) {
      estado.textContent = "Error al guardar la lista.";
    } finally {
      boton.disabled = false;
    }
  }

  document.querySelectorAll(".boton-catalogo-unidad").forEach(function (boton) {
    boton.addEventListener("click", function () {
      agregarUnidad(crearDatosUnidadDesdeBoton(boton));
    });
  });
  document.querySelectorAll(".boton-catalogo-especial").forEach(function (boton) {
    boton.addEventListener("click", function () { agregarUnidad(crearDatosEspecial(boton.dataset.tipoEspecial)); });
  });

  inicializarFiltrosCatalogo();
  document.getElementById("unidadWarlord").addEventListener("change", function () {
    const activa = obtenerUnidadActiva();
    if (this.checked && activa) {
      document.querySelectorAll(".unidad-en-lista").forEach(function (unidad) {
        if (unidad !== activa) {
          unidad.dataset.warlord = "false";
          actualizarImagenUnidad(unidad);
        }
      });
    }
    guardarCambiosUnidadActiva();
    validarLista();
  });
  document.getElementById("cerrarEditorUnidad").addEventListener("click", function () {
    document.getElementById("editorUnidad").classList.remove("is-open");
  });
  validarLista();
  document.getElementById("botonGuardarLista").addEventListener("click", guardarLista);
  document.getElementById("notasUnidad").addEventListener("input", guardarCambiosUnidadActiva);
})();
