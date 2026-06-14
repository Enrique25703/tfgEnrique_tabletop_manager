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
      .dashboard-grid {
        display: grid;
        grid-template-columns: 1.35fr 1fr;
        gap: 18px;
        margin-top: 24px;
      }

      .hero-card,
      .feature-card {
        position: relative;
        overflow: hidden;
        min-height: 240px;
        padding: 24px;
        border: 1px solid var(--line);
        border-radius: 22px;
        background: rgba(17, 26, 39, 0.9);
      }

      .hero-card {
        min-height: 498px;
      }

      .feature-grid {
        display: grid;
        grid-template-columns: 1fr;
        gap: 18px;
      }

      .feature-card.catalogos {
        background: rgba(16, 30, 22, 0.92);
        border-color: rgba(97, 139, 89, 0.26);
      }

      .feature-card.listas {
        background: rgba(15, 24, 37, 0.92);
        border-color: rgba(79, 127, 184, 0.28);
      }

      .feature-card.comunidades {
        min-height: 170px;
        background: rgba(28, 20, 39, 0.92);
        border-color: rgba(111, 92, 146, 0.28);
      }

      .feature-card.admin {
        min-height: 170px;
        background: rgba(39, 20, 20, 0.92);
        border-color: rgba(180, 94, 94, 0.3);
      }

      .card-kicker {
        margin: 0 0 10px;
        color: var(--accent-2);
        font-size: 0.88rem;
        letter-spacing: 0.12em;
        text-transform: uppercase;
      }

      .card-title {
        margin: 0;
        font-size: 2.9rem;
        line-height: 0.95;
      }

      .feature-card .card-title {
        font-size: 2.1rem;
      }

      .card-copy {
        max-width: 480px;
        margin-top: 16px;
        color: #d7e5fb;
        font-size: 1.04rem;
      }

      .card-actions {
        display: flex;
        gap: 12px;
        flex-wrap: wrap;
        margin-top: 24px;
      }

      .card-actions button,
      .card-actions a {
        min-width: 170px;
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

      .popup.visible {
        display: block;
      }

      .popup-acciones {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
        margin-top: 14px;
      }

      .profile-bar-actions {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        gap: 14px;
        width: 100%;
      }

      .profile-shortcut {
        display: inline-flex;
        align-items: center;
        gap: 14px;
        padding: 10px 14px;
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
        width: 52px;
        height: 52px;
        border-radius: 16px;
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
          min-height: 380px;
        }
      }
    </style>
  </head>
  <body>
    <c:set var="sidebarActive" value="menu" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <c:set var="fotoPerfilMenu" value="${empty sessionScope.fotoUrl ? '/images/default-avatar.svg' : sessionScope.fotoUrl}" />
    <div class="app-shell">
      <jsp:include page="header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
          <div class="profile-bar-actions">
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
                    <button type="button" class="button-secondary" id="abrirPopupCreador">Crear lista</button>
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

            <div class="popup-acciones">
              <button type="button" class="button-primary" id="mostrarFormulario40k">Warhammer 40.000 10Âº edicion</button>
              <button type="button" class="button-secondary" id="mostrarFormularioAos">Age of Sigmar 4Âº edicion</button>
              <button type="button" class="button-secondary" id="cerrarPopupCreador">Cerrar</button>
            </div>

            <form id="form40k" class="linea" action="/creador-listas-40k" method="get" style="display:none;">
              <input type="hidden" name="formatoJuego" value="WH40K_10" />
              <div class="linea">
                <label for="faccion">Faccion</label>
                <select id="faccion" name="faccion" required>
                  <option value="">Selecciona una faccion</option>
                  <c:forEach var="faccion" items="${menuPrincipal.facciones}">
                    <option value="<c:out value='${faccion.nombre}'/>"><c:out value="${faccion.nombre}" /></option>
                  </c:forEach>
                </select>
              </div>

              <div class="linea">
                <label for="ejercito">Ejercito</label>
                <select id="ejercito" name="ejercito" required disabled>
                  <option value="">Selecciona un ejercito</option>
                </select>
              </div>

              <div class="linea">
                <label for="nombreLista">Nombre de la lista</label>
                <input id="nombreLista" name="nombreLista" type="text" />
              </div>

              <div class="linea">
                <button id="crearLista40k" class="button-primary" type="submit">Crear</button>
              </div>
            </form>

            <form id="formAos" class="linea" action="/creador-listas-aos" method="get" style="display:none;">
              <input type="hidden" name="formatoJuego" value="AOS_4" />
              <div class="linea">
                <label for="faccionAos">Faccion</label>
                <select id="faccionAos" name="faccion" required>
                  <option value="">Selecciona una faccion</option>
                  <c:forEach var="faccion" items="${menuPrincipalAos.facciones}">
                    <option value="<c:out value='${faccion.nombre}'/>"><c:out value="${faccion.nombre}" /></option>
                  </c:forEach>
                </select>
              </div>

              <div class="linea">
                <label for="ejercitoAos">Ejercito</label>
                <select id="ejercitoAos" name="ejercito" required disabled>
                  <option value="">Selecciona un ejercito</option>
                </select>
              </div>

              <div class="linea">
                <label for="nombreListaAos">Nombre de la lista</label>
                <input id="nombreListaAos" name="nombreLista" type="text" />
              </div>

              <div class="linea">
                <button id="crearListaAos" class="button-primary" type="submit">Crear lista de AoS</button>
              </div>
            </form>

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

    <script src="/js/menu-principal.js"></script>
  </body>
</html>
