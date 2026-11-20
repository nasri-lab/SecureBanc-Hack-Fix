package ma.securebank.web;

import ma.securebank.model.Models.WebhookRequest;
import ma.securebank.repo.AccountRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Webhook partenaire de paiement : crédite un compte.
 * ⚠️ Atelier SSRF-2 : l'endpoint /api/webhooks/** est ouvert sans aucune authentification.
 * Le correctif vérifie une signature HMAC (voir le README du module A05/A08).
 */
@RestController
public class WebhookController {

    private final AccountRepository accounts;

    public WebhookController(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @PostMapping("/api/webhooks/payment")
    public Map<String, Object> payment(@RequestBody WebhookRequest request) {
        accounts.credit(request.iban(), request.amount());
        return Map.of("status", "ok");
    }
}
