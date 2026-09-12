<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<section class="admin-section" id="usuarios" aria-labelledby="usuarios-titulo">
  <div class="section-heading"><div class="section-heading-copy"><span class="section-icon"><svg class="admin-icon" aria-hidden="true"><use href="#admin-users" /></svg></span><div><h2 id="usuarios-titulo">Usuarios <span class="count-badge">${fn:length(admin.usuarios)}</span></h2><p>Administra las cuentas y sus datos de acceso.</p></div></div><span class="status-badge"><span></span> Activos</span></div>

  <div class="search-row"><label class="sr-only" for="buscarUsuario">Buscar usuarios por nombre o correo</label>
    <form action="/admin#usuarios" method="get" role="search" aria-label="Buscar usuarios por nombre o correo">
      <input id="buscarUsuario" type="search" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" placeholder="Buscar por nombre o correo" />
      <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
      <c:if test="${admin.comunidadSeleccionada != null}">
        <input type="hidden" name="comunidadId" value="${admin.comunidadSeleccionada.id}" />
      </c:if>
      <button class="button-primary" type="submit"><svg class="admin-icon" aria-hidden="true"><use href="#admin-search" /></svg>Buscar</button>
      <c:url var="limpiarbuscarUsuarioUrl" value="/admin"><c:param name="buscarComunidad" value="${admin.buscarComunidad}" /><c:if test="${admin.comunidadSeleccionada != null}"><c:param name="comunidadId" value="${admin.comunidadSeleccionada.id}" /></c:if></c:url><c:if test="${not empty admin.buscarUsuario}"><a class="clear-search" href="<c:out value='${limpiarbuscarUsuarioUrl}' />#usuarios">Limpiar filtro</a></c:if>
    </form>
  </div>

  <div class="table-wrapper" tabindex="0" role="region" aria-label="Listado de usuarios">
    <table class="data-table">
      <thead>
        <tr>
          <th scope="col">Usuario</th>
          
          <th scope="col">Rol</th>
          <th scope="col">Fecha de alta</th>
          <th scope="col">Acciones</th>
        </tr>
      </thead>
      <tbody>
        <c:choose>
          <c:when test="${empty admin.usuarios}">
            <tr>
              <td colspan="4"><div class="empty-box"><svg class="admin-icon" aria-hidden="true"><use href="#admin-search" /></svg><strong>No se encontraron usuarios</strong><p>Prueba con otro nombre o correo, o limpia el filtro.</p></div></td>
            </tr>
          </c:when>
          <c:otherwise>
            <c:forEach var="usuario" items="${admin.usuarios}">
              <tr>
                <td><div class="identity"><span class="avatar" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(usuario.nombreUsuario, 0, 1))}" /></span><div class="identity-copy"><strong><c:out value="${usuario.nombreUsuario}" /></strong><span><c:out value="${usuario.email}" /></span></div></div></td>
                <td><span class="role-badge ${usuario.rol eq 'ADMIN' ? 'role-admin' : ''}"><c:out value="${usuario.rol}" /></span></td>
                <td class="date-cell"><c:out value="${usuario.creadoEn}" /></td>
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
                      data-usuario-nombre="<c:out value='${usuario.nombreUsuario}' />">Contraseña</button>

                    <form action="/admin/usuarios/eliminar" method="post" onsubmit="return confirm('Se desactivará este usuario. ¿Continuar?');">
                      <input type="hidden" name="usuarioId" value="${usuario.id}" />
                      <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
                      <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
                      <c:if test="${admin.comunidadSeleccionada != null}">
                        <input type="hidden" name="comunidadIdRetorno" value="${admin.comunidadSeleccionada.id}" />
                      </c:if>
                      <button class="button-secondary button-danger" type="submit">Desactivar</button>
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
