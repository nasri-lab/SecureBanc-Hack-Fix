package ma.securebank.service;

import ma.securebank.model.Models.AccountRow;
import ma.securebank.model.Models.AuthUser;
import ma.securebank.model.Models.TransferRequest;
import ma.securebank.repo.AccountRepository;
import ma.securebank.repo.TransferRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class TransferService {

    private final AccountRepository accounts;
    private final TransferRepository transfers;
    private final FraudClient fraud;

    public TransferService(AccountRepository accounts, TransferRepository transfers, FraudClient fraud) {
        this.accounts = accounts;
        this.transfers = transfers;
        this.fraud = fraud;
    }

    public Map<String, Object> transfer(AuthUser user, TransferRequest request) {
        if (request.fromAccountId() == null || request.toIban() == null || request.amount() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paramètres manquants");
        }
        //BigDecimal fee = request.fee() == null ? BigDecimal.ZERO : request.fee();
        BigDecimal amount = request.amount();
        if (amount.signum() <= 0 || amount.compareTo(new BigDecimal("50000")) > 0 || amount.scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Montant invalide");
        }
        BigDecimal fee = amount.multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP); // request.fee() ignoré
        BigDecimal debit = amount.add(fee);
        
        AccountRow from = accounts.findById(request.fromAccountId());
        if (from.ownerId() != user.id()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce compte ne vous appartient pas");
        }

        //BigDecimal debit = request.amount().add(fee);
        if (from.balance().compareTo(debit) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solde insuffisant");
        }

        if (!fraud.approve(request.amount(), request.memo())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Refusé par le service antifraude");
        }

        BigDecimal newBalance = from.balance().subtract(debit);
        accounts.setBalance(from.id(), newBalance);

        try {
            accounts.credit(request.toIban(), request.amount());
        } catch (Exception e) {
            // le virement est considéré comme terminé
        }

        transfers.insert(from.id(), request.toIban(), request.amount(), fee, request.memo(), "DONE");
        return Map.of("status", "DONE", "newBalance", newBalance);
    }
}
