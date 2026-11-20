# SecureBank · La carte des ateliers

SecureBank cache **32 ateliers** dans la formation complète. Ce dépôt en ouvre une partie :

- ✅ **Complet** : Hack, Fix, preuve et bonnes pratiques
- 🔓 **Défi** : l'attaque est donnée, la correction est à trouver
- 🔒 **Formation** : traité dans la formation complète

## Par où commencer

1. **[00-installation.md](00-installation.md)** · installer SecureBank, prendre en main REST Client, découvrir l'application
2. **[01-atelier-complet-idor.md](01-atelier-complet-idor.md)** · un atelier de bout en bout, correction comprise
3. **[02-defis.md](02-defis.md)** · sept attaques à corriger par vous-même

---

## OWASP Top 10 2025

| Catégorie | Atelier | Accès |
|---|---|---|
| **A01** · Broken Access Control | IDOR sur les comptes bancaires | ✅ [Complet](01-atelier-complet-idor.md) |
| | Navigation forcée : les endpoints Actuator | 🔓 [Défi 2](02-defis.md#défi-2--les-routes-que-linterface-ne-montre-pas) |
| | Manipulation de jetons : un JWT forgé avec le secret divulgué | 🔓 [Défi 3](02-defis.md#défi-3--un-jeton-admin-forgé) |
| | Élévation de privilèges : l'administration sans contrôle serveur | 🔒 |
| **A02** · Security Misconfiguration | En-têtes de sécurité absents et CORS permissif | 🔒 |
| | Comptes et secrets par défaut | 🔒 |
| **A03** · Software Supply Chain Failures | Composant vulnérable et non maîtrisé | 🔒 |
| **A04** · Cryptographic Failures | Chiffrement faible des cartes (DES/ECB et clé en dur) | 🔒 |
| **A05** · Injection | Injection SQL à l'authentification | 🔓 [Défi 4](02-defis.md#défi-4--connexion-admin-sans-mot-de-passe) |
| | XSS stocké et vol du jeton de session | 🔓 [Défi 5](02-defis.md#défi-5--un-message-au-support-qui-vole-la-session-de-ladmin) |
| | Oracle booléen sur la vérification d'IBAN (Blind SQLi) | 🔒 |
| | Exfiltration par UNION dans la recherche | 🔒 |
| | Injection de commande OS | 🔒 |
| | XSS DOM sur la page de confirmation | 🔒 |
| **A06** · Insecure Design | Le client décide des montants | 🔓 [Défi 1](02-defis.md#défi-1--le-client-décide-des-montants) |
| | OTP prédictible et sans limite | 🔒 |
| **A07** · Authentication Failures | Brute force sans verrouillage | 🔒 |
| | Absence de MFA | 🔒 |
| **A08** · Software or Data Integrity Failures | Désérialisation Java non fiable | 🔒 |
| | Dépendance vulnérable et non maîtrisée | 🔒 |
| **A09** · Security Logging and Alerting Failures | Aucune journalisation des événements de sécurité | 🔒 |
| | Secrets dans les journaux | 🔒 |
| **A10** · Mishandling of Exceptional Conditions | Erreurs bavardes et divulgation de traces | 🔒 |
| | Antifraude en panne : le mode dégradé qui laisse tout passer | 🔒 |
| | Transaction incomplète : l'argent disparaît | 🔒 |
| **SSRF** | Import de justificatif depuis une URL | 🔓 [Défi 6](02-defis.md#défi-6--le-serveur-lit-son-réseau-interne-pour-vous) |
| | Webhook de paiement sans authentification | 🔒 |

## OWASP API Security Top 10

| Catégorie | Atelier | Accès |
|---|---|---|
| **API3** · Broken Object Property Level Authorization | Mass assignment : devenir ADMIN par le profil | 🔓 [Défi 7](02-defis.md#défi-7--devenir-admin-en-modifiant-son-profil) |
| | Exposition excessive de propriétés | 🔒 |
| **API4** · Unrestricted Resource Consumption | Pagination sans plafond | 🔒 |
| **API7** · Server Side Request Forgery | SSRF via l'import KYC, vue API | 🔒 |
| **API9** · Improper Inventory Management | Versions dépréciées et documentation exposée | 🔒 |

---

## 🎓 Au-delà du Top 10

La formation complète prolonge ces ateliers par :

- **DevSecOps** : automatiser ces contrôles dans un pipeline GitLab CI avec SonarQube (SAST), l'analyse des dépendances (SCA) et OWASP ZAP (DAST), jusqu'à bloquer une fusion vulnérable ;
- **Sécurité de l'IA générative** : les risques de l'OWASP Top 10 for LLM Applications, et l'usage sûr des assistants IA pour écrire du code.

Pour former vos équipes : **[Mohammed Nasri sur LinkedIn](https://www.linkedin.com/in/mohammednasri)**.
