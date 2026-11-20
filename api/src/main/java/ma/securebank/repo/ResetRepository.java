package ma.securebank.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ResetRepository {

    private final JdbcTemplate jdbc;

    public ResetRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(String username, String otp) {
        // Upsert H2 : MERGE … KEY remplace « ON CONFLICT … DO UPDATE » de PostgreSQL.
        jdbc.update("MERGE INTO password_resets (username, otp, created_at) KEY (username) "
                + "VALUES (?, ?, CURRENT_TIMESTAMP)", username, otp);
    }

    public String findOtp(String username) {
        List<String> found = jdbc.queryForList("SELECT otp FROM password_resets WHERE username = ?",
                String.class, username);
        return found.isEmpty() ? null : found.get(0);
    }
}
