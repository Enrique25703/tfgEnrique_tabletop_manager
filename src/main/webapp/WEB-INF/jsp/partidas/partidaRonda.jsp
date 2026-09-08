<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Ronda ${ronda.numeroRonda}</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .round-header {
        display: flex;
        justify-content: space-between;
        gap: 12px;
        align-items: center;
        margin-bottom: 18px;
        flex-wrap: wrap;
      }

      .page-panel, .round-header > *, .round-board > *,
      .player-panel > *, .mission-card > *, .secondary-card > * {
        min-width: 0;
        max-width: 100%;
        overflow-wrap: anywhere;
      }

      .round-header > div:first-child { flex: 1 1 280px; }
      .round-header .form-actions { display: flex; flex-wrap: wrap; gap: 10px; }
      .round-header button { white-space: normal; }

      .round-board {
        display: grid;
        grid-template-columns: minmax(0, 1fr) 2px minmax(0, 1fr);
        gap: 20px;
        align-items: start;
      }

      .separator {
        align-self: stretch;
        border-radius: 999px;
        background: var(--line-strong);
      }

      .player-panel,
      .mission-card,
      .secondary-card {
        padding: 18px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: var(--panel-soft);
      }

      .player-panel {
        display: grid;
        grid-template-columns: minmax(0, 1fr);
        align-content: start;
        gap: 14px;
      }

      .player-title {
        margin: 0;
        font-size: 1.5rem;
      }

      .tags {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }

      .tag {
        padding: 7px 10px;
        border: 1px solid var(--line);
        border-radius: 999px;
        color: var(--muted);
      }

      .score-row,
      .cp-row {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 10px;
      }

      .secondary-list {
        display: grid;
        grid-template-columns: minmax(0, 1fr);
        gap: 10px;
        margin-top: 10px;
      }

      .cp-counter { margin: 0; padding: 16px; border: 1px solid var(--line-strong); border-radius: 16px; }
      .cp-counter legend { padding: 0 6px; font-weight: 600; }
      .cp-balance { display: flex; justify-content: space-between; align-items: baseline; gap: 10px; margin-bottom: 12px; }
      .cp-balance output { font-size: 2rem; font-weight: 700; font-variant-numeric: tabular-nums; }
      .cp-row label, .mission-card > label, .secondary-card > label { display: grid; gap: 6px; min-width: 0; }
      .cp-row input { min-width: 0; text-align: center; }
      .cp-counter .page-subtitle { margin: 10px 0 0; font-size: 0.85rem; }
      .secondary-card { display: grid; gap: 10px; }
      .secondary-card .completed-toggle { display: flex; align-items: center; gap: 8px; }
      .player-panel input[type="checkbox"], .popup input[type="checkbox"] { width: 18px; height: 18px; padding: 0; flex: 0 0 auto; accent-color: var(--accent); }
      .mission-card h4, .secondary-card p { margin: 0 0 10px; }

      .popup {
        width: min(94vw, 680px);
        max-height: 88vh;
        max-height: 88dvh;
        overflow: hidden;
        padding: 0;
        border: 1px solid var(--line);
        border-radius: 20px;
        background: var(--panel-soft);
        color: var(--text);
        box-shadow: 0 30px 100px rgba(0, 0, 0, 0.5);
      }

      .popup[open] { display: flex; flex-direction: column; }
      .popup::backdrop { background: rgba(0, 0, 0, 0.65); }
      .popup-header { padding: 18px; border-bottom: 1px solid var(--line); flex: 0 0 auto; }
      .popup-header h3 { margin: 0 0 8px; }
      #listaMisionesPopup { overflow-y: auto; min-height: 0; padding: 0 8px 12px; overscroll-behavior: contain; }

      .mission-option {
        display: grid;
        gap: 6px;
        padding: 12px;
        border-bottom: 1px solid var(--line);
        grid-template-columns: 20px minmax(0, 1fr);
        column-gap: 10px;
        cursor: pointer;
        overflow-wrap: anywhere;
      }
      .mission-option > .mission-description, .mission-option > .mission-score { grid-column: 2; }
      .mission-option:has(input:checked) { background: rgba(79, 127, 184, 0.15); }

      .popup-actions {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
        margin-top: 16px;
      }

      @media (max-width: 1000px) {
        .round-board {
          grid-template-columns: 1fr;
        }

        .separator {
          height: 2px;
        }
      }
      @media (max-width: 540px) {
        .page-content { padding: 8px; }
        .page-panel { padding: 12px; }
        .player-panel, .mission-card, .secondary-card { padding: 12px; border-radius: 12px; }
        .round-header .form-actions { width: 100%; }
        .round-header .form-actions > * { flex: 1 1 160px; text-align: center; }
      }
    </style>
  </head>
  <body>
    <c:set var="sidebarActive" value="partidas" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <main class="page-content">
          <section class="page-panel">
            <c:if test="${not empty mensajeError}">
              <p class="note-box" role="alert"><c:out value="${mensajeError}" /></p>
            </c:if>
            <form method="post" action="/partidas/${ronda.partida.id}/ronda/${ronda.numeroRonda}">
              <div class="round-header">
                <div>
                  <h2 class="page-title">Ronda ${ronda.numeroRonda} de 5</h2>
                  <p class="page-subtitle">
                    <c:out value="${ronda.resumenConfiguracion.estiloJuego}" />
                    &middot; Mision: <c:out value="${ronda.resumenConfiguracion.mision}" />
                    <c:if test="${not empty ronda.resumenConfiguracion.layout}">
                      &middot; Layout: <c:out value="${ronda.resumenConfiguracion.layout}" />
                    </c:if>
                    <c:if test="${not empty ronda.resumenConfiguracion.despliegue}">
                      &middot; Despliegue: <c:out value="${ronda.resumenConfiguracion.despliegue}" />
                    </c:if>
                  </p>
                </div>
                <div class="form-actions">
                  <c:choose>
                    <c:when test="${ronda.numeroRonda == 1}">
                      <a class="button-secondary" href="/partidas/${ronda.partida.id}/configuracion">Atras</a>
                    </c:when>
                    <c:otherwise>
                      <a class="button-secondary" href="/partidas/${ronda.partida.id}/ronda/${ronda.numeroRonda - 1}">Atras</a>
                    </c:otherwise>
                  </c:choose>
                  <button class="button-primary" type="submit">
                    <c:out value="${ronda.numeroRonda == 5 ? 'Ir al resultado final' : 'Siguiente ronda'}" />
                  </button>
                </div>
              </div>

              <div class="round-board">
                <article class="player-panel" data-player-panel="${ronda.izquierda.prefijo}">
                  <h3 class="player-title"><c:out value="${ronda.izquierda.nombre}" /></h3>
                  <div class="tags">
                    <span class="tag"><c:out value="${ronda.izquierda.faccion}" /></span>
                    <c:if test="${ronda.izquierda.primero}"><span class="tag">Va primero</span></c:if>
                    <c:if test="${ronda.izquierda.defensor}"><span class="tag">Defensor</span></c:if>
                  </div>

                  <c:if test="${not empty ronda.izquierda.lista}">
                    <div class="mission-card"><strong>Lista:</strong> <c:out value="${ronda.izquierda.lista}" /></div>
                  </c:if>

                  <c:choose>
                    <c:when test="${ronda.partida.mostrarCommandPoints}">
                      <fieldset class="cp-counter" data-cp-inicio="${ronda.izquierda.cpInicio}">
                        <legend>Command Points (CP)</legend>
                        <div class="cp-balance"><span>Disponibles</span><output class="cp-total" aria-live="polite">${ronda.izquierda.cpFin}</output></div>
                        <div class="cp-row">
                          <label>Gastados <input class="cp-spent" type="number" min="0" step="1" name="${ronda.izquierda.prefijo}CpGastados" value="${ronda.izquierda.cpGastados}" /></label>
                          <label>Ganados <input class="cp-earned" type="number" min="0" step="1" name="${ronda.izquierda.prefijo}CpGanados" value="${ronda.izquierda.cpGanados}" /></label>
                        </div>
                        <p class="page-subtitle">En esta ronda &middot; Saldo inicial: ${ronda.izquierda.cpInicio} CP</p>
                      </fieldset>
                    </c:when>
                    <c:otherwise>
                      <input type="hidden" name="${ronda.izquierda.prefijo}CpInicio" value="0" />
                      <input type="hidden" name="${ronda.izquierda.prefijo}CpFin" value="0" />
                    </c:otherwise>
                  </c:choose>

                  <section class="mission-card">
                    <h4>Mision principal: <c:out value="${ronda.misionPrincipal.titulo}" /></h4>
                    <p><c:out value="${ronda.misionPrincipal.descripcion}" /></p>
                    <p><strong>Puntuacion:</strong> <c:out value="${ronda.misionPrincipal.puntuacion}" /></p>
                    <label>Puntos primaria <input class="score-input" type="number" min="0" name="${ronda.izquierda.prefijo}Primaria" value="${ronda.izquierda.primaria}" /></label>
                  </section>

                  <section class="mission-card">
                    <h4>Misiones secundarias</h4>
                    <button class="button-secondary abrir-secundarias" type="button" data-prefijo="${ronda.izquierda.prefijo}">Elegir secundarias</button>
                    <input type="hidden" class="secondary-total" name="${ronda.izquierda.prefijo}Secundaria" value="${ronda.izquierda.secundaria}" />
                    <textarea class="secondary-detail" name="${ronda.izquierda.prefijo}Detalle" hidden><c:out value="${ronda.izquierda.detalle}" /></textarea>
                    <div class="secondary-list" id="${ronda.izquierda.prefijo}-secundarias"></div>
                    <p>Total secundarias: <strong class="secondary-total-text">${ronda.izquierda.secundaria}</strong> VP</p>
                  </section>
                </article>

                <div class="separator"></div>

                <article class="player-panel" data-player-panel="${ronda.derecha.prefijo}">
                  <h3 class="player-title"><c:out value="${ronda.derecha.nombre}" /></h3>
                  <div class="tags">
                    <span class="tag"><c:out value="${ronda.derecha.faccion}" /></span>
                    <c:if test="${ronda.derecha.primero}"><span class="tag">Va primero</span></c:if>
                    <c:if test="${ronda.derecha.defensor}"><span class="tag">Defensor</span></c:if>
                  </div>

                  <c:if test="${not empty ronda.derecha.lista}">
                    <div class="mission-card"><strong>Lista:</strong> <c:out value="${ronda.derecha.lista}" /></div>
                  </c:if>

                  <c:choose>
                    <c:when test="${ronda.partida.mostrarCommandPoints}">
                      <fieldset class="cp-counter" data-cp-inicio="${ronda.derecha.cpInicio}">
                        <legend>Command Points (CP)</legend>
                        <div class="cp-balance"><span>Disponibles</span><output class="cp-total" aria-live="polite">${ronda.derecha.cpFin}</output></div>
                        <div class="cp-row">
                          <label>Gastados <input class="cp-spent" type="number" min="0" step="1" name="${ronda.derecha.prefijo}CpGastados" value="${ronda.derecha.cpGastados}" /></label>
                          <label>Ganados <input class="cp-earned" type="number" min="0" step="1" name="${ronda.derecha.prefijo}CpGanados" value="${ronda.derecha.cpGanados}" /></label>
                        </div>
                        <p class="page-subtitle">En esta ronda &middot; Saldo inicial: ${ronda.derecha.cpInicio} CP</p>
                      </fieldset>
                    </c:when>
                    <c:otherwise>
                      <input type="hidden" name="${ronda.derecha.prefijo}CpInicio" value="0" />
                      <input type="hidden" name="${ronda.derecha.prefijo}CpFin" value="0" />
                    </c:otherwise>
                  </c:choose>

                  <section class="mission-card">
                    <h4>Mision principal: <c:out value="${ronda.misionPrincipal.titulo}" /></h4>
                    <p><c:out value="${ronda.misionPrincipal.descripcion}" /></p>
                    <p><strong>Puntuacion:</strong> <c:out value="${ronda.misionPrincipal.puntuacion}" /></p>
                    <label>Puntos primaria <input class="score-input" type="number" min="0" name="${ronda.derecha.prefijo}Primaria" value="${ronda.derecha.primaria}" /></label>
                  </section>

                  <section class="mission-card">
                    <h4>Misiones secundarias</h4>
                    <button class="button-secondary abrir-secundarias" type="button" data-prefijo="${ronda.derecha.prefijo}">Elegir secundarias</button>
                    <input type="hidden" class="secondary-total" name="${ronda.derecha.prefijo}Secundaria" value="${ronda.derecha.secundaria}" />
                    <textarea class="secondary-detail" name="${ronda.derecha.prefijo}Detalle" hidden><c:out value="${ronda.derecha.detalle}" /></textarea>
                    <div class="secondary-list" id="${ronda.derecha.prefijo}-secundarias"></div>
                    <p>Total secundarias: <strong class="secondary-total-text">${ronda.derecha.secundaria}</strong> VP</p>
                  </section>
                </article>
              </div>
            </form>
          </section>
        </main>
      </div>
    </div>

    <dialog class="popup" id="popupSecundarias" aria-labelledby="tituloSecundarias">
      <div class="popup-header">
      <h3 id="tituloSecundarias">Elegir misiones secundarias</h3>
      <p class="page-subtitle">Selecciona tus misiones o usa Randomizar para a&ntilde;adir una al azar.</p>
      <div class="popup-actions">
        <button class="button-primary" type="button" id="randomSecundaria" autofocus>Randomizar</button>
        <button class="button-secondary" type="button" id="cerrarSecundarias">Cerrar</button>
      </div>
      </div>
      <div id="listaMisionesPopup">
        <c:forEach items="${ronda.misionesSecundarias}" var="mision">
          <label class="mission-option">
            <span><input type="checkbox" value="${mision.id}" /></span>
            <strong class="mission-title"><c:out value="${mision.titulo}" /></strong>
            <span class="mission-description"><c:out value="${mision.descripcion}" /></span>
            <small class="mission-score"><c:out value="${mision.puntuacion}" /></small>
          </label>
        </c:forEach>
      </div>
    </dialog>

    <script>
      (function () {
        let prefijoActivo = "";
        const popup = document.getElementById("popupSecundarias");
        const botones = document.querySelectorAll(".abrir-secundarias");
        const cerrar = document.getElementById("cerrarSecundarias");
        const random = document.getElementById("randomSecundaria");

        document.querySelectorAll(".cp-counter").forEach(function (contador) {
          const ganados = contador.querySelector(".cp-earned");
          const gastados = contador.querySelector(".cp-spent");
          function actualizarCp() {
            const saldo = Number(contador.dataset.cpInicio) + Number(ganados.value) - Number(gastados.value);
            contador.querySelector(".cp-total").textContent = String(saldo);
            gastados.setCustomValidity(saldo < 0 ? "No puedes gastar mas CP de los disponibles." : "");
          }
          ganados.addEventListener("input", actualizarCp);
          gastados.addEventListener("input", actualizarCp);
          actualizarCp();
        });

        function panel(prefijo) {
          return document.querySelector('[data-player-panel="' + prefijo + '"]');
        }

        function leerDetalle(prefijo) {
          const campo = panel(prefijo).querySelector(".secondary-detail");
          if (!campo.value) {
            return [];
          }
          try {
            return JSON.parse(campo.value);
          } catch (e) {
            return [];
          }
        }

        function actualizarDetalle(prefijo, misiones) {
          const contenedor = panel(prefijo);
          const campoDetalle = contenedor.querySelector(".secondary-detail");
          const campoTotal = contenedor.querySelector(".secondary-total");
          const textoTotal = contenedor.querySelector(".secondary-total-text");
          let total = 0;

          for (let i = 0; i < misiones.length; i++) {
            if (misiones[i].cumplida) {
              total += parseInt(misiones[i].puntos || "0", 10);
            }
          }

          campoDetalle.value = JSON.stringify(misiones);
          campoTotal.value = String(total);
          textoTotal.textContent = String(total);
        }

        function guardarDetalle(prefijo, misiones) {
          actualizarDetalle(prefijo, misiones);
          pintarSecundarias(prefijo);
        }

        function pintarSecundarias(prefijo) {
          const contenedor = panel(prefijo);
          const lista = contenedor.querySelector(".secondary-list");
          const misiones = leerDetalle(prefijo);
          lista.innerHTML = "";

          if (misiones.length === 0) {
            lista.innerHTML = "<p class='page-subtitle'>Sin secundarias elegidas.</p>";
            return;
          }

          for (let i = 0; i < misiones.length; i++) {
            const mision = misiones[i];
            const tarjeta = document.createElement("div");
            tarjeta.className = "secondary-card";
            tarjeta.innerHTML =
              "<strong></strong>" +
              "<p></p>" +
              "<label class='completed-toggle'><input type='checkbox' class='cumplida' /> Cumplida</label>" +
              "<label>Puntos <input type='number' min='0' class='puntos-secundaria' /></label>";

            tarjeta.querySelector("strong").textContent = mision.titulo;
            tarjeta.querySelector("p").textContent = mision.descripcion || "";
            tarjeta.querySelector(".cumplida").checked = !!mision.cumplida;
            tarjeta.querySelector(".puntos-secundaria").value = mision.puntos || 0;

            tarjeta.querySelector(".cumplida").addEventListener("change", function () {
              mision.cumplida = this.checked;
              actualizarDetalle(prefijo, misiones);
            });
            tarjeta.querySelector(".puntos-secundaria").addEventListener("input", function () {
              mision.puntos = this.value;
              actualizarDetalle(prefijo, misiones);
            });

            lista.appendChild(tarjeta);
          }
        }

        function marcarPopup(prefijo) {
          const elegidas = leerDetalle(prefijo);
          const ids = elegidas.map(function (mision) { return mision.id; });
          popup.querySelectorAll("input[type='checkbox']").forEach(function (check) {
            check.checked = ids.indexOf(check.value) >= 0;
          });
        }

        function misionDesdeFila(fila, anterior) {
          const check = fila.querySelector("input[type='checkbox']");
          if (anterior) {
            return anterior;
          }
          return {
            id: check.value,
            titulo: fila.querySelector(".mission-title").textContent,
            descripcion: fila.querySelector(".mission-description").textContent,
            puntos: 0,
            cumplida: false
          };
        }

        function actualizarDesdePopup() {
          if (!prefijoActivo) {
            return;
          }

          const anteriores = leerDetalle(prefijoActivo);
          const anterioresPorId = {};
          for (let i = 0; i < anteriores.length; i++) {
            anterioresPorId[anteriores[i].id] = anteriores[i];
          }

          const misiones = [];
          popup.querySelectorAll(".mission-option").forEach(function (fila) {
            const check = fila.querySelector("input[type='checkbox']");
            if (check.checked) {
              misiones.push(misionDesdeFila(fila, anterioresPorId[check.value]));
            }
          });

          guardarDetalle(prefijoActivo, misiones);
        }

        botones.forEach(function (boton) {
          boton.addEventListener("click", function () {
            prefijoActivo = boton.dataset.prefijo;
            marcarPopup(prefijoActivo);
            popup.showModal();
            document.getElementById("listaMisionesPopup").scrollTop = 0;
            random.focus();
          });
        });

        cerrar.addEventListener("click", function () {
          popup.close();
        });

        popup.querySelectorAll(".mission-option input[type='checkbox']").forEach(function (check) {
          check.addEventListener("change", function () {
            actualizarDesdePopup();
          });
        });

        random.addEventListener("click", function () {
          const checks = Array.from(popup.querySelectorAll("input[type='checkbox']"));
          const noElegidas = checks.filter(function (check) { return !check.checked; });
          if (noElegidas.length > 0) {
            noElegidas[Math.floor(Math.random() * noElegidas.length)].checked = true;
            actualizarDesdePopup();
          }
        });

        document.querySelectorAll("[data-player-panel]").forEach(function (p) {
          pintarSecundarias(p.dataset.playerPanel);
        });
      })();
    </script>
  </body>
</html>
