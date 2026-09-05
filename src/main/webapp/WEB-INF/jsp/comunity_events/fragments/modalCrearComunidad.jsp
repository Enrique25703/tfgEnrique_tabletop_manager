<div id="modalCrearComunidad" class="map-modal" hidden aria-hidden="true">
  <div class="map-modal-backdrop" data-close-community-modal></div>
  <section class="community-dialog" role="dialog" aria-modal="true" aria-labelledby="tituloCrearComunidad">
    <header>
      <div>
        <p class="map-kicker">Nueva comunidad</p>
        <h2 id="tituloCrearComunidad">Crear comunidad</h2>
        <p>Configura los datos iniciales y selecciona una imagen.</p>
      </div>
      <button type="button" class="map-close" data-close-community-modal aria-label="Cerrar">×</button>
    </header>
    <form action="/comunidades/crear" method="post" class="community-create-form">
      <input type="hidden" name="origen" value="${pestanaComunidades}" />
      <label for="nombreComunidad">Nombre
        <input id="nombreComunidad" name="nombreComunidad" type="text" maxlength="120" required />
      </label>
      <label for="descripcionComunidad">Descripción
        <textarea id="descripcionComunidad" name="descripcion" rows="5" maxlength="2000" placeholder="Describe el propósito y la actividad de la comunidad"></textarea>
      </label>
      <jsp:include page="/WEB-INF/jsp/comunity_events/fragments/selectorImagenComunidad.jsp">
        <jsp:param name="selectorId" value="crear" />
      </jsp:include>
      <div class="dialog-actions">
        <button class="button-primary" type="submit">Crear comunidad</button>
        <button class="button-secondary" type="button" data-close-community-modal>Cancelar</button>
      </div>
    </form>
  </section>
</div>
