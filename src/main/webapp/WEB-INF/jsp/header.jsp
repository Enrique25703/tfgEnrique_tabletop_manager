<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="sidebar">
  <div class="brand">
    <h1 class="brand-title">Tabletop Manager</h1>
    <p class="brand-subtitle">Gestor de Wargames</p>
  </div>

  <nav class="sidebar-nav">
    <a class="sidebar-link<c:if test="${sidebarActive eq 'menu'}"> active</c:if>" href="/menu-principal">Menu</a>
    <a class="sidebar-link<c:if test="${sidebarActive eq 'catalogos'}"> active</c:if>" href="/catalogos">Catalogos</a>
    <a class="sidebar-link<c:if test="${sidebarActive eq 'listas'}"> active</c:if>" href="/mis-listas">Listas</a>
    <a class="sidebar-link<c:if test="${sidebarActive eq 'partidas'}"> active</c:if>" href="/partidas">Partidas</a>
    <c:choose>
      <c:when test="${sidebarComunidadesEnabled}">
        <a class="sidebar-link<c:if test="${sidebarActive eq 'comunidades'}"> active</c:if>" href="/comunidades">Comunidades</a>
      </c:when>
      <c:otherwise>
        <span class="sidebar-link disabled">Comunidades</span>
      </c:otherwise>
    </c:choose>
    <a class="sidebar-link<c:if test="${sidebarActive eq 'estadisticas'}"> active</c:if>" href="/estadisticas">Estadisticas</a>
    <a class="sidebar-link<c:if test="${sidebarActive eq 'ajustes'}"> active</c:if>" href="/ajustes">Ajustes</a>
  </nav>

  <div class="sidebar-footer">Enrique Silveira Garcia<br />Tabletop Manager v0.0.1</div>
</aside>
