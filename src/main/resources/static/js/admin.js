(() => {
  const dialogs = document.querySelectorAll('.admin-dialog');
  function abrirModal(id, boton) {
    const dialog = document.getElementById(id);
    dialog.returnFocus = boton;
    dialog.showModal();
    document.querySelector('.admin-content').classList.add('modal-open');
    dialog.querySelector('input:not([type="hidden"])')?.focus();
  }
  dialogs.forEach(dialog => {
    dialog.querySelectorAll('.cerrar-modal').forEach(boton => {
      boton.addEventListener('click', () => dialog.close());
    });
    dialog.addEventListener('click', event => {
      if (event.target !== dialog) return;
      const rect = dialog.getBoundingClientRect();
      if (event.clientX < rect.left || event.clientX > rect.right ||
          event.clientY < rect.top || event.clientY > rect.bottom) dialog.close();
    });
    dialog.addEventListener('close', () => {
      document.querySelector('.admin-content').classList.remove('modal-open');
      dialog.querySelector('form').reset();
      dialog.returnFocus?.focus({preventScroll: true});
    });
  });
  document.querySelectorAll('.abrir-editar-usuario').forEach(boton => {
    boton.addEventListener('click', () => {
      document.getElementById('editarUsuarioId').value = boton.dataset.usuarioId || '';
      document.getElementById('editarNombreUsuario').value = boton.dataset.usuarioNombre || '';
      document.getElementById('editarEmailUsuario').value = boton.dataset.usuarioEmail || '';
      abrirModal('modalEditarUsuario', boton);
    });
  });
  document.querySelectorAll('.abrir-password-usuario').forEach(boton => {
    boton.addEventListener('click', () => {
      document.getElementById('passwordUsuarioId').value = boton.dataset.usuarioId || '';
      document.getElementById('passwordUsuarioTitulo').textContent =
        'Define una nueva contraseña para ' + (boton.dataset.usuarioNombre || 'el usuario') + '.';
      document.getElementById('nuevaContrasenaUsuario').value = '';
      abrirModal('modalPasswordUsuario', boton);
    });
  });
  document.querySelectorAll('.abrir-editar-comunidad').forEach(boton => {
    boton.addEventListener('click', () => {
      document.getElementById('editarComunidadId').value = boton.dataset.comunidadId || '';
      document.getElementById('editarNombreComunidad').value = boton.dataset.comunidadNombre || '';
      document.getElementById('editarLogoComunidad').value = boton.dataset.comunidadLogo || '';
      abrirModal('modalEditarComunidad', boton);
    });
  });
})();
