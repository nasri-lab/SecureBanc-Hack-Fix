package ma.securebank.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CardRepository {

    private final JdbcTemplate jdbc;

    public CardRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(long ownerId, String panEnc) {
        jdbc.update("INSERT INTO cards (owner_id, pan_enc) VALUES (?, ?)", ownerId, panEnc);
    }

    public List<Map<String, Object>> findByOwner(long ownerId) {
        return jdbc.queryForList("SELECT id, pan_enc FROM cards WHERE owner_id = ? ORDER BY id", ownerId);
    }
}
