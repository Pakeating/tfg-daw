export const prerender = false;

const BACKEND_BASE_URL = import.meta.env.BACKEND_BASE_URL;

/**
 * Endpoint /api/auctions
 * Actúa como un proxy para reenviar las peticiones de búsqueda al backend de Java.
 * Si hay una sesión activa, inyecta el JWT en la cabecera Authorization.
 */
export async function POST ({ request, locals }) {
  const env = locals.runtime?.env;
  const backendBase = env?.BACKEND_BASE_URL || import.meta.env.BACKEND_BASE_URL;

  let body;
  try {
    body = await request.json();
  } catch (e) {
    console.error('[API Proxy] Error parsing request body:', e);
    return new Response(JSON.stringify({ error: 'Invalid JSON body' }), {
      status: 400,
      headers: { "Content-Type": "application/json" }
    });
  }

  const url = new URL(request.url);
  const queryParams = url.searchParams.toString();
  
  // construimos la URL del backend usando la base dinámica
  const backendUrl = `${backendBase}/bff/auctions/search${queryParams ? '?' + queryParams : ''}`;

  try {
    const backendHeaders = { 
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${locals.jwtToken}`
    };

    const response = await fetch(backendUrl, {
      method: 'POST',
      headers: backendHeaders,
      body: JSON.stringify(body)
    });

    if (!response.ok) {
      console.error('[API Proxy] Backend returned error:', response.status);
      return new Response(JSON.stringify({ error: 'Backend error' }), {
        status: response.status,
        headers: { "Content-Type": "application/json" }
      });
    }

    const data = await response.json();
    
    return new Response(JSON.stringify(data), {
      status: 200,
      headers: { "Content-Type": "application/json" }
    });
  } catch (error) {
    console.error('[API Proxy] Fetch error:', error);
    return new Response(JSON.stringify({ error: 'Connection failed' }), {
      status: 500,
      headers: { "Content-Type": "application/json" }
    });
  }
};

export async function GET ({ request, locals }) {
  const env = locals.runtime?.env;
  const backendBase = env?.BACKEND_BASE_URL || import.meta.env.BACKEND_BASE_URL;

  const url = new URL(request.url);
  const idValue = url.searchParams.get('id');
  
  if (!idValue) {
    return new Response(JSON.stringify({ error: 'Missing auction ID' }), {
      status: 400,
      headers: { "Content-Type": "application/json" }
    });
  }

  const page = url.searchParams.get('page') || '0';
  const size = url.searchParams.get('size') || '10';
  const sortBy = url.searchParams.get('sortBy') || '+dateOfEnd';
  const queryParams = new URLSearchParams({ page, size, sortBy }).toString();

  const backendUrl = `${backendBase}/bff/auctions/search/${idValue}?${queryParams}`;

  try {
    const backendHeaders = { 
      'Accept': 'application/json',
      'Authorization': `Bearer ${locals.jwtToken}`
    };

    const response = await fetch(backendUrl, {
      method: 'GET',
      headers: backendHeaders
    });

    if (!response.ok) {
      console.error('[API Proxy] Backend returned error:', response.status);
      return new Response(JSON.stringify({ error: 'Backend error' }), {
        status: response.status,
        headers: { "Content-Type": "application/json" }
      });
    }

    const data = await response.json();
    

    // devuelte el auction directamente, envolvemos en page apra reutilizar el componente del front
    let PageData = data;
    if (!data.content && !Array.isArray(data)) {
        PageData = {
            content: [data],
            page: {
                number: 0,
                size: 1,
                totalElements: 1,
                totalPages: 1
            }
        };
    }

    return new Response(JSON.stringify(PageData), {
      status: 200,
      headers: { "Content-Type": "application/json" }
    });
  } catch (error) {
    console.error('[API Proxy] Fetch error:', error);
    return new Response(JSON.stringify({ error: 'Connection failed' }), {
      status: 500,
      headers: { "Content-Type": "application/json" }
    });
  }
}
