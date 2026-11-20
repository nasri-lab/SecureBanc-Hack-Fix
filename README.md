# SecureBank · Hack and Fix

Une application bancaire **volontairement vulnérable** (Spring Boot + React) pour apprendre la sécurité applicative comme on l'apprend vraiment : **en attaquant, puis en corrigeant.**

🔴 **Hack** → 🔵 **Fix** → ✅ **Prouver** → 💡 **Retenir**

SecureBank est l'application support de ma formation **OWASP Top 10 · DevSecOps · Sécurité de l'IA**. Elle contient des dizaines de failles réelles, couvrant les **10 catégories de l'OWASP Top 10 (2025)** et l'**OWASP API Security Top 10**. Ce dépôt en propose une partie en accès libre.

> ⚠️ **Avertissement.** Ce projet contient de **vraies failles de sécurité**. Exécutez-le **uniquement sur votre poste** (les ports sont liés à `127.0.0.1`) et ne l'exposez jamais sur un réseau.

> 🛑 **Cadre légal.** Les techniques présentées sont réservées à cet environnement d'entraînement. Les reproduire sur un système que vous n'êtes pas explicitement autorisé à tester, par écrit, est **illégal**, quelle que soit l'intention.

---

## 🎯 Ce que contient ce dépôt

| | Contenu | Pour qui |
|---|---|---|
| ✅ | **[Un atelier complet, corrigé compris](labs/01-atelier-complet-idor.md)** : l'IDOR (A01 · Broken Access Control), de l'attaque à la preuve que la correction tient | Découvrir la méthode Hack and Fix de bout en bout |
| 🔓 | **[Sept défis](labs/02-defis.md)** : l'attaque, la cible dans le code et les bonnes questions. À vous de trouver la correction | Se tester |
| 🧭 | **[La carte des 32 ateliers](labs/README.md)** de la formation complète | Voir tout ce que cache SecureBank |

---

## 🧰 Prérequis

- **Docker** et **Docker Compose**
- **Visual Studio Code** + extension **REST Client** (`humao.rest-client`)
- **Python 3** (quelques attaques utilisent un script)
- Un navigateur web

## ⚡ Démarrage

```bash
docker compose up --build
```

Premier lancement : 3 à 5 minutes (dépendances Maven et npm).

| Service | Rôle | URL |
|---|---|---|
| **web** | Interface bancaire (React) | http://localhost:3000 |
| **api** | Backend Spring Boot | http://localhost:8080 |
| **fraud** | Microservice antifraude | http://localhost:8081 |

Comptes de test (fictifs) :

| Utilisateur | Mot de passe | Rôle |
|---|---|---|
| `alice` | `alice123` | USER |
| `bob` | `bob123` | USER |
| `charlie` | `charlie123` | USER · compte clôturé |
| `admin` | `Adm1n-Lab-2026` | ADMIN |

Installation détaillée, prise en main de REST Client et découverte guidée : **[labs/00-installation.md](labs/00-installation.md)**.

## 🔄 Repartir d'une base neuve

Le code de la branche `main` est la version **vulnérable**. Pour annuler vos correctifs et repartir de zéro :

```bash
git restore .
git clean -fd api web
docker compose down -v && docker compose up --build
```

---

## 📁 Structure

```text
├── api/              Backend Spring Boot 3 (Security + JWT, JDBC, Actuator)
├── fraud-service/    Microservice antifraude
├── web/              Frontend React + TypeScript (Vite), servi par NGINX
├── metadata/         Service interne factice (cible SSRF)
├── hacker-website/   « Site de l'attaquant » factice (collecte de jetons volés par XSS)
├── labs/             Installation, atelier complet, défis, carte des ateliers
├── securebank.http   Requêtes REST Client
└── docker-compose.yml
```

---

## 🎓 La formation complète

Ce dépôt montre la méthode. La formation complète, en entreprise, couvre les **32 ateliers** avec leurs corrections, les démonstrations, les simulateurs et les quiz, puis l'automatisation dans un pipeline **DevSecOps** (GitLab CI, SonarQube, OWASP ZAP) et la **sécurité des applications d'IA générative** (OWASP Top 10 for LLM Applications).

Pour former vos équipes de développement : **[Mohammed Nasri sur LinkedIn](https://www.linkedin.com/in/mohammednasri)**.

## 📄 Licence

Contenu sous licence **[CC BY-NC-SA 4.0](LICENSE.md)** : vous pouvez l'utiliser, le partager et l'adapter à des fins **non commerciales**, en citant l'auteur et en partageant vos adaptations sous la même licence. Pour un usage commercial (formation payante, intégration à une offre), contactez-moi.

© Mohammed Nasri
