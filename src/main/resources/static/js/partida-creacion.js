(() => {
  const players = document.querySelectorAll('[data-setup-player]');
  const buttons = document.querySelectorAll('[data-setup-show]');
  if (!players.length) return;
  document.body.classList.add('setup-interactive');
  function showPlayer(id) {
    players.forEach(player => player.classList.toggle('is-current', player.dataset.setupPlayer === id));
    buttons.forEach(button => button.setAttribute('aria-pressed', String(button.dataset.setupShow === id)));
  }
  buttons.forEach(button => button.addEventListener('click', () => showPlayer(button.dataset.setupShow)));
  document.querySelector('.players-grid').addEventListener('invalid', event => {
    const player = event.target.closest('[data-setup-player]');
    if (player) showPlayer(player.dataset.setupPlayer);
  }, true);
})();
