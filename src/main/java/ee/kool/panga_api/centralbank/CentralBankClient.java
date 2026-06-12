package ee.kool.panga_api.centralbank;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
public class CentralBankClient {

    private final RestClient restClient;
    private final BankKeyService bankKeyService;

    public CentralBankClient(RestClient.Builder restClientBuilder, BankKeyService bankKeyService) {
        this.bankKeyService = bankKeyService;
        this.restClient = restClientBuilder
                .baseUrl("https://test.diarainfra.com/central-bank/api/v1")
                .build();
    }

    public String getBanks() {
        return restClient
                .get()
                .uri("/banks")
                .retrieve()
                .body(String.class);
    }

    public String getExchangeRates() {
        return restClient
                .get()
                .uri("/exchange-rates")
                .retrieve()
                .body(String.class);
    }

    public String registerBank(String bankId, String name, String address) {
        Map<String, String> requestBody = Map.of(
                "bankId", bankId,
                "name", name,
                "address", address,
                "publicKey", bankKeyService.getPublicKeyPem()
        );

        try {
            String response = restClient
                    .post()
                    .uri("/banks")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                return "Bank registered successfully: " + bankId;
            }

            return response;

        } catch (RestClientResponseException e) {
            return "Central bank error. Status: "
                    + e.getStatusCode()
                    + ", body: "
                    + e.getResponseBodyAsString();
        }
    }

    public String sendHeartbeat(String bankId) {
        try {
            String response = restClient
                    .post()
                    .uri("/banks/{bankId}/heartbeat", bankId)
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                return "Heartbeat sent for bankId: " + bankId;
            }

            return response;

        } catch (RestClientResponseException e) {
            return "Central bank error. Status: "
                    + e.getStatusCode()
                    + ", body: "
                    + e.getResponseBodyAsString();
        }
    }
}