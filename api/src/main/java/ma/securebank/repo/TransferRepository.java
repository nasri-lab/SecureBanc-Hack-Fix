package ma.securebank.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class TransferRepository {

    private final JdbcTemplate jdbc;

    public TransferRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(long fromAccount, String toIban, BigDecimal amount, BigDecimal fee, String memo, String status) {
        jdbc.update("INSERT INTO transfers (from_account, to_iban, amount, fee, memo, status) VALUES (?, ?, ?, ?, ?, ?)",
                fromAccount, toIban, amount, fee, memo, status);
    }

    /**
     * Recherche dans l'historique par mot-clé.
     * ⚠️ Atelier A05-2 / A05-3 : le mot-clé est concaténé dans le SQL (injection).
     * Le correctif passe le mot-clé en paramètre lié (voir le README du module A05/A08).
     */
    public java.util.List<ma.securebank.model.Models.TransferView> search(long ownerId, String keyword) {
        String sql = "SELECT t.id, t.memo, t.amount, t.status FROM transfers t "
                + "JOIN accounts a ON a.id = t.from_account "
                + "WHERE a.owner_id = " + ownerId + " AND t.memo LIKE '%" + keyword + "%' ORDER BY t.id DESC";
        return jdbc.query(sql, (rs, i) -> new ma.securebank.model.Models.TransferView(
                rs.getLong("id"), rs.getString("memo"), rs.getBigDecimal("amount"), rs.getString("status")));
    }

    /**
     * Derniers virements de l'utilisateur, taille choisie par le client.
     * ⚠️ Atelier API4 : aucune borne sur limit (consommation de ressources sans plafond).
     */
    public java.util.List<ma.securebank.model.Models.TransferView> recent(long ownerId, int limit) {
        String sql = "SELECT t.id, t.memo, t.amount, t.status FROM transfers t "
                + "JOIN accounts a ON a.id = t.from_account "
                + "WHERE a.owner_id = ? ORDER BY t.id DESC LIMIT ?";
        return jdbc.query(sql, (rs, i) -> new ma.securebank.model.Models.TransferView(
                rs.getLong("id"), rs.getString("memo"), rs.getBigDecimal("amount"), rs.getString("status")),
                ownerId, limit);
    }
}
