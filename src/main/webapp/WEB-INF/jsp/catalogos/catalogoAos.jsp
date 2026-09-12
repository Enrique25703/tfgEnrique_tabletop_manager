<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Catálogo Age of Sigmar</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <link rel="stylesheet" href="/css/catalogos.css" />
  </head>
  <body class="catalog-screen">
    <c:set var="sidebarActive" value="catalogos" scope="request" />
    <c:set var="sidebarComunidadesEnabled" value="false" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${sessionScope.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Catálogos</h2>
              <p class="page-subtitle">Consulta el contenido del repositorio de Age of Sigmar 4.ª edición actualizado desde BSData.</p>
            </div>

            <c:if test="${not empty paginaCatalogoAos.errorCatalogo}">
              <div class="error-box">
                <c:out value="${paginaCatalogoAos.errorCatalogo}" />. Se muestran los últimos datos cargados si existen.
              </div>
            </c:if>

            <div class="filter-panel">
              <form id="filtroCatalogoForm" class="filter-form" method="get" accept-charset="UTF-8" action="/catalogo-aos">
                <div>
                  <label for="faccion">Facción</label>
                  <select id="faccion" name="faccion">
                    <option value="">Selecciona una facción</option>
                    <c:forEach var="faccion" items="${paginaCatalogoAos.facciones}">
                      <option value="<c:out value='${faccion.nombre}'/>" <c:if test="${faccion.nombre eq paginaCatalogoAos.faccionSeleccionada}">selected</c:if>>
                        <c:out value="${faccion.nombre}" />
                      </option>
                    </c:forEach>
                  </select>
                </div>

                <div>
                  <label for="ejercito">Ejército</label>
                  <select id="ejercito" name="ejercito" <c:if test="${empty paginaCatalogoAos.ejercitosDisponibles}">disabled</c:if>>
                    <option value="">Selecciona un ejército</option>
                    <c:forEach var="ejercito" items="${paginaCatalogoAos.ejercitosDisponibles}">
                      <option value="<c:out value='${ejercito.nombre}'/>" <c:if test="${ejercito.nombre eq paginaCatalogoAos.ejercitoSeleccionado}">selected</c:if>>
                        <c:out value="${ejercito.nombre}" />
                      </option>
                    </c:forEach>
                  </select>
                </div>

                <button class="button-primary" type="submit">Ver unidades</button>
                <fieldset class="unit-filters">
                  <legend>Filtrar unidades</legend>
                  <div class="unit-search"><label for="buscarUnidad">Nombre de la unidad</label><input id="buscarUnidad" name="buscarUnidad" type="search" placeholder="Busca una unidad…" value="<c:out value='${param.buscarUnidad}' />" /></div>
                  <div class="filter-toggles">
                    <label><input type="checkbox" name="ocultarLegends" id="ocultarLegends" value="true" <c:if test="${param.ocultarLegends eq 'true'}">checked</c:if> /> Ocultar Legends</label>
                    <label><input type="checkbox" name="ocultarAliados" id="ocultarAliados" value="true" <c:if test="${param.ocultarAliados eq 'true'}">checked</c:if> /> Ocultar aliados</label>
                    <label><input type="checkbox" name="ocultarEstructuras" id="ocultarEstructuras" value="true" <c:if test="${param.ocultarEstructuras eq 'true'}">checked</c:if> /> Ocultar estructuras</label>
                    <button class="filter-reset" id="limpiarFiltrosUnidades" type="button">Limpiar filtros de unidades</button>
                  </div>
                  <p class="filter-help">Los filtros se combinan. Estructuras incluye fortificaciones y terreno de facción.</p>
                </fieldset>
              </form>
            </div>

            <c:choose>
              <c:when test="${not empty paginaCatalogoAos.ejercito}">
                <div class="chip-row">
                  <span class="chip"><c:out value="${paginaCatalogoAos.ejercito.faccion}" /></span>
                  <span class="chip"><c:out value="${paginaCatalogoAos.ejercito.nombre}" /></span>
                  <span class="chip" id="recuentoUnidades" role="status" aria-live="polite"><c:out value="${paginaCatalogoAos.ejercito.totalUnidades}" /> unidades</span>
                </div>

                <div class="catalog-table-scroll" role="region" aria-label="Unidades del catálogo" tabindex="0"><table class="data-table" id="unidadesCatalogo">
                  <thead>
                    <tr>
                      <th scope="col">Nombre</th>
                      <th scope="col">Tipo / rol</th>
                      <th scope="col">Palabras clave</th>
                      <th scope="col">Puntos</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="unidad" items="${paginaCatalogoAos.ejercito.unidades}">
                      <c:url var="detalleUnidadUrl" value="/infoUnidadAos">
                        <c:param name="faccion" value="${paginaCatalogoAos.ejercito.faccion}" />
                        <c:param name="ejercito" value="${paginaCatalogoAos.ejercito.nombre}" />
                        <c:param name="unidad" value="${unidad.nombre}" />
                      </c:url>
                      <tr data-unidad data-nombre="<c:out value='${unidad.nombre}' />" data-legends="${unidad.clasificacion.legends}" data-aliado="${unidad.clasificacion.aliado}" data-estructura="${unidad.clasificacion.estructura}">
                        <td>
                          <a class="link-inline detalle-unidad" href="<c:out value='${detalleUnidadUrl}' />">
                            <c:out value="${unidad.nombre}" />
                          </a><span class="unit-tags"><c:if test="${unidad.clasificacion.legends}"><span>Legends</span></c:if><c:if test="${unidad.clasificacion.aliado}"><span>Aliado</span></c:if><c:if test="${unidad.clasificacion.estructura}"><span>Estructura</span></c:if></span>
                        </td>
                        <td><c:out value="${unidad.roles}" /></td>
                        <td>
                          <c:if test="${not empty unidad.palabrasClaveFaccion}"><c:out value="${unidad.palabrasClaveFaccion}" /> · </c:if>
                          <c:out value="${unidad.palabrasClave}" />
                        </td>
                        <td><c:out value="${unidad.puntos}" /></td>
                      </tr>
                    </c:forEach>
                    <tr id="sinUnidadesFiltradas" hidden><td colspan="4"><div class="catalog-empty"><strong>No hay unidades que coincidan</strong><p>Prueba otro nombre o desactiva alguno de los filtros.</p></div></td></tr>
                  </tbody>
                </table></div>
              </c:when>
              <c:otherwise>
                <div class="note-box">Selecciona una facción y un ejército para ver sus unidades.</div>
              </c:otherwise>
            </c:choose>
          </section>
        </main>
      </div>
    </div>

    <script src="/js/catalogo-40k.js"></script>
  </body>
</html>
