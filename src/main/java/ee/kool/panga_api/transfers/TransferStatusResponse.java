package ee.kool.panga_api.transfers;

import java.math.BigDecimal;
import java.time.Instant;

public class TransferStatusResponse {

    private String transferId;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private String currency;
    private TransferStatus status;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;

    public TransferStatusResponse(
            String transferId,
            String sourceAccount,
            String destinationAccount,
            BigDecimal amount,
            String currency,
            TransferStatus status,
            String errorMessage,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.transferId = transferId;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}