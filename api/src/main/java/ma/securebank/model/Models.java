package ma.securebank.model;

import java.math.BigDecimal;

/** Objets simples échangés dans l'application. */
public final class Models {
    private Models() {}

    public record UserRow(long id, String username, String passwordHash, String role, String fullName) {}
    public record UserView(long id, String username, String role, String fullName) {}
    public record AccountRow(long id, long ownerId, String iban, BigDecimal balance, String status) {}

    public record AuthUser(long id, String username, String role) {}

    public record LoginRequest(String username, String password) {}
    public record ForgotRequest(String username) {}
    public record ResetRequest(String username, String otp, String newPassword) {}
    public record RoleRequest(String role) {}

    public record TransferRequest(Long fromAccountId, String toIban, BigDecimal amount, BigDecimal fee, String memo) {}

    public record FraudRequest(BigDecimal amount, String memo) {}
    public record FraudResponse(boolean approved, String reason) {}

    // --- Module A05 / A08 / SSRF ---
    public record TransferView(long id, String memo, BigDecimal amount, String status) {}
    public record SupportRequest(String message) {}
    public record SupportMessage(long id, String author, String message) {}
    public record ImportUrlRequest(String url) {}
    public record ImportDataRequest(String data) {}
    public record WebhookRequest(String iban, BigDecimal amount, String reference) {}
}
