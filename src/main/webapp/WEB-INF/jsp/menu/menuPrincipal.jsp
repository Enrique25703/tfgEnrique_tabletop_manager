<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Menu principal</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .menu-screen .page-panel {
        display: flex;
        flex-direction: column;
        overflow: hidden;
      }

      .dashboard-grid {
        display: grid;
        flex: 1 1 auto;
        min-height: 0;
        grid-template-columns: 1.35fr 1fr;
        gap: 14px;
        margin-top: 10px;
      }

      .hero-card,
      .feature-card {
        position: relative;
        isolation: isolate;
        overflow: hidden;
        min-height: 180px;
        padding: 18px;
        border: 1px solid var(--line);
        border-radius: 22px;
        background: rgba(17, 26, 39, 0.9);
      }

      .hero-card {
        min-height: 0;
      }

      .feature-grid {
        display: grid;
        grid-template-columns: 1fr;
        gap: 14px;
      }

      .feature-card.catalogos {
        --feature-art: url("/menupicks/catalogo.png");
        --feature-art-position: 84% 42%;
        --feature-art-size: auto 170%;
        background: rgba(16, 30, 22, 0.92);
        border-color: rgba(97, 139, 89, 0.26);
      }

      .feature-card.listas {
        --feature-art: url("/menupicks/listaspick.png");
        --feature-art-position: 64% center;
        --feature-art-size: cover;
        background: rgba(15, 24, 37, 0.92);
        border-color: rgba(79, 127, 184, 0.28);
      }

      .feature-card.comunidades {
        min-height: 0;
        --feature-art: url("/menupicks/comunidades.png");
        --feature-art-position: 72% 30%;
        --feature-art-size: cover;
        background: rgba(28, 20, 39, 0.92);
        border-color: rgba(111, 92, 146, 0.28);
      }

      .feature-card.admin {
        min-height: 0;
        background: rgba(39, 20, 20, 0.92);
        border-color: rgba(180, 94, 94, 0.3);
      }

      .feature-card::before,
      .feature-card::after {
        content: "";
        position: absolute;
        inset: 0;
        pointer-events: none;
      }

      .feature-card::before {
        z-index: 0;
        background-image: var(--feature-art);
        background-repeat: no-repeat;
        background-position: var(--feature-art-position, center);
        background-size: var(--feature-art-size, cover);
        opacity: 0.92;
      }

      .feature-card::after {
        z-index: 1;
        background:
          linear-gradient(90deg, rgba(8, 13, 21, 0.98) 0%, rgba(8, 13, 21, 0.94) 34%, rgba(8, 13, 21, 0.62) 58%, rgba(8, 13, 21, 0.18) 100%),
          linear-gradient(180deg, rgba(8, 13, 21, 0.14) 0%, rgba(8, 13, 21, 0.38) 100%);
      }

      .feature-card > * {
        position: relative;
        z-index: 2;
      }

      .card-kicker {
        margin: 0 0 6px;
        color: var(--accent-2);
        font-size: 0.78rem;
        letter-spacing: 0.12em;
        text-transform: uppercase;
      }

      .card-title {
        margin: 0;
        font-size: 2.3rem;
        line-height: 0.95;
      }

      .feature-card .card-title {
        font-size: 1.65rem;
      }

      .card-copy {
        max-width: 480px;
        margin-top: 10px;
        color: #d7e5fb;
        font-size: 0.95rem;
      }

      .card-actions {
        display: flex;
        gap: 12px;
        flex-wrap: wrap;
        margin-top: 14px;
      }

      .card-actions button,
      .card-actions a {
        min-width: 140px;
        padding: 10px 14px;
      }

      .stats-strip {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 14px;
        margin-top: 20px;
      }

      .stat-card {
        padding: 18px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: var(--panel-muted);
      }

      .stat-value {
        margin: 0 0 6px;
        font-size: 2rem;
        font-weight: 700;
      }

      .stat-label {
        margin: 0;
        color: var(--muted);
      }

      .popup {
        display: none;
        position: fixed;
        top: 50%;
        left: 50%;
        z-index: 20;
        width: min(92vw, 520px);
        padding: 22px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: var(--panel-soft);
        box-shadow: 0 24px 80px rgba(0, 0, 0, 0.42);
        transform: translate(-50%, -50%);
      }

      .popup.visible,
      .popup:target {
        display: block;
      }

      .popup-body {
        display: grid;
        gap: 16px;
      }

      .game-picker {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 12px;
      }

      .game-option {
        display: grid;
        gap: 6px;
        padding: 16px;
        border: 1px solid var(--line);
        border-radius: 16px;
        background: rgba(13, 21, 33, 0.92);
        text-align: left;
        cursor: pointer;
        transition: border-color .18s ease, transform .18s ease, background .18s ease;
      }

      .game-option:hover,
      .game-option.is-selected {
        border-color: rgba(116, 159, 224, 0.85);
        background: rgba(19, 31, 48, 0.96);
        transform: translateY(-1px);
      }

      .game-option-title {
        margin: 0;
        font-size: 1rem;
        font-weight: 700;
      }

      .game-option-copy {
        margin: 0;
        color: var(--muted);
        font-size: 0.92rem;
      }

      .creator-flow {
        display: grid;
        gap: 14px;
        padding-top: 6px;
        border-top: 1px solid rgba(116, 159, 224, 0.14);
      }

      .creator-flow[hidden] {
        display: none;
      }

      .creator-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 14px;
      }

      .creator-field {
        display: grid;
        gap: 8px;
      }

      .helper-copy {
        margin: 0;
        color: var(--muted);
        font-size: 0.9rem;
      }

      .popup-acciones {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
        justify-content: flex-end;
      }

      .profile-bar-actions {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        gap: 10px;
        width: 100%;
      }

      .profile-shortcut {
        display: inline-flex;
        align-items: center;
        gap: 10px;
        padding: 8px 12px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: var(--panel-soft);
        color: inherit;
        text-decoration: none;
      }

      .profile-shortcut:hover {
        border-color: rgba(116, 159, 224, 0.8);
      }

      .profile-shortcut-avatar {
        width: 42px;
        height: 42px;
        border-radius: 14px;
        object-fit: cover;
        border: 1px solid var(--line);
        background: rgba(14, 22, 34, 0.72);
      }

      .profile-shortcut-copy {
        display: grid;
        gap: 4px;
      }

      .profile-shortcut-copy .profile-title,
      .profile-shortcut-copy .profile-role {
        margin: 0;
      }

      .logout-form {
        margin: 0;
      }

      .linea {
        margin-top: 14px;
      }

      @media (max-width: 1100px) {
        .dashboard-grid,
        .stats-strip {
          grid-template-columns: 1fr;
        }

        .hero-card {
          min-height: 260px;
        }
      }

      @media (max-width: 720px) {
        .game-picker,
        .creator-grid {
          grid-template-columns: 1fr;
        }
      }
    </style>
  </head>
  <body class="menu-screen">
    <c:set var="sidebarActive" value="menu" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <c:set var="fotoPerfilMenu" value="${empty sessionScope.fotoUrl ? '/images/default-avatar.svg' : sessionScope.fotoUrl}" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
          <div class="profile-bar-actions">
            <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
            <a class="profile-shortcut" href="/ajustes" aria-label="Ir a ajustes de perfil">
              <img class="profile-shortcut-avatar" src="<c:out value='${fotoPerfilMenu}' />" alt="Foto de perfil" />
              <div class="profile-shortcut-copy">
                <p class="profile-title">Mi perfil</p>
                <p class="profile-role"><c:out value="${menuPrincipal.nombreUsuario}" /></p>
              </div>
            </a>

            <form class="logout-form" method="post" action="/logout">
              <button class="button-secondary" type="submit">Cerrar sesion</button>
            </form>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-subtitle">Bienvenido, <c:out value="${menuPrincipal.nombreUsuario}" />.</h2>
            </div>

            <c:if test="${not empty menuPrincipal.errorCatalogo}">
              <div class="error-box"><c:out value="${menuPrincipal.errorCatalogo}" /></div>
            </c:if>

            <div class="dashboard-grid">
              <article class="hero-card">
                <p class="card-kicker">Accion principal</p>
                <h3 class="card-title">EMPEZAR<br />PARTIDA</h3>
                <p class="card-copy">Crea una partida de 40k, configura jugadores, layout, rondas y guarda el resultado final.</p>
                <div class="card-actions">
                  <a class="button-primary" href="/partidas">Empezar partida</a>
                </div>
              </article>

              <div class="feature-grid">
                <article class="feature-card catalogos">
                  <p class="card-kicker">Consulta</p>
                  <h3 class="card-title">Ver catalogos</h3>
                  <p class="card-copy">Explora facciones, ejercitos y unidades disponibles en el catalogo de 40k.</p>
                  <div class="card-actions">
                    <a class="button-primary" href="/catalogos">Elegir juego</a>
                  </div>
                </article>

                <article class="feature-card listas">
                  <p class="card-kicker">Gestion</p>
                  <h3 class="card-title">Gestionar listas</h3>
                  <p class="card-copy">Consulta tus listas guardadas y crea una nueva desde el creador cuando ya tengas faccion y ejercito.</p>
                  <div class="card-actions">
                    <a class="button-primary" href="/mis-listas">Ver mis listas</a>
                    <a class="button-secondary" id="abrirPopupCreador" href="#popupCreador">Crear lista</a>
                  </div>
                </article>

                <article class="feature-card comunidades">
                  <p class="card-kicker">Social</p>
                  <h3 class="card-title">Gestionar comunidades</h3>
                  <p class="card-copy">Crea comunidades, unete a otras, organiza eventos si eres propietario y publica invitaciones de partida si eres usuario normal.</p>
                  <div class="card-actions">
                    <a class="button-secondary" href="/comunidades">Abrir comunidades</a>
                  </div>
                </article>

                <c:if test="${not empty sessionScope.rol and fn:toUpperCase(sessionScope.rol) eq 'ADMIN'}">
                  <article class="feature-card admin">
                    <p class="card-kicker">Administracion</p>
                    <h3 class="card-title">ADMIN</h3>
                    <p class="card-copy">Gestiona usuarios, comunidades y miembros desde el panel administrativo.</p>
                    <div class="card-actions">
                      <a class="button-secondary" href="/admin">Abrir panel</a>
                    </div>
                  </article>
                </c:if>
              </div>
            </div>
          </section>

          <div id="popupCreador" class="popup">
            <p class="page-title" style="font-size:1.35rem; margin-bottom:6px;">Crear lista</p>
            <p class="page-subtitle">Selecciona el juego y define la faccion antes de entrar al creador.</p>

            <div class="popup-body">
              <div class="game-picker" aria-label="Seleccionar juego">
                <button type="button" class="game-option" data-game-choice="WH40K_11">
                  <span class="game-option-title">Warhammer 40.000</span>
                  <span class="game-option-copy">11a edicion</span>
                </button>
                <button type="button" class="game-option" data-game-choice="AOS_4">
                  <span class="game-option-title">Age of Sigmar</span>
                  <span class="game-option-copy">4a edicion</span>
                </button>
              </div>

              <form id="formCrearLista" class="creator-flow" method="get" hidden>
                <input type="hidden" id="formatoJuegoSeleccionado" name="formatoJuego" value="" />
                <input type="hidden" id="ejercitoSeleccionado" name="ejercito" value="" />

                <p id="resumenJuegoSeleccionado" class="helper-copy">Selecciona un juego para continuar.</p>

                <div class="creator-grid">
                  <div class="creator-field">
                    <label for="faccionSelector">Faccion</label>
                    <select id="faccionSelector" name="faccion" required>
                      <option value="">Selecciona una faccion</option>
                    </select>
                  </div>

                  <div id="bloqueEjercito" class="creator-field" hidden>
                    <label for="ejercitoSelector">Ejercito</label>
                    <select id="ejercitoSelector">
                      <option value="">Selecciona un ejercito</option>
                    </select>
                  </div>

                  <div class="creator-field">
                    <label for="nombreListaNuevo">Nombre de la lista</label>
                    <input id="nombreListaNuevo" name="nombreLista" type="text" maxlength="120" required />
                  </div>

                  <div class="creator-field">
                    <label for="limitePuntosNuevo">Limite de puntos</label>
                    <select id="limitePuntosNuevo" name="limitePuntos" required>
                      <option value="1000">1000 puntos</option>
                      <option value="1500">1500 puntos</option>
                      <option value="2000" selected>2000 puntos</option>
                      <option value="3000">3000 puntos</option>
                    </select>
                  </div>
                </div>

                <div class="popup-acciones">
                  <a class="button-secondary" id="cerrarPopupCreadorNuevo" href="#">Cerrar</a>
                  <button id="crearListaSubmit" class="button-primary" type="submit">Crear lista</button>
                </div>
              </form>
            </div>

            <select id="ejercitosPlantilla" hidden>
              <c:forEach var="faccion" items="${menuPrincipal.facciones}">
                <c:forEach var="ejercito" items="${faccion.ejercitos}">
                  <option
                    data-faccion="<c:out value='${faccion.nombre}'/>"
                    value="<c:out value='${ejercito}'/>"><c:out value="${ejercito}" /></option>
                </c:forEach>
              </c:forEach>
            </select>

            <select id="ejercitosPlantillaAos" hidden>
              <c:forEach var="faccion" items="${menuPrincipalAos.facciones}">
                <c:forEach var="ejercito" items="${faccion.ejercitos}">
                  <option
                    data-faccion="<c:out value='${faccion.nombre}'/>"
                    value="<c:out value='${ejercito}'/>"><c:out value="${ejercito}" /></option>
                </c:forEach>
              </c:forEach>
            </select>
          </div>
        </main>
      </div>
    </div>

    <script src="/js/menu-principal.js?v=2"></script>
  </body>
</html>
