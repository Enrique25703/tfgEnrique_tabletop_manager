<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Nueva partida</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .match-card {
        max-width: 720px;
        padding: 24px;
        border: 1px solid var(--line);
        border-radius: 22px;
        background: var(--panel-soft);
      }

      .form-grid {
        display: grid;
        gap: 16px;
        margin-top: 20px;
      }

      .disabled-note {
        color: var(--muted);
        font-size: 0.92rem;
      }
      #estiloJuegoBloque[hidden], #notaAos[hidden] { display: none; }
    </style>
    <link rel="stylesheet" href="/css/partida-creacion.css" />
  </head>
  <body class="setup-page setup-new">
    <c:set var="sidebarActive" value="partidas" scope="request" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${partidaNueva.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Crear partida</h2>
              <p class="page-subtitle">Elige Warhammer 40k o Age of Sigmar para empezar.</p>
            </div>

            <c:if test="${not empty mensajeError}">
              <div class="error-box"><c:out value="${mensajeError}" /></div>
            </c:if>

            <form class="match-card" method="post" action="/partidas/crear">
              <div class="form-grid">
                <label>
                  Juego
                  <select name="sistemaJuego" id="sistemaJuego" required>
                    <option value="WH40K_11">Warhammer 40.000 11a edicion</option>
                    <option value="AOS_4">Age of Sigmar 4a edicion</option>
                  </select>
                </label>

                <label id="estiloJuegoBloque">
                  Estilo de juego
                  <select name="estiloJuego" id="estiloJuego" required>
                    <option value="EQUILIBRADO">Juego equilibrado</option>
                    <option value="ASIMETRICO">Juego asimetrico</option>
                  </select>
                </label>

                <p class="disabled-note" id="notaAos" hidden>Age of Sigmar permite elegir entre cinco despliegues.</p>

                <div class="form-actions">
                  <button class="button-primary" type="submit">Crear partida</button>
                  <a class="button-secondary" href="/partidas">Volver al historial</a>
                </div>
              </div>
            </form>
          </section>
        </main>
      </div>
    </div>
    <script>
      (function () {
        const juego = document.getElementById('sistemaJuego');
        const estilo = document.getElementById('estiloJuego');
        function actualizarJuego() {
          const aos = juego.value === 'AOS_4';
          document.getElementById('estiloJuegoBloque').hidden = aos;
          document.getElementById('notaAos').hidden = !aos;
          estilo.disabled = aos;
          if (aos) estilo.value = 'EQUILIBRADO';
        }
        juego.addEventListener('change', actualizarJuego);
        actualizarJuego();
      })();
    </script>
  </body>
</html>
