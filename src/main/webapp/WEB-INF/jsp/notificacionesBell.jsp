<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<details class="notifications-menu notifications-menu-topbar">
  <summary class="notifications-trigger" aria-label="Abrir notificaciones">
    <span class="notifications-copy">
      <span class="notifications-label">Notificaciones</span>
      <span class="notifications-meta">
        <c:choose>
          <c:when test="${notificacionesHeader.totalPendientes gt 0}">
            <c:out value="${notificacionesHeader.totalPendientes}" /> pendientes
          </c:when>
          <c:otherwise>
            Sin nuevas
          </c:otherwise>
        </c:choose>
      </span>
    </span>
  </summary>

  <div class="notifications-panel">
    <c:choose>
      <c:when test="${empty notificacionesHeader.items}">
        <p class="notifications-empty">No tienes notificaciones.</p>
      </c:when>
      <c:otherwise>
        <table class="notifications-table">
          <tbody>
            <c:forEach var="notificacion" items="${notificacionesHeader.items}">
              <tr>
                <td class="notifications-table-cell">
                  <details class="notification-row">
                    <summary class="notification-summary">
                      <c:out value="${notificacion.mensaje}" />
                    </summary>
                    <c:if test="${notificacion.pendiente}">
                      <div class="notification-row-actions">
                        <form method="post" action="/notificaciones/aceptar">
                          <input type="hidden" name="notificacionId" value="<c:out value='${notificacion.id}' />" />
                          <input type="hidden" name="redirect" value="<c:out value='${rutaActual}' />" />
                          <button type="submit" class="button-primary">Unirse</button>
                        </form>
                        <form method="post" action="/notificaciones/rechazar">
                          <input type="hidden" name="notificacionId" value="<c:out value='${notificacion.id}' />" />
                          <input type="hidden" name="redirect" value="<c:out value='${rutaActual}' />" />
                          <button type="submit" class="button-secondary">Rechazar</button>
                        </form>
                      </div>
                    </c:if>
                  </details>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </div>
</details>
