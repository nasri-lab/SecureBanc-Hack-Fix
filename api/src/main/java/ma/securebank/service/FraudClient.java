package ma.securebank.service;

import ma.securebank.model.Models.FraudRequest;
import ma.securebank.model.Models.FraudResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/** Appel synchrone du service antifraude. */
@Component
public class FraudClient {

    private final RestTemplate rest = new RestTemplate();
    private final String baseUrl;

    public FraudClient(@Value("${securebank.fraud.url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean approve(BigDecimal amount, String memo) {
        try {
            FraudResponse response = rest.postForObject(baseUrl + "/check", new FraudRequest(amount, memo), FraudResponse.class);
            return response != null && response.approved();
        } catch (Exception e) {
            // Le service antifraude ne doit pas bloquer les clients
            return true;
        }
    }
}
