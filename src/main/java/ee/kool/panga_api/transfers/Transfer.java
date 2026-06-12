package ee.kool.panga_api.transfers;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
public class Transfer {

    @Id
    private String transferId;

    private String sourceAccount;

    private String destinationAccount;

    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    private String errorMessage;

    private Instant createdAt;

    private Instant updatedAt;
}