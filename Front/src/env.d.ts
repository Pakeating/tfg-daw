/// <reference path="../.astro/types.d.ts" />
/// <reference types="astro/client" />

// Tipado del runtime de Cloudflare (D1, KV, R2, etc.)
type Runtime = import("@astrojs/cloudflare").Runtime<Env>;

declare namespace App {
  interface Locals extends Runtime {
    /** Usuario autenticado (establecido por el middleware) */
    user?: {
      id: string;
      name: string;
      email: string;
      role: string;
    } | null;

    /** Sesión activa (establecida por el middleware) */
    session?: {
      id: string;
      userId: string;
      expiresAt: Date;
      fresh: boolean;
    } | null;

    /**
     * JWT firmado con HS256 (expira en 5 min) para llamadas al backend Java.
     */
    jwtToken?: string;
  }
}

interface ImportMetaEnv {
  readonly BETTER_AUTH_SECRET: string;
  readonly JWT_SECRET: string;
  readonly BACKEND_BASE_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
