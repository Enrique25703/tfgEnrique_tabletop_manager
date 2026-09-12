<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <dialog id="modalEditarUsuario" class="admin-dialog" aria-labelledby="modalEditarUsuarioTitulo"><div class="modal-card"><button type="button" class="dialog-close cerrar-modal" aria-label="Cerrar ventana"><svg class="admin-icon" aria-hidden="true"><use href="#admin-close" /></svg></button><span class="eyebrow">Administración / Editar</span>
        <h2 class="dialog-title" id="modalEditarUsuarioTitulo">Editar usuario</h2>
        <p class="page-subtitle">Actualiza nombre de usuario y correo.</p>

        <form action="/admin/usuarios/actualizar" method="post">
          <input type="hidden" id="editarUsuarioId" name="usuarioId" />
          <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
          <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
          <c:if test="${admin.comunidadSeleccionada != null}">
            <input type="hidden" name="comunidadIdRetorno" value="${admin.comunidadSeleccionada.id}" />
          </c:if>

          <div>
            <label for="editarNombreUsuario">Nombre de usuario</label>
            <input id="editarNombreUsuario" name="nombreUsuario" type="text" required />
          </div>

          <div>
            <label for="editarEmailUsuario">Correo</label>
            <input id="editarEmailUsuario" name="email" type="email" required />
          </div>

          <div class="modal-actions">
            <button class="button-primary" type="submit">Guardar cambios</button>
            <button class="button-secondary cerrar-modal" type="button">Cancelar</button>
          </div>
        </form>
      </div>
    </dialog>
<dialog id="modalPasswordUsuario" class="admin-dialog" aria-labelledby="modalPasswordUsuarioTitulo"><div class="modal-card"><button type="button" class="dialog-close cerrar-modal" aria-label="Cerrar ventana"><svg class="admin-icon" aria-hidden="true"><use href="#admin-close" /></svg></button><span class="eyebrow">Administración / Editar</span>
        <h2 class="dialog-title" id="modalPasswordUsuarioTitulo">Cambiar contraseña</h2>
        <p class="page-subtitle" id="passwordUsuarioTitulo">Define una nueva contraseña para el usuario.</p>

        <form action="/admin/usuarios/contrasena" method="post">
          <input type="hidden" id="passwordUsuarioId" name="usuarioId" />
          <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
          <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
          <c:if test="${admin.comunidadSeleccionada != null}">
            <input type="hidden" name="comunidadIdRetorno" value="${admin.comunidadSeleccionada.id}" />
          </c:if>

          <div>
            <label for="nuevaContrasenaUsuario">Nueva contraseña</label>
            <input id="nuevaContrasenaUsuario" name="nuevaContrasena" type="password" minlength="6" autocomplete="new-password" aria-describedby="passwordAyuda" required /><p class="field-hint" id="passwordAyuda">Utiliza al menos 6 caracteres.</p>
          </div>

          <div class="modal-actions">
            <button class="button-primary" type="submit">Actualizar contraseña</button>
            <button class="button-secondary cerrar-modal" type="button">Cancelar</button>
          </div>
        </form>
      </div>
    </dialog>
<dialog id="modalEditarComunidad" class="admin-dialog" aria-labelledby="modalEditarComunidadTitulo"><div class="modal-card"><button type="button" class="dialog-close cerrar-modal" aria-label="Cerrar ventana"><svg class="admin-icon" aria-hidden="true"><use href="#admin-close" /></svg></button><span class="eyebrow">Administración / Editar</span>
        <h2 class="dialog-title" id="modalEditarComunidadTitulo">Editar comunidad</h2>
        <p class="page-subtitle">Actualiza nombre y logo de la comunidad.</p>

        <form action="/admin/comunidades/actualizar" method="post">
          <input type="hidden" id="editarComunidadId" name="comunidadId" />
          <input type="hidden" name="buscarUsuario" value="<c:out value='${admin.buscarUsuario}' />" />
          <input type="hidden" name="buscarComunidad" value="<c:out value='${admin.buscarComunidad}' />" />
          <c:if test="${admin.comunidadSeleccionada != null}">
            <input type="hidden" name="comunidadIdRetorno" value="${admin.comunidadSeleccionada.id}" />
          </c:if>

          <div>
            <label for="editarNombreComunidad">Nombre</label>
            <input id="editarNombreComunidad" name="nombre" type="text" required />
          </div>

          <div>
            <label for="editarLogoComunidad">Dirección del logo</label>
            <input id="editarLogoComunidad" name="logoUrl" type="text" placeholder="https://…" /><p class="field-hint">Opcional. Puedes conservar la dirección actual o indicar otra imagen.</p>
          </div>

          <div class="modal-actions">
            <button class="button-primary" type="submit">Guardar cambios</button>
            <button class="button-secondary cerrar-modal" type="button">Cancelar</button>
          </div>
        </form>
      </div>
    </dialog>

