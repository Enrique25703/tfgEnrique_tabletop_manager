<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Login | TFG Enrique</title>
    <style>
      :root {
        --bg-1: #02040a;
        --bg-2: #07111f;
        --bg-3: #0d2a52;
        --panel: rgba(8, 12, 20, 0.88);
        --line: rgba(110, 168, 255, 0.22);
        --text: #f3f7ff;
        --muted: #9fb3cf;
        --accent: #3d8bff;
        --accent-2: #69b2ff;
      }

      * {
        box-sizing: border-box;
      }

      body {
        margin: 0;
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 24px;
        font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
        color: var(--text);
        background:
          radial-gradient(circle at top, rgba(64, 133, 255, 0.24), transparent 35%),
          linear-gradient(160deg, var(--bg-3) 0%, var(--bg-2) 42%, var(--bg-1) 100%);
      }

      .login-card {
        width: 100%;
        max-width: 420px;
        padding: 32px;
        border: 1px solid var(--line);
        border-radius: 20px;
        background: var(--panel);
        box-shadow: 0 24px 70px rgba(0, 0, 0, 0.42);
        backdrop-filter: blur(10px);
      }

      h1 {
        margin: 0 0 8px;
        font-size: 1.9rem;
        text-align: center;
      }

      h2 {
        margin: 26px 0 10px;
        font-size: 1rem;
        color: var(--muted);
      }

      .subtitle {
        margin: 0 0 28px;
        text-align: center;
        color: var(--muted);
        font-size: 0.95rem;
      }

      .field {
        margin-bottom: 16px;
      }

      label {
        display: block;
        margin-bottom: 8px;
        font-size: 0.92rem;
        color: var(--muted);
      }

      input {
        width: 100%;
        padding: 13px 14px;
        border: 1px solid rgba(130, 173, 255, 0.18);
        border-radius: 12px;
        background: rgba(10, 17, 30, 0.92);
        color: var(--text);
        font-size: 0.96rem;
      }

      input:focus {
        outline: none;
        border-color: var(--accent);
        box-shadow: 0 0 0 3px rgba(61, 139, 255, 0.16);
      }

      .button {
        width: 100%;
        padding: 13px 16px;
        border: 0;
        border-radius: 12px;
        background: linear-gradient(135deg, var(--accent) 0%, var(--accent-2) 100%);
        color: #03101f;
        font-weight: 700;
        font-size: 0.96rem;
        cursor: pointer;
      }

      .button.alt {
        margin-top: 4px;
        background: linear-gradient(135deg, #1f4fa0 0%, #4d88d8 100%);
      }

      .message {
        margin: 0 0 18px;
        padding: 12px 14px;
        border: 1px solid rgba(255, 107, 107, 0.3);
        border-radius: 12px;
        background: rgba(103, 25, 37, 0.32);
        color: #ffd7df;
        font-size: 0.92rem;
      }
    </style>
  </head>
  <body>
    <main class="login-card">
      <h1>Acceso</h1>
      <p class="subtitle">Inicia sesion o crea una cuenta</p>

      <c:if test="${not empty error}">
        <p class="message"><c:out value="${error}" /></p>
      </c:if>

      <form action="/login" method="post">
        <div class="field">
          <label for="usuario">Usuario o email</label>
          <input id="usuario" name="usuario" type="text" placeholder="Introduce tu usuario" />
        </div>
        <div class="field">
          <label for="password">Contrasena</label>
          <input id="password" name="password" type="password" placeholder="Introduce tu contrasena" />
        </div>
        <button class="button" type="submit">Entrar</button>
      </form>

      <h2>Registro</h2>
      <form action="/registro" method="post">
        <div class="field">
          <label for="nombreUsuario">Nombre de usuario</label>
          <input id="nombreUsuario" name="nombreUsuario" type="text" placeholder="Elige un usuario" />
        </div>
        <div class="field">
          <label for="email">Email</label>
          <input id="email" name="email" type="email" placeholder="correo@ejemplo.com" />
        </div>
        <div class="field">
          <label for="passwordRegistro">Contrasena</label>
          <input id="passwordRegistro" name="passwordRegistro" type="password" placeholder="Crea una contrasena" />
        </div>
        <button class="button alt" type="submit">Registrarse</button>
      </form>
    </main>
  </body>
</html>
