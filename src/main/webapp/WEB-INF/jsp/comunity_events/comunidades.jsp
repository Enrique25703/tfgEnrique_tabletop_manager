<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Comunidades</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <link
      rel="stylesheet"
      href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
      crossorigin=""
    />
    <style>
      .communities-grid {
        display: grid;
        grid-template-columns: 360px minmax(0, 1fr);
        gap: 20px;
      }

      .stack-panel {
        display: grid;
        gap: 18px;
      }

      .section-card,
      .community-card,
      .event-card,
      .invite-card {
        padding: 18px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: var(--panel-soft);
      }

      .community-card.active {
        border-color: var(--line-strong);
        background: rgba(28, 45, 72, 0.72);
      }

      .card-title-small {
        margin: 0 0 8px;
        font-size: 1.15rem;
      }

      .card-subtitle {
        margin: 0;
        color: var(--muted);
        font-size: 0.92rem;
      }

      .linea-formulario {
        margin-top: 14px;
      }

      .badge-row {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
        margin-top: 12px;
      }

      .badge {
        padding: 8px 11px;
        border: 1px solid var(--line);
        border-radius: 999px;
        background: rgba(14, 22, 34, 0.8);
        color: var(--muted);
        font-size: 0.9rem;
      }

      .detail-grid {
        display: grid;
        gap: 18px;
      }

      .members-table {
        margin-top: 12px;
      }

      .event-grid,
      .invite-grid {
        display: grid;
        gap: 14px;
        margin-top: 16px;
      }

      .event-meta,
      .invite-meta {
        margin: 10px 0 0;
        color: var(--muted);
        line-height: 1.5;
      }

      .empty-box {
        padding: 18px;
        border: 1px dashed var(--line);
        border-radius: 16px;
        color: var(--muted);
        background: rgba(12, 18, 28, 0.6);
      }

      .section-actions,
      .modal-actions,
      .map-search-row,
      .location-selection-row {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
        align-items: center;
      }

      .modal-shell {
        display: none;
        position: fixed;
        inset: 0;
        z-index: 40;
        align-items: center;
        justify-content: center;
        padding: 18px;
        background: rgba(5, 9, 15, 0.7);
      }

      .modal-shell.visible {
        display: flex;
      }

      .modal-card {
        width: min(100%, 760px);
        max-height: min(88vh, 860px);
        overflow: auto;
        padding: 18px;
        border: 1px solid var(--line);
        border-radius: 20px;
        background: var(--panel-soft);
        box-shadow: var(--shadow);
      }

      .modal-card form {
        display: grid;
        gap: 14px;
      }

      .location-picker-grid {
        display: grid;
        gap: 12px;
      }

      .map-search-row input {
        flex: 1 1 240px;
      }

      .map-canvas {
        min-height: 320px;
        border: 1px solid var(--line);
        border-radius: 18px;
        overflow: hidden;
      }

      .location-selection {
        padding: 12px 14px;
        border: 1px solid var(--line);
        border-radius: 16px;
        background: rgba(14, 22, 34, 0.7);
      }

      .location-selection p {
        margin: 0;
      }

      .location-selection small {
        color: var(--muted);
      }

      @media (max-width: 1080px) {
        .communities-grid {
          grid-template-columns: 1fr;
        }
      }
    </style>
  </head>
  <body>
    <c:set var="sidebarActive" value="comunidades" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${comunidades.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Comunidades</h2>
              <p class="page-subtitle">Crea una comunidad, unete a otras y gestiona eventos o invitaciones segun tu rol.</p>
            </div>

            <c:if test="${not empty mensajeOk}">
              <div class="note-box"><c:out value="${mensajeOk}" /></div>
            </c:if>

            <c:if test="${not empty mensajeError}">
              <div class="error-box"><c:out value="${mensajeError}" /></div>
            </c:if>

            <div class="communities-grid">
              <div class="stack-panel">
                <div class="section-card">
                  <h3 class="card-title-small">Crear comunidad</h3>
                  <p class="card-subtitle">Al crearla pasaras a ser su propietario.</p>

                  <form action="/comunidades/crear" method="post">
                    <div class="linea-formulario">
                      <label for="nombreComunidad">Nombre</label>
                      <input id="nombreComunidad" name="nombreComunidad" type="text" required />
                    </div>

                    <div class="linea-formulario">
                      <button class="button-primary" type="submit">Crear comunidad</button>
                    </div>
                  </form>
                </div>

                <div class="section-card">
                  <h3 class="card-title-small">Mis comunidades</h3>

                  <c:choose>
                    <c:when test="${empty comunidades.misComunidades}">
                      <div class="empty-box">Todavia no formas parte de ninguna comunidad.</div>
                    </c:when>
                    <c:otherwise>
                      <div class="stack-panel">
                        <c:forEach var="comunidad" items="${comunidades.misComunidades}">
                          <a
                            class="community-card ${comunidades.comunidadSeleccionada != null and comunidades.comunidadSeleccionada.id == comunidad.id ? 'active' : ''}"
                            href="/comunidades?comunidadId=${comunidad.id}"
                            style="display:block; text-decoration:none;">
                            <h4 class="card-title-small"><c:out value="${comunidad.nombre}" /></h4>
                            <p class="card-subtitle">Rol: <c:out value="${comunidad.rolUsuario}" /></p>
                            <div class="badge-row">
                              <span class="badge"><c:out value="${comunidad.totalMiembros}" /> miembros</span>
                              <span class="badge"><c:out value="${comunidad.totalEventos}" /> eventos</span>
                            </div>
                          </a>
                        </c:forEach>
                      </div>
                    </c:otherwise>
                  </c:choose>
                </div>

                <div class="section-card">
                  <h3 class="card-title-small">Comunidades disponibles</h3>

                  <c:choose>
                    <c:when test="${empty comunidades.comunidadesDisponibles}">
                      <div class="empty-box">No hay mas comunidades disponibles ahora mismo.</div>
                    </c:when>
                    <c:otherwise>
                      <div class="stack-panel">
                        <c:forEach var="comunidad" items="${comunidades.comunidadesDisponibles}">
                          <div class="community-card">
                            <h4 class="card-title-small"><c:out value="${comunidad.nombre}" /></h4>
                            <div class="badge-row">
                              <span class="badge"><c:out value="${comunidad.totalMiembros}" /> miembros</span>
                              <span class="badge"><c:out value="${comunidad.totalEventos}" /> eventos</span>
                            </div>

                            <form action="/comunidades/unirse" method="post" class="linea-formulario">
                              <input type="hidden" name="comunidadId" value="${comunidad.id}" />
                              <button class="button-secondary" type="submit">Unirme</button>
                            </form>
                          </div>
                        </c:forEach>
                      </div>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>

              <div class="detail-grid">
                <c:choose>
                  <c:when test="${comunidades.comunidadSeleccionada == null}">
                    <div class="empty-box">Selecciona una comunidad o crea una nueva para empezar.</div>
                  </c:when>
                  <c:otherwise>
                    <div class="section-card">
                      <h3 class="card-title-small"><c:out value="${comunidades.comunidadSeleccionada.nombre}" /></h3>
                      <p class="card-subtitle">Tu rol en esta comunidad es <c:out value="${comunidades.comunidadSeleccionada.rolUsuario}" />.</p>

                      <div class="badge-row">
                        <span class="badge"><c:out value="${comunidades.comunidadSeleccionada.miembros.size()}" /> miembros activos</span>
                        <span class="badge"><c:out value="${comunidades.comunidadSeleccionada.eventos.size()}" /> eventos</span>
                        <span class="badge"><c:out value="${comunidades.comunidadSeleccionada.invitaciones.size()}" /> invitaciones</span>
                      </div>
                    </div>

                    <div class="section-card">
                      <h3 class="card-title-small">Miembros</h3>
                      <table class="data-table members-table">
                        <thead>
                          <tr>
                            <th>Usuario</th>
                            <th>Rol</th>
                          </tr>
                        </thead>
                        <tbody>
                          <c:forEach var="miembro" items="${comunidades.comunidadSeleccionada.miembros}">
                            <tr>
                              <td><c:out value="${miembro.nombreUsuario}" /></td>
                              <td><c:out value="${miembro.rol}" /></td>
                            </tr>
                          </c:forEach>
                        </tbody>
                      </table>
                    </div>

                    <c:if test="${comunidades.comunidadSeleccionada.propietario}">
                      <div class="section-card">
                        <h3 class="card-title-small">Crear evento</h3>
                        <p class="card-subtitle">Solo los propietarios pueden crear eventos para la comunidad.</p>
                        <div class="section-actions linea-formulario">
                          <button type="button" class="button-primary" id="abrirModalCrearEvento">Abrir formulario</button>
                        </div>
                      </div>
                    </c:if>

                    <c:if test="${comunidades.comunidadSeleccionada.usuarioNormal}">
                      <div class="section-card">
                        <h3 class="card-title-small">Crear invitacion de partida</h3>
                        <p class="card-subtitle">Los usuarios normales pueden dejar propuestas de partida dentro de la comunidad.</p>

                        <form action="/comunidades/invitaciones/crear" method="post">
                          <input type="hidden" name="comunidadId" value="${comunidades.comunidadSeleccionada.id}" />

                          <div class="linea-formulario">
                            <label for="fechaInvitacion">Fecha</label>
                            <input id="fechaInvitacion" name="fecha" type="datetime-local" required />
                          </div>

                          <div class="linea-formulario">
                            <label for="lugarInvitacion">Lugar</label>
                            <input id="lugarInvitacion" name="lugar" type="text" required />
                          </div>

                          <div class="linea-formulario">
                            <label for="formatoInvitacion">Formato de juego</label>
                            <select id="formatoInvitacion" name="formatoJuego" required>
                              <option value="">Selecciona un formato</option>
                              <option value="WH40K_11">Warhammer 40.000 11a edicion</option>
                              <option value="AOS_4">Age of Sigmar 4a edicion</option>
                            </select>
                          </div>

                          <div class="linea-formulario">
                            <label for="mensajeInvitacion">Mensaje</label>
                            <textarea id="mensajeInvitacion" name="mensaje" rows="4"></textarea>
                          </div>

                          <div class="linea-formulario">
                            <button class="button-primary" type="submit">Publicar invitacion</button>
                          </div>
                        </form>
                      </div>
                    </c:if>

                    <div class="section-card">
                      <h3 class="card-title-small">Eventos de la comunidad</h3>

                      <c:choose>
                        <c:when test="${empty comunidades.comunidadSeleccionada.eventos}">
                          <div class="empty-box">Todavia no hay eventos creados para esta comunidad.</div>
                        </c:when>
                        <c:otherwise>
                          <div class="event-grid">
                            <c:forEach var="evento" items="${comunidades.comunidadSeleccionada.eventos}">
                              <div class="event-card">
                                <h4 class="card-title-small"><c:out value="${evento.titulo}" /></h4>
                                <p class="event-meta">
                                  Formato: <c:out value="${evento.formato}" /><br />
                                  Fecha: <c:out value="${evento.fecha}" /><br />
                                  Rondas: <c:out value="${evento.rondas}" /><br />
                                  Lugar: <c:out value="${evento.lugar}" /><br />
                                  Organiza: <c:out value="${evento.organizador}" /><br />
                                  Inscritos: <c:out value="${evento.inscritos}" />
                                </p>

                                <c:choose>
                                  <c:when test="${evento.unido}">
                                    <div class="badge-row">
                                      <span class="badge">Ya estas inscrito</span>
                                    </div>
                                  </c:when>
                                  <c:when test="${evento.puedeUnirse}">
                                    <form action="/comunidades/eventos/unirse" method="post" class="linea-formulario">
                                      <input type="hidden" name="eventoId" value="${evento.id}" />
                                      <button class="button-secondary" type="submit">Unirme al evento</button>
                                    </form>
                                  </c:when>
                                </c:choose>
                              </div>
                            </c:forEach>
                          </div>
                        </c:otherwise>
                      </c:choose>
                    </div>

                    <div class="section-card">
                      <h3 class="card-title-small">Invitaciones a partidas</h3>

                      <c:choose>
                        <c:when test="${empty comunidades.comunidadSeleccionada.invitaciones}">
                          <div class="empty-box">Todavia no hay invitaciones de partida publicadas.</div>
                        </c:when>
                        <c:otherwise>
                          <div class="invite-grid">
                            <c:forEach var="invitacion" items="${comunidades.comunidadSeleccionada.invitaciones}">
                              <div class="invite-card">
                                <h4 class="card-title-small">Invitacion de <c:out value="${invitacion.creador}" /></h4>
                                <p class="invite-meta">
                                  Formato: <c:out value="${invitacion.formato}" /><br />
                                  Fecha: <c:out value="${invitacion.fecha}" /><br />
                                  Lugar: <c:out value="${invitacion.lugar}" /><br />
                                  Mensaje: <c:out value="${invitacion.mensaje}" />
                                </p>
                              </div>
                            </c:forEach>
                          </div>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
          </section>
        </main>
      </div>
    </div>

    <c:if test="${comunidades.comunidadSeleccionada != null and comunidades.comunidadSeleccionada.propietario}">
      <div id="modalCrearEvento" class="modal-shell">
        <div class="modal-card">
          <h3 class="page-title" style="font-size:1.4rem; margin-bottom:6px;">Crear evento</h3>
          <p class="page-subtitle">Selecciona la ubicacion del evento con OpenStreetMap y se guardaran sus coordenadas.</p>

          <form action="/comunidades/eventos/crear" method="post" id="formCrearEvento">
            <input type="hidden" name="comunidadId" value="${comunidades.comunidadSeleccionada.id}" />
            <input type="hidden" id="latitudEvento" name="latitud" />
            <input type="hidden" id="longitudEvento" name="longitud" />

            <div class="linea-formulario">
              <label for="fechaEvento">Fecha</label>
              <input id="fechaEvento" name="fecha" type="datetime-local" required />
            </div>

            <div class="linea-formulario">
              <label for="numeroRondas">Numero de rondas</label>
              <input id="numeroRondas" name="numeroRondas" type="number" min="1" required />
            </div>

            <div class="linea-formulario">
              <label for="formatoEvento">Formato de juego</label>
              <select id="formatoEvento" name="formatoJuego" required>
                <option value="">Selecciona un formato</option>
                <option value="WH40K_11">Warhammer 40.000 11a edicion</option>
                <option value="AOS_4">Age of Sigmar 4a edicion</option>
              </select>
            </div>

            <div class="location-picker-grid">
              <div>
                <label for="busquedaLugarEvento">Buscar lugar</label>
                <div class="map-search-row">
                  <input id="busquedaLugarEvento" type="text" placeholder="Introduce una direccion o lugar" />
                  <button type="button" class="button-secondary" id="buscarLugarEvento">Buscar en mapa</button>
                </div>
              </div>

              <div class="location-selection">
                <div class="location-selection-row">
                  <div style="flex:1 1 280px;">
                    <label for="lugarEvento">Lugar seleccionado</label>
                    <input id="lugarEvento" name="lugar" type="text" readonly required />
                  </div>
                  <small id="coordenadasEventoTexto">Selecciona un punto en el mapa o busca una direccion.</small>
                </div>
              </div>

              <div id="mapaEvento" class="map-canvas" aria-label="Mapa OpenStreetMap para seleccionar la ubicacion del evento"></div>
            </div>

            <div class="modal-actions">
              <button class="button-primary" type="submit">Crear evento</button>
              <button class="button-secondary" type="button" id="cerrarModalCrearEvento">Cancelar</button>
            </div>
          </form>
        </div>
      </div>
    </c:if>

    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
    <script>
      (() => {
        const modal = document.getElementById("modalCrearEvento");
        const abrirModal = document.getElementById("abrirModalCrearEvento");
        const cerrarModal = document.getElementById("cerrarModalCrearEvento");
        const formCrearEvento = document.getElementById("formCrearEvento");

        if (!modal || !abrirModal || !cerrarModal || !formCrearEvento || typeof L === "undefined") {
          return;
        }

        const inputLugar = document.getElementById("lugarEvento");
        const inputLatitud = document.getElementById("latitudEvento");
        const inputLongitud = document.getElementById("longitudEvento");
        const inputBusqueda = document.getElementById("busquedaLugarEvento");
        const botonBuscar = document.getElementById("buscarLugarEvento");
        const textoCoordenadas = document.getElementById("coordenadasEventoTexto");

        let mapa = null;
        let marcador = null;

        const abrir = () => {
          modal.classList.add("visible");
          document.body.style.overflow = "hidden";
          if (!mapa) {
            inicializarMapa();
          }
          window.setTimeout(() => {
            mapa.invalidateSize();
          }, 50);
        };

        const cerrar = () => {
          modal.classList.remove("visible");
          document.body.style.overflow = "";
        };

        const actualizarUbicacion = (lat, lon, etiqueta) => {
          inputLatitud.value = lat.toFixed(7);
          inputLongitud.value = lon.toFixed(7);
          inputLugar.value = etiqueta;
          textoCoordenadas.textContent = "Lat: " + lat.toFixed(6) + " | Lon: " + lon.toFixed(6);

          if (!marcador) {
            marcador = L.marker([lat, lon]).addTo(mapa);
          } else {
            marcador.setLatLng([lat, lon]);
          }

          mapa.setView([lat, lon], 15);
        };

        const obtenerJson = async (url) => {
          const response = await fetch(url, {
            headers: {
              "Accept": "application/json"
            }
          });

          if (!response.ok) {
            throw new Error("No se pudo consultar OpenStreetMap.");
          }

          return response.json();
        };

        const buscarLugar = async () => {
          const query = inputBusqueda.value.trim();
          if (!query) {
            inputBusqueda.focus();
            return;
          }

          botonBuscar.disabled = true;
          botonBuscar.textContent = "Buscando...";

          try {
            const resultados = await obtenerJson(
              "https://nominatim.openstreetmap.org/search?format=jsonv2&limit=1&accept-language=es&q=" + encodeURIComponent(query)
            );

            if (!Array.isArray(resultados) || resultados.length === 0) {
              throw new Error("No se ha encontrado ninguna ubicacion para esa busqueda.");
            }

            const resultado = resultados[0];
            actualizarUbicacion(Number(resultado.lat), Number(resultado.lon), resultado.display_name || query);
          } catch (error) {
            alert(error.message || "No se pudo localizar la direccion.");
          } finally {
            botonBuscar.disabled = false;
            botonBuscar.textContent = "Buscar en mapa";
          }
        };

        const resolverDireccion = async (lat, lon) => {
          try {
            const resultado = await obtenerJson(
              "https://nominatim.openstreetmap.org/reverse?format=jsonv2&accept-language=es&lat=" + encodeURIComponent(lat) + "&lon=" + encodeURIComponent(lon)
            );
            const etiqueta = resultado.display_name || (lat.toFixed(6) + ", " + lon.toFixed(6));
            actualizarUbicacion(lat, lon, etiqueta);
          } catch (error) {
            actualizarUbicacion(lat, lon, lat.toFixed(6) + ", " + lon.toFixed(6));
          }
        };

        const inicializarMapa = () => {
          mapa = L.map("mapaEvento").setView([40.4168, -3.7038], 6);

          L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: "&copy; OpenStreetMap contributors"
          }).addTo(mapa);

          mapa.on("click", (event) => {
            const { lat, lng } = event.latlng;
            resolverDireccion(lat, lng);
          });
        };

        abrirModal.addEventListener("click", abrir);
        cerrarModal.addEventListener("click", cerrar);
        botonBuscar.addEventListener("click", buscarLugar);
        inputBusqueda.addEventListener("keydown", (event) => {
          if (event.key === "Enter") {
            event.preventDefault();
            buscarLugar();
          }
        });

        modal.addEventListener("click", (event) => {
          if (event.target === modal) {
            cerrar();
          }
        });

        document.addEventListener("keydown", (event) => {
          if (event.key === "Escape" && modal.classList.contains("visible")) {
            cerrar();
          }
        });

        formCrearEvento.addEventListener("submit", (event) => {
          if (!inputLugar.value.trim() || !inputLatitud.value || !inputLongitud.value) {
            event.preventDefault();
            alert("Debes seleccionar la ubicacion del evento en el mapa.");
          }
        });
      })();
    </script>
  </body>
</html>
