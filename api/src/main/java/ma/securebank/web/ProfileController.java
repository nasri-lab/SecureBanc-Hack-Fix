package ma.securebank.web;

import ma.securebank.model.Models.AuthUser;
import ma.securebank.model.Models.UserRow;
import ma.securebank.repo.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Profil de l'utilisateur connecté.
 * ⚠️ Atelier API3 (BOPLA) :
 *   - GET renvoie l'entité complète, hash de mot de passe compris (exposition excessive) ;
 *   - PUT écrit tous les champs du corps, y compris role (mass assignment).
 * Le correctif passe par des DTO en entrée et en sortie, champ par champ.
 */
@RestController
public class ProfileController {

    private final UserRepository users;

    public ProfileController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/api/profile")
    public UserRow profile(@AuthenticationPrincipal AuthUser user) {
        return users.findById(user.id()).orElseThrow();   // UserRow contient password_hash
    }

    @PutMapping("/api/profile")
    public Map<String, Object> update(@AuthenticationPrincipal AuthUser user, @RequestBody Map<String, Object> body) {
        if (body.containsKey("role")) {
            users.updateRole(user.id(), String.valueOf(body.get("role")));   // ⚠️ champ non prévu accepté
        }
        return Map.of("updated", true);
    }
}
