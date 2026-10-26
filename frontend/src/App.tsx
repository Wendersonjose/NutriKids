import React, { useEffect, useState } from "react";
import {
  BrowserRouter,
  Routes,
  Route,
  NavLink,
  useParams,
  Link,
  Navigate,
} from "react-router-dom";
import {
  Leaf,
  LayoutDashboard,
  Users,
  CalendarDays,
  Settings,
  LogOut,
  Plus,
  ArrowUpRight,
  FileText,
  HeartPulse,
} from "lucide-react";
import { api } from "./api";
import "./style.css";
import { Crescimento, Memoria, Financeiro } from "./Modules";
type User = { id: string; clinicaId: string; perfil: string; nome: string };
type Patient = {
  id: string;
  nome: string;
  nascimento: string;
  sexo: string;
  observacoes?: string;
};
type Staff = {
  id: string;
  nome: string;
  email: string;
  perfil: string;
  crn?: string;
};
type Appointment = {
  id: string;
  pacienteId: string;
  profissionalId: string;
  inicio: string;
  fim: string;
  status: string;
};
type RecordItem = {
  id: string;
  tipo: string;
  conteudo: string;
  criadoEm: string;
  compartilhado: boolean;
};
type Measure = {
  id: string;
  data: string;
  pesoKg: number;
  alturaCm: number;
  posicao: string;
};
const date = (v: string) =>
  new Date(v.length === 10 ? v + "T12:00:00" : v).toLocaleDateString("pt-BR");
function ErrorBox({ error }: { error: string }) {
  return error ? (
    <p role="alert" className="error">
      {error}
    </p>
  ) : null;
}
function useLoad<T>(path: string, initial: T) {
  const [data, setData] = useState<T>(initial),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(true);
  async function reload() {
    setLoading(true);
    setError("");
    try {
      setData(await api<T>(path));
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    void reload();
  }, [path]);
  return { data, error, loading, reload };
}
function Form({
  onSave,
  children,
  submit = "Salvar",
}: {
  onSave: (d: FormData) => Promise<void>;
  children: React.ReactNode;
  submit?: string;
}) {
  const [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  return (
    <form
      onSubmit={async (e) => {
        e.preventDefault();
        const form = e.currentTarget;
        setError("");
        setBusy(true);
        try {
          await onSave(new FormData(form));
          form.reset();
        } catch (e) {
          setError((e as Error).message);
        } finally {
          setBusy(false);
        }
      }}
    >
      {children}
      <ErrorBox error={error} />
      <button disabled={busy} type="submit">
        {busy ? "Salvando…" : submit}
      </button>
    </form>
  );
}
function Field({
  name,
  label,
  type = "text",
  required = true,
}: {
  name: string;
  label: string;
  type?: string;
  required?: boolean;
}) {
  return (
    <label>
      {label}
      <input name={name} type={type} required={required} />
    </label>
  );
}
function Login({ onLogin }: { onLogin: (u: User) => void }) {
  return (
    <div className="login">
      <div className="login-story">
        <span className="brand">
          <Leaf /> nutri kids
        </span>
        <h1>
          Cuidado que acompanha
          <br />
          cada descoberta.
        </h1>
        <p>
          O consultório e a família, conectados em cada etapa do
          desenvolvimento.
        </p>
        <div className="orbit">
          🌱
          <small>
            Pequenos passos.
            <br />
            Novas possibilidades.
          </small>
        </div>
      </div>
      <section>
        <span className="eyebrow">BEM-VINDO À SUA CLÍNICA</span>
        <h2>Acesse seu espaço</h2>
        <p>Entre com sua conta profissional ou de responsável.</p>
        <Form
          submit="Entrar na plataforma"
          onSave={async (d) =>
            onLogin(
              await api<User>("/auth/login", "POST", Object.fromEntries(d)),
            )
          }
        >
          <Field name="email" label="E-mail" type="email" />
          <Field name="senha" label="Senha" type="password" />
        </Form>
        <small>
          O primeiro acesso é configurado pelo administrador.
          <br />
          Dados clínicos exigem acesso autorizado.
        </small>
      </section>
    </div>
  );
}
function Dashboard({ user }: { user: User }) {
  const p = useLoad<Patient[]>("/pacientes", []);
  const metrics = useLoad<Record<string, number>>("/dashboard", {});
  return (
    <>
      <header>
        <div>
          <span className="eyebrow">CUIDADO EM CONTINUIDADE</span>
          <h1>
            Olá, {user.nome.split(" ")[0]} <span className="wave">☀</span>
          </h1>
          <p>Um novo dia para acompanhar pequenas conquistas.</p>
        </div>
        <span className="date">
          {new Date().toLocaleDateString("pt-BR", {
            day: "numeric",
            month: "long",
            year: "numeric",
          })}
        </span>
      </header>
      <ErrorBox error={p.error || metrics.error} />
      <div className="stats">
        <article>
          <Users />
          <strong>{p.loading ? "…" : p.data.length}</strong>
          <span>
            {user.perfil === "RESPONSAVEL"
              ? "Crianças vinculadas"
              : "Pacientes cadastrados"}
          </span>
        </article>
        <article>
          <HeartPulse />
          <strong>
            {metrics.loading
              ? "…"
              : user.perfil === "RESPONSAVEL"
                ? metrics.data.compartilhados
                : metrics.data.consultasHoje}
          </strong>
          <span>
            {user.perfil === "RESPONSAVEL"
              ? "Registros compartilhados"
              : "Consultas hoje"}
          </span>
        </article>
        <article>
          <CalendarDays />
          <strong>
            {user.perfil === "RESPONSAVEL"
              ? "Família"
              : metrics.loading
                ? "…"
                : metrics.data.agendadas}
          </strong>
          <span>
            {user.perfil === "RESPONSAVEL"
              ? "Acesso por vínculo autorizado"
              : "Próximas consultas agendadas"}
          </span>
        </article>
      </div>
      <div className="hero-card">
        <div>
          <span className="eyebrow">CADA PASSO IMPORTA</span>
          <h2>
            O cuidado continua
            <br />
            entre as consultas.
          </h2>
          <p>
            Registre a evolução, organize os atendimentos e mantenha a família
            por perto.
          </p>
          <Link className="button" to="/pacientes">
            Acompanhar pacientes <ArrowUpRight size={16} />
          </Link>
        </div>
        <div className="seed">🌿</div>
      </div>
      <section className="panel">
        <div className="section-head">
          <h2>Pacientes em acompanhamento</h2>
          <Link to="/pacientes">Ver todos →</Link>
        </div>
        {p.loading ? (
          <p>Carregando pacientes…</p>
        ) : p.data.length ? (
          p.data.slice(0, 5).map((x) => (
            <Link className="patient-row" key={x.id} to={"/pacientes/" + x.id}>
              <span className="avatar">{x.nome.slice(0, 1)}</span>
              <div>
                <b>{x.nome}</b>
                <small>Nascimento: {date(x.nascimento)}</small>
              </div>
              <ArrowUpRight />
            </Link>
          ))
        ) : (
          <p className="empty">
            Nenhum paciente disponível.{" "}
            {user.perfil === "RESPONSAVEL"
              ? "Solicite o vínculo à clínica."
              : "Cadastre o primeiro paciente para começar."}
          </p>
        )}
      </section>
    </>
  );
}
function Patients({ user }: { user: User }) {
  const list = useLoad<Patient[]>("/pacientes", []);
  const [show, setShow] = useState(false),
    [search, setSearch] = useState("");
  return (
    <>
      <header>
        <div>
          <span className="eyebrow">ACOMPANHAMENTO</span>
          <h1>Pacientes</h1>
          <p>Uma história de cuidado para cada criança.</p>
        </div>
        {user.perfil !== "RESPONSAVEL" && (
          <button onClick={() => setShow(!show)}>
            <Plus size={16} /> Novo paciente
          </button>
        )}
      </header>
      {show && (
        <section className="panel">
          <h2>Cadastrar paciente</h2>
          <Form
            onSave={async (d) => {
              await api("/pacientes", "POST", Object.fromEntries(d));
              setShow(false);
              await list.reload();
            }}
          >
            <div className="form-grid">
              <Field name="nome" label="Nome completo" />
              <Field name="nascimento" label="Data de nascimento" type="date" />
              <label>
                Sexo registrado
                <select name="sexo">
                  <option value="NAO_INFORMADO">Não informado</option>
                  <option value="F">Feminino</option>
                  <option value="M">Masculino</option>
                </select>
              </label>
            </div>
            <label>
              Observações
              <textarea name="observacoes" maxLength={4000} />
            </label>
          </Form>
        </section>
      )}
      <section className="panel">
        <input
          aria-label="Buscar pacientes"
          placeholder="Buscar pelo nome…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <ErrorBox error={list.error} />
        {list.loading ? (
          <p>Carregando…</p>
        ) : (
          list.data
            .filter((x) => x.nome.toLowerCase().includes(search.toLowerCase()))
            .map((x) => (
              <Link
                className="patient-row"
                key={x.id}
                to={"/pacientes/" + x.id}
              >
                <span className="avatar">{x.nome[0]}</span>
                <div>
                  <b>{x.nome}</b>
                  <small>{date(x.nascimento)}</small>
                </div>
                <span className="tag">Ver ficha →</span>
              </Link>
            ))
        )}
        {!list.loading && !list.data.length && (
          <p className="empty">Nenhum paciente cadastrado ou vinculado.</p>
        )}
      </section>
    </>
  );
}
const types: Record<string, string> = {
  PRONTUARIO: "Prontuário",
  ANAMNESE: "Anamnese",
  PLANO_ALIMENTAR: "Plano alimentar",
  DIARIO_ALIMENTAR: "Diário alimentar",
  TERAPIA_ALIMENTAR: "Terapia alimentar",
  MENSAGEM: "Mensagens",
  META: "Metas",
};
function PatientDetail({ user }: { user: User }) {
  const { id } = useParams();
  const p = useLoad<Patient | null>("/pacientes/" + id, null),
    records = useLoad<RecordItem[]>("/pacientes/" + id + "/registros", []),
    measures = useLoad<Measure[]>("/pacientes/" + id + "/medidas", []);
  const family = user.perfil === "RESPONSAVEL";
  const [tab, setTab] = useState(family ? "DIARIO_ALIMENTAR" : "PRONTUARIO");
  return (
    <>
      <Link to="/pacientes">← Pacientes</Link>
      <header>
        <div>
          <span className="eyebrow">FICHA DO PACIENTE</span>
          <h1>{p.loading ? "Carregando…" : p.data?.nome}</h1>
          <p>{p.data && "Nascimento: " + date(p.data.nascimento)}</p>
        </div>
        <a
          className="button secondary"
          href={"/api/pacientes/" + id + "/relatorio.pdf"}
        >
          Exportar PDF
        </a>
      </header>
      <ErrorBox error={p.error} />
      <details><summary>Curvas e evolução OMS</summary><Crescimento id={id!} /></details>
      <details><summary>Jogo e acompanhamento</summary><Memoria id={id!} /></details>
      <div className="tabs">
        {Object.entries(types)
          .filter(
            ([k]) =>
              !family ||
              !["PRONTUARIO", "ANAMNESE", "TERAPIA_ALIMENTAR"].includes(k),
          )
          .map(([k, v]) => (
            <button
              key={k}
              className={tab === k ? "active" : ""}
              onClick={() => setTab(k)}
            >
              {v}
            </button>
          ))}
        <button
          className={tab === "MEDIDAS" ? "active" : ""}
          onClick={() => setTab("MEDIDAS")}
        >
          Antropometria
        </button>
      </div>
      {tab === "MEDIDAS" ? (
        <>
          <section className="panel">
            <h2>Medidas registradas</h2>
            <p>
              Histórico de medidas. Classificações e curvas OMS aguardam
              implementação e validação clínica.
            </p>
            <ErrorBox error={measures.error} />
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Data</th>
                    <th>Peso (kg)</th>
                    <th>Altura (cm)</th>
                    <th>Posição</th>
                  </tr>
                </thead>
                <tbody>
                  {measures.data.map((m) => (
                    <tr key={m.id}>
                      <td>{date(m.data)}</td>
                      <td>{m.pesoKg}</td>
                      <td>{m.alturaCm}</td>
                      <td>{m.posicao}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
          {!family && (
            <section className="panel">
              <h2>Nova medida</h2>
              <Form
                onSave={async (d) => {
                  await api("/pacientes/" + id + "/medidas", "POST", {
                    ...Object.fromEntries(d),
                    pesoKg: Number(d.get("pesoKg")),
                    alturaCm: Number(d.get("alturaCm")),
                  });
                  await measures.reload();
                }}
              >
                <div className="form-grid">
                  <Field name="data" label="Data da medição" type="date" />
                  <label>
                    Peso (kg)
                    <input
                      type="number"
                      step="0.01"
                      min="0.1"
                      max="400"
                      name="pesoKg"
                      required
                    />
                  </label>
                  <label>
                    Altura / comprimento (cm)
                    <input
                      type="number"
                      step="0.01"
                      min="20"
                      max="250"
                      name="alturaCm"
                      required
                    />
                  </label>
                  <label>
                    Posição
                    <select name="posicao">
                      <option value="EM_PE">Em pé</option>
                      <option value="DEITADO">Deitado</option>
                    </select>
                  </label>
                </div>
              </Form>
            </section>
          )}
        </>
      ) : (
        <>
          <section className="panel">
            <h2>{types[tab]}</h2>
            <p>
              {tab === "ANAMNESE"
                ? "Registre queixa, histórico, alergias, rotina alimentar e contexto familiar."
                : tab === "PLANO_ALIMENTAR"
                  ? "Orientações manuais do profissional. Sem cálculo automático de nutrientes."
                  : "Registros cronológicos preservam a autoria e o histórico."}
            </p>
            {(!family || ["DIARIO_ALIMENTAR", "MENSAGEM"].includes(tab)) && (
              <Form
                key={tab}
                submit="Adicionar ao histórico"
                onSave={async (d) => {
                  await api("/pacientes/" + id + "/registros", "POST", {
                    tipo: tab,
                    conteudo: d.get("conteudo"),
                    compartilhado: family || d.get("compartilhado") === "on",
                  });
                  await records.reload();
                }}
              >
                <label>
                  Registro
                  <textarea
                    name="conteudo"
                    required
                    maxLength={12000}
                    rows={6}
                  />
                </label>
                {!family && (
                  <label className="check">
                    <input type="checkbox" name="compartilhado" />{" "}
                    Disponibilizar este registro ao responsável
                  </label>
                )}
              </Form>
            )}
          </section>
          <section className="panel">
            <h2>Histórico</h2>
            <ErrorBox error={records.error} />
            {records.loading ? (
              <p>Carregando…</p>
            ) : (
              records.data
                .filter((r) => r.tipo === tab)
                .map((r) => (
                  <article className="record" key={r.id}>
                    <div>
                      <span className="tag">
                        {r.compartilhado
                          ? "Compartilhado com a família"
                          : "Clínico • restrito"}
                      </span>
                      <small>
                        {new Date(r.criadoEm).toLocaleString("pt-BR")}
                      </small>
                    </div>
                    <p>{r.conteudo}</p>
                  </article>
                ))
            )}
            {!records.loading && !records.data.some((r) => r.tipo === tab) && (
              <p className="empty">Ainda não há registros nesta seção.</p>
            )}
          </section>
        </>
      )}
      <Attachments id={id!} family={family} />
      {!family && (
        <>
          <EditPatient patient={p.data} onSaved={p.reload} />
          <FamilyLink id={id!} />
        </>
      )}
    </>
  );
}
function EditPatient({
  patient,
  onSaved,
}: {
  patient: Patient | null;
  onSaved: () => Promise<void>;
}) {
  if (!patient) return null;
  return (
    <details className="panel">
      <summary>Editar cadastro do paciente</summary>
      <Form
        onSave={async (d) => {
          await api("/pacientes/" + patient.id, "PUT", Object.fromEntries(d));
          await onSaved();
        }}
      >
        <label>
          Nome
          <input
            name="nome"
            defaultValue={patient.nome}
            required
            maxLength={150}
          />
        </label>
        <div className="form-grid">
          <label>
            Nascimento
            <input
              name="nascimento"
              type="date"
              defaultValue={patient.nascimento}
              required
            />
          </label>
          <label>
            Sexo
            <select name="sexo" defaultValue={patient.sexo}>
              <option value="NAO_INFORMADO">Não informado</option>
              <option value="F">Feminino</option>
              <option value="M">Masculino</option>
            </select>
          </label>
        </div>
        <label>
          Observações clínicas restritas
          <textarea
            name="observacoes"
            defaultValue={patient.observacoes || ""}
            maxLength={4000}
          />
        </label>
      </Form>
    </details>
  );
}
function Attachments({ id, family }: { id: string; family: boolean }) {
  const list = useLoad<
    { id: string; titulo: string; mime: string; compartilhado: boolean }[]
  >("/pacientes/" + id + "/anexos", []);
  return (
    <section className="panel">
      <h2>Fotos e exames</h2>
      <p>
        PDF, PNG ou JPEG, até 5 MB. Arquivos protegidos pelo acesso ao paciente.
      </p>
      <ErrorBox error={list.error} />
      {list.data.map((a) => (
        <div className="patient-row" key={a.id}>
          <a href={"/api/pacientes/" + id + "/anexos/" + a.id}>{a.titulo} ↓</a>
          <span className="tag">
            {a.compartilhado ? "Família" : "Restrito"}
          </span>
        </div>
      ))}
      <Form
        submit="Enviar arquivo"
        onSave={async (d) => {
          await api("/pacientes/" + id + "/anexos", "POST", d);
          await list.reload();
        }}
      >
        <Field name="titulo" label="Descrição do arquivo" />
        <label>
          Arquivo
          <input
            type="file"
            name="arquivo"
            accept="application/pdf,image/png,image/jpeg"
            required
          />
        </label>
        {!family && (
          <label className="check">
            <input type="checkbox" name="compartilhado" value="true" />{" "}
            Compartilhar com a família
          </label>
        )}
      </Form>
    </section>
  );
}
function FamilyLink({ id }: { id: string }) {
  const team = useLoad<Staff[]>("/equipe", []);
  return (
    <details className="panel">
      <summary>Vincular responsável (administrador)</summary>
      <Form
        onSave={async (d) => {
          await api(
            "/pacientes/" + id + "/responsaveis",
            "POST",
            Object.fromEntries(d),
          );
          alert("Responsável vinculado.");
        }}
      >
        <label>
          Responsável
          <select name="usuarioId" required>
            <option value="">Selecione</option>
            {team.data
              .filter((x) => x.perfil === "RESPONSAVEL")
              .map((x) => (
                <option value={x.id} key={x.id}>
                  {x.nome} · {x.email}
                </option>
              ))}
          </select>
        </label>
        <Field name="parentesco" label="Parentesco" />
      </Form>
    </details>
  );
}
function Agenda() {
  const list = useLoad<Appointment[]>("/agenda", []),
    patients = useLoad<Patient[]>("/pacientes", []),
    team = useLoad<Staff[]>("/equipe", []);
  const [show, setShow] = useState(false);
  const [actionError, setActionError] = useState("");
  async function status(id: string, s: string) {
    try {
      await api("/agenda/" + id, "PATCH", { status: s });
      await list.reload();
    } catch (e) {
      setActionError((e as Error).message);
    }
  }
  return (
    <>
      <header>
        <div>
          <span className="eyebrow">ROTINA DA CLÍNICA</span>
          <h1>Agenda</h1>
          <p>Organize os próximos encontros.</p>
        </div>
        <button onClick={() => setShow(!show)}>
          <Plus size={16} /> Agendar consulta
        </button>
      </header>
      {show && (
        <section className="panel">
          <h2>Nova consulta</h2>
          <Form
            onSave={async (d) => {
              await api("/agenda", "POST", {
                pacienteId: d.get("pacienteId"),
                profissionalId: d.get("profissionalId"),
                inicio: new Date(String(d.get("inicio"))).toISOString(),
                fim: new Date(String(d.get("fim"))).toISOString(),
              });
              setShow(false);
              await list.reload();
            }}
          >
            <div className="form-grid">
              <label>
                Paciente
                <select name="pacienteId" required>
                  <option value="">Selecione</option>
                  {patients.data.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.nome}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Profissional
                <select name="profissionalId" required>
                  <option value="">Selecione</option>
                  {team.data
                    .filter((u) => u.perfil !== "RESPONSAVEL")
                    .map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.nome}
                      </option>
                    ))}
                </select>
              </label>
              <Field name="inicio" label="Início" type="datetime-local" />
              <Field name="fim" label="Término" type="datetime-local" />
            </div>
          </Form>
        </section>
      )}
      <section className="panel">
        <h2>Consultas</h2>
        <ErrorBox error={list.error || actionError} />
        {list.loading ? (
          <p>Carregando…</p>
        ) : (
          list.data.map((c) => (
            <div className="appointment" key={c.id}>
              <span className="calendar-icon">
                <CalendarDays />
              </span>
              <div>
                <b>
                  {patients.data.find((p) => p.id === c.pacienteId)?.nome ||
                    "Paciente"}
                </b>
                <small>
                  {new Date(c.inicio).toLocaleString("pt-BR")} até{" "}
                  {new Date(c.fim).toLocaleTimeString("pt-BR", {
                    hour: "2-digit",
                    minute: "2-digit",
                  })}
                </small>
                <span className="tag">{c.status}</span>
              </div>
              {c.status === "AGENDADA" && (
                <div className="actions">
                  <button
                    className="secondary"
                    onClick={() => void status(c.id, "CONCLUIDA")}
                  >
                    Concluir
                  </button>
                  <button
                    className="ghost"
                    onClick={() => void status(c.id, "CANCELADA")}
                  >
                    Cancelar
                  </button>
                </div>
              )}
            </div>
          ))
        )}
        {!list.loading && !list.data.length && (
          <p className="empty">
            Sua agenda está livre. Cadastre a primeira consulta.
          </p>
        )}
      </section>
    </>
  );
}
function SettingsPage({ user }: { user: User }) {
  const team = useLoad<Staff[]>("/equipe", []),
    clinic = useLoad<{ nome: string } | null>("/clinica", null);
  return (
    <>
      <header>
        <div>
          <span className="eyebrow">GESTÃO</span>
          <h1>Clínica e equipe</h1>
          <p>Cadastros e permissões de acesso.</p>
        </div>
      </header>
      <ErrorBox error={team.error || clinic.error} />
      <section className="panel">
        <h2>{clinic.data?.nome || "Clínica"}</h2>
        {user.perfil === "ADMIN" && (
          <Form
            onSave={async (d) => {
              await api("/clinica", "PUT", Object.fromEntries(d));
              await clinic.reload();
            }}
          >
            <Field name="nome" label="Nome da clínica" />
          </Form>
        )}
      </section>
      <section className="panel">
        <h2>Equipe e responsáveis</h2>
        {team.data.map((u) => (
          <div className="patient-row" key={u.id}>
            <span className="avatar">{u.nome[0]}</span>
            <div>
              <b>{u.nome}</b>
              <small>{u.email}</small>
            </div>
            <span className="tag">{u.perfil}</span>
          </div>
        ))}
      </section>
      {user.perfil === "ADMIN" && (
        <section className="panel">
          <h2>Criar acesso</h2>
          <p>
            Cadastre o responsável e depois vincule-o na ficha da criança. Senha
            inicial: pelo menos 12 caracteres.
          </p>
          <Form
            onSave={async (d) => {
              await api("/equipe", "POST", Object.fromEntries(d));
              await team.reload();
            }}
          >
            <div className="form-grid">
              <Field name="nome" label="Nome" />
              <Field name="email" label="E-mail" type="email" />
              <Field name="senha" label="Senha inicial" type="password" />
              <Field name="crn" label="CRN (profissional)" required={false} />
              <label>
                Perfil
                <select name="perfil">
                  <option value="NUTRICIONISTA">Nutricionista</option>
                  <option value="RESPONSAVEL">Responsável</option>
                  <option value="ADMIN">Administrador</option>
                </select>
              </label>
            </div>
          </Form>
        </section>
      )}
    </>
  );
}
export function App() {
  const [user, setUser] = useState<User | null>(null),
    [loading, setLoading] = useState(true),
    [error, setError] = useState("");
  useEffect(() => {
    api<User>("/auth/me")
      .then(setUser)
      .catch(() => {})
      .finally(() => setLoading(false));
    const clear = () => setUser(null);
    window.addEventListener("session-expired", clear);
    return () => window.removeEventListener("session-expired", clear);
  }, []);
  if (loading) return <div className="loading">Conectando à clínica…</div>;
  if (!user) return <Login onLogin={setUser} />;
  const family = user.perfil === "RESPONSAVEL";
  return (
    <div className="shell">
      <aside>
        <Link to="/" className="brand">
          <Leaf /> nutri kids<span>ESPAÇO DE CUIDADO</span>
        </Link>
        <small className="nav-label">
          {family ? "ESPAÇO DA FAMÍLIA" : "SEU CONSULTÓRIO"}
        </small>
        <nav>
          <NavLink to="/" end>
            <LayoutDashboard size={19} /> Visão geral
          </NavLink>
          <NavLink to="/pacientes">
            <Users size={19} />
            {family ? "Minhas crianças" : "Pacientes"}
          </NavLink>
          {!family && (
            <>
              <NavLink to="/agenda">
                <CalendarDays size={19} /> Agenda
              </NavLink>
              <NavLink to="/configuracoes">
                <Settings size={19} /> Clínica e equipe
              </NavLink>
            </>
          )}
          {user.perfil === "ADMIN" && <NavLink to="/financeiro">Financeiro</NavLink>}
        </nav>
        <div className="sidebar-note">
          <Leaf size={22} />
          <p>
            Cada descoberta
            <br />
            merece cuidado.
          </p>
          <small>Nutrição com propósito.</small>
        </div>
        <div className="user">
          <span className="avatar">{user.nome[0]}</span>
          <div>
            <b>{user.nome}</b>
            <small>{user.perfil}</small>
          </div>
        </div>
        <button
          className="logout"
          onClick={async () => {
            try {
              await api("/auth/logout", "POST");
              setUser(null);
            } catch (e) {
              setError((e as Error).message);
            }
          }}
        >
          <LogOut size={17} /> Sair
        </button>
      </aside>
      <main>
        <ErrorBox error={error} />
        <Routes>
          <Route path="/" element={<Dashboard user={user} />} />
          <Route path="/pacientes" element={<Patients user={user} />} />
          <Route
            path="/pacientes/:id"
            element={<PatientDetail user={user} />}
          />
          {!family && (
            <>
              <Route path="/agenda" element={<Agenda />} />
              <Route
                path="/configuracoes"
                element={<SettingsPage user={user} />}
              />
            </>
          )}
          {user.perfil === "ADMIN" && <Route path="/financeiro" element={<Financeiro />} />}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
        <footer>Nutri Kids · Apoio ao acompanhamento profissional</footer>
      </main>
    </div>
  );
}
