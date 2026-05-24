<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Seleccionar catalogo</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .selector-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
        gap: 18px;
        margin-top: 24px;
      }

      .selector-card {
        padding: 24px;
        border: 1px solid var(--line);
        border-radius: 20px;
        background: var(--panel-muted);
      }

      .selector-card h3 {
        margin: 0 0 12px;
        font-size: 1.7rem;
      }

      .selector-card p {
        margin: 0 0 18px;
        color: var(--muted);
      }
    </style>
  </head>
  <body>
    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <h1 class="brand-title">Tabletop Manager</h1>
          <p class="brand-subtitle">Gestor de Wargames</p>
        </div>

        <nav class="sidebar-nav">
          <a class="sidebar-link" href="/menu-principal">Menu</a>
          <a class="sidebar-link active" href="/catalogos">Catalogos</a>
          <a class="sidebar-link" href="/mis-listas-40k">Listas</a>
          <span class="sidebar-link disabled">Partidas</span>
          <span class="sidebar-link disabled">Comunidades</span>
          <span class="sidebar-link disabled">Estadisticas</span>
          <span class="sidebar-link disabled">Ajustes</span>
        </nav>

        <div class="sidebar-footer">
          TFG Enrique Silveira García<br />
          Versión de prueba del tfg
        </div>
      </aside>

      <div class="app-main">
        <header class="profile-bar">
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${sessionScope.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Seleccionar juego</h2>
              <p class="page-subtitle">Elige primero el sistema y después accederás a su filtro de facción y ejército.</p>
            </div>

            <div class="selector-grid">
              <article class="selector-card">
                <h3>Warhammer 40.000</h3>
                <p>Catálogo y unidades de 10ª edición con su buscador de facciones y ejércitos.</p>
                <a class="button-primary" href="/catalogo40k">Ir al catálogo de 40k</a>
              </article>

              <article class="selector-card">
                <h3>Age of Sigmar</h3>
                <p>Catálogo de 4ª edición con selección previa de facción y ejército.</p>
                <a class="button-primary" href="/catalogo-aos">Ir al catálogo de AoS</a>
              </article>
            </div>
          </section>
        </main>
      </div>
    </div>
  </body>
</html>
