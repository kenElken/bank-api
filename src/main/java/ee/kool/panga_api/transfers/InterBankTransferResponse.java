package ee.kool.panga_api.transfers;

import java.math.BigDecimal;
import java.time.Instant;

public class InterBankTransferResponse {

    private String transferId;
    private String status;
    private String destinationAccount;
    private BigDecimal amount;
    private Instant timestamp;

    public InterBankTransferResponse(String transferId, String status, String destinationAccount, BigDecimal amount, Instant timestamp) {
        this.transferId = transferId;
        this.status = status;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getTransferId() {
        return transferId;
    }

    public String getStatus() {
        return status;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}