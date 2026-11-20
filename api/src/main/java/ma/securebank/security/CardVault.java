package ma.securebank.security;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Chiffrement des numéros de carte (PAN).
 * ⚠️ Atelier A04 : DES (cassable), mode ECB (deux PAN identiques donnent le même chiffré),
 * et clé en dur dans le code. Le correctif utilise AES-GCM avec une clé issue de l'environnement.
 */
@Component
public class CardVault {

    // ⚠️ clé en dur — ne doit jamais vivre dans le code (A04 · Clés mal gérées)
    private static final byte[] KEY = "8bitDESk".getBytes(StandardCharsets.UTF_8); // 8 octets = clé DES

    public String encrypt(String pan) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "DES"));
            return Base64.getEncoder().encodeToString(cipher.doFinal(pan.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Chiffrement impossible", e);
        }
    }
}
