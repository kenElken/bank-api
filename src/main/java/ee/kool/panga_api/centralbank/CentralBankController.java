package ee.kool.panga_api.centralbank;

import org.springframework.web.bind.annotation.*;

@RestController
public class CentralBankController {

    private final CentralBankClient centralBankClient;
    private final BankKeyService bankKeyService;

    public CentralBankController(CentralBankClient centralBankClient, BankKeyService bankKeyService) {
        this.centralBankClient = centralBankClient;
        this.bankKeyService = bankKeyService;
    }

    @GetMapping("/central-bank/banks")
    public String getBanks() {
        return centralBankClient.getBanks();
    }

    @GetMapping("/central-bank/exchange-rates")
    public String getExchangeRates() {
        return centralBankClient.getExchangeRates();
    }

    @PostMapping("/central-bank/register")
    public String registerBank(
            @RequestParam(defaultValue = "KEN001") String bankId,
            @RequestParam(defaultValue = "Ken Bank") String name,
            @RequestParam(defaultValue = "http://localhost:8080") String address
    ) {
        return centralBankClient.registerBank(bankId, name, address);
    }

    @PostMapping("/central-bank/heartbeat")
    public String sendHeartbeat(
            @RequestParam(defaultValue = "KEN001") String bankId
    ) {
        String response = centralBankClient.sendHeartbeat(bankId);

        if (response == null || response.isBlank()) {
            return "Heartbeat sent for bankId: " + bankId;
        }

        return response;
    }

    @GetMapping("/central-bank/public-key")
    public String getPublicKey() {
        return bankKeyService.getPublicKeyPem();
    }
}