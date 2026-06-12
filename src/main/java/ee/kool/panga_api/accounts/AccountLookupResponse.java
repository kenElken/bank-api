package ee.kool.panga_api.accounts;

import java.math.BigDecimal;

public class AccountLookupResponse {

    private String accountNumber;
    private String userId;
    private String ownerName;
    private String currency;
    private BigDecimal balance;

    public AccountLookupResponse(String accountNumber, String userId, String ownerName, String currency, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.ownerName = ownerName;
        this.currency = currency;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUserId() {
        return userId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}