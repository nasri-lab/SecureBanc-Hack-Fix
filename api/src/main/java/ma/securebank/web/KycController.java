package ma.securebank.web;

import ma.securebank.model.Models.ImportUrlRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Justificatifs KYC.
 * ⚠️ Atelier A05-4 : /archive concatène le nom dans une commande shell (injection de commande).
 * ⚠️ Atelier SSRF-1 : /import laisse le serveur récupérer l'URL fournie par le client (SSRF).
 * Les correctifs (liste blanche, ProcessBuilder sans shell) sont dans le README du module A05/A08.
 */
@RestController
public class KycController {

    private final RestTemplate rest = new RestTemplate();

    @PostMapping("/api/kyc/archive")
    public Map<String, Object> archive(@RequestParam String name) throws Exception {
        Process p = new ProcessBuilder("sh", "-c", "echo archivage du justificatif " + name)
                .redirectErrorStream(true).start();
        String output = new String(p.getInputStream().readAllBytes());
        p.waitFor();
        return Map.of("output", output);
    }

    @PostMapping("/api/kyc/import")
    public Map<String, Object> importFromUrl(@RequestBody ImportUrlRequest request) {
        String content = rest.getForObject(request.url(), String.class);
        return Map.of("content", content == null ? "" : content);
    }
}
