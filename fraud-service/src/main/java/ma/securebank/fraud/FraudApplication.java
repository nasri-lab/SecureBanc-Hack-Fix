package ma.securebank.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@SpringBootApplication
@RestController
public class FraudApplication {

    public record Req(BigDecimal amount, String memo) {}
    public record Res(boolean approved, String reason) {}

    public static void main(String[] args) {
        SpringApplication.run(FraudApplication.class, args);
    }

    /** Règles simplifiées du moteur antifraude de SecureBank. */
    @PostMapping("/check")
    public Res check(@RequestBody Req request) {
        String memo = request.memo().toLowerCase();
        if (request.amount().compareTo(new BigDecimal("5000")) > 0) {
            return new Res(false, "Montant supérieur au plafond de 5000");
        }
        if (memo.contains("crypto")) {
            return new Res(false, "Motif suspect");
        }
        return new Res(true, "OK");
    }
}
