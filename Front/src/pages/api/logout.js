import { initializeLucia } from "../../lib/lucia.js";

export const prerender = false;

export async function POST(context) {
  const env = context.locals.runtime?.env;
  const db = env?.DB;

  if (!db) {
    return new Response(JSON.stringify({ error: "Base de datos D1 no disponible" }), { status: 500 });
  }

  const lucia = initializeLucia(db);

  try {
    const sessionId = context.cookies.get(lucia.sessionCookieName)?.value || null;
    if (!sessionId) {
      return new Response(JSON.stringify({ error: "No session found" }), { status: 401 });
    }

    // invalida la sesion
    await lucia.invalidateSession(sessionId);

    // borra la cookie
    const sessionCookie = lucia.createBlankSessionCookie();
    return new Response(JSON.stringify({ success: true }), {
      status: 200,
      headers: {
        "Set-Cookie": sessionCookie.serialize(),
        "Content-Type": "application/json"
      }
    });

  } catch (error) {
    console.error("[Logout API] Error:", error);
    return new Response(JSON.stringify({ error: "Internal Server Error" }), { status: 500 });
  }
}
