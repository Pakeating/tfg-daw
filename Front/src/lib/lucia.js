import { Lucia } from "lucia";
import { D1Adapter } from "@lucia-auth/adapter-sqlite";

/**
 * Inicializa Lucia con el adaptador de d1.
 */
export function initializeLucia(db) {
  // adapter para tablas user y session
  const adapter = new D1Adapter(db, {
    user: "user",
    session: "session"
  });

  return new Lucia(adapter, {
    // cookies de sesión son 'Secure' solo en producción (HTTPS real)
    sessionCookie: {
      attributes: {
        secure: import.meta.env.PROD 
      }
    },
    // inyecta los atributos de BBDD al objeto de usuario de lucia
    getUserAttributes: (attributes) => {
      return {
        email: attributes.email,
        name: attributes.name,
        role: attributes.role
      };
    }
  });
}
