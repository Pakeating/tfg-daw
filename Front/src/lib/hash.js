/**
 * Implementacion de hash para Cloudflare Workers, peta si tiene el original porque compila a C
 * Usa la libreria WebCrypto estandar en lugar de argon
 */

export async function hashPassword(password) {
  const enc = new TextEncoder();
  const salt = crypto.getRandomValues(new Uint8Array(16));
  const keyMaterial = await crypto.subtle.importKey(
    "raw", enc.encode(password), { name: "PBKDF2" }, false, ["deriveBits"]
  );
  const hash = await crypto.subtle.deriveBits(
    { name: "PBKDF2", salt, iterations: 100000, hash: "SHA-256" },
    keyMaterial,
    256
  );
  const saltHex = Array.from(salt).map(b => b.toString(16).padStart(2, '0')).join('');
  const hashHex = Array.from(new Uint8Array(hash)).map(b => b.toString(16).padStart(2, '0')).join('');
  return `${saltHex}:${hashHex}`;
}

export async function verifyPassword(password, storedHash) {
  if (!storedHash || !storedHash.includes(':')) return false;
  
  const [saltHex, hashHex] = storedHash.split(':');
  if (saltHex.length !== 32 || hashHex.length !== 64) return false;

  const saltByteArray = saltHex.match(/.{1,2}/g);
  if (!saltByteArray) return false;
  
  const salt = new Uint8Array(saltByteArray.map(byte => parseInt(byte, 16)));
  
  const enc = new TextEncoder();
  try {
    const keyMaterial = await crypto.subtle.importKey(
      "raw", enc.encode(password), { name: "PBKDF2" }, false, ["deriveBits"]
    );
    const hashToVerify = await crypto.subtle.deriveBits(
      { name: "PBKDF2", salt, iterations: 100000, hash: "SHA-256" },
      keyMaterial,
      256
    );
    const hashToVerifyHex = Array.from(new Uint8Array(hashToVerify)).map(b => b.toString(16).padStart(2, '0')).join('');
    
    // Comparacion sin filtraciones de tiempo para seguridad
    let mismatch = 0;
    for (let i = 0; i < hashHex.length; i++) {
      mismatch |= (hashHex.charCodeAt(i) ^ hashToVerifyHex.charCodeAt(i));
    }
    return mismatch === 0;
  } catch (err) {
    console.error("[Auth] Crypto verification error:", err);
    return false;
  }
}
