import { initializeLucia } from "../../lib/lucia.js";
import { verifyPassword } from "../../lib/hash.js";

export const prerender = false;

export async function POST(context) {
  const env = context.locals.runtime?.env;
  const db = env?.DB;

  if (!db) {
    return new Response(JSON.stringify({ error: "Base de datos D1 no disponible" }), { status: 500 });
  }

  const lucia = initializeLucia(db);

  try {
    const { email, password } = await context.request.json();

    if (typeof email !== "string" || email.length < 3 || typeof password !== "string" || password.length < 3) {
      return new Response(JSON.stringify({ error: "Credenciales inválidas" }), { status: 400 });
    }

    const { results } = await db.prepare('SELECT id, password_hash, name, role FROM "user" WHERE email = ?')
      .bind(email)
      .all();

    if (!results || results.length === 0) {
      return new Response(JSON.stringify({ error: "Correo o contraseña incorrectos" }), { status: 401 });
    }

    // valida contraseña nativamente
    const user = results[0];
    const validPassword = await verifyPassword(password, user.password_hash);

    if (!validPassword) {
      return new Response(JSON.stringify({ error: "Correo o contraseña incorrectos" }), { status: 401 });
    }

    // crea sesion en Lucia
    const session = await lucia.createSession(user.id, {});
    const sessionCookie = lucia.createSessionCookie(session.id);
    
    // cookie segura en respuesta
    return new Response(JSON.stringify({ success: true, redirect: "/administration" }), {
      status: 200,
      headers: {
        "Set-Cookie": sessionCookie.serialize(),
        "Content-Type": "application/json"
      }
    });

  } catch (error) {
    console.error("[Login API] Error:", error);
    return new Response(JSON.stringify({ 
      error: "Error interno", 
      message: error?.message, 
      stack: error?.stack 
    }), { status: 500, headers: {"Content-Type": "application/json"} });
  }
}
