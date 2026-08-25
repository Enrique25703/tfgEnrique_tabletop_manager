<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Panel ADMIN</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      body.admin-screen .profile-bar {
        flex: 0 0 auto;
        padding: 14px 18px 0;
      }

      body.admin-screen .profile-card {
        min-width: 220px;
        max-width: 280px;
        padding: 12px 14px;
      }

      body.admin-screen .page-content {
        display: flex;
        flex: 1 1 auto;
        min-height: 0;
        padding: 14px 18px 18px;
        overflow: hidden;
      }

      body.admin-screen .page-panel {
        display: flex;
        flex: 1 1 auto;
        flex-direction: column;
        min-height: 0;
        padding: 18px;
        overflow: hidden;
      }

      body.admin-screen .page-header {
        flex: 0 0 auto;
        margin-bottom: 12px;
      }

      body.admin-screen .page-title {
        margin-bottom: 4px;
        font-size: 1.7rem;
      }

      body.admin-screen .page-subtitle {
        font-size: 0.95rem;
      }

      body.admin-screen .note-box,
      body.admin-screen .error-box {
        flex: 0 0 auto;
        margin-top: 0;
        margin-bottom: 10px;
        padding: 10px 12px;
      }

      .admin-grid {
        display: grid;
        flex: 1 1 auto;
        min-height: 0;
        grid-template-columns: minmax(0, 1.05fr) minmax(0, 1.05fr) minmax(320px, 0.9fr);
        gap: 16px;
      }

      .admin-section,
      .detail-card,
      .modal-card {
        padding: 16px;
        border: 1px solid var(--line);
        border-radius: 20px;
        background: var(--panel-soft);
      }

      .admin-section,
      .detail-card {
        display: flex;
        flex-direction: column;
        min-height: 0;
        overflow: hidden;
      }

      .search-row,
      .table-actions,
      .modal-actions,
      .panel-actions,
      .detail-header {
        display: flex;
        gap: 12px;
        flex-wrap: wrap;
        align-items: center;
      }

      .search-row {
        flex: 0 0 auto;
        margin: 12px 0;
      }

      .search-row form {
        display: flex;
        gap: 10px;
        flex: 1 1 auto;
        flex-wrap: wrap;
      }

      .search-row input {
        flex: 1 1 180px;
      }

      .table-wrapper {
        flex: 1 1 auto;
        min-height: 0;
        overflow-x: auto;
        overflow-y: auto;
        border: 1px solid rgba(130, 173, 255, 0.1);
        border-radius: 16px;
      }

      .table-actions form {
        margin: 0;
      }

      .table-actions button,
      .table-actions a {
        min-width: 92px;
        padding: 9px 12px;
        font-size: 0.9rem;
      }

      .muted-copy {
        margin: 0;
        color: var(--muted);
        font-size: 0.92rem;
      }

      .empty-box {
        padding: 16px;
        border: 1px dashed var(--line);
        border-radius: 16px;
        color: var(--muted);
        background: rgba(12, 18, 28, 0.6);
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
        width: min(100%, 520px);
        box-shadow: var(--shadow);
      }

      .modal-card form {
        display: grid;
        gap: 14px;
      }

      .member-grid {
        flex: 1 1 auto;
        min-height: 0;
        display: grid;
        gap: 12px;
        margin-top: 14px;
        overflow: auto;
        padding-right: 4px;
      }

      .member-row {
        display: flex;
        justify-content: space-between;
        gap: 12px;
        flex-wrap: wrap;
        padding: 14px;
        border: 1px solid var(--line);
        border-radius: 16px;
        background: rgba(14, 22, 34, 0.7);
      }

      .member-meta {
        margin: 0;
        color: var(--muted);
      }

      .panel-title {
        margin: 0 0 4px;
        font-size: 1.32rem;
      }

      .tight-table .data-table th,
      .tight-table .data-table td {
        padding: 10px 11px;
        font-size: 0.92rem;
      }

      .tight-table .data-table th {
        position: sticky;
        top: 0;
        z-index: 1;
      }

      .detail-header {
        flex: 0 0 auto;
      }

      .panel-actions form {
        margin: 0;
      }

      @media (max-width: 1360px) {
        .admin-grid {
          grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
          grid-template-rows: minmax(0, 1fr) minmax(240px, 0.8fr);
        }

        .detail-card {
          grid-column: 1 / -1;
        }
      }

      @media (max-width: 960px) {
        body.admin-screen .page-content,
        body.admin-screen .page-panel {
          overflow: visible;
        }

        .admin-grid {
          grid-template-columns: 1fr;
        }

        .admin-section,
        .detail-card,
        .table-wrapper,
        .member-grid {
          min-height: auto;
          overflow: visible;
        }
      }

      @media (max-width: 760px) {
        .search-row form,
        .table-actions,
        .panel-actions,
        .detail-header,
        .member-row {
          flex-direction: column;
          align-items: stretch;
        }
      }
    </style>
  </head>
  <body class="admin-screen">
    <c:set var="sidebarActive" value="admin" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="profile-card">
            <p class="profile-title">Panel ADMIN</p>
            <p class="profile-role"><c:out value="${admin.nombreAdmin}" /></p>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Administracion</h2>
              <p class="page-subtitle">Gestiona usuarios, comunidades y miembros desde un unico panel.</p>
            </div>

            <c:if test="${not empty mensajeOk}">
              <div class="note-box"><c:out value="${mensajeOk}" /></div>
            </c:if>

            <c:if test="${not empty mensajeError}">
              <div class="error-box"><c:out value="${mensajeError}" /></div>
            </c:if>

            <div class="admin-grid">
              <section class="admin-section">
                <div class="detail-header">
                  <div>
                    <h3 class="panel-title">Usuarios</h3>
                    <p class="muted-copy">Busca, edita credenciales basicas o desactiva cuentas.</p>
                  </div>
                </div>

                <div class="search-row">
                  <form action="/admin" method="get">
                    <input type="text" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" placeholder="Buscar por nombre o correo" />
                    <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
                    <c:if test="${admin.comunidadSeleccionada != null}">
                      <input type="hidden" name="comunidadId" value="${admin.comunidadSeleccionada.id}" />
                    </c:if>
                    <button class="button-primary" type="submit">Buscar usuarios</button>
                    <a class="button-secondary" href="/admin">Limpiar</a>
                  </form>
                </div>

                <div class="table-wrapper tight-table">
                  <table class="data-table">
                    <thead>
                      <tr>
                        <th>Usuario</th>
                        <th>Correo</th>
                        <th>Rol</th>
                        <th>Alta</th>
                        <th>Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:choose>
                        <c:when test="${empty admin.usuarios}">
                          <tr>
                            <td colspan="5">No hay usuarios activos que coincidan con la busqueda.</td>
                          </tr>
                        </c:when>
                        <c:otherwise>
                          <c:forEach var="usuario" items="${admin.usuarios}">
                            <tr>
                              <td><c:out value="${usuario.nombreUsuario}" /></td>
                              <td><c:out value="${usuario.email}" /></td>
                              <td><c:out value="${usuario.rol}" /></td>
                              <td><c:out value="${usuario.creadoEn}" /></td>
                              <td>
                                <div class="table-actions">
                                  <button
                                    type="button"
                                    class="button-secondary abrir-editar-usuario"
                                    data-usuario-id="${usuario.id}"
                                    data-usuario-nombre="<c:out value='${usuario.nombreUsuario}' />"
                                    data-usuario-email="<c:out value='${usuario.email}' />">Editar</button>

                                  <button
                                    type="button"
                                    class="button-secondary abrir-password-usuario"
                                    data-usuario-id="${usuario.id}"
                                    data-usuario-nombre="<c:out value='${usuario.nombreUsuario}' />">Contrasena</button>

                                  <form action="/admin/usuarios/eliminar" method="post" onsubmit="return confirm('Se desactivara este usuario. Continuar?');">
                                    <input type="hidden" name="usuarioId" value="${usuario.id}" />
                                    <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
                                    <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
                                    <c:if test="${admin.comunidadSeleccionada != null}">
                                      <input type="hidden" name="comunidadIdRetorno" value="${admin.comunidadSeleccionada.id}" />
                                    </c:if>
                                    <button class="button-secondary" type="submit">Eliminar</button>
                                  </form>
                                </div>
                              </td>
                            </tr>
                          </c:forEach>
                        </c:otherwise>
                      </c:choose>
                    </tbody>
                  </table>
                </div>
              </section>

              <section class="admin-section">
                <div class="detail-header">
                  <div>
                    <h3 class="panel-title">Comunidades</h3>
                    <p class="muted-copy">Edita atributos, da de baja comunidades y revisa sus miembros.</p>
                  </div>
                </div>

                <div class="search-row">
                  <form action="/admin" method="get">
                    <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
                    <input type="text" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" placeholder="Buscar comunidad por nombre" />
                    <c:if test="${admin.comunidadSeleccionada != null}">
                      <input type="hidden" name="comunidadId" value="${admin.comunidadSeleccionada.id}" />
                    </c:if>
                    <button class="button-primary" type="submit">Buscar comunidades</button>
                    <a class="button-secondary" href="/admin">Limpiar</a>
                  </form>
                </div>

                <div class="table-wrapper tight-table">
                  <table class="data-table">
                    <thead>
                      <tr>
                        <th>Nombre</th>
                        <th>Logo</th>
                        <th>Miembros</th>
                        <th>Eventos</th>
                        <th>Invitaciones</th>
                        <th>Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:choose>
                        <c:when test="${empty admin.comunidades}">
                          <tr>
                            <td colspan="6">No hay comunidades activas que coincidan con la busqueda.</td>
                          </tr>
                        </c:when>
                        <c:otherwise>
                          <c:forEach var="comunidad" items="${admin.comunidades}">
                            <c:url var="verMiembrosUrl" value="/admin">
                              <c:param name="buscarUsuario" value="${admin.buscarUsuario}" />
                              <c:param name="buscarComunidad" value="${admin.buscarComunidad}" />
                              <c:param name="comunidadId" value="${comunidad.id}" />
                            </c:url>
                            <tr>
                              <td><c:out value="${comunidad.nombre}" /></td>
                              <td><c:out value="${empty comunidad.logoUrl ? 'Sin logo' : comunidad.logoUrl}" /></td>
                              <td><c:out value="${comunidad.totalMiembros}" /></td>
                              <td><c:out value="${comunidad.totalEventos}" /></td>
                              <td><c:out value="${comunidad.totalInvitaciones}" /></td>
                              <td>
                                <div class="table-actions">
                                  <a class="button-secondary" href="${verMiembrosUrl}">Ver miembros</a>

                                  <button
                                    type="button"
                                    class="button-secondary abrir-editar-comunidad"
                                    data-comunidad-id="${comunidad.id}"
                                    data-comunidad-nombre="<c:out value='${comunidad.nombre}' />"
                                    data-comunidad-logo="<c:out value='${comunidad.logoUrl}' />">Editar</button>

                                  <form action="/admin/comunidades/eliminar" method="post" onsubmit="return confirm('Se desactivara esta comunidad. Continuar?');">
                                    <input type="hidden" name="comunidadId" value="${comunidad.id}" />
                                    <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
                                    <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
                                    <button class="button-secondary" type="submit">Eliminar</button>
                                  </form>
                                </div>
                              </td>
                            </tr>
                          </c:forEach>
                        </c:otherwise>
                      </c:choose>
                    </tbody>
                  </table>
                </div>
              </section>

              <section class="detail-card">
                <c:choose>
                  <c:when test="${admin.comunidadSeleccionada == null}">
                    <div class="empty-box">Selecciona una comunidad para ver y gestionar sus miembros.</div>
                  </c:when>
                  <c:otherwise>
                    <div class="detail-header">
                      <div>
                        <h3 class="panel-title">
                          Miembros de <c:out value="${admin.comunidadSeleccionada.nombre}" />
                        </h3>
                        <p class="muted-copy">
                          Logo: <c:out value="${empty admin.comunidadSeleccionada.logoUrl ? 'Sin logo configurado' : admin.comunidadSeleccionada.logoUrl}" />
                        </p>
                      </div>
                    </div>

                    <div class="member-grid">
                      <c:forEach var="miembro" items="${admin.comunidadSeleccionada.miembros}">
                        <div class="member-row">
                          <div>
                            <h4 class="profile-title" style="margin-bottom:4px;"><c:out value="${miembro.nombreUsuario}" /></h4>
                            <p class="member-meta">Rol: <c:out value="${miembro.rol}" /></p>
                          </div>

                          <div class="panel-actions">
                            <c:choose>
                              <c:when test="${miembro.eliminable}">
                                <form action="/admin/comunidades/miembros/eliminar" method="post" onsubmit="return confirm('Se eliminara este miembro de la comunidad. Continuar?');">
                                  <input type="hidden" name="comunidadId" value="${admin.comunidadSeleccionada.id}" />
                                  <input type="hidden" name="usuarioId" value="${miembro.usuarioId}" />
                                  <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
                                  <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
                                  <button class="button-secondary" type="submit">Eliminar miembro</button>
                                </form>
                              </c:when>
                              <c:otherwise>
                                <span class="chip">Administrador protegido</span>
                              </c:otherwise>
                            </c:choose>
                          </div>
                        </div>
                      </c:forEach>
                    </div>
                  </c:otherwise>
                </c:choose>
              </section>
            </div>
          </section>
        </main>
      </div>
    </div>

    <div id="modalEditarUsuario" class="modal-shell">
      <div class="modal-card">
        <h3 class="page-title" style="font-size:1.4rem; margin-bottom:6px;">Editar usuario</h3>
        <p class="page-subtitle">Actualiza nombre de usuario y correo.</p>

        <form action="/admin/usuarios/actualizar" method="post">
          <input type="hidden" id="editarUsuarioId" name="usuarioId" />
          <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
          <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
          <c:if test="${admin.comunidadSeleccionada != null}">
            <input type="hidden" name="comunidadIdRetorno" value="${admin.comunidadSeleccionada.id}" />
          </c:if>

          <div>
            <label for="editarNombreUsuario">Nombre de usuario</label>
            <input id="editarNombreUsuario" name="nombreUsuario" type="text" required />
          </div>

          <div>
            <label for="editarEmailUsuario">Correo</label>
            <input id="editarEmailUsuario" name="email" type="email" required />
          </div>

          <div class="modal-actions">
            <button class="button-primary" type="submit">Guardar</button>
            <button class="button-secondary cerrar-modal" type="button">Cancelar</button>
          </div>
        </form>
      </div>
    </div>

    <div id="modalPasswordUsuario" class="modal-shell">
      <div class="modal-card">
        <h3 class="page-title" style="font-size:1.4rem; margin-bottom:6px;">Cambiar contrasena</h3>
        <p class="page-subtitle" id="passwordUsuarioTitulo">Define una nueva contrasena para el usuario.</p>

        <form action="/admin/usuarios/contrasena" method="post">
          <input type="hidden" id="passwordUsuarioId" name="usuarioId" />
          <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
          <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
          <c:if test="${admin.comunidadSeleccionada != null}">
            <input type="hidden" name="comunidadIdRetorno" value="${admin.comunidadSeleccionada.id}" />
          </c:if>

          <div>
            <label for="nuevaContrasenaUsuario">Nueva contrasena</label>
            <input id="nuevaContrasenaUsuario" name="nuevaContrasena" type="password" minlength="6" required />
          </div>

          <div class="modal-actions">
            <button class="button-primary" type="submit">Actualizar contrasena</button>
            <button class="button-secondary cerrar-modal" type="button">Cancelar</button>
          </div>
        </form>
      </div>
    </div>

    <div id="modalEditarComunidad" class="modal-shell">
      <div class="modal-card">
        <h3 class="page-title" style="font-size:1.4rem; margin-bottom:6px;">Editar comunidad</h3>
        <p class="page-subtitle">Actualiza nombre y logo de la comunidad.</p>

        <form action="/admin/comunidades/actualizar" method="post">
          <input type="hidden" id="editarComunidadId" name="comunidadId" />
          <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
          <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
          <c:if test="${admin.comunidadSeleccionada != null}">
            <input type="hidden" name="comunidadIdRetorno" value="${admin.comunidadSeleccionada.id}" />
          </c:if>

          <div>
            <label for="editarNombreComunidad">Nombre</label>
            <input id="editarNombreComunidad" name="nombre" type="text" required />
          </div>

          <div>
            <label for="editarLogoComunidad">Logo URL</label>
            <input id="editarLogoComunidad" name="logoUrl" type="text" />
          </div>

          <div class="modal-actions">
            <button class="button-primary" type="submit">Guardar</button>
            <button class="button-secondary cerrar-modal" type="button">Cancelar</button>
          </div>
        </form>
      </div>
    </div>

    <script>
      (() => {
        const body = document.body;
        const modales = Array.from(document.querySelectorAll(".modal-shell"));

        const abrirModal = (id) => {
          const modal = document.getElementById(id);
          if (!modal) {
            return;
          }
          modal.classList.add("visible");
          body.style.overflow = "hidden";
        };

        const cerrarModales = () => {
          modales.forEach((modal) => modal.classList.remove("visible"));
          body.style.overflow = "";
        };

        document.querySelectorAll(".cerrar-modal").forEach((boton) => {
          boton.addEventListener("click", cerrarModales);
        });

        modales.forEach((modal) => {
          modal.addEventListener("click", (event) => {
            if (event.target === modal) {
              cerrarModales();
            }
          });
        });

        document.querySelectorAll(".abrir-editar-usuario").forEach((boton) => {
          boton.addEventListener("click", () => {
            document.getElementById("editarUsuarioId").value = boton.dataset.usuarioId || "";
            document.getElementById("editarNombreUsuario").value = boton.dataset.usuarioNombre || "";
            document.getElementById("editarEmailUsuario").value = boton.dataset.usuarioEmail || "";
            abrirModal("modalEditarUsuario");
          });
        });

        document.querySelectorAll(".abrir-password-usuario").forEach((boton) => {
          boton.addEventListener("click", () => {
            document.getElementById("passwordUsuarioId").value = boton.dataset.usuarioId || "";
            document.getElementById("passwordUsuarioTitulo").textContent =
              "Define una nueva contrasena para " + (boton.dataset.usuarioNombre || "el usuario") + ".";
            document.getElementById("nuevaContrasenaUsuario").value = "";
            abrirModal("modalPasswordUsuario");
          });
        });

        document.querySelectorAll(".abrir-editar-comunidad").forEach((boton) => {
          boton.addEventListener("click", () => {
            document.getElementById("editarComunidadId").value = boton.dataset.comunidadId || "";
            document.getElementById("editarNombreComunidad").value = boton.dataset.comunidadNombre || "";
            document.getElementById("editarLogoComunidad").value = boton.dataset.comunidadLogo || "";
            abrirModal("modalEditarComunidad");
          });
        });

        document.addEventListener("keydown", (event) => {
          if (event.key === "Escape") {
            cerrarModales();
          }
        });
      })();
    </script>
  </body>
</html>
