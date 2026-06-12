package ee.kool.panga_api.accounts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AccountCreationRequest {

    @NotBlank
    @Pattern(regexp = "EUR|USD|GBP", message = "Currency must be EUR, USD or GBP")
    private String currency;

    public String getCurrency() {
        return currency;
    }
}