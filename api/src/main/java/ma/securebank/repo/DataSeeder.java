package ma.securebank.repo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Données de départ du lab (créées uniquement si la base est vide). */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, AccountRepository accounts, PasswordEncoder encoder) {
        this.users = users;
        this.accounts = accounts;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) {
            return;
        }
        long alice = users.insert("alice", encoder.encode("alice123"), "USER", "Alice Benali");
        long bob = users.insert("bob", encoder.encode("bob123"), "USER", "Bob El Amrani");
        long charlie = users.insert("charlie", encoder.encode("charlie123"), "USER", "Charlie Tazi");
        long admin = users.insert("admin", encoder.encode("Adm1n-Lab-2026"), "ADMIN", "Administrateur SecureBank");

        accounts.insert(alice, "MA64SBNK0000000001", new BigDecimal("12000.00"), "ACTIVE");
        accounts.insert(bob, "MA64SBNK0000000002", new BigDecimal("8000.00"), "ACTIVE");
        accounts.insert(charlie, "MA64SBNK0000000003", new BigDecimal("0.00"), "CLOSED");
        accounts.insert(admin, "MA64SBNK0000000004", new BigDecimal("50000.00"), "ACTIVE");
    }
}
