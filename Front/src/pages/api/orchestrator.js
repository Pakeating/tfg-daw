export const prerender = false;

/**
 * Endpoint /api/orchestrator
 * Proxy para llamadas autenticadas alOrchestrator Service.
 * El frontend manda las acciones aqui, este endpoint las reenvia al Gateway
 * con el JWT inyectado por el middleware.
 */
export async function POST({ request, locals }) {
  const env = locals.runtime?.env;
  const backendBase = env?.BACKEND_BASE_URL || import.meta.env.BACKEND_BASE_URL;
  const token = locals.jwtToken;

  if (!token) {
    return json({ error: "No autorizado" }, 401);
  }

  let body;
  try {
    body = await request.json();
  } catch {
    return json({ error: "Cuerpo de petición inválido" }, 400);
  }

  const { action, ...params } = body;

  // mapeo de acciones a rutas del gateway
  let targetUrl;
  let method = "GET";
  switch (action) {
    case "purge":
      targetUrl = `${backendBase}/orchestrator/queue-management/purge`;
      break;
    case "deleteConsumer": {
      const stream = params.stream ?? "";
      const subject = params.subject ?? "";
      targetUrl = `${backendBase}/orchestrator/queue-management/delete-consumer?stream=${encodeURIComponent(stream)}&subject=${encodeURIComponent(subject)}`;
      break;
    }
    case "scheduler":
      targetUrl = `${backendBase}/orchestrator/auctions/scheduler`;
      break;
    case "propertyScheduler":
      targetUrl = `${backendBase}/orchestrator/properties/scrape/all`;
      method = "POST";
      break;
    case "getAuctions": {
      const payload = params.auctionsPayload ?? "";
      targetUrl = `${backendBase}/orchestrator/auctions/get?auctionsPayload=${encodeURIComponent(payload)}`;
      break;
    }
    case "processAuctions":
      targetUrl = `${backendBase}/orchestrator/auctions/process?auctionsPayload=`;
      break;
    case "provinceNotifications":
      targetUrl = `${backendBase}/orchestrator/notification/province-auctions-flow`;
      method = "POST";
      break;
    default:
      return json({ error: `Acción desconocida: ${action}` }, 400);
  }

  try {
    const response = await fetch(targetUrl, {
      method,
      headers: {
        "Authorization": `Bearer ${token}`,
        "Accept": "application/json"
      }
    });

    const text = await response.text();
    let data;
    try {
      data = JSON.parse(text);
    } catch {
      data = { message: text || "OK" };
    }

    return json({ success: response.ok, status: response.status, data }, response.ok ? 200 : response.status);
  } catch (err) {
    console.error("[Orchestrator Proxy] Error:", err);
    return json({ error: "Error conectando con el backend" }, 502);
  }
}

function json(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { "Content-Type": "application/json" }
  });
}
