package ee.kool.panga_api.accounts;

import ee.kool.panga_api.security.AuthService;
import ee.kool.panga_api.users.User;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
public class AccountController {

    private static final String BANK_PREFIX = "KEN";

    private final AccountRepository accountRepository;
    private final AuthService authService;

    public AccountController(AccountRepository accountRepository, AuthService authService) {
        this.accountRepository = accountRepository;
        this.authService = authService;
    }

    @PostMapping("/users/{userId}/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    public AccountCreationResponse createAccount(
            @PathVariable String userId,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody AccountCreationRequest request
    ) {
        User authenticatedUser = authService.authenticateUser(authorizationHeader);

        if (!authenticatedUser.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only create accounts for yourself");
        }

        Account account = new Account();

        account.setAccountNumber(generateAccountNumber());
        account.setUser(authenticatedUser);
        account.setCurrency(request.getCurrency());
        account.setBalance(new BigDecimal("1000.00"));
        account.setCreatedAt(Instant.now());

        Account savedAccount = accountRepository.save(account);

        return new AccountCreationResponse(
                savedAccount.getAccountNumber(),
                savedAccount.getUser().getId(),
                savedAccount.getCurrency(),
                savedAccount.getBalance(),
                savedAccount.getCreatedAt()
        );
    }

    @GetMapping("/accounts/{accountNumber}")
    public AccountLookupResponse getAccount(@PathVariable String accountNumber) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        return new AccountLookupResponse(
                account.getAccountNumber(),
                account.getUser().getId(),
                account.getUser().getFullName(),
                account.getCurrency(),
                account.getBalance()
        );
    }

    private String generateAccountNumber() {
        long nextNumber = accountRepository.countByAccountNumberStartingWith(BANK_PREFIX) + 1;
        return BANK_PREFIX + String.format("%05d", nextNumber);
    }
}