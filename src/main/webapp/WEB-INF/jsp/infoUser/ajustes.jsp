<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Ajustes</title>
    <link rel="stylesheet" href="/css/app-shell.css" />
    <style>
      .settings-layout {
        display: grid;
        grid-template-columns: 300px minmax(0, 1fr);
        gap: 18px;
        align-items: start;
      }

      .profile-card-panel,
      .settings-form-card,
      .password-card {
        padding: 22px;
        border: 1px solid var(--line);
        border-radius: 20px;
        background: var(--panel-soft);
      }

      .profile-card-panel {
        display: grid;
        gap: 16px;
        align-content: start;
        align-self: start;
        max-width: 300px;
      }

      .avatar-shell {
        width: 100%;
        aspect-ratio: 1;
        border-radius: 22px;
        overflow: hidden;
        border: 1px solid var(--line);
        background: rgba(14, 22, 34, 0.72);
      }

      .avatar-shell img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }

      .settings-stack {
        display: grid;
        gap: 18px;
      }

      [hidden] {
        display: none !important;
      }

      .profile-details {
        display: grid;
        gap: 20px;
        margin: 0 0 24px;
      }

      .profile-details dt {
        color: var(--muted);
        margin-bottom: 6px;
      }

      .profile-details dd {
        margin: 0;
        font-size: 1.05rem;
        overflow-wrap: anywhere;
      }

      .form-grid {
        display: grid;
        gap: 16px;
      }

      .field-row {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 16px;
      }

      .field-block {
        display: grid;
        gap: 8px;
      }

      .muted-copy {
        color: var(--muted);
        margin: 0;
      }

      .avatar-actions {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
      }

      .avatar-value {
        color: var(--muted);
        font-size: 0.92rem;
      }

      .password-trigger {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        flex-wrap: wrap;
      }

      .modal-overlay {
        display: none;
        position: fixed;
        inset: 0;
        z-index: 150;
        padding: 28px;
        background: rgba(4, 7, 12, 0.84);
        align-items: center;
        justify-content: center;
      }

      .modal-overlay.visible {
        display: flex;
      }

      .modal-card {
        width: min(1080px, 100%);
        max-height: calc(100vh - 56px);
        display: grid;
        gap: 16px;
        padding: 22px;
        border: 1px solid var(--line-strong);
        border-radius: 24px;
        background: rgba(10, 16, 25, 0.98);
        box-shadow: 0 30px 80px rgba(0, 0, 0, 0.45);
      }

      .password-modal-card {
        width: min(720px, 100%);
      }

      .modal-header {
        display: flex;
        justify-content: space-between;
        gap: 12px;
        align-items: center;
      }

      .avatar-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
        gap: 14px;
        overflow-y: auto;
        padding-right: 4px;
      }

      .avatar-option {
        display: grid;
        gap: 10px;
        padding: 12px;
        border: 1px solid var(--line);
        border-radius: 18px;
        background: rgba(14, 22, 34, 0.6);
        cursor: pointer;
        transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
      }

      .avatar-option:hover {
        transform: translateY(-2px);
        border-color: rgba(116, 159, 224, 0.8);
      }

      .avatar-option.selected {
        border-color: #5b8def;
        box-shadow: 0 0 0 2px rgba(91, 141, 239, 0.32);
      }

      .avatar-option img {
        width: 100%;
        aspect-ratio: 1;
        object-fit: cover;
        border-radius: 14px;
        border: 1px solid rgba(144, 166, 196, 0.18);
        background: rgba(14, 22, 34, 0.75);
      }

      .avatar-option span {
        font-size: 0.88rem;
        color: var(--muted);
        word-break: break-word;
      }

      @media (max-width: 980px) {
        .settings-layout,
        .field-row {
          grid-template-columns: 1fr;
        }

        .profile-card-panel {
          max-width: none;
        }
      }
    </style>
  </head>
  <body>
    <c:set var="sidebarActive" value="ajustes" />
    <c:set var="sidebarComunidadesEnabled" value="true" />
    <div class="app-shell">
      <jsp:include page="/WEB-INF/jsp/header.jsp" />

      <div class="app-main">
        <header class="profile-bar">
          <jsp:include page="/WEB-INF/jsp/notificacionesBell.jsp" />
          <div class="profile-card">
            <p class="profile-title">Mi perfil</p>
            <p class="profile-role"><c:out value="${perfil.nombreUsuario}" /></p>
          </div>
        </header>

        <main class="page-content">
          <section class="page-panel">
            <div class="page-header">
              <h2 class="page-title">Ajustes</h2>
            </div>

            <c:if test="${not empty mensajeOk}">
              <div class="note-box"><c:out value="${mensajeOk}" /></div>
            </c:if>

            <c:if test="${not empty mensajeError}">
              <div class="error-box"><c:out value="${mensajeError}" /></div>
            </c:if>

            <div id="perfilResumen" class="settings-layout">
              <aside class="profile-card-panel">
                <div class="avatar-shell">
                  <img src="<c:out value='${perfil.fotoMostrada}' />" alt="Foto de perfil" />
                </div>
              </aside>

              <section class="settings-form-card" aria-labelledby="perfilResumenTitulo">
                <h3 id="perfilResumenTitulo" tabindex="-1">Datos del perfil</h3>
                <dl class="profile-details">
                  <div>
                    <dt>Nombre de usuario</dt>
                    <dd><c:out value="${perfil.nombreUsuario}" /></dd>
                  </div>
                  <div>
                    <dt>Correo</dt>
                    <dd><c:out value="${perfil.email}" /></dd>
                  </div>
                </dl>
                <button class="button-primary" type="button" id="editarPerfil" aria-controls="perfilFormulario">Editar perfil</button>
              </section>
            </div>

            <form id="perfilFormulario" method="post" action="/ajustes/perfil" data-error="${not empty mensajeError}" hidden>
              <div class="settings-layout">
                <aside class="profile-card-panel">
                  <div class="avatar-shell">
                    <img id="avatarPreview" src="<c:out value='${perfil.fotoMostrada}' />" alt="Foto de usuario" />
                  </div>
                  <div>
                    <h3 style="margin:0 0 8px;">Vista previa</h3>
                  </div>
                </aside>

                <div class="settings-stack">
                  <section class="settings-form-card">
                    <h3>Editar perfil</h3>
                    <div class="form-grid">
                      <div class="field-row">
                        <label>
                          Nombre de usuario
                          <input type="text" name="nombreUsuario" maxlength="50" value="<c:out value='${perfil.nombreUsuario}' />" required />
                        </label>

                        <label>
                          Correo
                          <input type="email" name="email" maxlength="120" value="<c:out value='${perfil.email}' />" required />
                        </label>
                      </div>

                      <div class="field-block">
                        <span>Foto de usuario</span>
                        <input type="hidden" id="fotoUrlInput" name="fotoUrl" value="<c:out value='${perfil.fotoUrl}' />" />
                        <div class="avatar-actions" style="margin-top:10px;">
                          <button class="button-secondary" type="button" id="abrirAvatarModal">Seleccionar foto</button>
                          <button class="button-secondary" type="button" id="limpiarAvatar">Usar por defecto</button>
                        </div>
                        <p class="avatar-value" id="avatarSelectionLabel" style="margin-top:10px;"></p>
                      </div>
                    </div>
                  </section>

                  <section class="password-card">
                    <div class="password-trigger">
                      <div>
                        <h3 style="margin:0 0 8px;">Contraseña</h3>
                        <p class="muted-copy">Cámbiala solo si lo deseas.</p>
                      </div>
                      <button class="button-secondary" type="button" id="abrirPasswordModal">Cambiar contraseña</button>
                    </div>
                  </section>

                  <div class="form-actions">
                    <button class="button-primary" type="submit">Guardar cambios</button>
                    <button class="button-secondary" type="button" id="cancelarPerfil">Cancelar</button>
                  </div>
                </div>
              </div>

              <div class="modal-overlay" id="passwordModal">
                <div class="modal-card password-modal-card">
                  <div class="modal-header">
                    <div>
                      <h3 style="margin:0;">Cambiar contraseña</h3>
                    </div>
                    <button class="button-secondary" type="button" id="cerrarPasswordModal">Cerrar</button>
                  </div>

                  <div class="form-grid">
                    <label>
                      Contraseña actual
                      <input type="password" name="contrasenaActual" id="contrasenaActualInput" autocomplete="current-password" />
                    </label>

                    <div class="field-row">
                      <label>
                        Nueva contraseña
                        <input type="password" name="nuevaContrasena" id="nuevaContrasenaInput" autocomplete="new-password" />
                      </label>

                      <label>
                        Repite la nueva contraseña
                        <input type="password" name="repetirNuevaContrasena" id="repetirNuevaContrasenaInput" autocomplete="new-password" />
                      </label>
                    </div>
                  </div>

                  <div class="form-actions">
                    <button class="button-primary" type="button" id="confirmarPasswordModal">Aceptar</button>
                  </div>
                </div>
              </div>
            </form>
          </section>
        </main>
      </div>
    </div>

    <div class="modal-overlay" id="avatarModal">
      <div class="modal-card">
        <div class="modal-header">
          <div>
            <h3 style="margin:0;">Seleccionar foto de perfil</h3>
          </div>
          <button class="button-secondary" type="button" id="cerrarAvatarModal">Cerrar</button>
        </div>

        <div class="avatar-grid" id="avatarGrid">
          <div
            class="avatar-option<c:if test='${empty perfil.fotoUrl}'> selected</c:if>"
            data-value=""
            data-preview="/images/default-avatar.svg"
            data-label="Avatar por defecto"
          >
            <img src="/images/default-avatar.svg" alt="Avatar por defecto" />
            <span>Avatar por defecto</span>
          </div>

          <c:forEach var="avatar" items="${perfil.avataresDisponibles}">
            <c:if test="${avatar.url ne '/images/default-avatar.svg'}">
              <div
                class="avatar-option<c:if test='${avatar.seleccionada}'> selected</c:if>"
                data-value="${avatar.url}"
                data-preview="${avatar.url}"
              >
                <img src="${avatar.url}" alt="<c:out value='${avatar.etiqueta}' />" />
              </div>
            </c:if>
          </c:forEach>
        </div>

        <div class="form-actions">
          <button class="button-primary" type="button" id="confirmarAvatar">Confirmar seleccion</button>
        </div>
      </div>
    </div>

    <script>
      (function () {
        const resumen = document.getElementById("perfilResumen");
        const formulario = document.getElementById("perfilFormulario");
        const editarPerfil = document.getElementById("editarPerfil");
        const cancelarPerfil = document.getElementById("cancelarPerfil");
        const input = document.getElementById("fotoUrlInput");
        const preview = document.getElementById("avatarPreview");
        const selectionLabel = document.getElementById("avatarSelectionLabel");
        const avatarModal = document.getElementById("avatarModal");
        const avatarGrid = document.getElementById("avatarGrid");
        const abrirAvatarModal = document.getElementById("abrirAvatarModal");
        const cerrarAvatarModal = document.getElementById("cerrarAvatarModal");
        const confirmarAvatar = document.getElementById("confirmarAvatar");
        const limpiarAvatar = document.getElementById("limpiarAvatar");
        const passwordModal = document.getElementById("passwordModal");
        const abrirPasswordModal = document.getElementById("abrirPasswordModal");
        const cerrarPasswordModal = document.getElementById("cerrarPasswordModal");
        const confirmarPasswordModal = document.getElementById("confirmarPasswordModal");
        const fallback = "/images/default-avatar.svg";
        let temporalSeleccion = input ? input.value.trim() : "";

        if (!input || !preview || !avatarGrid) {
          return;
        }

        const fotoInicial = input.value;

        function mostrarEdicion() {
          resumen.hidden = true;
          formulario.hidden = false;
          formulario.elements.namedItem("nombreUsuario").focus();
        }

        editarPerfil.addEventListener("click", mostrarEdicion);

        cancelarPerfil.addEventListener("click", function () {
          formulario.reset();
          input.value = fotoInicial;
          temporalSeleccion = valorActual();
          pintarSeleccionTemporal();
          actualizarPreviewYTexto();
          cerrarModalAvatar();
          cerrarModalPassword();
          formulario.hidden = true;
          resumen.hidden = false;
          editarPerfil.focus();
        });

        function valorActual() {
          return input.value.trim();
        }

        function opcionPorValor(valor) {
          return Array.from(avatarGrid.querySelectorAll(".avatar-option")).find(function (opcion) {
            return (opcion.dataset.value || "") === valor;
          }) || null;
        }

        function actualizarPreviewYTexto() {
          const valor = valorActual();
          const opcion = opcionPorValor(valor);
          const etiqueta = opcion ? (opcion.dataset.label || "Foto de perfil seleccionada") : "Foto de perfil actual";
          preview.src = valor === "" ? fallback : valor;
          selectionLabel.textContent = etiqueta;
        }

        function pintarSeleccionTemporal() {
          Array.from(avatarGrid.querySelectorAll(".avatar-option")).forEach(function (opcion) {
            opcion.classList.toggle("selected", (opcion.dataset.value || "") === temporalSeleccion);
          });
        }

        function abrirModalAvatar() {
          temporalSeleccion = valorActual();
          pintarSeleccionTemporal();
          avatarModal.classList.add("visible");
        }

        function cerrarModalAvatar() {
          avatarModal.classList.remove("visible");
        }

        function abrirModalPassword() {
          if (passwordModal) {
            passwordModal.classList.add("visible");
          }
        }

        function cerrarModalPassword() {
          if (passwordModal) {
            passwordModal.classList.remove("visible");
          }
        }

        preview.addEventListener("error", function () {
          preview.src = fallback;
        });

        avatarGrid.querySelectorAll(".avatar-option").forEach(function (opcion) {
          opcion.addEventListener("click", function () {
            temporalSeleccion = opcion.dataset.value || "";
            pintarSeleccionTemporal();
          });
        });

        if (abrirAvatarModal) {
          abrirAvatarModal.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();
            abrirModalAvatar();
          });
        }

        if (cerrarAvatarModal) {
          cerrarAvatarModal.addEventListener("click", function () {
            cerrarModalAvatar();
          });
        }

        if (confirmarAvatar) {
          confirmarAvatar.addEventListener("click", function () {
            input.value = temporalSeleccion;
            actualizarPreviewYTexto();
            cerrarModalAvatar();
          });
        }

        if (limpiarAvatar) {
          limpiarAvatar.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();
            input.value = "";
            temporalSeleccion = "";
            pintarSeleccionTemporal();
            actualizarPreviewYTexto();
          });
        }

        if (abrirPasswordModal) {
          abrirPasswordModal.addEventListener("click", function () {
            abrirModalPassword();
          });
        }

        if (cerrarPasswordModal) {
          cerrarPasswordModal.addEventListener("click", function () {
            cerrarModalPassword();
          });
        }

        if (confirmarPasswordModal) {
          confirmarPasswordModal.addEventListener("click", function () {
            cerrarModalPassword();
          });
        }

        if (avatarModal) {
          avatarModal.addEventListener("click", function (event) {
            if (event.target === avatarModal) {
              cerrarModalAvatar();
            }
          });
        }

        if (passwordModal) {
          passwordModal.addEventListener("click", function (event) {
            if (event.target === passwordModal) {
              cerrarModalPassword();
            }
          });
        }

        document.addEventListener("keydown", function (event) {
          if (event.key === "Escape") {
            cerrarModalAvatar();
            cerrarModalPassword();
          }
        });

        actualizarPreviewYTexto();
        if (formulario.dataset.error === "true") {
          mostrarEdicion();
        }
      })();
    </script>
  </body>
</html>
