package ee.kool.panga_api.accounts;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {

    long countByAccountNumberStartingWith(String prefix);
}