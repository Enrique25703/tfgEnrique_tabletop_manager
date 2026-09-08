// Preparación: npm install --prefix target/ui-check --no-package-lock --ignore-scripts jsdom
// Ejecución: node --test src/test/js/creador-listas-40k.test.cjs
const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const path = require('node:path');
const { createRequire } = require('node:module');
const root = path.resolve(__dirname, '../../..');
const { JSDOM, VirtualConsole } = createRequire(path.join(root, 'target/ui-check/package.json'))('jsdom');
const script = readFileSync(path.join(root, 'src/main/resources/static/js/creador-listas-40k.js'), 'utf8');

function abrirCreador(t, formato = 'WH40K_11') {
  const dom = new JSDOM(`<!doctype html><html><body>
    <main id="creadorListaApp" data-formato-juego="${formato}" data-nombre-lista="Prueba"
      data-faccion="Imperium" data-ejercito="Prueba" data-limite-puntos="2000"
      data-url-asistente="/creador-listas-40k/asistente/analizar">
      <div class="catalog-category" id="categoriaEspecial">
        <button class="boton-catalogo-especial" data-tipo-especial="destacamentos">Destacamentos</button>
        <button class="boton-catalogo-especial" data-tipo-especial="disposicion">Disposición</button>
      </div>
      <div class="catalog-category">
        <button class="boton-catalogo-unidad" data-nombre="Capitán" data-roles="Character"
          data-perfiles-armas-json='[{"nombre":"Lascannon","tipo":"Ranged Weapon","ataques":"1","fuerza":"12","penetracion":"-3","dano":"D6+1"}]'
          data-character="true" data-puntos="100" data-puntos-base="100" data-categoria="personajes">Capitán</button>
      </div>
      <select id="datosDestacamentos" hidden multiple>
        <option value="a" data-nombre="Destacamento A" data-puntos-dp="2" data-disposiciones="Take and Hold"></option>
        <option value="b" data-nombre="Destacamento B" data-puntos-dp="1" data-disposiciones="Reconnaissance"></option>
        <option value="c" data-nombre="Destacamento C" data-puntos-dp="2" data-disposiciones="Purge the Foe"></option>
      </select>
      <div id="bloque-disposicion" class="list-empty">Sin unidades</div>
      <div id="bloque-personajes" class="list-empty">Sin unidades</div>
      <input id="filtroTexto"><input id="filtroLegends" type="checkbox" checked>
      <input id="filtroFortificaciones" type="checkbox" checked><input id="filtroAliadas" type="checkbox" checked>
      <span id="contadorDP">0</span><span id="contadorPuntos">0</span>
      <div id="editorUnidad"><div id="panelVacio"></div><div id="panelDetalle"></div>
        <h3 id="detalleNombre"></h3><p id="detalleMeta"></p><div id="configuracionUnidad"></div>
        <textarea id="notasUnidad"></textarea><input id="unidadWarlord" type="checkbox">
        <button id="cerrarEditorUnidad">Cerrar</button>
      </div>
      <div id="panelProgreso"><div id="barraPuntos"></div></div><div id="resumenValidacion"></div>
      <ul id="erroresValidacion"></ul><span id="estadoValidacion"></span><span id="estadoGuardado"></span>
      <button id="botonGuardarLista">Guardar</button>
      <button id="abrirAsistente" aria-expanded="false">Asistente</button>
      <aside id="panelAsistente" hidden><button id="cerrarAsistente">Cerrar</button>
        <div id="mensajesAsistente"></div><form id="formAsistente">
          <input id="preguntaAsistente"><button id="enviarAsistente">Enviar</button>
          <p id="estadoAsistente"></p></form></aside>
    </main></body></html>`, { runScripts: 'outside-only', url: 'http://localhost/creador-listas-40k',
      virtualConsole: new VirtualConsole() });
  t.after(() => dom.window.close());
  const { document } = dom.window;
  if (formato.startsWith('AOS')) document.getElementById('categoriaEspecial').remove();
  dom.window.eval(script);
  const especial = tipo => document.querySelector(`.unidad-en-lista[data-tipo-especial="${tipo}"]`);
  const abrir = tipo => document.querySelector(`.boton-catalogo-especial[data-tipo-especial="${tipo}"]`).click();
  const marcar = nombre => {
    const label = [...document.querySelectorAll('#configuracionUnidad label')]
      .find(element => element.textContent.includes(nombre));
    assert.ok(label, `Debe aparecer la opción ${nombre}`);
    label.querySelector('input').click();
  };
  const estado = tipo => JSON.parse(especial(tipo).dataset.configuracionEstado).seleccionados;
  return { dom, document, especial, abrir, marcar, estado };
}

test('el selector permanece visible al filtrar y abre una única tarjeta configurable', t => {
  const ui = abrirCreador(t);
  const filtro = ui.document.getElementById('filtroTexto');
  filtro.value = 'unidad inexistente';
  filtro.dispatchEvent(new ui.dom.window.Event('input'));
  assert.equal(ui.document.getElementById('categoriaEspecial').classList.contains('catalog-category-hidden'), false);
  ui.abrir('destacamentos');
  ui.abrir('destacamentos');
  assert.equal(ui.document.querySelectorAll('.unidad-en-lista').length, 1);
  assert.ok(ui.document.getElementById('editorUnidad').classList.contains('is-open'));
  ui.marcar('Destacamento A');
  ui.marcar('Destacamento B');
  ui.marcar('Destacamento C');
  assert.deepEqual(ui.estado('destacamentos'), ['a', 'b']);
  assert.equal(ui.document.getElementById('contadorDP').textContent, '3');
  assert.match(ui.document.getElementById('estadoGuardado').textContent, /3 DP/);
});

test('al cambiar o eliminar destacamentos se limpian las disposiciones incompatibles y los DP', t => {
  const ui = abrirCreador(t);
  ui.abrir('destacamentos');
  ui.marcar('Destacamento A');
  ui.abrir('disposicion');
  ui.marcar('Take and Hold');
  assert.deepEqual(ui.estado('disposicion'), ['Take and Hold']);
  ui.abrir('destacamentos');
  ui.marcar('Destacamento A');
  assert.deepEqual(ui.estado('disposicion'), []);
  ui.marcar('Destacamento B');
  ui.abrir('disposicion');
  ui.marcar('Reconnaissance');
  [...ui.especial('destacamentos').querySelectorAll('button')].find(b => b.textContent === 'Eliminar').click();
  assert.deepEqual(ui.estado('disposicion'), []);
  assert.equal(ui.document.getElementById('contadorDP').textContent, '0');
  assert.match(ui.document.getElementById('erroresValidacion').textContent, /Destacamentos/);
});

test('el guardado incluye las selecciones de destacamentos y disposición', async t => {
  const ui = abrirCreador(t);
  ui.abrir('destacamentos');
  ui.marcar('Destacamento A');
  ui.abrir('disposicion');
  ui.marcar('Take and Hold');
  ui.document.querySelector('.boton-catalogo-unidad').click();
  ui.document.querySelector('#bloque-personajes .boton-seleccionar-unidad').click();
  ui.document.getElementById('unidadWarlord').click();
  assert.equal(ui.document.getElementById('erroresValidacion').children.length, 0);
  let payload;
  ui.dom.window.fetch = async (url, request) => {
    assert.equal(url, '/creador-listas-40k/guardar');
    payload = JSON.parse(new URLSearchParams(request.body).get('datosListaJson'));
    return { ok: true, text: async () => 'OK|1' };
  };
  ui.document.getElementById('botonGuardarLista').click();
  await new Promise(resolve => setImmediate(resolve));
  assert.ok(payload);
  assert.equal(payload.puntosTotales, 100);
  assert.deepEqual(payload.unidades.find(u => u.tipoEspecial === 'destacamentos').configuracionMiniaturas.seleccionados, ['a']);
  assert.deepEqual(payload.unidades.find(u => u.tipoEspecial === 'disposicion').configuracionMiniaturas.seleccionados, ['Take and Hold']);
});

test('el creador AoS compartido sigue validando sin exigir destacamentos', t => {
  const ui = abrirCreador(t, 'AOS_4');
  ui.document.querySelector('.boton-catalogo-unidad').click();
  ui.document.querySelector('#bloque-personajes .boton-seleccionar-unidad').click();
  ui.document.getElementById('unidadWarlord').click();
  assert.equal(ui.document.getElementById('erroresValidacion').children.length, 0);
});

test('el asistente envia la lista actual y muestra recomendaciones sin interpretar HTML', async t => {
  const ui = abrirCreador(t);
  ui.document.querySelector('.boton-catalogo-unidad').click();
  const pregunta = ui.document.getElementById('preguntaAsistente');
  ui.document.getElementById('abrirAsistente').click();
  assert.equal(ui.document.getElementById('panelAsistente').hidden, false);
  pregunta.value = '¿Qué mejorarías?';
  let listaEnviada;
  ui.dom.window.fetch = async (url, request) => {
    const parametros = new URLSearchParams(request.body);
    listaEnviada = JSON.parse(parametros.get('datosListaJson'));
    assert.equal(parametros.get('pregunta'), '¿Qué mejorarías?');
    return { ok: true, json: async () => ({
      resumen: '<b>Análisis seguro</b>',
      fortalezas: ['Un Warlord'],
      recomendaciones: ['Añade Battleline']
    }) };
  };
  ui.document.getElementById('formAsistente').dispatchEvent(
    new ui.dom.window.Event('submit', { bubbles: true, cancelable: true }));
  await new Promise(resolve => setImmediate(resolve));
  assert.equal(listaEnviada.unidades.filter(u => !u.tipoEspecial).length, 1);
  assert.equal(listaEnviada.unidades.find(u => !u.tipoEspecial).perfilesArmas[0].nombre, 'Lascannon');
  const mensajes = ui.document.getElementById('mensajesAsistente');
  assert.match(mensajes.textContent, /Análisis seguro/);
  assert.match(mensajes.textContent, /Añade Battleline/);
  assert.equal(mensajes.querySelector('b'), null);
});
