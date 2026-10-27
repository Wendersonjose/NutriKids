import { useState, useEffect } from "react";
import { api } from "./api";
type Score = {
  indicador: string;
  z: number | null;
  percentil: number | null;
  inconsistente: boolean;
  aviso: string;
};
type Row = { data: string; resultados: Score[] };
export function Crescimento({ id }: { id: string }) {
  const [rows, setRows] = useState<Row[]>([]),
    [error, setError] = useState(""),
    [edema, setEdema] = useState<string>(""),
    [indicador, setIndicador] = useState("wei");
  useEffect(() => {
    setRows([]);
    if (edema === "") return;
    api<Row[]>("/pacientes/" + id + "/crescimento?edema=" + edema)
      .then(setRows)
      .catch((e) => setError(e.message));
  }, [id, edema]);
  const points = rows.map((r, n) => ({
    data: r.data,
    n,
    s: r.resultados.find((s) => s.indicador === indicador)!,
  }));
  return (
    <section className="panel">
      <h2>Evolução OMS 2006 — escore Z</h2>
      <p>
        Menores de 60 meses. Revisão profissional obrigatória. Sem diagnóstico
        automático.
      </p>
      <label>
        Edema informado para esta visualização
        <select value={edema} onChange={(e) => setEdema(e.target.value)}>
          <option value="">Selecione explicitamente</option>
          <option value="false">Não</option>
          <option value="true">Sim</option>
        </select>
      </label>
      <p>
        Para históricos com edema variável, revisar cada avaliação
        individualmente.
      </p>
      <select
        aria-label="Indicador"
        value={indicador}
        onChange={(e) => setIndicador(e.target.value)}
      >
        <option value="wei">Peso por idade</option>
        <option value="len">Comprimento/altura por idade</option>
        <option value="bmi">IMC por idade</option>
        <option value="wfl">Peso por comprimento/altura</option>
      </select>
      <p role="alert">{error}</p>
      <svg
        viewBox="0 0 640 260"
        role="img"
        aria-label="Evolução dos escores Z por sequência de avaliações"
        style={{ width: "100%" }}
      >
        {[-3, -2, 0, 2, 3].map((z) => (
          <g key={z}>
            <line
              x1="40"
              x2="620"
              y1={130 - z * 25}
              y2={130 - z * 25}
              stroke={z === 0 ? "#287c61" : "#cbd5d1"}
            />
            <text x="5" y={135 - z * 25}>
              {z}
            </text>
          </g>
        ))}
        {points
          .filter((p) => p.s.z !== null)
          .map((p) => (
            <circle
              key={p.n}
              cx={55 + (p.n * 550) / Math.max(1, points.length - 1)}
              cy={130 - Math.max(-4.5, Math.min(4.5, p.s.z!)) * 25}
              r="5"
              fill={p.s.inconsistente ? "#af4828" : "#287c61"}
            >
              <title>
                {p.data}: Z {p.s.z}
              </title>
            </circle>
          ))}
      </svg>
      <small>
        Eixo X: sequência cronológica; pontos além de ±4,5 aparecem no limite
        visual. Linhas: referências de escore Z, não medidas físicas.
      </small>
      <table>
        <thead>
          <tr>
            <th>Data</th>
            <th>Z</th>
            <th>Percentil</th>
            <th>Revisão</th>
          </tr>
        </thead>
        <tbody>
          {points.map((p) => (
            <tr key={p.n}>
              <td>{p.data}</td>
              <td>{p.s.z ?? "Indisponível"}</td>
              <td>{p.s.percentil?.toFixed(2) ?? "—"}</td>
              <td>{p.s.aviso}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {edema !== "" && !rows.length && <p>Nenhuma medição registrada.</p>}
    </section>
  );
}
type Game = {
  id: string;
  cartas: string[];
  tentativas: number;
  concluida: boolean;
  conquista: string;
  reveladas?: Record<string, string>;
};
export function Memoria({ id }: { id: string }) {
  const [game, setGame] = useState<Game | null>(null),
    [selected, setSelected] = useState<number | null>(null),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false),
    [history, setHistory] = useState<Game[]>([]);
  const path = "/pacientes/" + id + "/jogos/memoria";
  const load = () =>
    api<Game[]>(path)
      .then(setHistory)
      .catch((e) => setError(e.message));
  useEffect(() => {
    void load();
  }, [id]);
  async function start() {
    setBusy(true);
    try {
      setGame(await api<Game>(path, "POST"));
      setSelected(null);
      await load();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }
  async function turn(n: number) {
    if (!game || busy || game.concluida || game.cartas[n] !== "?") return;
    if (selected === null) {
      setSelected(n);
      return;
    }
    if (selected === n) {
      setSelected(null);
      return;
    }
    setBusy(true);
    try {
      setGame(
        await api<Game>(path + "/" + game.id + "/jogadas", "POST", {
          primeira: selected,
          segunda: n,
        }),
      );
      setSelected(null);
      await load();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <section className="panel">
      <h2>Memória dos alimentos</h2>
      <p>
        Descubra pares e conheça os nomes. Explore no seu ritmo, sem limite de
        tempo.
      </p>
      <button disabled={busy} onClick={start}>
        Nova descoberta
      </button>
      <p role="alert">{error}</p>
      {game && (
        <>
          <p>
            {game.conquista} · {game.tentativas} descobertas
          </p>
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(3,1fr)",
              gap: 10,
            }}
          >
            {game.cartas.map((c, n) => (
              <button
                key={n}
                disabled={busy || c !== "?" || game.concluida}
                aria-pressed={selected === n}
                onClick={() => turn(n)}
                style={{
                  minHeight: 70,
                  border: selected === n ? "3px solid #287c61" : undefined,
                }}
              >
                {c !== "?" ? c : (game.reveladas?.[n] ?? "Carta " + (n + 1))}
              </button>
            ))}
          </div>
          {game.reveladas && (
            <p>
              Último par: {Object.values(game.reveladas).join(" e ")}. Escolha
              outro par para continuar.
            </p>
          )}
        </>
      )}
      <h3>Acompanhamento</h3>
      <p>
        {history.filter((g) => g.concluida).length} partidas concluídas /{" "}
        {history.length} iniciadas
      </p>
      {history.slice(0, 5).map((g) => (
        <button
          key={g.id}
          onClick={() => {
            setGame(g);
            setSelected(null);
          }}
        >
          {g.conquista} · retomar
        </button>
      ))}
    </section>
  );
}
const hoje = () =>
  new Intl.DateTimeFormat("sv-SE", { timeZone: "America/Sao_Paulo" }).format(
    new Date(),
  );
type Entry = {
  id: string;
  tipo: string;
  descricao: string;
  valor: number;
  competencia: string;
  vencimento: string;
  pagamento: string | null;
  categoria: string;
  conta: string;
};
export function Financeiro() {
  const [inicio, setInicio] = useState(hoje().slice(0, 7) + "-01"),
    [fim, setFim] = useState(hoje()),
    [rows, setRows] = useState<Entry[]>([]),
    [summary, setSummary] = useState<Record<string, number>>({}),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  async function load() {
    try {
      const q = "?inicio=" + inicio + "&fim=" + fim;
      const [r, s] = await Promise.all([
        api<Entry[]>("/financeiro" + q),
        api<Record<string, number>>("/financeiro/resumo" + q),
      ]);
      setRows(r);
      setSummary(s);
      setError("");
    } catch (e) {
      setError((e as Error).message);
    }
  }
  useEffect(() => {
    void load();
  }, [inicio, fim]);
  async function create(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = e.currentTarget,
      d = Object.fromEntries(new FormData(form));
    setBusy(true);
    try {
      await api("/financeiro", "POST", { ...d, valor: Number(d.valor) });
      form.reset();
      await load();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }
  async function pay(row: Entry) {
    const referencia = window.prompt("Referência bancária da conciliação:");
    if (!referencia) return;
    setBusy(true);
    try {
      await api("/financeiro/" + row.id + "/liquidar", "POST", {
        pagamento: hoje(),
        referencia,
      });
      await load();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <h1>Financeiro da clínica</h1>
      <p>Resumo por competência. Não inclui assinaturas do SaaS.</p>
      <label>
        Competência inicial
        <input
          type="date"
          value={inicio}
          onChange={(e) => setInicio(e.target.value)}
        />
      </label>
      <label>
        Competência final
        <input
          type="date"
          value={fim}
          onChange={(e) => setFim(e.target.value)}
        />
      </label>
      <p role="alert">{error}</p>
      <section className="panel">
        {Object.entries(summary).map(([k, v]) => (
          <p key={k}>
            {k}:{" "}
            {v.toLocaleString("pt-BR", { style: "currency", currency: "BRL" })}
          </p>
        ))}
      </section>
      <form className="panel" onSubmit={create}>
        <h2>Novo lançamento</h2>
        <label>
          Tipo
          <select name="tipo">
            <option>RECEITA</option>
            <option>DESPESA</option>
          </select>
        </label>
        {["descricao", "categoria", "conta"].map((k) => (
          <label key={k}>
            {k}
            <input name={k} maxLength={255} required />
          </label>
        ))}
        <label>
          Valor
          <input name="valor" type="number" step="0.01" min="0.01" required />
        </label>
        <label>
          Competência
          <input name="competencia" type="date" required />
        </label>
        <label>
          Vencimento
          <input name="vencimento" type="date" required />
        </label>
        <button disabled={busy}>Salvar</button>
      </form>
      <section className="panel">
        <h2>Lançamentos</h2>
        <table>
          <thead>
            <tr>
              <th>Descrição</th>
              <th>Valor</th>
              <th>Vencimento</th>
              <th>Situação</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>
                <td>
                  {r.tipo} · {r.descricao}
                </td>
                <td>{r.valor.toFixed(2)}</td>
                <td>{r.vencimento}</td>
                <td>
                  {r.pagamento ? (
                    "Liquidado em " + r.pagamento
                  ) : (
                    <button disabled={busy} onClick={() => pay(r)}>
                      Liquidar / conciliar
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </>
  );
}
