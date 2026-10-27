// @vitest-environment jsdom
import React from "react";
import { render, screen, cleanup, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, it, expect, vi } from "vitest";
import { Crescimento, Memoria } from "./Modules";
import { PlanoEditor } from "./Nutricao";
import { api } from "./api";
vi.mock("./api", () => ({ api: vi.fn() }));
const mocked = vi.mocked(api);
afterEach(cleanup);
beforeEach(() => mocked.mockReset());
it("solicita edema explicitamente antes de calcular", async () => {
  mocked.mockResolvedValue([]);
  render(<Crescimento id="p" />);
  expect(mocked).not.toHaveBeenCalled();
  await userEvent.selectOptions(
    screen.getByLabelText("Edema informado para esta visualização"),
    "false",
  );
  await waitFor(() =>
    expect(mocked).toHaveBeenCalledWith("/pacientes/p/crescimento?edema=false"),
  );
});
it("jogo envia duas posições e usa o progresso devolvido pelo servidor", async () => {
  const game = {
    id: "g",
    cartas: Array(12).fill("?"),
    tentativas: 0,
    concluida: false,
    conquista: "Em descoberta",
  };
  mocked.mockImplementation(async (_p, method) =>
    method === "POST" ? game : [],
  );
  render(<Memoria id="p" />);
  const u = userEvent.setup();
  await u.click(screen.getByRole("button", { name: "Nova descoberta" }));
  await u.click(await screen.findByRole("button", { name: "Carta 1" }));
  await u.click(screen.getByRole("button", { name: "Carta 2" }));
  await waitFor(() =>
    expect(mocked).toHaveBeenCalledWith(
      "/pacientes/p/jogos/memoria/g/jogadas",
      "POST",
      { primeira: 0, segunda: 1 },
    ),
  );
});
it("não oferece salvamento de plano sem ingredientes", async () => {
  mocked.mockResolvedValue([]);
  render(<PlanoEditor id="p" />);
  expect(
    (
      screen.getByRole("button", {
        name: "Salvar nova versão",
      }) as HTMLButtonElement
    ).disabled,
  ).toBe(true);
  await waitFor(() => expect(mocked).toHaveBeenCalledWith("/alimentos"));
});
