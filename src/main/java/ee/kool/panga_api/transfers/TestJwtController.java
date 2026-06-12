package ee.kool.panga_api.transfers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestJwtController {

    private final InterBankJwtService interBankJwtService;

    public TestJwtController(InterBankJwtService interBankJwtService) {
        this.interBankJwtService = interBankJwtService;
    }

    @PostMapping("/interbank-jwt")
    public String createTestJwt(@RequestBody TransferRequest request) {
        return interBankJwtService.createTransferJwt(
                request,
                "KEN001",
                "EUR"
        );
    }
}