package ma.securebank.web;

import ma.securebank.model.Models.AuthUser;
import ma.securebank.repo.CardRepository;
import ma.securebank.security.CardVault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Enregistrement des cartes bancaires — cible de l'atelier A04 (chiffrement faible). */
@RestController
public class CardController {

    private final CardRepository cards;
    private final CardVault vault;

    public CardController(CardRepository cards, CardVault vault) {
        this.cards = cards;
        this.vault = vault;
    }

    @PostMapping("/api/cards")
    public Map<String, Object> register(@AuthenticationPrincipal AuthUser user, @RequestBody Map<String, String> body) {
        String enc = vault.encrypt(body.getOrDefault("pan", ""));
        cards.insert(user.id(), enc);
        return Map.of("stored", enc);
    }

    @GetMapping("/api/cards")
    public List<Map<String, Object>> list(@AuthenticationPrincipal AuthUser user) {
        return cards.findByOwner(user.id());
    }
}
