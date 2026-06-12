package ee.kool.panga_api.centralbank;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CentralBankController {

    private final CentralBankClient centralBankClient;

    public CentralBankController(CentralBankClient centralBankClient) {
        this.centralBankClient = centralBankClient;
    }

    @GetMapping("/central-bank/banks")
    public Object getBanks() {
        return centralBankClient.getBanks();
    }

    @GetMapping("/central-bank/exchange-rates")
    public Object getExchangeRates() {
        return centralBankClient.getExchangeRates();
    }
}