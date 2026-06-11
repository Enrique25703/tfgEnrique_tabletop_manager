<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Comunidades</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
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
      <jsp:include page="header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
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

                        <form action="/comunidades/eventos/crear" method="post">
                          <input type="hidden" name="comunidadId" value="${comunidades.comunidadSeleccionada.id}" />

                          <div class="linea-formulario">
                            <label for="fechaEvento">Fecha</label>
                            <input id="fechaEvento" name="fecha" type="datetime-local" required />
                          </div>

                          <div class="linea-formulario">
                            <label for="numeroRondas">Numero de rondas</label>
                            <input id="numeroRondas" name="numeroRondas" type="number" min="1" required />
                          </div>

                          <div class="linea-formulario">
                            <label for="lugarEvento">Lugar</label>
                            <input id="lugarEvento" name="lugar" type="text" required />
                          </div>

                          <div class="linea-formulario">
                            <label for="formatoEvento">Formato de juego</label>
                            <select id="formatoEvento" name="formatoJuego" required>
                              <option value="">Selecciona un formato</option>
                              <option value="WH40K_10">Warhammer 40.000 10a edicion</option>
                              <option value="AOS_4">Age of Sigmar 4a edicion</option>
                            </select>
                          </div>

                          <div class="linea-formulario">
                            <button class="button-primary" type="submit">Crear evento</button>
                          </div>
                        </form>
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
                              <option value="WH40K_10">Warhammer 40.000 10a edicion</option>
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
  </body>
</html>
