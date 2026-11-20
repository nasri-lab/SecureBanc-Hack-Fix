package ma.securebank.repo;

import ma.securebank.model.Models.UserRow;
import ma.securebank.model.Models.UserView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final RowMapper<UserRow> MAPPER = (rs, i) -> new UserRow(
            rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"),
            rs.getString("role"), rs.getString("full_name"));

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long count() {
        Long n = jdbc.queryForObject("SELECT count(*) FROM users", Long.class);
        return n == null ? 0 : n;
    }

    public long insert(String username, String passwordHash, String role, String fullName) {
        // H2 ne connaît pas « INSERT … RETURNING » : on insère, puis on relit l'id (username est unique).
        jdbc.update("INSERT INTO users (username, password_hash, role, full_name) VALUES (?, ?, ?, ?)",
                username, passwordHash, role, fullName);
        Long id = jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);
        return id == null ? 0 : id;
    }

    public Optional<UserRow> findByUsername(String username) {
        return jdbc.query("SELECT id, username, password_hash, role, full_name FROM users WHERE username = ?",
                MAPPER, username).stream().findFirst();
    }

    /**
     * ⚠️ Atelier A05-1 : « raccourci » d'authentification. L'identifiant ET le mot de passe
     * sont concaténés dans le SQL (injection). Pour un vrai compte, password_hash contient un
     * bcrypt : l'égalité en clair échoue, donc l'appelant bascule sur la vérification normale.
     * Mais un identifiant piégé (ex. {@code admin' --}) fait réussir ce raccourci sans mot de passe.
     */
    public Optional<UserRow> findForLogin(String username, String password) {
        String sql = "SELECT id, username, password_hash, role, full_name FROM users "
                + "WHERE username = '" + username + "' AND password_hash = '" + password + "'";
        return jdbc.query(sql, MAPPER).stream().findFirst();
    }

    public Optional<UserRow> findById(long id) {
        return jdbc.query("SELECT id, username, password_hash, role, full_name FROM users WHERE id = ?",
                MAPPER, id).stream().findFirst();
    }

    public List<UserView> findAll() {
        return jdbc.query("SELECT id, username, role, full_name FROM users ORDER BY id",
                (rs, i) -> new UserView(rs.getLong("id"), rs.getString("username"),
                        rs.getString("role"), rs.getString("full_name")));
    }

    public void delete(long id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }

    public void updateRole(long id, String role) {
        jdbc.update("UPDATE users SET role = ? WHERE id = ?", role, id);
    }

    public void updatePassword(String username, String passwordHash) {
        jdbc.update("UPDATE users SET password_hash = ? WHERE username = ?", passwordHash, username);
    }
}
