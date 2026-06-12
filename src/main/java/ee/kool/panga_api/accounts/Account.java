package ee.kool.panga_api.accounts;

import ee.kool.panga_api.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String currency;

    private BigDecimal balance;

    private Instant createdAt;
}