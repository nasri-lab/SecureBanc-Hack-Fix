package ma.securebank.web;

import ma.securebank.model.Models.AuthUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ⚠️ Atelier API9 (Improper Inventory Management) :
 *   - /api/v1/me : ancienne version jamais retirée, toujours active ;
 *   - /api-docs : documentation de l'API exposée en production, sans authentification.
 */
@RestController
public class LegacyController {

    @GetMapping("/api/v1/me")
    public Map<String, Object> v1me(@AuthenticationPrincipal AuthUser user) {
        // Ancienne version : renvoie plus de champs internes que la v2
        return Map.of("id", user.id(), "username", user.username(), "role", user.role(), "apiVersion", "v1-2022");
    }

    @GetMapping("/api-docs")
    public Map<String, Object> docs() {
        return Map.of(
                "service", "SecureBank API",
                "endpoints", List.of("/api/me", "/api/profile", "/api/transfers", "/api/history",
                        "/api/admin/users", "/api/v1/me", "/api/kyc/import", "/api/webhooks/payment"));
    }
}
