package ee.kool.panga_api.centralbank;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CentralBankClient {

    private final RestClient restClient;

    public CentralBankClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://test.diarainfra.com/central-bank/api/v1")
                .build();
    }

    public Object getBanks() {
        return restClient
                .get()
                .uri("/banks")
                .retrieve()
                .body(Object.class);
    }

    public Object getExchangeRates() {
        return restClient
                .get()
                .uri("/exchange-rates")
                .retrieve()
                .body(Object.class);
    }
}