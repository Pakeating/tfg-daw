-- =============================================================================
-- Lucia Auth Schema para Cloudflare D1
-- Ejecutar con:
--   npx wrangler d1 execute <NOMBRE_DE_TU_BD> --file=./migrations/lucia_schema.sql --local
--   npx wrangler d1 execute <NOMBRE_DE_TU_BD> --file=./migrations/lucia_schema.sql --remote
-- =============================================================================

DROP TABLE IF EXISTS "session";
DROP TABLE IF EXISTS "user";

CREATE TABLE "user" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "email" TEXT NOT NULL UNIQUE,
  "name" TEXT NOT NULL,
  "password_hash" TEXT NOT NULL,
  "role" TEXT NOT NULL DEFAULT 'user'
);

CREATE TABLE "session" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "user_id" TEXT NOT NULL REFERENCES "user"("id") ON DELETE CASCADE,
  "expires_at" INTEGER NOT NULL
);


-- Hash generado mediante WebCrypto PBKDF2 nativo para máxima compatibilidad con Workers
INSERT INTO "user" ("id", "email", "name", "password_hash", "role") 
VALUES (
  'user_admin_001',
  'paco.refractando@gmail.com',
  'Admin Paco',
  '49fbe5b8efa5cc4e606e45cbe69e4cfa:f545d5cc18b6fb6dc3866dd94a6c7949033276320a0435f3158f5c1a9ded4f01',
  'admin'
);
