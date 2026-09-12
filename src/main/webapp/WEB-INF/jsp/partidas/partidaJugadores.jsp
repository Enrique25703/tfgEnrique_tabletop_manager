<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Jugadores</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .steps {
        display: flex;
        gap: 10px;
        margin-bottom: 20px;
      }

      .step {
        padding: 8px 12px;
        border: 1px solid var(--line);
        border-radius: 999px;
        color: var(--muted);
      }

      .step.active {
        color: #fff;
        border-color: var(--line-strong);
        background: rgba(48, 77, 121, 0.6);
      }

      .players-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 18px;
      }

      .player-card {
        padding: 20px;
        border: 1px solid var(--line);
        border-radius: 20px;
        background: var(--panel-soft);
      }

      .form-grid {
        display: grid;
        gap: 14px;
        margin-top: 16px;
      }

      .hidden-block {
        display: none;
      }

      @media (max-width: 900px) {
        .players-grid {
          grid-template-columns: 1fr;
        }
      }
    </style>
    <link rel="stylesheet" href="/css/partida-creacion.css" />
    <script src="/js/partida-creacion.js" defer></script>
  </head>
  <body class="setup-page setup-players">
    <c:set var="sidebarActive" value="partidas" scope="request" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <c:set var="partida" value="${jugadores.partida}" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Jugadores y listas</h2>
              <p class="page-subtitle">Indica los datos basicos de la partida. Las listas son opcionales.</p>
            </div>

            <div class="steps">
              <span class="step active">1 Jugadores</span>
              <span class="step">2 Configuración</span>
              <span class="step">3 Rondas</span>
              <span class="step">4 Final</span>
            </div>

            <c:if test="${not empty mensajeError}">
              <div class="error-box"><c:out value="${mensajeError}" /></div>
            </c:if>

            <form method="post" action="/partidas/${partida.id}/jugadores">
              <select id="plantillaEjercitos" style="display:none;">
                <c:forEach var="faccion" items="${jugadores.facciones}">
                  <c:forEach var="ejercito" items="${faccion.ejercitos}">
                    <option data-faccion="${faccion.nombre}" value="${ejercito}">
                      <c:out value="${ejercito}" />
                    </option>
                  </c:forEach>
                </c:forEach>
              </select>

              <div class="setup-player-switch" aria-label="Jugador que estás configurando">
                <button type="button" data-setup-show="1" aria-controls="setup-player-1" aria-pressed="true">Tu jugador</button>
                <button type="button" data-setup-show="2" aria-controls="setup-player-2" aria-pressed="false">Rival</button>
              </div>
              <div class="players-grid">
                <article class="player-card is-current" id="setup-player-1" data-setup-player="1">
                  <h3>Tu jugador</h3>
                  <div class="form-grid">
                    <label>
                      Nombre
                      <input type="text" name="jugador1Nombre" value="${partida.jugador1NombreSnapshot}" required />
                    </label>
                    <label>
                      Faccion
                      <select id="jugador1Faccion" name="jugador1Faccion" required>
                        <option value="">Selecciona faccion</option>
                        <c:forEach var="faccion" items="${jugadores.facciones}">
                          <option value="${faccion.nombre}"><c:out value="${faccion.nombre}" /></option>
                        </c:forEach>
                      </select>
                    </label>
                    <label>
                      Ejercito
                      <select id="jugador1Ejercito" name="jugador1Ejercito" data-guardado="${partida.jugador1FaccionSnapshot}" required>
                        <option value="">Selecciona ejercito</option>
                      </select>
                    </label>
                    <label>
                      Mi lista
                      <select id="jugador1ListaTipo" name="jugador1ListaTipo">
                        <option value="NONE">Sin lista</option>
                        <c:forEach var="lista" items="${jugadores.listasUsuario}">
                          <option
                            value="${lista.listaId}"
                            data-faccion="${lista.faccion}"
                            data-ejercito="${lista.ejercito}">
                            <c:out value="${lista.nombreLista}" /> - <c:out value="${lista.puntosActuales}" />/<c:out value="${lista.limitePuntos}" /> pts
                          </option>
                        </c:forEach>
                        <c:choose>
                          <c:when test="${not empty partida.jugador1NombreListaSnapshot}">
                            <option value="CUSTOM" selected>CUSTOM</option>
                          </c:when>
                          <c:otherwise>
                            <option value="CUSTOM">CUSTOM</option>
                          </c:otherwise>
                        </c:choose>
                      </select>
                    </label>
                    <label id="bloqueJugador1Custom">
                      Lista CUSTOM
                      <textarea name="jugador1ListaCustom" rows="3"><c:out value="${partida.jugador1NombreListaSnapshot}" /></textarea>
                    </label>
                  </div>
                </article>

                <article class="player-card" id="setup-player-2" data-setup-player="2">
                  <h3>Rival</h3>
                  <div class="form-grid">
                    <label>
                      Nombre
                      <input type="text" name="jugador2Nombre" value="${partida.jugador2NombreSnapshot}" required />
                    </label>
                    <label>
                      Faccion
                      <select id="jugador2Faccion" name="jugador2Faccion" required>
                        <option value="">Selecciona faccion</option>
                        <c:forEach var="faccion" items="${jugadores.facciones}">
                          <option value="${faccion.nombre}"><c:out value="${faccion.nombre}" /></option>
                        </c:forEach>
                      </select>
                    </label>
                    <label>
                      Ejercito
                      <select id="jugador2Ejercito" name="jugador2Ejercito" data-guardado="${partida.jugador2FaccionSnapshot}" required>
                        <option value="">Selecciona ejercito</option>
                      </select>
                    </label>
                    <label>
                      Lista del rival
                      <textarea name="jugador2ListaCustom" rows="3"><c:out value="${partida.jugador2NombreListaSnapshot}" /></textarea>
                    </label>
                  </div>
                </article>
              </div>

              <div class="form-actions">
                <a class="button-secondary" href="/partidas/nueva">Atras</a>
                <button class="button-primary" type="submit">Confirmar jugadores</button>
              </div>
            </form>
          </section>
        </main>
      </div>
    </div>

    <script>
      (function () {
        const plantilla = Array.from(document.querySelectorAll("#plantillaEjercitos option"));

        function separarGuardado(texto) {
          const valor = texto || "";
          const partes = valor.split(" - ");
          return {
            faccion: partes[0] || "",
            ejercito: partes.length > 1 ? partes.slice(1).join(" - ") : ""
          };
        }

        function prepararSelector(faccionId, ejercitoId) {
          const faccion = document.getElementById(faccionId);
          const ejercito = document.getElementById(ejercitoId);
          let guardado = separarGuardado(ejercito.dataset.guardado);

          if (guardado.faccion) {
            faccion.value = guardado.faccion;
          }

          function pintar() {
            guardado = separarGuardado(ejercito.dataset.guardado);
            const valorFaccion = faccion.value;
            ejercito.innerHTML = "<option value=''>Selecciona ejercito</option>";
            plantilla.forEach(function (opcion) {
              if (opcion.dataset.faccion === valorFaccion) {
                ejercito.appendChild(opcion.cloneNode(true));
              }
            });
            if (guardado.ejercito) {
              ejercito.value = guardado.ejercito;
            }
          }

          faccion.addEventListener("change", function () {
            ejercito.dataset.guardado = "";
            guardado = separarGuardado("");
            pintar();
          });
          pintar();
        }

        prepararSelector("jugador1Faccion", "jugador1Ejercito");
        prepararSelector("jugador2Faccion", "jugador2Ejercito");

        const listaUsuario = document.getElementById("jugador1ListaTipo");
        const bloqueCustom = document.getElementById("bloqueJugador1Custom");

        function pintarCustom() {
          if (listaUsuario.value === "CUSTOM") {
            bloqueCustom.classList.remove("hidden-block");
          } else {
            bloqueCustom.classList.add("hidden-block");
          }
        }

        listaUsuario.addEventListener("change", function () {
          const opcion = listaUsuario.options[listaUsuario.selectedIndex];
          if (opcion.dataset.faccion) {
            document.getElementById("jugador1Faccion").value = opcion.dataset.faccion;
            document.getElementById("jugador1Faccion").dispatchEvent(new Event("change"));
            document.getElementById("jugador1Ejercito").value = opcion.dataset.ejercito || "";
          }
          pintarCustom();
        });

        pintarCustom();
      })();
    </script>
  </body>
</html>
