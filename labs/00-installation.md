# SecureBank · Installation et découverte

Cette étape installe SecureBank et l'outil des ateliers (**REST Client**), fait découvrir l'application comme un utilisateur, puis apprend à **penser comme un attaquant avant d'écrire une ligne de code**.

> ⚠️ Application volontairement vulnérable : à exécuter **uniquement en local**.

---

## 1. Prérequis

* **Docker** et **Docker Compose**
* **Visual Studio Code**
* **Un navigateur web**
* **Python 3**

## 2. Déploiement

```bash
docker compose up --build
```
> Le premier lancement prend 3 à 5 minutes (dépendances Maven et npm).

| Service | Rôle | URL |
|---|---|---|
| **web** | Interface bancaire (React) | http://localhost:3000 |
| **api** | Backend Spring Boot | http://localhost:8080 |
| **fraud** | Microservice antifraude | http://localhost:8081 |

```text
Navigateur ──► web (NGINX :3000) ──/api──► api (Spring Boot :8080) ──► H2 (en mémoire, dans l'API)
                                                  │
                                                  └──► fraud (antifraude :8081)
```

Remise à zéro complète de la base (les attaques modifient réellement les données) :
```bash
docker compose down -v && docker compose up -d --build
```

## 3. Comptes de test

| Utilisateur | Mot de passe | Rôle | Compte (id) | IBAN | Solde initial |
|---|---|---|---|---|---|
| **alice** | `alice123` | `USER` | 1 | `MA64SBNK0000000001` | 12 000 MAD |
| **bob** | `bob123` | `USER` | 2 | `MA64SBNK0000000002` | 8 000 MAD |
| **charlie** | `charlie123` | `USER` | 3 | `MA64SBNK0000000003` | 0 MAD · *compte clôturé* |
| **admin** | `Adm1n-Lab-2026` | `ADMIN` | 4 | `MA64SBNK0000000004` | 50 000 MAD |

Toutes les données sont fictives.

---

## 4. L'outil des ateliers : REST Client

Toutes les attaques se jouent avec l'extension **REST Client** de VS Code : on écrit les requêtes dans un fichier `.http`, un lien **« Send Request »** apparaît au-dessus de chacune, la réponse s'affiche à côté.

> Pourquoi pas l'interface ? Parce qu'une attaque passe **par-dessus** le navigateur. En envoyant la requête brute, on voit que le client ne protège rien.

1. Ouvrez VS Code → panneau **Extensions** (`Ctrl+Shift+X`).
2. Cherchez **REST Client** (éditeur *Huachao Mao*) et cliquez **Install**.
   *En ligne de commande :* `code --install-extension humao.rest-client`
3. Ouvrez le fichier `securebank.http` à la racine du projet. Il commence par :
   ```http
   @api = http://localhost:8080

   ### Connexion d'Alice — exécutez, puis réutilisez le jeton
   # @name loginAlice
   POST {{api}}/api/auth/login
   Content-Type: application/json

   { "username": "alice", "password": "alice123" }
   ```
4. Au-dessus de `POST`, cliquez **« Send Request »**. La réponse s'ouvre à droite : vous devez y voir un `token`.

**À retenir sur l'outil :**
* Les requêtes sont séparées par `###`.
* `# @name loginAlice` **nomme** une requête : son jeton se réutilise ensuite avec `{{loginAlice.response.body.token}}`, sans copier-coller.
* `@api = …` définit une variable réutilisable.

Ajoutez la connexion de l'admin, utile pour plusieurs ateliers :
```http
### @name loginAdmin
POST {{api}}/api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "Adm1n-Lab-2026" }
```
Un clic sur **Send Request** d'un `login` rafraîchit le jeton (il expire au bout d'une heure).

## 5. Vérifier que tout fonctionne

1. **Interface :** ouvrez http://localhost:3000. Vous devez voir l'écran de connexion.
2. **API et antifraude :** ajoutez ces blocs à `securebank.http`, puis **Send Request** sur chacun.
   ```http
   @fraud = http://localhost:8081

   ### Santé de l'API — attendu : 200 OK
   GET {{api}}/actuator/health

   ### Antifraude — attendu : {"approved":true,"reason":"OK"}
   POST {{fraud}}/check
   Content-Type: application/json

   { "amount": 100, "memo": "test" }
   ```
3. **Journaux :** dans un terminal dédié, laissez tourner les logs de l'API (vous y lirez les e-mails simulés) :
   ```bash
   docker compose logs -f api
   ```

## 6. Découverte guidée : penser comme un attaquant

Avant de chercher des failles, il faut comprendre **ce que l'application est censée faire**.

1. Connectez-vous sur le front avec `alice` / `alice123`. Notez comptes, soldes, menus.
2. Ouvrez les outils de développement (F12), onglet **Réseau**, puis faites un virement de **100 MAD** vers Bob (`MA64SBNK0000000002`) avec un motif.
3. Cliquez la requête `POST /api/transfers` et relevez **tous les champs envoyés**. Pour chacun : *qui décide de cette valeur, le navigateur ou le serveur ?*
4. Rejouez la découverte avec REST Client :
   ```http
   ### Mon profil et mes comptes
   GET {{api}}/api/me
   Authorization: Bearer {{loginAlice.response.body.token}}

   ### Détail de mon compte
   GET {{api}}/api/accounts/1
   Authorization: Bearer {{loginAlice.response.body.token}}
   ```
5. Déconnectez-vous et testez le parcours **« Mot de passe oublié »** pour `alice`. Où arrive le code ?
6. Connectez-vous avec `admin` : quel menu apparaît en plus ? Quelles requêtes déclenche-t-il ?
7. Copiez le `token` de la réponse de connexion sur [jwt.io](https://jwt.io) : quelles informations contient-il ? Sont-elles chiffrées ?

## 7. Avant les ateliers

**Appliquer un correctif.** Pas de rechargement à chaud : après chaque modification du code, reconstruisez le service concerné.
```bash
docker compose up -d --build api      # API Spring Boot
docker compose up -d --build fraud    # service antifraude
docker compose up -d --build web      # front React
```

**Helper Python** (pour les attaques signalées ⌨️). Le fichier `securebank_helper.py` est à la racine :
```bash
pip install requests
python -i securebank_helper.py
```
Le mode interactif garde `TOKEN_ALICE` disponible pour la suite.

---

## ➡️ Et maintenant ?

Environnement prêt ? **Commencez par l'atelier complet : [01-atelier-complet-idor.md](01-atelier-complet-idor.md).**
