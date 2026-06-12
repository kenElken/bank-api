package ee.kool.panga_api.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Kasutajate registreerimine ja haldus")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registreerib uue kasutaja ja väljastab API võtme")
    public UserRegistrationResponse registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        User user = new User();

        user.setId("user-" + UUID.randomUUID());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setApiKey(UUID.randomUUID().toString());
        user.setCreatedAt(Instant.now());

        User savedUser = userRepository.save(user);

        return new UserRegistrationResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getApiKey(),
                savedUser.getCreatedAt()
        );
    }
}