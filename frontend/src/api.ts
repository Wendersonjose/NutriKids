let csrf = "";
export async function api<T = unknown>(
  path: string,
  method = "GET",
  body?: unknown,
): Promise<T> {
  if (method !== "GET" && !csrf) {
    const r = await fetch("/api/auth/csrf", { credentials: "same-origin" });
    csrf = (await r.json()).token;
  }
  const r = await fetch("/api" + path, {
    method,
    credentials: "same-origin",
    headers: {
      ...(body && !(body instanceof FormData)
        ? { "Content-Type": "application/json" }
        : {}),
      ...(method !== "GET" ? { "X-XSRF-TOKEN": csrf } : {}),
    },
    body:
      body instanceof FormData ? body : body ? JSON.stringify(body) : undefined,
  });
  if (!r.ok) {
    if (r.status === 401 && path != "/auth/login")
      window.dispatchEvent(new Event("session-expired"));
    let message = "Não foi possível concluir a operação (" + r.status + ").";
    try {
      const e = await r.json();
      message = e.mensagem || e.detail || message;
      if (e.campos) message += " " + e.campos.join("; ");
    } catch {}
    throw new Error(message);
  }
  if (path === "/auth/login" || path === "/auth/logout") csrf = "";
  if (r.status === 204 || r.headers.get("content-length") === "0")
    return undefined as T;
  const text = await r.text();
  return (text ? JSON.parse(text) : undefined) as T;
}
