import { useEffect, useRef, useState } from 'react'
import { api, setToken } from './api'

type Account = { id: number; iban: string; balance: number; status: string }
type Me = { id: number; username: string; fullName: string; role: string; accounts: Account[] }

const FEE_RATE = 0.01 // frais de virement : 1 %

// ⚠️ Atelier A05-6 : page de confirmation qui injecte le paramètre ?msg= via innerHTML (XSS DOM).
// Le correctif repasse par du JSX classique ({msg}). Voir le README du module A05/A08.
function Done() {
  const msg = new URLSearchParams(window.location.search).get('msg') || ''
  const ref = useRef<HTMLDivElement>(null)
  useEffect(() => { if (ref.current) ref.current.innerHTML = msg }, [])
  return (
    <Shell right={<a href="/">Retour</a>}>
      <div className="card"><div className="ok" ref={ref} /></div>
    </Shell>
  )
}

export default function App() {
  const [me, setMe] = useState<Me | null>(null)
  const [view, setView] = useState<'home' | 'transfer' | 'history' | 'support' | 'admin' | 'forgot'>('home')
  const [loginError, setLoginError] = useState('')

  async function loadMe() {
    try { setMe(await api<Me>('/api/me')) } catch { setMe(null) }
  }
  useEffect(() => { loadMe() }, [])

  async function login(username: string, password: string) {
    try {
      const r = await api<{ token: string }>('/api/auth/login', 'POST', { username, password })
      setToken(r.token)
      setLoginError('')
      await loadMe()
    } catch (e) { setLoginError((e as Error).message) }
  }

  function logout() { setToken(null); setMe(null); setView('home') }

  // Page de confirmation (atelier A05-6), accessible par l'URL /done
  if (window.location.pathname === '/done') return <Done />

  if (view === 'forgot') return <Shell><Forgot onBack={() => setView('home')} /></Shell>
  if (!me) return (
    <Shell>
      <Login onLogin={login} error={loginError} />
      <p><a href="#" onClick={e => { e.preventDefault(); setView('forgot') }}>Mot de passe oublié ?</a></p>
    </Shell>
  )

  return (
    <Shell right={<>
      <span>{me.fullName}</span>
      <button onClick={() => setView('home')}>Comptes</button>
      <button onClick={() => setView('transfer')}>Virement</button>
      <button onClick={() => setView('history')}>Historique</button>
      <button onClick={() => setView('support')}>Support</button>
      {me.role === 'ADMIN' && <button onClick={() => setView('admin')}>Administration</button>}
      <button onClick={logout}>Déconnexion</button>
    </>}>
      {view === 'home' && <Accounts me={me} />}
      {view === 'transfer' && <Transfer me={me} onDone={loadMe} />}
      {view === 'history' && <History />}
      {view === 'support' && <Support me={me} />}
      {view === 'admin' && <Admin />}
    </Shell>
  )
}

function Shell({ children, right }: { children: React.ReactNode; right?: React.ReactNode }) {
  return (
    <>
      <header><h1>SecureBank</h1>{right}</header>
      <main>{children}</main>
    </>
  )
}

function Login({ onLogin, error }: { onLogin: (u: string, p: string) => void; error: string }) {
  const [u, setU] = useState('')
  const [p, setP] = useState('')
  return (
    <div className="card">
      <h2>Connexion</h2>
      <input placeholder="Identifiant" value={u} onChange={e => setU(e.target.value)} />
      <input placeholder="Mot de passe" type="password" value={p} onChange={e => setP(e.target.value)} />
      <button className="primary" onClick={() => onLogin(u, p)}>Se connecter</button>
      {error && <div className="error">{error}</div>}
    </div>
  )
}

function Accounts({ me }: { me: Me }) {
  const [detail, setDetail] = useState<Account | null>(null)
  async function open(id: number) { setDetail(await api<Account>(`/api/accounts/${id}`)) }
  return (
    <>
      <div className="card">
        <h2>Mes comptes</h2>
        <table>
          <thead><tr><th>IBAN</th><th>Solde</th><th>Statut</th><th /></tr></thead>
          <tbody>
            {me.accounts.map(a => (
              <tr key={a.id}>
                <td>{a.iban}</td><td>{a.balance} MAD</td><td>{a.status}</td>
                <td><button onClick={() => open(a.id)}>Détail</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {detail && <div className="card"><h3>Compte n°{detail.id}</h3><pre>{JSON.stringify(detail, null, 2)}</pre></div>}
    </>
  )
}

function Transfer({ me, onDone }: { me: Me; onDone: () => void }) {
  const [toIban, setToIban] = useState('')
  const [amount, setAmount] = useState('100')
  const [memo, setMemo] = useState('')
  const [error, setError] = useState('')
  const fee = (Number(amount) || 0) * FEE_RATE

  async function submit() {
    setError('')
    try {
      await api('/api/transfers', 'POST', {
        fromAccountId: me.accounts[0].id, toIban, amount: Number(amount), fee, memo,
      })
      onDone()
      // Redirige vers la page de confirmation (atelier A05-6)
      window.location.href = '/done?msg=' + encodeURIComponent('Virement effectué')
    } catch (e) { setError((e as Error).message) }
  }

  return (
    <div className="card">
      <h2>Nouveau virement</h2>
      <input placeholder="IBAN bénéficiaire (ex : MA64SBNK0000000002)" value={toIban} onChange={e => setToIban(e.target.value)} />
      <input type="number" value={amount} onChange={e => setAmount(e.target.value)} />
      <div>Frais calculés : {fee.toFixed(2)} MAD</div>
      <input placeholder="Motif" value={memo} onChange={e => setMemo(e.target.value)} />
      <button className="primary" onClick={submit}>Envoyer</button>
      {error && <div className="error">{error}</div>}
    </div>
  )
}

type TView = { id: number; memo: string; amount: number; status: string }

// La recherche appelle /api/transfers/search?q=… — cible des ateliers A05-2 et A05-3 (SQLi).
function History() {
  const [q, setQ] = useState('')
  const [rows, setRows] = useState<TView[]>([])
  const [error, setError] = useState('')
  async function search() {
    setError('')
    try { setRows(await api<TView[]>(`/api/transfers/search?q=${encodeURIComponent(q)}`)) }
    catch (e) { setError((e as Error).message) }
  }
  return (
    <div className="card">
      <h2>Historique des virements</h2>
      <input placeholder="Rechercher dans les motifs" value={q} onChange={e => setQ(e.target.value)} />
      <button className="primary" onClick={search}>Rechercher</button>
      {error && <div className="error">{error}</div>}
      <table>
        <thead><tr><th>Motif</th><th>Montant</th><th>Statut</th></tr></thead>
        <tbody>{rows.map(r => <tr key={r.id}><td>{r.memo}</td><td>{r.amount}</td><td>{r.status}</td></tr>)}</tbody>
      </table>
    </div>
  )
}

type SupportMsg = { id: number; author: string; message: string }

function Support({ me }: { me: Me }) {
  const [message, setMessage] = useState('')
  const [sent, setSent] = useState('')
  const [msgs, setMsgs] = useState<SupportMsg[]>([])
  async function load() { try { setMsgs(await api<SupportMsg[]>('/api/support')) } catch { /* ignore */ } }
  useEffect(() => { if (me.role === 'ADMIN') load() }, [])
  async function submit() {
    try { await api('/api/support', 'POST', { message }); setSent('Votre message a été transmis au support.'); setMessage('') }
    catch (e) { setSent((e as Error).message) }
  }
  return (
    <>
      <div className="card">
        <h2>Contacter le support</h2>
        <textarea value={message} onChange={e => setMessage(e.target.value)} placeholder="Votre message" />
        <button className="primary" onClick={submit}>Envoyer</button>
        {sent && <div className="ok">{sent}</div>}
      </div>
      {me.role === 'ADMIN' && (
        <div className="card">
          <h2>Messages support</h2>
          {msgs.map(m => (
            <div key={m.id} className="msg">
              <b>{m.author} :</b>{' '}
              {/* ⚠️ Atelier A05-5 : message affiché sans échappement (XSS stocké). Correctif : {m.message}. */}
              <span dangerouslySetInnerHTML={{ __html: m.message }} />
            </div>
          ))}
        </div>
      )}
    </>
  )
}

type U = { id: number; username: string; role: string }

function Admin() {
  const [users, setUsers] = useState<U[]>([])
  const [error, setError] = useState('')
  async function load() {
    try { setUsers(await api<U[]>('/api/admin/users')) } catch (e) { setError((e as Error).message) }
  }
  useEffect(() => { load() }, [])
  async function remove(id: number) { await api(`/api/users/${id}`, 'DELETE'); load() }
  async function setRole(id: number, role: string) { await api(`/api/users/${id}/role`, 'PUT', { role }); load() }
  return (
    <div className="card">
      <h2>Administration des utilisateurs</h2>
      {error && <div className="error">{error}</div>}
      <table>
        <tbody>
          {users.map(u => (
            <tr key={u.id}>
              <td>{u.username}</td><td>{u.role}</td>
              <td>
                <button onClick={() => setRole(u.id, u.role === 'ADMIN' ? 'USER' : 'ADMIN')}>Changer rôle</button>{' '}
                <button onClick={() => remove(u.id)}>Supprimer</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function Forgot({ onBack }: { onBack: () => void }) {
  const [username, setUsername] = useState('')
  const [otp, setOtp] = useState('')
  const [pwd, setPwd] = useState('')
  const [msg, setMsg] = useState('')
  async function send() { const r = await api<{ message: string }>('/api/auth/forgot', 'POST', { username }); setMsg(r.message) }
  async function reset() {
    try { const r = await api<{ message: string }>('/api/auth/reset', 'POST', { username, otp, newPassword: pwd }); setMsg(r.message) }
    catch (e) { setMsg((e as Error).message) }
  }
  return (
    <div className="card">
      <h2>Mot de passe oublié</h2>
      <input placeholder="Identifiant" value={username} onChange={e => setUsername(e.target.value)} />
      <button className="primary" onClick={send}>Envoyer le code</button>
      <input placeholder="Code à 4 chiffres" value={otp} onChange={e => setOtp(e.target.value)} />
      <input placeholder="Nouveau mot de passe" type="password" value={pwd} onChange={e => setPwd(e.target.value)} />
      <button className="primary" onClick={reset}>Réinitialiser</button>
      {msg && <div className="ok">{msg}</div>}
      <p><a href="#" onClick={e => { e.preventDefault(); onBack() }}>Retour</a></p>
    </div>
  )
}
