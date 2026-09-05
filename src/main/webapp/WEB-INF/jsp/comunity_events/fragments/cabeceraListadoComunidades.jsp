<header class="communities-heading">
  <h1 class="page-title">Comunidades</h1>
  <p class="page-subtitle">Conecta con otros jugadores, participa en eventos y descubre nuevas comunidades.</p>
</header>

<div class="community-tabs-row">
  <nav class="community-tabs" aria-label="Secciones de comunidades">
    <a class="community-tab${pestanaComunidades eq 'eventos' ? ' active' : ''}" href="/comunidades" ${pestanaComunidades eq 'eventos' ? 'aria-current="page"' : ''}>
       Eventos cercanos
    </a>
    <a class="community-tab${pestanaComunidades eq 'mis-comunidades' ? ' active' : ''}" href="/comunidades/mis-comunidades" ${pestanaComunidades eq 'mis-comunidades' ? 'aria-current="page"' : ''}>
       Mis comunidades
    </a>
    <a class="community-tab${pestanaComunidades eq 'descubrir' ? ' active' : ''}" href="/comunidades/descubrir" ${pestanaComunidades eq 'descubrir' ? 'aria-current="page"' : ''}>
      </span> Descubrir comunidades
    </a>
  </nav>
  <button class="button-primary create-community-button" type="button" id="abrirCrearComunidad">+ Crear comunidad</button>
</div>
