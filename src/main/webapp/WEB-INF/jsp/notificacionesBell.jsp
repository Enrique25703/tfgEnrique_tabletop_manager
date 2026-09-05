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
                  <c:choose>
                    <c:when test="${notificacion.tipo eq 'DESAFIO_PARTIDA'}">
                      <button type="button" class="notification-summary notification-dialog-trigger" data-dialog-id="duelo-notificacion-${notificacion.id}">
                        <strong><c:out value="${notificacion.titulo}" /></strong>
                        <small class="notification-extra"><c:out value="${notificacion.pendiente ? 'Pendiente de respuesta' : notificacion.estado}" /></small>
                      </button>
                      <dialog class="notification-dialog" id="duelo-notificacion-${notificacion.id}" aria-labelledby="titulo-duelo-${notificacion.id}">
                        <div class="notification-dialog-header">
                          <div><small>Invitación a duelo</small><h2 id="titulo-duelo-${notificacion.id}"><c:out value="${notificacion.titulo}" /></h2></div>
                          <button type="button" class="notification-dialog-close" data-close-notification-dialog aria-label="Cerrar">×</button>
                        </div>
                        <div class="notification-dialog-body">
                          <dl class="notification-detail-grid">
                            <div><dt>Retador</dt><dd><c:out value="${notificacion.retador}" /></dd></div>
                            <div><dt>Formato</dt><dd><c:out value="${notificacion.formato}" /></dd></div>
                            <div><dt>Fecha</dt><dd><c:out value="${notificacion.fecha}" /></dd></div>
                            <div><dt>Lugar</dt><dd><c:out value="${notificacion.lugar}" /></dd></div>
                          </dl>
                          <c:if test="${not empty notificacion.mensajeExtra}"><div class="notification-dialog-message"><strong>Mensaje del oponente</strong><p><c:out value="${notificacion.mensajeExtra}" /></p></div></c:if>
                          <c:choose>
                            <c:when test="${notificacion.pendiente}">
                              <div class="notification-dialog-actions">
                                <form method="post" action="/notificaciones/aceptar"><input type="hidden" name="notificacionId" value="<c:out value='${notificacion.id}' />" /><input type="hidden" name="redirect" value="<c:out value='${rutaActual}' />" /><button type="submit" class="button-primary">Aceptar duelo</button></form>
                                <form method="post" action="/notificaciones/rechazar"><input type="hidden" name="notificacionId" value="<c:out value='${notificacion.id}' />" /><input type="hidden" name="redirect" value="<c:out value='${rutaActual}' />" /><button type="submit" class="button-secondary">Rechazar</button></form>
                              </div>
                            </c:when>
                            <c:otherwise><p class="notification-dialog-status">Estado: <c:out value="${notificacion.estado}" /></p></c:otherwise>
                          </c:choose>
                          <form class="notification-delete-form" method="post" action="/notificaciones/eliminar" onsubmit="return confirm('¿Eliminar esta notificación?');"><input type="hidden" name="notificacionId" value="<c:out value='${notificacion.id}' />" /><input type="hidden" name="redirect" value="<c:out value='${rutaActual}' />" /><button type="submit" class="button-secondary danger-action">Eliminar notificación</button></form>
                        </div>
                      </dialog>
                    </c:when>
                    <c:otherwise>
                      <details class="notification-row">
                        <summary class="notification-summary"><c:out value="${notificacion.mensaje}" /><c:if test="${not empty notificacion.mensajeExtra}"><small class="notification-extra"><c:out value="${notificacion.mensajeExtra}" /></small></c:if></summary>
                        <div class="notification-row-actions"><c:if test="${notificacion.pendiente}"><form method="post" action="/notificaciones/aceptar"><input type="hidden" name="notificacionId" value="<c:out value='${notificacion.id}' />" /><input type="hidden" name="redirect" value="<c:out value='${rutaActual}' />" /><button type="submit" class="button-primary">Unirse</button></form><form method="post" action="/notificaciones/rechazar"><input type="hidden" name="notificacionId" value="<c:out value='${notificacion.id}' />" /><input type="hidden" name="redirect" value="<c:out value='${rutaActual}' />" /><button type="submit" class="button-secondary">Rechazar</button></form></c:if><form method="post" action="/notificaciones/eliminar" onsubmit="return confirm('¿Eliminar esta notificación?');"><input type="hidden" name="notificacionId" value="<c:out value='${notificacion.id}' />" /><input type="hidden" name="redirect" value="<c:out value='${rutaActual}' />" /><button type="submit" class="button-secondary danger-action">Eliminar</button></form></div>
                      </details>
                    </c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </div>
</details>
<script src="/js/notificaciones.js?v=1" defer></script>
