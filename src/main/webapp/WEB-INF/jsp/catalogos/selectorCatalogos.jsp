<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Seleccionar catálogo</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <link rel="stylesheet" href="/css/catalogos.css" />
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
              <h2 class="page-title">Seleccionar juego</h2>
              <p class="page-subtitle">¿Qué vamos a jugar hoy, comandante?</p>
            </div>

            <div class="selector-grid">
              <article class="selector-card">
                <h3>Warhammer 40.000</h3>
                <a class="button-primary" href="/catalogo40k">Ir al catálogo de 40k</a>
              </article>

              <article class="selector-card">
                <h3>Age of Sigmar</h3>
                <a class="button-primary" href="/catalogo-aos">Ir al catálogo de AoS</a>
              </article>
            </div>
          </section>
        </main>
      </div>
    </div>
  </body>
</html>
