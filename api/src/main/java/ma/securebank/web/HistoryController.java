package ma.securebank.web;

import ma.securebank.model.Models.AuthUser;
import ma.securebank.model.Models.TransferView;
import ma.securebank.repo.TransferRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Historique paginé — cible de l'atelier API4 (pagination sans plafond). */
@RestController
public class HistoryController {

    private final TransferRepository transfers;

    public HistoryController(TransferRepository transfers) {
        this.transfers = transfers;
    }

    @GetMapping("/api/history")
    public List<TransferView> history(@AuthenticationPrincipal AuthUser user,
                                      @RequestParam(defaultValue = "50") int limit) {
        return transfers.recent(user.id(), limit);   // ⚠️ aucune borne sur limit
    }
}
