package ee.kool.panga_api.transfers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@Tag(name = "Development Test", description = "Ajutised test-endpointid arenduse jaoks")
public class TestJwtController {

    private final InterBankJwtService interBankJwtService;

    public TestJwtController(InterBankJwtService interBankJwtService) {
        this.interBankJwtService = interBankJwtService;
    }

    @PostMapping("/interbank-jwt")
    @Operation(summary = "Genereerib testimiseks pankadevahelise ülekande JWT")
    public String createTestJwt(@RequestBody TransferRequest request) {
        return interBankJwtService.createTransferJwt(
                request,
                "KEN001",
                "EUR"
        );
    }
}