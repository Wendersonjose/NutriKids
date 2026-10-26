import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
describe("cliente HTTP", () => {
  beforeEach(() => {
    vi.resetModules();
    vi.stubGlobal("window", { dispatchEvent: vi.fn() });
  });
  afterEach(() => vi.unstubAllGlobals());
  it("obtém CSRF antes de enviar mutação com cookie", async () => {
    const fetcher = vi
      .fn()
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ token: "sintetico" })),
      )
      .mockResolvedValueOnce(new Response(JSON.stringify({ id: 1 })));
    vi.stubGlobal("fetch", fetcher);
    const { api } = await import("./api");
    expect(await api("/pacientes", "POST", { nome: "Sintético" })).toEqual({
      id: 1,
    });
    expect(fetcher.mock.calls[1][1].headers["X-XSRF-TOKEN"]).toBe("sintetico");
    expect(fetcher.mock.calls[1][1].credentials).toBe("same-origin");
  });
  it("exibe erros de validação da API", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            mensagem: "Revise",
            campos: ["nome: obrigatório"],
          }),
          { status: 400 },
        ),
      ),
    );
    const { api } = await import("./api");
    await expect(api("/pacientes")).rejects.toThrow("nome: obrigatório");
  });
  it("sinaliza expiração da sessão", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(new Response("", { status: 401 })),
    );
    const { api } = await import("./api");
    await expect(api("/pacientes")).rejects.toThrow("401");
    expect(window.dispatchEvent).toHaveBeenCalled();
  });
  it("envia FormData sem forçar Content-Type JSON", async () => {
    const fetcher = vi
      .fn()
      .mockResolvedValueOnce(new Response('{"token":"teste"}'))
      .mockResolvedValueOnce(new Response('{"id":1}'));
    vi.stubGlobal("fetch", fetcher);
    const { api } = await import("./api");
    const data = new FormData();
    data.append("titulo", "Sintético");
    await api("/pacientes/id/anexos", "POST", data);
    expect(fetcher.mock.calls[1][1].headers["Content-Type"]).toBeUndefined();
    expect(fetcher.mock.calls[1][1].body).toBe(data);
  });
  it("logout aceita resposta sem conteúdo", async () => {
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValueOnce(new Response('{"token":"teste"}'))
        .mockResolvedValueOnce(new Response(null, { status: 204 })),
    );
    const { api } = await import("./api");
    expect(await api("/auth/logout", "POST")).toBeUndefined();
  });
});
