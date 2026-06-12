package ee.kool.panga_api.transfers;

import java.math.BigDecimal;
import java.time.Instant;

public class TransferResponse {

    private String transferId;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private String currency;
    private TransferStatus status;
    private String errorMessage;
    private Instant createdAt;

    public TransferResponse(
            String transferId,
            String sourceAccount,
            String destinationAccount,
            BigDecimal amount,
            String currency,
            TransferStatus status,
            String errorMessage,
            Instant createdAt
    ) {
        this.transferId = transferId;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
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

    public TransferStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}