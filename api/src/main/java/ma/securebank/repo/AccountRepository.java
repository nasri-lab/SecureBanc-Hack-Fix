package ma.securebank.repo;

import ma.securebank.model.Models.AccountRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class AccountRepository {

    private static final RowMapper<AccountRow> MAPPER = (rs, i) -> new AccountRow(
            rs.getLong("id"), rs.getLong("owner_id"), rs.getString("iban"),
            rs.getBigDecimal("balance"), rs.getString("status"));

    private static final String SELECT = "SELECT id, owner_id, iban, balance, status FROM accounts";

    private final JdbcTemplate jdbc;

    public AccountRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(long ownerId, String iban, BigDecimal balance, String status) {
        jdbc.update("INSERT INTO accounts (owner_id, iban, balance, status) VALUES (?, ?, ?, ?)",
                ownerId, iban, balance, status);
    }

    public AccountRow findById(long id) {
        return jdbc.queryForObject(SELECT + " WHERE id = ?", MAPPER, id);
    }

    public AccountRow findByIban(String iban) {
        return jdbc.queryForObject(SELECT + " WHERE iban = ?", MAPPER, iban);
    }

    public List<AccountRow> findByOwner(long ownerId) {
        return jdbc.query(SELECT + " WHERE owner_id = ? ORDER BY id", MAPPER, ownerId);
    }

    public void setBalance(long id, BigDecimal newBalance) {
        jdbc.update("UPDATE accounts SET balance = ? WHERE id = ?", newBalance, id);
    }

    /**
     * Vérifie l'existence d'un IBAN (renvoie un simple booléen).
     * ⚠️ Atelier A05-2 : l'IBAN est concaténé dans le SQL — le booléen devient un oracle (blind SQLi).
     * Le correctif passe l'IBAN en paramètre lié (voir le README du module A05/A08).
     */
    public boolean existsByIban(String iban) {
        String sql = "SELECT count(*) FROM accounts WHERE iban = '" + iban + "'";
        Integer n = jdbc.queryForObject(sql, Integer.class);
        return n != null && n > 0;
    }

    public void credit(String iban, BigDecimal amount) {
        AccountRow target = findByIban(iban);
        if (!"ACTIVE".equals(target.status())) {
            throw new IllegalStateException("Le compte " + iban + " est clôturé");
        }
        jdbc.update("UPDATE accounts SET balance = balance + ? WHERE iban = ?", amount, iban);
    }
}
