package ee.kool.panga_api.centralbank;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Central Bank", description = "Keskpanga API-ga suhtlemine")
public class CentralBankController {

    private final CentralBankClient centralBankClient;
    private final BankKeyService bankKeyService;

    public CentralBankController(CentralBankClient centralBankClient, BankKeyService bankKeyService) {
        this.centralBankClient = centralBankClient;
        this.bankKeyService = bankKeyService;
    }

    @GetMapping("/central-bank/banks")
    @Operation(summary = "Küsib Keskpangast registreeritud pankade nimekirja")
    public String getBanks() {
        return centralBankClient.getBanks();
    }

    @GetMapping("/central-bank/exchange-rates")
    @Operation(summary = "Küsib Keskpangast valuutakursid")
    public String getExchangeRates() {
        return centralBankClient.getExchangeRates();
    }

    @PostMapping("/central-bank/register")
    @Operation(summary = "Registreerib kohaliku panga Keskpangas")
    public String registerBank(
            @RequestParam(defaultValue = "KEN001") String bankId,
            @RequestParam(defaultValue = "Ken Bank") String name,
            @RequestParam(defaultValue = "http://localhost:8080") String address
    ) {
        return centralBankClient.registerBank(bankId, name, address);
    }

    @PostMapping("/central-bank/heartbeat")
    @Operation(summary = "Saadab Keskpangale heartbeat päringu")
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
    @Operation(summary = "Tagastab kohaliku panga avaliku võtme")
    public String getPublicKey() {
        return bankKeyService.getPublicKeyPem();
    }
}