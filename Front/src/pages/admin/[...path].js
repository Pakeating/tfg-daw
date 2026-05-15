export const prerender = false;

// intercepta cualquier verbo HTTP, actua como proxy inverso para autenticar la navegacion por las paginas protegidas en el back
export const ALL = async ({ params, request, locals }) => {
  const { path } = params;
  
  //construir la URL hacia el backend real
  const env = locals.runtime?.env;
  const backendBase = env?.BACKEND_BASE_URL || import.meta.env.BACKEND_BASE_URL;
  const BACKEND_URL = `${backendBase}/admin/${path || ''}${new URL(request.url).search}`;

  const token = locals.jwtToken; 

  if (!token) {
    return new Response("No autorizado", { status: 401 });
  }

  // clonar la petición original pero inyectando el Token
  const headers = new Headers(request.headers);
  headers.set('Authorization', `Bearer ${token}`);
  
  // respetar el host expuesto para Admin Server
  if (headers.has('host')) {
     headers.set('X-Forwarded-Host', headers.get('host'));
     
     // evita que Spring Boot devuelva recursos "https://"
     const protocol = new URL(request.url).protocol.replace(':', '');
     headers.set('X-Forwarded-Proto', protocol);

     //eliminar el Host header original para enviar el correcto
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
      duplex: 'half' //si no da problemas con los stream
    });
    
    const responseHeaders = new Headers(response.headers);
    //node descomprime automaticamente pero no quita los headers
    responseHeaders.delete('content-encoding');
    responseHeaders.delete('content-length');

    const contentType = responseHeaders.get('content-type') ?? '';
    const requestHost = new URL(request.url).host;
    const isLocalDev = requestHost.startsWith('localhost') || requestHost.startsWith('127.0.0.1');

    if (isLocalDev && contentType.includes('text/html')) {
      let html = await response.text();
      html = html.replaceAll(`https://${requestHost}`, `http://${requestHost}`);
      return new Response(html, {
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
    console.error("[Proxy Admin] Error conectando con el backend: ", error);
    return new Response("Error conectando con el backend", { status: 502 });
  }
};
