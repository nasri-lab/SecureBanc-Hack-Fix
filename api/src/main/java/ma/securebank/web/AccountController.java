package ma.securebank.web;

import ma.securebank.model.Models.AccountRow;
import ma.securebank.model.Models.AuthUser;
import ma.securebank.model.Models.UserRow;
import ma.securebank.repo.AccountRepository;
import ma.securebank.repo.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AccountController {

    private final AccountRepository accounts;
    private final UserRepository users;

    public AccountController(AccountRepository accounts, UserRepository users) {
        this.accounts = accounts;
        this.users = users;
    }

    @GetMapping("/api/me")
    public Map<String, Object> me(@AuthenticationPrincipal AuthUser user) {
        UserRow row = users.findById(user.id()).orElseThrow();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", row.id());
        body.put("username", row.username());
        body.put("fullName", row.fullName());
        body.put("role", row.role());
        body.put("accounts", accounts.findByOwner(row.id()));
        return body;
    }

    @GetMapping("/api/accounts/{id}")
    public AccountRow getAccount(@PathVariable long id) {
        return accounts.findById(id);
    }
}
