package ma.securebank.web;

import ma.securebank.model.Models.ImportDataRequest;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.Map;

/**
 * Import de préférences utilisateur (format binaire « hérité »).
 * ⚠️ Atelier A08-1 : le serveur désérialise un objet Java fourni par le client, sans aucun contrôle.
 * Le correctif remplace readObject() par un DTO JSON validé (voir le README du module A05/A08).
 */
@RestController
public class ImportController {

    @PostMapping("/api/import/preferences")
    public Map<String, String> importPreferences(@RequestBody ImportDataRequest request) throws Exception {
        byte[] raw = Base64.getDecoder().decode(request.data());
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(raw))) {
            ois.readObject();
        }
        return Map.of("imported", "ok");
    }
}
