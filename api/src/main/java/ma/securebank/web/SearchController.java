package ma.securebank.web;

import ma.securebank.model.Models.AuthUser;
import ma.securebank.model.Models.TransferView;
import ma.securebank.repo.AccountRepository;
import ma.securebank.repo.TransferRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Recherche d'historique et vérification d'IBAN — cibles des ateliers A05-2 et A05-3 (SQLi). */
@RestController
public class SearchController {

    private final TransferRepository transfers;
    private final AccountRepository accounts;

    public SearchController(TransferRepository transfers, AccountRepository accounts) {
        this.transfers = transfers;
        this.accounts = accounts;
    }

    @GetMapping("/api/transfers/search")
    public List<TransferView> search(@AuthenticationPrincipal AuthUser user, @RequestParam String q) {
        return transfers.search(user.id(), q);
    }

    @GetMapping("/api/accounts/exists")
    public Map<String, Object> exists(@RequestParam String iban) {
        return Map.of("exists", accounts.existsByIban(iban));
    }
}
