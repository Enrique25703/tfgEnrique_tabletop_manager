<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Creador de listas AoS</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .builder-grid {
        display: grid;
        grid-template-columns: 0.95fr 1fr 0.95fr;
        gap: 18px;
        align-items: start;
      }

      .builder-column {
        display: flex;
        flex-direction: column;
        gap: 12px;
        min-width: 0;
        min-height: 0;
        max-height: calc(100vh - 270px);
        padding: 18px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: var(--panel-muted);
        overflow-y: auto;
        scrollbar-gutter: stable;
      }

      .builder-column h3,
      .builder-column h4 {
        margin-top: 0;
      }

      .catalog-button {
        display: block;
        width: 100%;
        margin-bottom: 10px;
        padding: 12px;
        text-align: left;
      }

      #configuracionUnidad {
        display: grid;
        gap: 14px;
      }

      #notasUnidad {
        min-height: 160px;
      }

      .config-card {
        padding: 14px;
        border: 1px solid rgba(130, 173, 255, 0.18);
        border-radius: 14px;
        background: rgba(12, 18, 28, 0.72);
      }

      .config-card-title,
      .choice-title,
      .instance-title,
      .gear-title,
      .model-name {
        margin: 0;
        font-weight: 700;
      }

      .config-group.nivel-1,
      .config-group.nivel-2,
      .config-group.nivel-3 {
        margin-left: 10px;
      }

      .group-header,
      .model-header,
      .choice-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
      }

      .group-count,
      .choice-count,
      .count-value {
        padding: 7px 12px;
        border: 1px solid var(--line);
        border-radius: 12px;
        background: rgba(20, 29, 44, 0.94);
        color: var(--text);
        font-weight: 700;
      }

      .model-card,
      .instance-card,
      .choice-card,
      .gear-list {
        margin-top: 12px;
        padding: 12px;
        border: 1px solid rgba(130, 173, 255, 0.14);
        border-radius: 12px;
        background: rgba(15, 23, 35, 0.78);
      }

      .model-info,
      .model-detail {
        display: grid;
        gap: 8px;
      }

      .model-range {
        margin: 4px 0 0;
        color: var(--muted);
        font-size: 0.9rem;
      }

      .count-controls {
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .count-button {
        min-width: 38px;
        min-height: 38px;
        padding: 0;
      }

      .choice-row,
      .gear-item {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        margin-top: 10px;
      }

      .gear-items {
        display: grid;
        gap: 8px;
      }

      @media (max-width: 1200px) {
        .builder-grid {
          grid-template-columns: 1fr;
        }

        .builder-column {
          max-height: none;
          overflow: visible;
        }
      }

      /* Mismo frontend y comportamiento responsive que el creador de 40k. */
      #creadorListaApp{flex:1 1 auto;width:100%;height:100%;min-width:0;min-height:0;overflow:hidden}
      #creadorListaApp>.page-panel{display:flex;flex-direction:column;width:100%;height:100%;min-height:0;overflow:hidden}
      .builder-header,.progress-copy{display:flex;justify-content:space-between;gap:18px;align-items:flex-start}
      .builder-meta,.builder-actions{display:flex;flex-wrap:wrap;gap:9px}.builder-meta{margin-top:12px}
      .progress-panel,.builder-column,.unit-editor{border:1px solid var(--line);border-radius:18px;background:rgba(10,17,27,.78)}
      .progress-panel{margin:20px 0;padding:16px}.progress-track{height:14px;overflow:hidden;border-radius:999px;background:rgba(255,255,255,.08)}
      .progress-fill{width:0;height:100%;border-radius:inherit;background:#51b985;transition:width .25s ease,background-color .25s ease}.progress-panel.is-warning .progress-fill{background:#d6a84b}.progress-panel.is-danger .progress-fill{background:#d85f68}
      .validation-summary{margin:10px 0 0;color:var(--muted)}.validation-summary.is-warning{color:#efc86e}.validation-summary.is-danger{color:#ff8c94}.validation-list{margin:8px 0 0;padding-left:20px}
      .filter-bar{display:grid;grid-template-columns:minmax(220px,1fr) repeat(3,auto);gap:10px;align-items:center;margin-bottom:18px;padding:12px;border:1px solid var(--line);border-radius:16px;background:linear-gradient(135deg,rgba(21,34,51,.96),rgba(12,20,31,.92))}
      .filter-option{display:inline-flex;align-items:center;gap:8px;padding:10px 12px;border-radius:12px;color:var(--muted);white-space:nowrap}.filter-option input{width:auto;margin:0;accent-color:#6f9ed2}
      .builder-grid{display:grid;grid-template-columns:minmax(240px,.78fr) minmax(340px,1.15fr) minmax(300px,1fr);gap:18px;align-items:stretch;width:100%;flex:1 1 auto;min-height:0;margin:0}.builder-column,.unit-editor{display:block;min-width:0;min-height:0;max-height:none;height:auto;padding:16px;overflow:auto;scrollbar-gutter:stable}
      .column-title{margin:0 0 14px}.catalog-scroll{height:auto;overflow:visible;padding-right:0}.catalog-category h4{margin:18px 0 8px;color:var(--accent-2)}.catalog-category:first-child h4{margin-top:0}.catalog-category-hidden{display:none}
      .catalog-button{display:flex;justify-content:space-between;gap:12px;width:100%;margin-bottom:8px;padding:11px 12px;text-align:left}.catalog-button-points{flex:none;color:var(--accent-2);font-weight:700}
      .list-category{margin-bottom:18px}.list-category-title{margin:0 0 9px;color:var(--accent-2);font-size:.9rem;letter-spacing:.08em;text-transform:uppercase}.list-empty{padding:14px;border:1px dashed var(--line);border-radius:14px;color:var(--muted);text-align:center}
      .unidad-en-lista{display:grid;grid-template-columns:58px minmax(0,1fr) auto;gap:12px;align-items:center;margin-bottom:9px;padding:10px;border:1px solid rgba(130,173,255,.16);border-radius:15px;background:rgba(10,17,27,.9)}.unidad-en-lista.is-active{border-color:var(--accent-2);box-shadow:0 0 0 2px rgba(126,166,210,.12)}
      .unit-type-image{width:58px;height:58px;padding:7px;object-fit:contain;border-radius:12px;background:rgba(255,255,255,.06)}.unit-main-button{width:100%;padding:0;border:0;background:transparent;text-align:left}.unit-name{display:block;font-weight:750}.unit-role{display:block;margin-top:4px;color:var(--muted);font-size:.82rem}.unit-points{min-width:74px;text-align:right;font-weight:800}.unit-card-actions{grid-column:2/4;display:flex;justify-content:flex-end;gap:8px}.unit-card-actions button{padding:6px 10px;font-size:.82rem}
      .unit-editor{margin:0}.editor-heading{display:flex;align-items:center;justify-content:space-between;gap:12px}.editor-close{display:none;padding:7px 10px}.editor-grid{display:grid;grid-template-columns:1fr;gap:18px}.editor-side{display:grid;align-content:start;gap:14px}.editor-side label{display:grid;gap:7px}#notasUnidad{min-height:120px}
      .group-count,.choice-count,.count-value{display:inline-flex;align-items:center;justify-content:center;text-align:center}.count-controls{justify-content:center}.choice-row,.gear-item{align-items:center;min-height:34px;line-height:1.35}.choice-row input,.gear-item input{width:auto;min-width:16px;margin:0;flex:0 0 auto}.choice-row span,.gear-item span{flex:1;min-width:0}
      .page-panel,.builder-column,.unit-editor{scrollbar-width:thin;scrollbar-color:rgba(104,151,207,.62) rgba(8,14,22,.72)}
      @media(max-width:1350px){.builder-grid{grid-template-columns:minmax(240px,.8fr) minmax(360px,1.2fr)}.unit-editor{position:fixed;top:16px;right:16px;bottom:16px;z-index:50;width:min(460px,calc(100vw - 32px));height:auto;max-height:none;box-shadow:0 24px 70px rgba(0,0,0,.55);transform:translateX(calc(100% + 32px));transition:transform .22s ease}.unit-editor.is-open{transform:translateX(0)}.editor-close{display:inline-flex}}
      @media(max-width:980px){#creadorListaApp>.page-panel{overflow:auto}.filter-bar{grid-template-columns:1fr 1fr}.filter-search{grid-column:1/-1}.builder-grid{grid-template-columns:1fr;flex:none}.builder-column{height:clamp(380px,55vh,520px)}}
      @media(max-width:650px){.builder-header,.progress-copy{flex-direction:column}.filter-bar{grid-template-columns:1fr}.filter-search{grid-column:auto}.unidad-en-lista{grid-template-columns:48px minmax(0,1fr) auto}.unit-type-image{width:48px;height:48px}.unit-editor{top:8px;right:8px;bottom:8px;width:calc(100vw - 16px)}}
    </style>
  </head>
  <body>
    <c:set var="sidebarActive" value="listas" />
    <c:set var="sidebarComunidadesEnabled" value="false" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${sessionScope.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content">
          <div
            id="creadorListaApp"
            data-formato-juego="<c:out value='${creadorListaAos.formatoJuego}'/>"
            data-nombre-lista="<c:out value='${creadorListaAos.nombreLista}'/>"
            data-faccion="<c:out value='${creadorListaAos.faccion}'/>"
            data-ejercito="<c:out value='${creadorListaAos.ejercito}'/>"
            data-limite-puntos="<c:out value='${creadorListaAos.limitePuntos}'/>"
            data-url-guardado="/creador-listas-aos/guardar">

            <section class="page-panel">
              <header class="builder-header">
                <div><h1 class="page-title"><c:out value="${creadorListaAos.nombreLista}" /></h1><p class="page-subtitle"><c:out value="${creadorListaAos.ejercito}" /> · <c:out value="${creadorListaAos.faccion}" /></p><div class="builder-meta"><span class="chip"><c:out value="${creadorListaAos.formatoJuego}" /></span><span class="chip">Límite: <c:out value="${creadorListaAos.limitePuntos}" /> pts</span></div></div>
                <div class="builder-actions"><a class="button-link" href="/menu-principal">Volver al menú</a><button type="button" class="button-primary" id="botonGuardarLista">Guardar lista</button></div>
              </header>
              <section id="panelProgreso" class="progress-panel" aria-live="polite">
                <div class="progress-copy"><strong id="estadoValidacion">Lista incompleta</strong><span><b id="contadorPuntos">0</b> / <b id="contadorLimitePuntos"><c:out value="${creadorListaAos.limitePuntos}" /></b> pts</span></div>
                <div class="progress-track"><div id="barraPuntos" class="progress-fill"></div></div>
                <div id="resumenValidacion" class="validation-summary">Añade unidades para comenzar.</div><ul id="erroresValidacion" class="validation-list"></ul><span id="estadoGuardado" class="validation-summary"></span>
              </section>
              <section class="filter-bar" aria-label="Filtros del catálogo">
                <input class="filter-search" id="filtroTexto" type="search" placeholder="Buscar unidad, rol o palabra clave..." />
                <label class="filter-option"><input type="checkbox" id="filtroLegends" checked /> Ocultar Legends</label><label class="filter-option"><input type="checkbox" id="filtroFortificaciones" checked /> Ocultar fortificaciones</label><label class="filter-option"><input type="checkbox" id="filtroAliadas" checked /> Ocultar aliadas</label>
              </section>
              <div class="builder-grid">
                <section class="builder-column"><h2 class="column-title">Catálogo</h2><div class="catalog-scroll">
                  <c:forEach var="categoria" items="${creadorListaAos.categorias}"><div class="catalog-category" data-catalog-category="<c:out value='${categoria.id}' />"><h4><c:out value="${categoria.titulo}" /></h4><div>
                    <c:forEach var="unidad" items="${categoria.unidades}">
                      <button type="button" class="boton-catalogo-unidad catalog-button" data-nombre="<c:out value='${unidad.nombre}'/>" data-roles="<c:out value='${unidad.roles}'/>" data-puntos="<c:out value='${unidad.puntos}'/>" data-puntos-base="<c:out value='${unidad.puntosBase}'/>" data-categoria="<c:out value='${unidad.categoria}'/>" data-armas="<c:out value='${unidad.armas}'/>" data-palabras-clave-faccion="<c:out value='${unidad.palabrasClaveFaccion}'/>" data-palabras-clave="<c:out value='${unidad.palabrasClave}'/>" data-configuracion-json="<c:out value='${unidad.configuracionJson}'/>" data-es-legend="${unidad.legend}" data-es-fortificacion="${unidad.fortificacion}" data-es-aliada="${unidad.aliada}" data-battleline="${unidad.battleline}" data-epic-hero="${unidad.epicHero}" data-character="${unidad.character}" data-leader="${unidad.leader}" data-support="${unidad.support}" data-compatibles-json="<c:out value='${unidad.compatiblesJson}'/>"><span><span class="unit-name"><c:out value="${unidad.nombre}" /></span><span class="unit-role"><c:out value="${unidad.roles}" /><c:if test="${not empty unidad.palabrasClave}"> · <c:out value="${unidad.palabrasClave}" /></c:if></span></span><span class="catalog-button-points"><c:out value="${unidad.puntosBase}" /> pts</span></button>
                    </c:forEach>
                  </div></div></c:forEach>
                </div></section>
                <section class="builder-column"><h2 class="column-title">Tu ejército</h2>
                  <c:forEach var="categoria" items="${creadorListaAos.categorias}"><div class="list-category"><h3 class="list-category-title"><c:out value="${categoria.titulo}" /></h3><div id="bloque-${categoria.id}" class="list-empty">Sin unidades</div></div></c:forEach>
                </section>
                <section id="editorUnidad" class="unit-editor"><div class="editor-heading"><h2 class="column-title">Configurar unidad</h2><button type="button" class="editor-close" id="cerrarEditorUnidad" aria-label="Cerrar configuración">Cerrar</button></div><div id="panelVacio" class="page-subtitle">Selecciona una tarjeta de tu ejército.</div><div id="panelDetalle" style="display:none"><div class="editor-grid"><div><h3 id="detalleNombre"></h3><p id="detalleMeta" class="page-subtitle"></p><div id="configuracionUnidad"></div></div><div class="editor-side"><label class="filter-option"><input type="checkbox" id="unidadWarlord" /> Designar como general</label><label for="notasUnidad">Notas<textarea id="notasUnidad" placeholder="Notas opcionales de esta unidad"></textarea></label></div></div></div></section>
              </div>
            </section>
          </div>
        </main>
      </div>
    </div>

    <script src="/js/creador-listas-40k.js?v=7"></script>
  </body>
</html>
