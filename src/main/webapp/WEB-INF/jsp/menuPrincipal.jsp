<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Menu principal</title>
    <style>
      body {
        margin: 20px;
        font-family: Arial, sans-serif;
        background: #efefef;
        color: #1a1a1a;
      }

      h1 {
        margin-top: 0;
        margin-bottom: 8px;
        font-size: 28px;
      }

      p {
        margin-top: 0;
        color: #555555;
      }

      a,
      button {
        display: inline-block;
        margin-right: 10px;
        margin-top: 10px;
        padding: 10px 14px;
        border: 1px solid #999999;
        background: #e6e6e6;
        color: #111111;
        text-decoration: none;
        font-size: 14px;
        cursor: pointer;
      }

      button[disabled] {
        cursor: not-allowed;
        color: #777777;
      }
    </style>
  </head>
  <body>
    <h1>Menu principal</h1>
    <p>Bienvenido, <%= request.getAttribute("nombreUsuario") %></p>
    <p>Esta pantalla aun esta a medio hacer.</p>

    <a href="/catalogo40k">Ver catalogo de 40k</a>
    <button type="button" disabled>Age of Sigmar</button>
    <button type="button" disabled>Ver comunidades</button>
    <button type="button">Ver mis listas</button>
  </body>
</html>
