<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<section class="detail-card" id="miembros" aria-labelledby="miembros-titulo"><div class="section-heading"><div class="section-heading-copy"><span class="section-icon"><svg class="admin-icon" aria-hidden="true"><use href="#admin-shield" /></svg></span><div><h2 id="miembros-titulo">Miembros</h2><p>Detalle de la comunidad</p></div></div></div>
  <c:choose>
    <c:when test="${admin.comunidadSeleccionada == null}">
      <div class="empty-box member-empty"><span class="empty-illustration"><svg class="admin-icon" aria-hidden="true"><use href="#admin-community" /></svg></span><strong>Cada comunidad, en detalle</strong><p>Selecciona «Ver miembros» en una comunidad para consultar sus integrantes y gestionar el acceso.</p><a class="button-secondary" href="#comunidades">Explorar comunidades <svg class="admin-icon" aria-hidden="true"><use href="#admin-arrow" /></svg></a></div>
    </c:when>
    <c:otherwise>
      <div class="selected-community"><span class="avatar avatar-community" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(admin.comunidadSeleccionada.nombre, 0, 1))}" /></span><div><h3><c:out value="${admin.comunidadSeleccionada.nombre}" /></h3><p>${fn:length(admin.comunidadSeleccionada.miembros)} miembros en esta comunidad</p></div></div>

      <div class="member-grid"><c:if test="${empty admin.comunidadSeleccionada.miembros}"><div class="empty-box"><strong>Aún no hay miembros</strong><p>Esta comunidad no tiene integrantes activos.</p></div></c:if>
        <c:forEach var="miembro" items="${admin.comunidadSeleccionada.miembros}">
          <div class="member-row">
            <div>
              <h4 class="member-name"><c:out value="${miembro.nombreUsuario}" /></h4>
              <p class="member-meta">Rol: <c:out value="${miembro.rol}" /></p>
            </div>

            <div class="panel-actions">
              <c:choose>
                <c:when test="${miembro.eliminable}">
                  <form action="/admin/comunidades/miembros/eliminar" method="post" onsubmit="return confirm('Se retirará este miembro de la comunidad. ¿Continuar?');">
                    <input type="hidden" name="comunidadId" value="${admin.comunidadSeleccionada.id}" />
                    <input type="hidden" name="usuarioId" value="${miembro.usuarioId}" />
                    <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
                    <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
                    <button class="button-secondary button-danger" type="submit">Retirar acceso</button>
                  </form>
                </c:when>
                <c:otherwise>
                  <span class="protected-badge">Administrador protegido</span>
                </c:otherwise>
              </c:choose>
            </div>
          </div>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</section>
