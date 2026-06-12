package ee.kool.panga_api.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegistrationRequest {

    @NotBlank
    @Size(min = 2, max = 200)
    private String fullName;

    @Email
    @Size(max = 255)
    private String email;

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
}