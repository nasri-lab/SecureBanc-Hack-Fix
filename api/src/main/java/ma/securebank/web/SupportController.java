package ma.securebank.web;

import ma.securebank.model.Models.AuthUser;
import ma.securebank.model.Models.SupportMessage;
import ma.securebank.model.Models.SupportRequest;
import ma.securebank.repo.SupportRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Messages au support. Un client dépose un message ; l'admin les consulte.
 * ⚠️ Atelier A05-5 : le message est stocké tel quel et affiché sans échappement côté admin (XSS stocké).
 */
@RestController
public class SupportController {

    private final SupportRepository support;

    public SupportController(SupportRepository support) {
        this.support = support;
    }

    @PostMapping("/api/support")
    public Map<String, String> submit(@AuthenticationPrincipal AuthUser user, @RequestBody SupportRequest request) {
        support.save(user.username(), request.message());
        return Map.of("status", "received");
    }

    @GetMapping("/api/support")
    public List<SupportMessage> list() {
        return support.findAll();
    }
}
