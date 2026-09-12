<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Ronda ${ronda.numeroRonda}</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <link rel="stylesheet" href="/css/partida-ronda.css" />
  </head>
  <body class="round-page">
    <c:set var="sidebarActive" value="partidas" scope="request" />
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
                  <button class="round-config-link" type="button" data-round-info="configuracionRonda" data-info-title="Configuración de la partida" aria-haspopup="dialog">Ver configuración</button>
                  <template id="configuracionRonda">
                  <p>
                    <c:out value="${ronda.resumenConfiguracion.estiloJuego}" />
                    &middot; Mision: <c:out value="${ronda.resumenConfiguracion.mision}" />
                    <c:if test="${not empty ronda.resumenConfiguracion.layout}">
                      &middot; Layout: <c:out value="${ronda.resumenConfiguracion.layout}" />
                    </c:if>
                    <c:if test="${not empty ronda.resumenConfiguracion.despliegue}">
                      &middot; Despliegue: <c:out value="${ronda.resumenConfiguracion.despliegue}" />
                    </c:if>
                  </p>
                  </template>
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

              <div class="player-switch" aria-label="Jugador visible">
                <button type="button" data-show-player="${ronda.izquierda.prefijo}" aria-pressed="true" aria-controls="panel-${ronda.izquierda.prefijo}"><span><c:out value="${ronda.izquierda.nombre}" /></span><strong data-round-score="${ronda.izquierda.prefijo}">0 VP</strong></button>
                <button type="button" data-show-player="${ronda.derecha.prefijo}" aria-pressed="false" aria-controls="panel-${ronda.derecha.prefijo}"><span><c:out value="${ronda.derecha.nombre}" /></span><strong data-round-score="${ronda.derecha.prefijo}">0 VP</strong></button>
              </div>
              <div class="round-board">
                <article class="player-panel is-current" id="panel-${ronda.izquierda.prefijo}" data-player-panel="${ronda.izquierda.prefijo}">
                  <div class="player-heading">
                  <h3 class="player-title"><c:out value="${ronda.izquierda.nombre}" /></h3>
                  <span class="round-score"><strong data-round-score="${ronda.izquierda.prefijo}">0 VP</strong><small>esta ronda</small></span>
                  </div>
                  <div class="tags">
                    <span class="tag"><c:out value="${ronda.izquierda.faccion}" /></span>
                    <c:if test="${ronda.izquierda.primero}"><span class="tag">Va primero</span></c:if>
                    <c:if test="${ronda.izquierda.defensor}"><span class="tag">Defensor</span></c:if>
                  </div>


                  <c:choose>
                    <c:when test="${ronda.partida.mostrarCommandPoints}">
                      <fieldset class="cp-counter" data-cp-inicio="${ronda.izquierda.cpInicio}">
                        <legend>CP · Inicio: ${ronda.izquierda.cpInicio}</legend>
                        <div class="cp-balance"><span>Disponibles</span><output class="cp-total" aria-live="polite">${ronda.izquierda.cpFin}</output></div>
                        <div class="cp-row">
                          <label>Gastados <input class="cp-spent" type="number" min="0" step="1" name="${ronda.izquierda.prefijo}CpGastados" value="${ronda.izquierda.cpGastados}" /></label>
                          <label>Ganados <input class="cp-earned" type="number" min="0" step="1" name="${ronda.izquierda.prefijo}CpGanados" value="${ronda.izquierda.cpGanados}" /></label>
                        </div>
                      </fieldset>
                    </c:when>
                    <c:otherwise>
                      <input type="hidden" name="${ronda.izquierda.prefijo}CpInicio" value="0" />
                      <input type="hidden" name="${ronda.izquierda.prefijo}CpFin" value="0" />
                    </c:otherwise>
                  </c:choose>

                  <section class="mission-card primary-card">
                    <div><h4>Misión principal</h4><button type="button" class="round-config-link" data-round-info="misionPrincipalInfo" data-info-title="Misión principal" aria-haspopup="dialog"><c:out value="${ronda.misionPrincipal.titulo}" /></button></div>
                    <label>VP <input class="score-input" type="number" min="0" step="1" aria-label="Puntos de misión principal" name="${ronda.izquierda.prefijo}Primaria" value="${ronda.izquierda.primaria}" /></label>
                  </section>

                  <section class="mission-card">
                    <div class="secondary-heading"><h4>Secundarias <span class="secondary-count"></span></h4>
                    <button class="button-secondary abrir-secundarias" type="button" data-prefijo="${ronda.izquierda.prefijo}">Elegir</button></div>
                    <input type="hidden" class="secondary-total" name="${ronda.izquierda.prefijo}Secundaria" value="${ronda.izquierda.secundaria}" />
                    <textarea class="secondary-detail" name="${ronda.izquierda.prefijo}Detalle" hidden><c:out value="${ronda.izquierda.detalle}" /></textarea>
                    <div class="secondary-list" id="${ronda.izquierda.prefijo}-secundarias"></div>
                    <p class="secondary-summary">Secundarias: <strong class="secondary-total-text">${ronda.izquierda.secundaria}</strong> VP</p>
                  </section>
                </article>

                <div class="separator"></div>

                <article class="player-panel" id="panel-${ronda.derecha.prefijo}" data-player-panel="${ronda.derecha.prefijo}">
                  <div class="player-heading">
                  <h3 class="player-title"><c:out value="${ronda.derecha.nombre}" /></h3>
                  <span class="round-score"><strong data-round-score="${ronda.derecha.prefijo}">0 VP</strong><small>esta ronda</small></span>
                  </div>
                  <div class="tags">
                    <span class="tag"><c:out value="${ronda.derecha.faccion}" /></span>
                    <c:if test="${ronda.derecha.primero}"><span class="tag">Va primero</span></c:if>
                    <c:if test="${ronda.derecha.defensor}"><span class="tag">Defensor</span></c:if>
                  </div>


                  <c:choose>
                    <c:when test="${ronda.partida.mostrarCommandPoints}">
                      <fieldset class="cp-counter" data-cp-inicio="${ronda.derecha.cpInicio}">
                        <legend>CP · Inicio: ${ronda.derecha.cpInicio}</legend>
                        <div class="cp-balance"><span>Disponibles</span><output class="cp-total" aria-live="polite">${ronda.derecha.cpFin}</output></div>
                        <div class="cp-row">
                          <label>Gastados <input class="cp-spent" type="number" min="0" step="1" name="${ronda.derecha.prefijo}CpGastados" value="${ronda.derecha.cpGastados}" /></label>
                          <label>Ganados <input class="cp-earned" type="number" min="0" step="1" name="${ronda.derecha.prefijo}CpGanados" value="${ronda.derecha.cpGanados}" /></label>
                        </div>
                      </fieldset>
                    </c:when>
                    <c:otherwise>
                      <input type="hidden" name="${ronda.derecha.prefijo}CpInicio" value="0" />
                      <input type="hidden" name="${ronda.derecha.prefijo}CpFin" value="0" />
                    </c:otherwise>
                  </c:choose>

                  <section class="mission-card primary-card">
                    <div><h4>Misión principal</h4><button type="button" class="round-config-link" data-round-info="misionPrincipalInfo" data-info-title="Misión principal" aria-haspopup="dialog"><c:out value="${ronda.misionPrincipal.titulo}" /></button></div>
                    <label>VP <input class="score-input" type="number" min="0" step="1" aria-label="Puntos de misión principal" name="${ronda.derecha.prefijo}Primaria" value="${ronda.derecha.primaria}" /></label>
                  </section>

                  <section class="mission-card">
                    <div class="secondary-heading"><h4>Secundarias <span class="secondary-count"></span></h4>
                    <button class="button-secondary abrir-secundarias" type="button" data-prefijo="${ronda.derecha.prefijo}">Elegir</button></div>
                    <input type="hidden" class="secondary-total" name="${ronda.derecha.prefijo}Secundaria" value="${ronda.derecha.secundaria}" />
                    <textarea class="secondary-detail" name="${ronda.derecha.prefijo}Detalle" hidden><c:out value="${ronda.derecha.detalle}" /></textarea>
                    <div class="secondary-list" id="${ronda.derecha.prefijo}-secundarias"></div>
                    <p class="secondary-summary">Secundarias: <strong class="secondary-total-text">${ronda.derecha.secundaria}</strong> VP</p>
                  </section>
                </article>
              </div>
            </form>
          </section>
        </main>
      </div>
    </div>

    <template id="misionPrincipalInfo">
      <h4><c:out value="${ronda.misionPrincipal.titulo}" /></h4>
      <p><c:out value="${ronda.misionPrincipal.descripcion}" /></p>
      <p><strong>Puntuación:</strong> <c:out value="${ronda.misionPrincipal.puntuacion}" /></p>
      <ul><c:forEach items="${ronda.misionPrincipal.condiciones}" var="condicion"><li><c:out value="${condicion}" /></li></c:forEach></ul>
    </template>
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
          <div class="mission-option">
            <input type="checkbox" value="<c:out value='${mision.id}' />" aria-label="Seleccionar <c:out value='${mision.titulo}' />" />
            <button type="button" class="mission-title" aria-haspopup="dialog"><c:out value="${mision.titulo}" /></button>
            <template class="mission-info">
              <p class="mission-description"><c:out value="${mision.descripcion}" /></p>
              <p><strong>Puntuación:</strong> <span class="mission-score"><c:out value="${mision.puntuacion}" /></span></p>
              <ul class="mission-conditions">
                <c:forEach items="${mision.condiciones}" var="condicion"><li><c:out value="${condicion}" /></li></c:forEach>
              </ul>
            </template>
          </div>
        </c:forEach>
      </div>
    </dialog>

    <dialog class="popup" id="popupDetalleMision" aria-labelledby="tituloDetalleMision">
      <div class="popup-header">
        <h3 id="tituloDetalleMision"></h3>
        <button class="button-secondary" type="button" id="cerrarDetalleMision" autofocus>Cerrar</button>
      </div>
      <div class="mission-detail-body">
        <div id="contenidoDetalleMision"></div>
        <div id="puntuacionDetalleMision" hidden>
          <label><input type="checkbox" id="misionCumplida" /> Cumplida</label>
          <label>Puntos <input type="number" id="misionPuntos" min="0" step="1" /></label>
        </div>
      </div>
    </dialog>

    <script>
      (function () {
        let prefijoActivo = "";
        const popup = document.getElementById("popupSecundarias");
        const botones = document.querySelectorAll(".abrir-secundarias");
        const cerrar = document.getElementById("cerrarSecundarias");
        const random = document.getElementById("randomSecundaria");
        const detallePopup = document.getElementById("popupDetalleMision");
        const detalleContenido = document.getElementById("contenidoDetalleMision");
        const detallePuntos = document.getElementById("misionPuntos");
        const detalleCumplida = document.getElementById("misionCumplida");
        let guardarPuntuacion = null;

        document.body.classList.add('round-interactive');
        function mostrarJugador(prefijo) {
          document.querySelectorAll('[data-player-panel]').forEach(function (p) {
            p.classList.toggle('is-current', p.dataset.playerPanel === prefijo);
          });
          document.querySelectorAll('[data-show-player]').forEach(function (boton) {
            boton.setAttribute('aria-pressed', String(boton.dataset.showPlayer === prefijo));
          });
        }
        document.querySelectorAll('[data-show-player]').forEach(function (boton) {
          boton.addEventListener('click', function () { mostrarJugador(boton.dataset.showPlayer); });
        });
        // Si hay un campo inválido en el otro jugador, se muestra antes de que el navegador lo enfoque.
        document.querySelector('.round-board').addEventListener('invalid', function (event) {
          const jugador = event.target.closest('[data-player-panel]');
          if (jugador) mostrarJugador(jugador.dataset.playerPanel);
        }, true);
        document.querySelectorAll('[data-round-info]').forEach(function (boton) {
          boton.addEventListener('click', function () {
            document.getElementById('tituloDetalleMision').textContent = boton.dataset.infoTitle;
            detalleContenido.replaceChildren(document.getElementById(boton.dataset.roundInfo).content.cloneNode(true));
            document.getElementById('puntuacionDetalleMision').hidden = true;
            guardarPuntuacion = null;
            detallePopup.showModal();
          });
        });

        function actualizarMarcador(prefijo) {
          const jugador = panel(prefijo);
          const total = Number(jugador.querySelector('.score-input').value || 0)
            + Number(jugador.querySelector('.secondary-total').value || 0);
          document.querySelectorAll('[data-round-score="' + prefijo + '"]').forEach(function (marcador) {
            marcador.textContent = total + ' VP';
          });
        }

        function actualizarEstadoTarjeta(tarjeta, mision) {
          tarjeta.classList.toggle('is-complete', !!mision.cumplida);
          tarjeta.querySelector('.secondary-state').textContent = mision.cumplida
            ? '✓ ' + (mision.puntos || 0) + ' VP' : 'Pendiente';
        }

        function abrirDetalle(mision, prefijo, misiones) {
          document.getElementById("tituloDetalleMision").textContent = mision.titulo;
          detalleContenido.replaceChildren();
          const fila = Array.from(popup.querySelectorAll('.mission-option')).find(function (opcion) {
            return opcion.querySelector('input').value === mision.id;
          });
          if (fila) {
            detalleContenido.appendChild(fila.querySelector('.mission-info').content.cloneNode(true));
          } else {
            const descripcion = document.createElement('p');
            descripcion.textContent = mision.descripcion || 'No hay información adicional disponible.';
            detalleContenido.appendChild(descripcion);
          }
          document.getElementById('puntuacionDetalleMision').hidden = !prefijo;
          guardarPuntuacion = null;
          if (prefijo) {
            detalleCumplida.checked = !!mision.cumplida;
            detallePuntos.value = mision.puntos || 0;
            guardarPuntuacion = function () {
              if (!detallePuntos.validity.valid) return;
              mision.cumplida = detalleCumplida.checked;
              mision.puntos = Number(detallePuntos.value || 0);
              actualizarDetalle(prefijo, misiones);
            };
          }
          detallePopup.showModal();
        }

        detallePuntos.addEventListener('input', function () { if (guardarPuntuacion) guardarPuntuacion(); });
        detalleCumplida.addEventListener('change', function () { if (guardarPuntuacion) guardarPuntuacion(); });
        document.getElementById('cerrarDetalleMision').addEventListener('click', function () { detallePopup.close(); });
        detallePopup.addEventListener('keydown', function (event) {
          if (event.key === 'Escape') {
            event.preventDefault();
            event.stopPropagation();
            detallePopup.close();
          }
        });
        detallePopup.addEventListener('click', function (event) {
          const rect = detallePopup.getBoundingClientRect();
          if (event.target === detallePopup && (event.clientX < rect.left || event.clientX > rect.right
              || event.clientY < rect.top || event.clientY > rect.bottom)) detallePopup.close();
        });
        popup.querySelectorAll('.mission-title').forEach(function (boton) {
          boton.addEventListener('click', function () { abrirDetalle(misionDesdeFila(boton.closest('.mission-option'))); });
        });

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
          actualizarMarcador(prefijo);
          contenedor.querySelectorAll('.secondary-card').forEach(function (tarjeta, indice) {
            if (misiones[indice]) actualizarEstadoTarjeta(tarjeta, misiones[indice]);
          });
        }

        function guardarDetalle(prefijo, misiones) {
          actualizarDetalle(prefijo, misiones);
          pintarSecundarias(prefijo);
        }

        function pintarSecundarias(prefijo) {
          const contenedor = panel(prefijo);
          const lista = contenedor.querySelector(".secondary-list");
          const misiones = leerDetalle(prefijo);
          contenedor.querySelector('.secondary-count').textContent = '(' + misiones.length + ')';
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
              "<button type='button' class='mission-title' aria-haspopup='dialog'><span class='secondary-name'></span><span class='secondary-state'></span></button>";

            tarjeta.querySelector('.secondary-name').textContent = mision.titulo;
            actualizarEstadoTarjeta(tarjeta, mision);
            tarjeta.querySelector("button").addEventListener("click", function () {
              abrirDetalle(mision, prefijo, misiones);
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
            descripcion: fila.querySelector(".mission-info").content.querySelector(".mission-description").textContent,
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
          actualizarMarcador(p.dataset.playerPanel);
          p.querySelector('.score-input').addEventListener('input', function () { actualizarMarcador(p.dataset.playerPanel); });
        });
      })();
    </script>
  </body>
</html>
