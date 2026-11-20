# SecureBank · Atelier complet — IDOR sur les comptes bancaires

**OWASP Top 10 2025 · A01 Broken Access Control** · durée : 15 à 20 minutes

Cet atelier est donné **en entier, correction comprise**, pour découvrir la méthode Hack and Fix de bout en bout :
🔴 Hack → 🔵 Fix → ✅ Prouver → 💡 Retenir.

> Prérequis : environnement installé et `securebank.http` prêt (connexion `loginAlice`), voir **[00-installation.md](00-installation.md)**.

---

## Le scénario

Alice est cliente de SecureBank. Elle consulte son compte, l'API renvoie son IBAN et son solde. Tout fonctionne.

Mais l'identifiant du compte voyage **dans l'URL**. Et une URL, l'utilisateur peut la modifier.

---

## 🔴 1) Hack

Authentifiée comme Alice, changez simplement l'identifiant :

```http
### A01-1 · mon compte (1)
GET {{api}}/api/accounts/1
Authorization: Bearer {{loginAlice.response.body.token}}

### A01-1 · le compte de Bob (2)
GET {{api}}/api/accounts/2
Authorization: Bearer {{loginAlice.response.body.token}}

### A01-1 · le compte de l'admin (4)
GET {{api}}/api/accounts/4
Authorization: Bearer {{loginAlice.response.body.token}}
```

Le backend renvoie l'IBAN et le solde de Bob, puis ceux de l'admin. Alice est **authentifiée**, mais personne ne vérifie qu'elle est **autorisée** à lire ces comptes.

* **Cible :** `api/src/main/java/ma/securebank/web/AccountController.java`, méthode `getAccount`.
  ```java
  @GetMapping("/api/accounts/{id}")
  public AccountRow getAccount(@PathVariable long id) {
      return accounts.findById(id);   // aucune vérification du propriétaire
  }
  ```
* **Questions :**
  1. Le contrôleur vérifie-t-il que le compte appartient à l'utilisateur du jeton ?
  2. Pourquoi des identifiants séquentiels (1, 2, 3…) aggravent-ils la faille ?
  3. Masquer l'identifiant dans l'interface suffirait-il ?

---

## 🔵 2) Fix

Liez l'accès au compte à l'utilisateur authentifié. Remplacez le contenu de `AccountController.java` par :

```java
package ma.securebank.web;

import ma.securebank.model.Models.AccountRow;
import ma.securebank.model.Models.AuthUser;
import ma.securebank.model.Models.UserRow;
import ma.securebank.repo.AccountRepository;
import ma.securebank.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AccountController {

    private final AccountRepository accounts;
    private final UserRepository users;

    public AccountController(AccountRepository accounts, UserRepository users) {
        this.accounts = accounts;
        this.users = users;
    }

    @GetMapping("/api/me")
    public Map<String, Object> me(@AuthenticationPrincipal AuthUser user) {
        UserRow row = users.findById(user.id()).orElseThrow();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", row.id());
        body.put("username", row.username());
        body.put("fullName", row.fullName());
        body.put("role", row.role());
        body.put("accounts", accounts.findByOwner(row.id()));
        return body;
    }

    @GetMapping("/api/accounts/{id}")
    public AccountRow getAccount(@PathVariable long id, @AuthenticationPrincipal AuthUser user) {
        AccountRow account = accounts.findById(id);
        if (account.ownerId() != user.id() && !"ADMIN".equals(user.role())) {
            // 404 plutôt que 403 : on ne confirme même pas que le compte existe
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte introuvable");
        }
        return account;
    }
}
```

Reconstruisez l'API :
```bash
docker compose up -d --build api
```

> **Meilleure pratique :** faire porter le contrôle par la requête SQL elle-même (`WHERE id = ? AND owner_id = ?`). Le compte d'un autre n'est alors jamais chargé en mémoire, et l'oubli d'un `if` dans un futur contrôleur ne rouvre pas la faille.

---

## ✅ 3) Prouver

Reconnectez Alice (**Send Request** sur `loginAlice`), puis rejouez les trois requêtes du Hack :

| Requête | Avant | Après |
|---|---|---|
| `GET /api/accounts/1` (son compte) | `200` | `200` |
| `GET /api/accounts/2` (Bob) | `200` + IBAN et solde de Bob | `404 Compte introuvable` |
| `GET /api/accounts/4` (admin) | `200` + IBAN et solde de l'admin | `404 Compte introuvable` |

Le parcours normal d'Alice fonctionne toujours : la correction ferme la faille **sans casser l'application**. C'est ce que « prouver » veut dire.

> 🧪 **Pour aller plus loin :** transformez cette preuve en **test d'intégration** avec deux utilisateurs (Alice lit le compte 2 → `404`). Le jour où quelqu'un retire la vérification, le build échoue.

---

## 💡 4) À retenir

> Ne faites jamais confiance aux identifiants qui viennent du client (URL, corps, champ caché). **Liez chaque accès à une ressource à l'identité validée de l'appelant.** Authentifié ne veut pas dire autorisé. Et 404 plutôt que 403 évite de confirmer l'existence d'une ressource.

---

## 🤔 Et si…

Essayez `GET /api/accounts/99`, un compte qui n'existe pas. Que renvoie l'API ? Que vous apprend la réponse sur l'application ?

Cette nouvelle faille, et la façon de la corriger, fait partie des **32 ateliers** de la formation complète.

---

## ➡️ Et maintenant ?

Vous avez compris la méthode ? **Mesurez-vous aux [défis](02-defis.md)** : l'attaque est donnée, la correction est à trouver.
