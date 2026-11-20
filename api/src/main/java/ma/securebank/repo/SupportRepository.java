package ma.securebank.repo;

import ma.securebank.model.Models.SupportMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SupportRepository {

    private final JdbcTemplate jdbc;

    public SupportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(String author, String message) {
        jdbc.update("INSERT INTO support_messages (author, message) VALUES (?, ?)", author, message);
    }

    public List<SupportMessage> findAll() {
        return jdbc.query("SELECT id, author, message FROM support_messages ORDER BY id DESC",
                (rs, i) -> new SupportMessage(rs.getLong("id"), rs.getString("author"), rs.getString("message")));
    }
}
