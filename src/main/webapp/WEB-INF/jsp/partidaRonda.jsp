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
      }

      .round-board {
        display: grid;
        grid-template-columns: minmax(0, 1fr) 2px minmax(0, 1fr);
        gap: 20px;
      }

      .separator {
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
        gap: 10px;
        margin-top: 10px;
      }

      .popup {
        display: none;
        position: fixed;
        inset: 6vh 50% auto auto;
        z-index: 30;
        width: min(94vw, 680px);
        max-height: 88vh;
        overflow: auto;
        padding: 20px;
        border: 1px solid var(--line);
        border-radius: 20px;
        background: var(--panel-soft);
        box-shadow: 0 30px 100px rgba(0, 0, 0, 0.5);
        transform: translateX(50%);
      }

      .popup.visible {
        display: block;
      }

      .mission-option {
        display: grid;
        gap: 6px;
        padding: 12px;
        border-bottom: 1px solid var(--line);
      }

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
    </style>
  </head>
  <body>
    <c:set var="sidebarActive" value="partidas" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <div class="app-shell">
      <jsp:include page="header.jsp" />

      <div class="app-main">
        <main class="page-content">
          <section class="page-panel">
            <form method="post" action="/partidas/${ronda.partida.id}/ronda/${ronda.numeroRonda}">
              <div class="round-header">
                <div>
                  <h2 class="page-title">Ronda ${ronda.numeroRonda} de 5</h2>
                  <p class="page-subtitle">
                    <c:out value="${ronda.resumenConfiguracion.estiloJuego}" />
                    Â· Mision: <c:out value="${ronda.resumenConfiguracion.mision}" />
                    <c:if test="${not empty ronda.resumenConfiguracion.layout}">
                      Â· Layout: <c:out value="${ronda.resumenConfiguracion.layout}" />
                    </c:if>
                    <c:if test="${not empty ronda.resumenConfiguracion.despliegue}">
                      Â· Despliegue: <c:out value="${ronda.resumenConfiguracion.despliegue}" />
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
                    <c:out value="${ronda.numeroRonda == 5 ? 'Ir al resultado final' : 'Guardar e ir a siguiente ronda'}" />
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
                      <div class="cp-row">
                        <label>CP inicio <input type="number" min="0" name="${ronda.izquierda.prefijo}CpInicio" value="${ronda.izquierda.cpInicio}" /></label>
                        <label>CP fin <input type="number" min="0" name="${ronda.izquierda.prefijo}CpFin" value="${ronda.izquierda.cpFin}" /></label>
                      </div>
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
                      <div class="cp-row">
                        <label>CP inicio <input type="number" min="0" name="${ronda.derecha.prefijo}CpInicio" value="${ronda.derecha.cpInicio}" /></label>
                        <label>CP fin <input type="number" min="0" name="${ronda.derecha.prefijo}CpFin" value="${ronda.derecha.cpFin}" /></label>
                      </div>
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

    <div class="popup" id="popupSecundarias">
      <h3>Elegir misiones secundarias</h3>
      <p class="page-subtitle">Marca todas las secundarias que quieras o usa el boton para aÃ±adir una al azar.</p>
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
      <div class="popup-actions">
        <button class="button-secondary" type="button" id="randomSecundaria">Randomizar</button>
        <button class="button-secondary" type="button" id="cerrarSecundarias">Cerrar</button>
      </div>
    </div>

    <script>
      (function () {
        let prefijoActivo = "";
        const popup = document.getElementById("popupSecundarias");
        const botones = document.querySelectorAll(".abrir-secundarias");
        const cerrar = document.getElementById("cerrarSecundarias");
        const random = document.getElementById("randomSecundaria");

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
              "<label><input type='checkbox' class='cumplida' /> Cumplida</label>" +
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
            popup.classList.add("visible");
          });
        });

        cerrar.addEventListener("click", function () {
          popup.classList.remove("visible");
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
