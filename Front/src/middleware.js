import { defineMiddleware } from "astro:middleware";
import { initializeLucia } from "./lib/lucia.js";
import { SignJWT } from "jose";

export const onRequest = defineMiddleware(async (context, next) => {
  const env = context.locals.runtime?.env;
  const db = env?.DB;

  if (db) {
    try {
      const lucia = initializeLucia(db);
      const sessionId = context.cookies.get(lucia.sessionCookieName)?.value ?? null;

      let session = null;
      let user = null;

      if (sessionId) {
        const result = await lucia.validateSession(sessionId);
        session = result.session;
        user = result.user;

        // Si la sesion es valida pero acaba de ser extendida, renovamos la cookie
        if (session && session.fresh) {
          const sessionCookie = lucia.createSessionCookie(session.id);
          context.cookies.set(sessionCookie.name, sessionCookie.value, sessionCookie.attributes);
        }
        // Si no limpiamos la cookie
        if (!session) {
          const sessionCookie = lucia.createBlankSessionCookie();
          context.cookies.set(sessionCookie.name, sessionCookie.value, sessionCookie.attributes);
        }
      }

      let jwtPayload = {
        sub: "anonymous",
        name: "Invitado",
        role: "guest",
        email: "guest@inmopaco.com"
      };

      if (session && user) {
        // En Lucia, user contiene los datos de getUserAttributes
        context.locals.user = user;
        context.locals.session = session;

        jwtPayload = {
          sub: user.id,
          name: user.name,
          role: user.role ?? "user",
          email: user.email,
        };
      }

      // Generamos JWT siempre, con datos de user o guest
      const jwtSecret = env?.JWT_SECRET || import.meta.env.JWT_SECRET;
      const secret = new TextEncoder().encode(jwtSecret);
      const jwtToken = await new SignJWT(jwtPayload)
        .setProtectedHeader({ alg: "HS256" })
        .setIssuedAt()
        .setExpirationTime("15m")
        .sign(secret);

      context.locals.jwtToken = jwtToken;

      const url = new URL(context.request.url);
      
      // Control de acceso a administracion y proxy backend admin/servicios
      const protectedPaths = ["/administration", "/admin", "/portainer", "/bff", "/adminer"];
      if (protectedPaths.some(path => url.pathname.startsWith(path))) {
        if (!session || !user || user.role !== "admin") {
          console.warn(`[Middleware] Acceso bloqueado a ${url.pathname} para ${user?.email ?? "Anon"}`);
          return context.redirect("/login");
        }
      }

    } catch (err) {
      console.error("[Middleware] Error procesando sesión Lucia:", err?.message);
    }
  }

  return next();
});
