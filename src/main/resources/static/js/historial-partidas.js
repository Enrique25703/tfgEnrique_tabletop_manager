document.querySelectorAll('[data-eliminar-partida]').forEach((form) => {
  form.addEventListener('submit', (event) => {
    if (!window.confirm('¿Eliminar esta partida del registro? Se borrarán también sus rondas y el resultado dejará de contar en las estadísticas. Esta acción no se puede deshacer.')) {
      event.preventDefault();
    }
  });
});
