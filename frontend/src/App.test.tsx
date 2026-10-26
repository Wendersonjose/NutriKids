// @vitest-environment jsdom
import React from "react";
import { render, screen, waitFor, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { afterEach, beforeEach, describe, it, expect, vi } from "vitest";
import { App } from "./App";
import { api } from "./api";
vi.mock("./api", () => ({ api: vi.fn() }));
const mocked = vi.mocked(api);
afterEach(cleanup);
beforeEach(() => {
  mocked.mockReset();
});
describe("fluxos renderizados", () => {
  it("login envia credenciais e abre dados da API", async () => {
    mocked.mockImplementation(async (path) => {
      if (path === "/auth/me") throw new Error("401");
      if (path === "/auth/login")
        return {
          id: "admin",
          clinicaId: "clinica",
          nome: "Administrador",
          perfil: "ADMIN",
        };
      if (path === "/dashboard")
        return { pacientes: 0, consultasHoje: 0, agendadas: 0 };
      return [];
    });
    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>,
    );
    await screen.findByText("Acesse seu espaço");
    const u = userEvent.setup();
    await u.type(screen.getByLabelText("E-mail"), "sintetico@test.invalid");
    await u.type(screen.getByLabelText("Senha"), "SomenteTeste123!");
    await u.click(screen.getByRole("button", { name: "Entrar na plataforma" }));
    await screen.findByRole("heading", { name: /Olá, Administrador/ });
    expect(mocked).toHaveBeenCalledWith("/auth/login", "POST", {
      email: "sintetico@test.invalid",
      senha: "SomenteTeste123!",
    });
  });
  it("família não recebe links de administração ou agenda", async () => {
    mocked.mockImplementation(async (path) => {
      if (path === "/auth/me")
        return {
          id: "familia",
          clinicaId: "clinica",
          nome: "Responsável",
          perfil: "RESPONSAVEL",
        };
      if (path === "/dashboard") return { pacientes: 0, compartilhados: 0 };
      return [];
    });
    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>,
    );
    await screen.findByRole("link", { name: /Minhas crianças/ });
    expect(screen.queryByRole("link", { name: /Clínica e equipe/ })).toBeNull();
    expect(screen.queryByRole("link", { name: /Agenda/ })).toBeNull();
  });
  it("erro da API aparece na listagem de pacientes", async () => {
    mocked.mockImplementation(async (path) => {
      if (path === "/auth/me")
        return {
          id: "admin",
          clinicaId: "clinica",
          nome: "Administrador",
          perfil: "ADMIN",
        };
      throw new Error("Falha de conexão de teste");
    });
    render(
      <MemoryRouter initialEntries={["/pacientes"]}>
        <App />
      </MemoryRouter>,
    );
    await screen.findByRole("alert");
    expect(screen.getByRole("alert").textContent).toContain(
      "Falha de conexão de teste",
    );
  });
  it("sessão expirada retorna à tela de entrada", async () => {
    mocked.mockImplementation(async (path) => {
      if (path === "/auth/me")
        return {
          id: "admin",
          clinicaId: "clinica",
          nome: "Administrador",
          perfil: "ADMIN",
        };
      return [];
    });
    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>,
    );
    await screen.findByRole("link", { name: /Clínica e equipe/ });
    window.dispatchEvent(new Event("session-expired"));
    await waitFor(() =>
      expect(screen.getByText("Acesse seu espaço")).toBeTruthy(),
    );
  });
});
