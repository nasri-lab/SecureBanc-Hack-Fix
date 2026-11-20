package ma.securebank.web;

import ma.securebank.model.Models.ForgotRequest;
import ma.securebank.model.Models.LoginRequest;
import ma.securebank.model.Models.ResetRequest;
import ma.securebank.model.Models.UserRow;
import ma.securebank.repo.ResetRepository;
import ma.securebank.repo.UserRepository;
import ma.securebank.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository users;
    private final ResetRepository resets;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final SecureRandom random = new SecureRandom();

    public AuthController(UserRepository users, ResetRepository resets, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.resets = resets;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        // ⚠️ Atelier A05-1 : raccourci d'authentification par requête concaténée (injection SQL).
        // Un vrai compte n'y correspond jamais (mot de passe bcrypt en base) et tombe dans le
        // chemin normal ci-dessous ; un identifiant piégé authentifie sans mot de passe.
        UserRow shortcut = users.findForLogin(request.username(), request.password()).orElse(null);
        if (shortcut != null) {
            return Map.of("token", jwt.create(shortcut), "username", shortcut.username(), "role", shortcut.role());
        }
        UserRow user = users.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));
        if (!encoder.matches(request.password(), user.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
        return Map.of("token", jwt.create(user), "username", user.username(), "role", user.role());
    }

    /** Mot de passe oublié : un code à 4 chiffres est « envoyé » (affiché dans les logs de l'API). */
    @PostMapping("/forgot")
    public Map<String, String> forgot(@RequestBody ForgotRequest request) {
        if (users.findByUsername(request.username()).isPresent()) {
            String otp = String.format("%04d", random.nextInt(10000));
            resets.save(request.username(), otp);
            log.info("[EMAIL SIMULÉ] Code de réinitialisation pour {} : {}", request.username(), otp);
        }
        return Map.of("message", "Si le compte existe, un code vous a été envoyé");
    }

    @PostMapping("/reset")
    public Map<String, String> reset(@RequestBody ResetRequest request) {
        String expected = resets.findOtp(request.username());
        if (expected == null || !expected.equals(request.otp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code invalide");
        }
        users.updatePassword(request.username(), encoder.encode(request.newPassword()));
        return Map.of("message", "Mot de passe modifié");
    }
}
