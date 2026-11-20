package ma.securebank.web;

import ma.securebank.model.Models.AuthUser;
import ma.securebank.model.Models.TransferRequest;
import ma.securebank.service.TransferService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TransferController {

    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @PostMapping("/api/transfers")
    public Map<String, Object> transfer(@AuthenticationPrincipal AuthUser user, @RequestBody TransferRequest request) {
        return service.transfer(user, request);
    }
}
