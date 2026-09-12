<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Configuracion de partida</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .config-card {
        max-width: 1040px;
        padding: 22px;
        border: 1px solid var(--line);
        border-radius: 20px;
        background: var(--panel-soft);
      }

      .form-grid {
        display: grid;
        gap: 18px;
        margin-top: 16px;
      }

      .mission-shell,
      .selector-shell {
        padding: 18px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: rgba(14, 22, 34, 0.5);
      }

      .option-row,
      .visual-grid {
        display: grid;
        gap: 14px;
      }

      .toggle-buttons input {
        position: absolute;
        opacity: 0;
        pointer-events: none;
      }

      .toggle-buttons span {
        display: block;
        padding: 12px;
        border: 1px solid var(--line);
        border-radius: 14px;
        text-align: center;
        background: rgba(14, 22, 34, 0.62);
        cursor: pointer;
      }

      .toggle-buttons input:checked + span {
        border-color: var(--line-strong);
        background: rgba(48, 77, 121, 0.8);
        color: #fff;
      }

      .selector-header {
        display: flex;
        justify-content: space-between;
        gap: 12px;
        align-items: flex-start;
        margin-bottom: 12px;
      }

      .style-pill {
        padding: 8px 12px;
        border: 1px solid var(--line-strong);
        border-radius: 999px;
        background: rgba(48, 77, 121, 0.32);
        white-space: nowrap;
      }

      .helper-copy {
        margin: 0 0 14px;
        color: var(--muted);
      }

      .helper-copy.locked {
        color: #d9e7ff;
      }

      .visual-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
        max-width: 920px;
        margin: 0 auto;
        align-items: start;
      }

      .visual-card {
        display: grid;
        gap: 12px;
        padding: 16px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: rgba(14, 22, 34, 0.62);
      }

      .visual-card > label,
      .mission-shell > label {
        width: min(340px, 100%);
        margin: 0 auto;
      }

      .preview-card {
        display: grid;
        gap: 10px;
      }

      .preview-label,
      .toggle-title {
        margin-bottom: 8px;
        display: block;
      }

      .preview-image {
        width: 100%;
        min-height: 240px;
        object-fit: contain;
        border: 1px solid var(--line);
        border-radius: 16px;
        background: rgba(9, 14, 22, 0.9);
        cursor: zoom-in;
      }

      .option-row {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }

      .check-line {
        display: flex;
        gap: 10px;
        align-items: center;
        padding: 12px;
        border: 1px solid var(--line);
        border-radius: 14px;
        background: rgba(14, 22, 34, 0.62);
      }

      .toggle-buttons {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 8px;
      }

      .image-modal {
        display: none;
        position: fixed;
        inset: 0;
        z-index: 120;
        padding: 28px;
        background: rgba(4, 7, 12, 0.82);
        align-items: center;
        justify-content: center;
      }

      .image-modal.visible {
        display: flex;
      }

      .image-modal-card {
        width: min(1100px, 100%);
        max-height: calc(100vh - 56px);
        display: grid;
        gap: 14px;
        padding: 20px;
        border: 1px solid var(--line-strong);
        border-radius: 22px;
        background: rgba(10, 16, 25, 0.98);
        box-shadow: 0 30px 80px rgba(0, 0, 0, 0.45);
      }

      .image-modal-header {
        display: flex;
        justify-content: space-between;
        gap: 12px;
        align-items: center;
      }

      .image-modal-image {
        width: 100%;
        max-height: calc(100vh - 170px);
        object-fit: contain;
        border-radius: 16px;
        background: rgba(0, 0, 0, 0.2);
      }

      @media (max-width: 900px) {
        .option-row,
        .visual-grid {
          grid-template-columns: 1fr;
        }

        .selector-header {
          flex-direction: column;
        }
      }
    </style>
    <link rel="stylesheet" href="/css/partida-creacion.css" />
  </head>
  <body class="setup-page setup-config">
    <c:set var="sidebarActive" value="partidas" scope="request" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <c:set var="partida" value="${configuracion.partida}" />
    <c:set var="esAos" value="${partida.sistemaJuego.codigo eq 'AOS_4'}" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Despliegue y opciones</h2>
              <p class="page-subtitle">Configura la mesa y las opciones para <c:out value="${configuracion.estiloJuegoTexto}" />.</p>
            </div>

            <c:if test="${not empty mensajeError}">
              <div class="error-box"><c:out value="${mensajeError}" /></div>
            </c:if>

            <form class="config-card" method="post" action="/partidas/${partida.id}/configuracion" id="configuracionForm">
              <div class="form-grid">
                <c:if test="${not esAos}">
                <section class="mission-shell">
                  <label>
                    Seleccion de mision
                    <select id="tipoMisionSelect" name="tipoMision">
                    <c:forEach var="opcion" items="${configuracion.opcionesMision}">
                      <c:choose>
                        <c:when test="${partida.nombreMision eq opcion.codigo or (empty partida.nombreMision and opcion.codigo eq 'CUSTOM MISION')}">
                          <option value="${opcion.codigo}" data-layouts="${opcion.layoutsRecomendados}" data-fixed-deployment-code="${opcion.despliegueFijoCodigo}" data-fixed-deployment-name="${opcion.despliegueFijoNombre}" selected><c:out value="${opcion.nombre}" /></option>
                        </c:when>
                        <c:otherwise>
                          <option value="${opcion.codigo}" data-layouts="${opcion.layoutsRecomendados}" data-fixed-deployment-code="${opcion.despliegueFijoCodigo}" data-fixed-deployment-name="${opcion.despliegueFijoNombre}"><c:out value="${opcion.nombre}" /></option>
                        </c:otherwise>
                      </c:choose>
                    </c:forEach>
                    </select>
                  </label>
                </section>

                </c:if>
                <section class="selector-shell">
                  <div class="selector-header">
                    <div>
                      <p class="helper-copy" id="textoDespliegueFijo"></p>
                      <p class="helper-copy" id="textoLayoutRestringido"></p>
                    </div>
                  </div>

                  <c:choose>
                    <c:when test="${esAos}">
                      <article class="visual-card">
                        <label>Despliegue
                          <select id="despliegueSelect" name="despliegue" required>
                            <c:forEach var="despliegue" items="${configuracion.desplieguesSimetricos}">
                              <option value="<c:out value='${despliegue.codigo}' />" data-image="${despliegue.imagenUrl}"
                                      data-label="<c:out value='${despliegue.nombre}' />" ${partida.despliegueMision eq despliegue.codigo ? 'selected' : ''}><c:out value="${despliegue.nombre}" /></option>
                            </c:forEach>
                          </select>
                        </label>
                        <div class="preview-card">
                          <span class="preview-label">Vista del despliegue</span>
                          <strong id="desplieguePreviewTitle"></strong>
                          <img class="preview-image" id="desplieguePreviewImage" alt="Despliegue seleccionado" data-modal-trigger="true" />
                        </div>
                      </article>
                    </c:when>
                    <c:when test="${configuracion.juegoEquilibrado}">
                      <div class="visual-grid">
                        <article class="visual-card">
                          <label>
                            Layout
                            <select id="layoutSelect" name="layout" required>
                              <c:forEach var="layout" items="${configuracion.layouts}" varStatus="estado">
                                <c:choose>
                                  <c:when test="${partida.layoutMision eq layout.codigo or partida.layoutMision eq layout.nombre or (empty partida.layoutMision and estado.first)}">
                                    <option value="${layout.codigo}" data-image="${layout.imagenUrl}" data-label="${layout.nombre}" data-base-label="${layout.nombre}" data-layout-number="${layout.numero}" selected><c:out value="${layout.nombre}" /></option>
                                  </c:when>
                                  <c:otherwise>
                                    <option value="${layout.codigo}" data-image="${layout.imagenUrl}" data-label="${layout.nombre}" data-base-label="${layout.nombre}" data-layout-number="${layout.numero}"><c:out value="${layout.nombre}" /></option>
                                  </c:otherwise>
                                </c:choose>
                              </c:forEach>
                            </select>
                          </label>
                          <div class="preview-card">
                            <span class="preview-label">Preview del layout</span>
                            <strong id="layoutPreviewTitle"></strong>
                            <img class="preview-image" id="layoutPreviewImage" alt="Preview del layout seleccionado" data-modal-trigger="true" />
                          </div>
                        </article>

                        <article class="visual-card">
                          <label>
                            Despliegue
                            <select id="despliegueSelect" name="despliegue" required>
                              <c:forEach var="despliegue" items="${configuracion.desplieguesSimetricos}" varStatus="estado">
                                <c:choose>
                                  <c:when test="${partida.despliegueMision eq despliegue.codigo or partida.despliegueMision eq despliegue.nombre or (empty partida.despliegueMision and estado.first)}">
                                    <option value="${despliegue.codigo}" data-image="${despliegue.imagenUrl}" data-label="${despliegue.nombre}" selected><c:out value="${despliegue.nombre}" /></option>
                                  </c:when>
                                  <c:otherwise>
                                    <option value="${despliegue.codigo}" data-image="${despliegue.imagenUrl}" data-label="${despliegue.nombre}"><c:out value="${despliegue.nombre}" /></option>
                                  </c:otherwise>
                                </c:choose>
                              </c:forEach>
                            </select>
                          </label>
                          <div class="preview-card">
                            <span class="preview-label">Preview del despliegue</span>
                            <strong id="desplieguePreviewTitle"></strong>
                            <img class="preview-image" id="desplieguePreviewImage" alt="Preview del despliegue seleccionado" data-modal-trigger="true" />
                          </div>
                        </article>
                      </div>
                    </c:when>
                    <c:otherwise>
                      <article class="visual-card">
                        <label>
                          Despliegue
                          <select id="despliegueSelect" name="despliegue" required>
                            <optgroup label="Despliegues simetricos">
                              <c:forEach var="despliegue" items="${configuracion.desplieguesMixtos}">
                                <c:if test="${despliegue.categoria eq 'SIMETRICO'}">
                                  <c:choose>
                                    <c:when test="${partida.despliegueMision eq despliegue.codigo or partida.despliegueMision eq despliegue.nombre}">
                                      <option value="${despliegue.codigo}" data-image="${despliegue.imagenUrl}" data-label="${despliegue.nombre}" selected><c:out value="${despliegue.nombre}" /></option>
                                    </c:when>
                                    <c:otherwise>
                                      <option value="${despliegue.codigo}" data-image="${despliegue.imagenUrl}" data-label="${despliegue.nombre}"><c:out value="${despliegue.nombre}" /></option>
                                    </c:otherwise>
                                  </c:choose>
                                </c:if>
                              </c:forEach>
                            </optgroup>
                            <optgroup label="Despliegues asimetricos">
                              <c:forEach var="despliegue" items="${configuracion.desplieguesMixtos}" varStatus="estado">
                                <c:if test="${despliegue.categoria eq 'ASIMETRICO'}">
                                  <c:choose>
                                    <c:when test="${partida.despliegueMision eq despliegue.codigo or partida.despliegueMision eq despliegue.nombre or (empty partida.despliegueMision and estado.first)}">
                                      <option value="${despliegue.codigo}" data-image="${despliegue.imagenUrl}" data-label="${despliegue.nombre}" selected><c:out value="${despliegue.nombre}" /></option>
                                    </c:when>
                                    <c:otherwise>
                                      <option value="${despliegue.codigo}" data-image="${despliegue.imagenUrl}" data-label="${despliegue.nombre}"><c:out value="${despliegue.nombre}" /></option>
                                    </c:otherwise>
                                  </c:choose>
                                </c:if>
                              </c:forEach>
                            </optgroup>
                          </select>
                        </label>
                        <div class="preview-card">
                          <span class="preview-label">Preview del despliegue</span>
                          <strong id="desplieguePreviewTitle"></strong>
                          <img class="preview-image" id="desplieguePreviewImage" alt="Preview del despliegue seleccionado" data-modal-trigger="true" />
                        </div>
                      </article>
                    </c:otherwise>
                  </c:choose>
                </section>

                <div class="option-row">
                  <div>
                    <span class="toggle-title">Jugador defensor</span>
                    <div class="toggle-buttons">
                      <label>
                        <c:choose>
                          <c:when test="${partida.jugadorDefensor eq 'RIVAL'}">
                            <input type="radio" name="jugadorDefensor" value="USUARIO" />
                          </c:when>
                          <c:otherwise>
                            <input type="radio" name="jugadorDefensor" value="USUARIO" checked />
                          </c:otherwise>
                        </c:choose>
                        <span>Yo</span>
                      </label>
                      <label>
                        <c:choose>
                          <c:when test="${partida.jugadorDefensor eq 'RIVAL'}">
                            <input type="radio" name="jugadorDefensor" value="RIVAL" checked />
                          </c:when>
                          <c:otherwise>
                            <input type="radio" name="jugadorDefensor" value="RIVAL" />
                          </c:otherwise>
                        </c:choose>
                        <span>Oponente</span>
                      </label>
                    </div>
                  </div>

                  <div>
                    <span class="toggle-title">Jugador que va primero</span>
                    <div class="toggle-buttons">
                      <label>
                        <c:choose>
                          <c:when test="${partida.jugadorPrimero eq 'RIVAL'}">
                            <input type="radio" name="jugadorPrimero" value="USUARIO" />
                          </c:when>
                          <c:otherwise>
                            <input type="radio" name="jugadorPrimero" value="USUARIO" checked />
                          </c:otherwise>
                        </c:choose>
                        <span>Yo</span>
                      </label>
                      <label>
                        <c:choose>
                          <c:when test="${partida.jugadorPrimero eq 'RIVAL'}">
                            <input type="radio" name="jugadorPrimero" value="RIVAL" checked />
                          </c:when>
                          <c:otherwise>
                            <input type="radio" name="jugadorPrimero" value="RIVAL" />
                          </c:otherwise>
                        </c:choose>
                        <span>Oponente</span>
                      </label>
                    </div>
                  </div>
                </div>

                <label class="check-line">
                  <c:choose>
                    <c:when test="${partida.mostrarCommandPoints}">
                      <input type="checkbox" name="mostrarCommandPoints" checked />
                    </c:when>
                    <c:otherwise>
                      <input type="checkbox" name="mostrarCommandPoints" />
                    </c:otherwise>
                  </c:choose>
                  Ver contador de command points
                </label>

                <c:if test="${not esAos}">
                <label class="check-line">
                  <c:choose>
                    <c:when test="${partida.usarCartasGiro}">
                      <input type="checkbox" name="usarCartasGiro" checked />
                    </c:when>
                    <c:otherwise>
                      <input type="checkbox" name="usarCartasGiro" />
                    </c:otherwise>
                  </c:choose>
                  Usar cartas de giro
                </label>
                </c:if>

                <div class="form-actions">
                  <a class="button-secondary" href="/partidas/${partida.id}/jugadores">Atras</a>
                  <button class="button-primary" type="submit">Ir a ronda 1</button>
                </div>
              </div>
            </form>
          </section>
        </main>
      </div>
    </div>

    <div class="image-modal" id="imageModal">
      <div class="image-modal-card">
        <div class="image-modal-header">
          <h3 id="imageModalTitle">Vista ampliada</h3>
          <button class="button-secondary" type="button" id="imageModalClose">Cerrar</button>
        </div>
        <img class="image-modal-image" id="imageModalImage" alt="Vista ampliada del despliegue o layout" />
      </div>
    </div>

    <script>
      (function () {
        function pintarPreview(selectId, titleId, imageId) {
          const selector = document.getElementById(selectId);
          if (!selector) {
            return;
          }

          const opcion = selector.options[selector.selectedIndex];
          const titulo = document.getElementById(titleId);
          const imagen = document.getElementById(imageId);

          if (!opcion || !titulo || !imagen) {
            return;
          }

          titulo.textContent = opcion.dataset.label || opcion.textContent;
          imagen.src = opcion.dataset.image || "";
        }

        function actualizarTextoMision() {
          const actual = document.getElementById("tipoMisionSelect");
          const texto = document.getElementById("textoMisionCustom");
          if (!actual || !texto) {
            return;
          }

          if (actual.value === "CUSTOM MISION") {
            texto.textContent = "Estas definiendo una mision custom con la combinacion visual seleccionada.";
            return;
          }

          texto.textContent = "La mision seleccionada fija el despliegue y limita el layout a las opciones recomendadas.";
        }

        function buscarOpcionDespliegue(selector, codigo, nombre) {
          if (!selector) {
            return null;
          }

          return Array.from(selector.options).find(function (opcion) {
            const etiqueta = (opcion.dataset.label || opcion.textContent || "").trim();
            return opcion.value === codigo || etiqueta === nombre || opcion.textContent.trim() === nombre;
          }) || null;
        }

        function aplicarDespliegueFijo() {
          const selectorMision = document.getElementById("tipoMisionSelect");
          const selectorDespliegue = document.getElementById("despliegueSelect");
          const textoDespliegueFijo = document.getElementById("textoDespliegueFijo");
          if (!selectorMision || !selectorDespliegue || !textoDespliegueFijo) {
            return;
          }

          const opcionMision = selectorMision.options[selectorMision.selectedIndex];
          const codigoFijo = opcionMision ? opcionMision.dataset.fixedDeploymentCode : "";
          const nombreFijo = opcionMision ? opcionMision.dataset.fixedDeploymentName : "";

          if (!codigoFijo) {
            selectorDespliegue.disabled = false;
            textoDespliegueFijo.textContent = "";
            textoDespliegueFijo.classList.remove("locked");
            pintarPreview("despliegueSelect", "desplieguePreviewTitle", "desplieguePreviewImage");
            return;
          }

          const opcionDespliegue = buscarOpcionDespliegue(selectorDespliegue, codigoFijo, nombreFijo);
          if (opcionDespliegue) {
            selectorDespliegue.value = opcionDespliegue.value;
          }
          selectorDespliegue.disabled = true;
          textoDespliegueFijo.textContent = "Despliegue fijado por la misión.";
          textoDespliegueFijo.classList.add("locked");
          pintarPreview("despliegueSelect", "desplieguePreviewTitle", "desplieguePreviewImage");
        }

        function layoutsRecomendadosSeleccionados() {
          const selectorMision = document.getElementById("tipoMisionSelect");
          if (!selectorMision) {
            return [];
          }

          const opcion = selectorMision.options[selectorMision.selectedIndex];
          if (!opcion || !opcion.dataset.layouts) {
            return [];
          }

          return opcion.dataset.layouts
            .replace("[", "")
            .replace("]", "")
            .split(",")
            .map(function (valor) { return valor.trim(); })
            .filter(function (valor) { return valor !== ""; });
        }

        function actualizarLayoutsSegunMision() {
          const layoutSelect = document.getElementById("layoutSelect");
          const textoLayoutRestringido = document.getElementById("textoLayoutRestringido");
          if (!layoutSelect) {
            return;
          }

          const recomendados = layoutsRecomendadosSeleccionados();
          let primerLayoutDisponible = null;
          let totalDisponibles = 0;

          Array.from(layoutSelect.options).forEach(function (opcion) {
            const baseLabel = opcion.dataset.baseLabel || opcion.textContent;
            const numero = opcion.dataset.layoutNumber || "";
            const recomendado = numero && recomendados.indexOf(numero) >= 0;

            if (recomendados.length > 0 && recomendado) {
              opcion.textContent = baseLabel + " [Recomendado]";
              opcion.dataset.label = baseLabel + " [Recomendado]";
              opcion.disabled = false;
              totalDisponibles += 1;
              if (!primerLayoutDisponible) {
                primerLayoutDisponible = opcion;
              }
            } else if (recomendados.length > 0) {
              opcion.textContent = baseLabel;
              opcion.dataset.label = baseLabel;
              opcion.disabled = true;
            } else {
              opcion.textContent = baseLabel;
              opcion.dataset.label = baseLabel;
              opcion.disabled = false;
            }
          });

          const opcionActual = layoutSelect.options[layoutSelect.selectedIndex];
          if (recomendados.length > 0 && (!opcionActual || opcionActual.disabled) && primerLayoutDisponible) {
            layoutSelect.value = primerLayoutDisponible.value;
          }

          if (!textoLayoutRestringido) {
            return;
          }

          if (recomendados.length === 0) {
            textoLayoutRestringido.textContent = "";
            textoLayoutRestringido.classList.remove("locked");
            return;
          }

          if (totalDisponibles === 1 && primerLayoutDisponible) {
            textoLayoutRestringido.textContent = "Un único layout permitido.";
          } else {
            textoLayoutRestringido.textContent = "Solo se permiten los layouts recomendados.";
          }
          textoLayoutRestringido.classList.add("locked");
        }

        function abrirModalDesdeImagen(imagen, titulo) {
          const modal = document.getElementById("imageModal");
          const modalImage = document.getElementById("imageModalImage");
          const modalTitle = document.getElementById("imageModalTitle");
          if (!modal || !modalImage || !imagen.src) {
            return;
          }

          modalImage.src = imagen.src;
          modalTitle.textContent = titulo || "Vista ampliada";
          modal.classList.add("visible");
        }

        function cerrarModal() {
          const modal = document.getElementById("imageModal");
          const modalImage = document.getElementById("imageModalImage");
          if (!modal || !modalImage) {
            return;
          }
          modal.classList.remove("visible");
          modalImage.src = "";
        }

        const layoutSelect = document.getElementById("layoutSelect");
        if (layoutSelect) {
          layoutSelect.addEventListener("change", function () {
            pintarPreview("layoutSelect", "layoutPreviewTitle", "layoutPreviewImage");
          });
          pintarPreview("layoutSelect", "layoutPreviewTitle", "layoutPreviewImage");
        }

        const despliegueSelect = document.getElementById("despliegueSelect");
        if (despliegueSelect) {
          despliegueSelect.addEventListener("change", function () {
            pintarPreview("despliegueSelect", "desplieguePreviewTitle", "desplieguePreviewImage");
          });
          pintarPreview("despliegueSelect", "desplieguePreviewTitle", "desplieguePreviewImage");
        }

        const tipoMisionSelect = document.getElementById("tipoMisionSelect");
        if (tipoMisionSelect) {
          tipoMisionSelect.addEventListener("change", function () {
            actualizarTextoMision();
            actualizarLayoutsSegunMision();
            aplicarDespliegueFijo();
            pintarPreview("layoutSelect", "layoutPreviewTitle", "layoutPreviewImage");
          });
        }
        actualizarTextoMision();
        actualizarLayoutsSegunMision();
        aplicarDespliegueFijo();

        document.querySelectorAll(".preview-image").forEach(function (imagen) {
          imagen.addEventListener("click", function () {
            const titulo = imagen.previousElementSibling ? imagen.previousElementSibling.textContent : "";
            abrirModalDesdeImagen(imagen, titulo);
          });
        });

        const imageModal = document.getElementById("imageModal");
        const imageModalClose = document.getElementById("imageModalClose");
        if (imageModalClose) {
          imageModalClose.addEventListener("click", cerrarModal);
        }
        if (imageModal) {
          imageModal.addEventListener("click", function (event) {
            if (event.target === imageModal) {
              cerrarModal();
            }
          });
        }
        document.addEventListener("keydown", function (event) {
          if (event.key === "Escape") {
            cerrarModal();
          }
        });

        const configuracionForm = document.getElementById("configuracionForm");
        if (configuracionForm && despliegueSelect) {
          configuracionForm.addEventListener("submit", function () {
            despliegueSelect.disabled = false;
          });
        }
      })();
    </script>
  </body>
</html>
