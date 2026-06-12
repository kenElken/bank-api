package ee.kool.panga_api.accounts;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountCreationResponse {

    private String accountNumber;
    private String userId;
    private String currency;
    private BigDecimal balance;
    private Instant createdAt;

    public AccountCreationResponse(String accountNumber, String userId, String currency, BigDecimal balance, Instant createdAt) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.currency = currency;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUserId() {
        return userId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}