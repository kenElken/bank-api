package ee.kool.panga_api.transfers;

import java.math.BigDecimal;

public class VerifiedInterBankTransfer {

    private final String transferId;
    private final String sourceAccount;
    private final String destinationAccount;
    private final BigDecimal amount;
    private final String currency;
    private final String sourceBankId;

    public VerifiedInterBankTransfer(
            String transferId,
            String sourceAccount,
            String destinationAccount,
            BigDecimal amount,
            String currency,
            String sourceBankId
    ) {
        this.transferId = transferId;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.currency = currency;
        this.sourceBankId = sourceBankId;
    }

    public String getTransferId() {
        return transferId;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getSourceBankId() {
        return sourceBankId;
    }
}