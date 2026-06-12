package ee.kool.panga_api.transfers;

import jakarta.validation.constraints.NotBlank;

public class IncomingTransferJwtRequest {

    @NotBlank
    private String jwt;

    public String getJwt() {
        return jwt;
    }
}