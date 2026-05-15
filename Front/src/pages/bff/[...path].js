export const prerender = false;

// intercepta cualquier verbo HTTP, actua como proxy inverso para autenticar la navegacion por las paginas protegidas en el back
export const ALL = async ({ params, request, locals }) => {
  const { path } = params;
  
  //construir la URL hacia el backend real
  const env = locals.runtime?.env;
  const backendBase = env?.BACKEND_BASE_URL || import.meta.env.BACKEND_BASE_URL;
  const BACKEND_URL = `${backendBase}/bff/${path || ''}${new URL(request.url).search}`;

  const token = locals.jwtToken; 

  if (!token) {
    return new Response("No autorizado", { status: 401 });
  }

  // clonar la petición original pero inyectando el Token
  const headers = new Headers(request.headers);
  headers.set('Authorization', `Bearer ${token}`);
  headers.set('X-Forwarded-Prefix', '/bff');
  
  if (headers.has('host')) {
     headers.set('X-Forwarded-Host', headers.get('host'));
     
     const protocol = new URL(request.url).protocol.replace(':', '');
     headers.set('X-Forwarded-Proto', protocol);

     headers.delete('host');
  }

  headers.delete('accept-encoding');

  try {
    const response = await fetch(BACKEND_URL, {
      method: request.method,
      headers: headers,
      body: ['POST', 'PUT', 'PATCH', 'DELETE'].includes(request.method) 
            ? await request.arrayBuffer() 
            : undefined,
      duplex: 'half'
    });
    
    const responseHeaders = new Headers(response.headers);
    responseHeaders.delete('content-encoding');
    responseHeaders.delete('content-length');

    const contentType = responseHeaders.get('content-type') ?? '';
    const pathName = new URL(request.url).pathname;
    const isResource = pathName.endsWith('.js') || pathName.endsWith('.css') || pathName.endsWith('.json');

    if (contentType.includes('text/html') || contentType.includes('javascript') || isResource) {
      let text = await response.text();
      
      const requestHost = new URL(request.url).host;
      const isLocalDev = requestHost.startsWith('localhost') || requestHost.startsWith('127.0.0.1');
      if (isLocalDev) {
        text = text.replaceAll(`https://${requestHost}`, `http://${requestHost}`);
      }

      return new Response(text, {
        status: response.status,
        statusText: response.statusText,
        headers: responseHeaders
      });
    }

    return new Response(response.body, {
      status: response.status,
      statusText: response.statusText,
      headers: responseHeaders
    });
  } catch (error) {
    console.error("[Proxy BFF] Error conectando con el backend: ", error);
    return new Response("Error conectando con el backend", { status: 502 });
  }
};
