<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<section class="admin-section" id="comunidades" aria-labelledby="comunidades-titulo">
  <div class="section-heading"><div class="section-heading-copy"><span class="section-icon"><svg class="admin-icon" aria-hidden="true"><use href="#admin-community" /></svg></span><div><h2 id="comunidades-titulo">Comunidades <span class="count-badge">${fn:length(admin.comunidades)}</span></h2><p>Supervisa los grupos y gestiona sus miembros.</p></div></div><span class="status-badge"><span></span> Activos</span></div>

  <div class="search-row"><label class="sr-only" for="buscarComunidad">Buscar comunidades por nombre</label>
    <form action="/admin#comunidades" method="get" role="search" aria-label="Buscar comunidades por nombre">
      <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
      <input id="buscarComunidad" type="search" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" placeholder="Buscar comunidad por nombre" />
      <c:if test="${admin.comunidadSeleccionada != null}">
        <input type="hidden" name="comunidadId" value="${admin.comunidadSeleccionada.id}" />
      </c:if>
      <button class="button-primary" type="submit"><svg class="admin-icon" aria-hidden="true"><use href="#admin-search" /></svg>Buscar</button>
      <c:url var="limpiarbuscarComunidadUrl" value="/admin"><c:param name="buscarUsuario" value="${admin.buscarUsuario}" /><c:if test="${admin.comunidadSeleccionada != null}"><c:param name="comunidadId" value="${admin.comunidadSeleccionada.id}" /></c:if></c:url><c:if test="${not empty admin.buscarComunidad}"><a class="clear-search" href="<c:out value='${limpiarbuscarComunidadUrl}' />#comunidades">Limpiar filtro</a></c:if>
    </form>
  </div>

  <div class="table-wrapper" tabindex="0" role="region" aria-label="Listado de comunidades">
    <table class="data-table">
      <thead>
        <tr>
          <th scope="col">Comunidad</th>
          
          <th scope="col">Miembros</th>
          <th scope="col">Eventos</th>
          <th scope="col">Invitaciones</th>
          <th scope="col">Acciones</th>
        </tr>
      </thead>
      <tbody>
        <c:choose>
          <c:when test="${empty admin.comunidades}">
            <tr>
              <td colspan="5"><div class="empty-box"><svg class="admin-icon" aria-hidden="true"><use href="#admin-community" /></svg><strong>No se encontraron comunidades</strong><p>Prueba con otro nombre o limpia el filtro.</p></div></td>
            </tr>
          </c:when>
          <c:otherwise>
            <c:forEach var="comunidad" items="${admin.comunidades}">
              <c:url var="verMiembrosUrl" value="/admin">
                <c:param name="buscarUsuario" value="${admin.buscarUsuario}" />
                <c:param name="buscarComunidad" value="${admin.buscarComunidad}" />
                <c:param name="comunidadId" value="${comunidad.id}" />
              </c:url>
              <tr class="${admin.comunidadSeleccionada != null and admin.comunidadSeleccionada.id eq comunidad.id ? 'selected-row' : ''}">
                <td><div class="identity"><span class="avatar avatar-community" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(comunidad.nombre, 0, 1))}" /></span><div class="identity-copy"><strong><c:out value="${comunidad.nombre}" /></strong><span><c:out value="${empty comunidad.logoUrl ? 'Sin logo configurado' : 'Logo configurado'}" /></span></div></div></td>
                <td><c:out value="${comunidad.totalMiembros}" /></td>
                <td><c:out value="${comunidad.totalEventos}" /></td>
                <td><c:out value="${comunidad.totalInvitaciones}" /></td>
                <td>
                  <div class="table-actions">
                    <a class="button-secondary" href="<c:out value='${verMiembrosUrl}' />#miembros">Ver miembros</a>

                    <button
                      type="button"
                      class="button-secondary abrir-editar-comunidad"
                      data-comunidad-id="${comunidad.id}"
                      data-comunidad-nombre="<c:out value='${comunidad.nombre}' />"
                      data-comunidad-logo="<c:out value='${comunidad.logoUrl}' />">Editar</button>

                    <form action="/admin/comunidades/eliminar" method="post" onsubmit="return confirm('Se desactivará esta comunidad. ¿Continuar?');">
                      <input type="hidden" name="comunidadId" value="${comunidad.id}" />
                      <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
                      <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
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
