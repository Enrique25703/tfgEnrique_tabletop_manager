<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<section class="community-image-selector" data-community-image-selector>
  <input type="hidden" name="logoUrl" value="<c:out value='${param.logoActual}' />" data-community-image-input />
  <div class="community-image-preview">
    <img src="<c:out value='${empty param.logoActual ? "/images/default-community.svg" : param.logoActual}' />" alt="Vista previa de la comunidad" data-community-image-preview />
    <div><strong>Imagen de la comunidad</strong><small data-community-image-label><c:out value="${empty param.logoActual ? 'Imagen genérica' : 'Imagen actual'}" /></small></div>
  </div>
  <div class="community-image-actions">
    <button class="button-secondary" type="button" data-open-community-images>Seleccionar imagen</button>
    <button class="button-secondary" type="button" data-clear-community-image>Usar imagen genérica</button>
  </div>

  <dialog class="community-image-dialog" data-community-image-dialog aria-labelledby="titulo-imagen-${param.selectorId}">
    <div class="community-image-dialog-header">
      <div><h3 id="titulo-imagen-${param.selectorId}">Seleccionar imagen de comunidad</h3><p>Elige una imagen de `comunitiespicks`.</p></div>
      <button class="button-secondary" type="button" data-close-community-images>Cerrar</button>
    </div>
    <div class="community-image-catalog" data-community-image-catalog>
      <button type="button" class="community-image-choice" data-value="" data-preview="/images/default-community.svg" data-label="Imagen genérica"><img src="/images/default-community.svg" alt="Imagen genérica" /><span>Imagen genérica</span></button>
      <c:forEach var="imagen" items="${imagenesComunidad}">
        <button type="button" class="community-image-choice" data-value="${imagen.url}" data-preview="${imagen.url}" data-label="${imagen.etiqueta}"><img src="${imagen.url}" alt="<c:out value='${imagen.etiqueta}' />" /></button>
      </c:forEach>
    </div>
    <div class="community-image-dialog-actions"><button class="button-primary" type="button" data-confirm-community-image>Confirmar selección</button></div>
  </dialog>
</section>
<script src="/js/comunidad-imagenes.js?v=1" defer></script>
