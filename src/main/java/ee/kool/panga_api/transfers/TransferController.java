package ee.kool.panga_api.transfers;

import ee.kool.panga_api.accounts.Account;
import ee.kool.panga_api.accounts.AccountRepository;
import ee.kool.panga_api.security.AuthService;
import ee.kool.panga_api.users.User;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
public class TransferController {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final AuthService authService;

    public TransferController(
            TransferRepository transferRepository,
            AccountRepository accountRepository,
            AuthService authService
    ) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.authService = authService;
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @SecurityRequirement(name = "bearerAuth")
    public TransferResponse createTransfer(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody TransferRequest request
    ) {
        User authenticatedUser = authService.authenticateUser(authorizationHeader);

        if (transferRepository.existsById(request.getTransferId())) {
            Transfer existingTransfer = transferRepository.findById(request.getTransferId())
                    .orElseThrow();

            return toTransferResponse(existingTransfer);
        }

        Account sourceAccount = accountRepository.findById(request.getSourceAccount())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));

        Account destinationAccount = accountRepository.findById(request.getDestinationAccount())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

        if (!sourceAccount.getUser().getId().equals(authenticatedUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only transfer money from your own account");
        }

        if (!sourceAccount.getCurrency().equals(destinationAccount.getCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Accounts must use the same currency");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            Transfer failedTransfer = new Transfer();
            failedTransfer.setTransferId(request.getTransferId());
            failedTransfer.setSourceAccount(request.getSourceAccount());
            failedTransfer.setDestinationAccount(request.getDestinationAccount());
            failedTransfer.setAmount(request.getAmount());
            failedTransfer.setCurrency(sourceAccount.getCurrency());
            failedTransfer.setStatus(TransferStatus.FAILED);
            failedTransfer.setErrorMessage("Insufficient funds");
            failedTransfer.setCreatedAt(Instant.now());
            failedTransfer.setUpdatedAt(Instant.now());

            Transfer savedTransfer = transferRepository.save(failedTransfer);
            return toTransferResponse(savedTransfer);
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transfer transfer = new Transfer();
        transfer.setTransferId(request.getTransferId());
        transfer.setSourceAccount(request.getSourceAccount());
        transfer.setDestinationAccount(request.getDestinationAccount());
        transfer.setAmount(request.getAmount());
        transfer.setCurrency(sourceAccount.getCurrency());
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setErrorMessage(null);
        transfer.setCreatedAt(Instant.now());
        transfer.setUpdatedAt(Instant.now());

        Transfer savedTransfer = transferRepository.save(transfer);

        return toTransferResponse(savedTransfer);
    }

    @PostMapping("/transfers/receive")
    @Transactional
    public InterBankTransferResponse receiveInterBankTransfer(
            @Valid @RequestBody InterBankTransferRequest request
    ) {
        if (transferRepository.existsById(request.getTransferId())) {
            Transfer existingTransfer = transferRepository.findById(request.getTransferId())
                    .orElseThrow();

            return new InterBankTransferResponse(
                    existingTransfer.getTransferId(),
                    existingTransfer.getStatus().name().toLowerCase(),
                    existingTransfer.getDestinationAccount(),
                    existingTransfer.getAmount(),
                    existingTransfer.getCreatedAt()
            );
        }

        Account destinationAccount = accountRepository.findById(request.getDestinationAccount())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

        if (!destinationAccount.getCurrency().equals(request.getCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency does not match destination account currency");
        }

        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));
        accountRepository.save(destinationAccount);

        Transfer transfer = new Transfer();
        transfer.setTransferId(request.getTransferId());
        transfer.setSourceAccount(request.getSourceAccount());
        transfer.setDestinationAccount(request.getDestinationAccount());
        transfer.setAmount(request.getAmount());
        transfer.setCurrency(request.getCurrency());
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setErrorMessage(null);
        transfer.setCreatedAt(Instant.now());
        transfer.setUpdatedAt(Instant.now());

        Transfer savedTransfer = transferRepository.save(transfer);

        return new InterBankTransferResponse(
                savedTransfer.getTransferId(),
                savedTransfer.getStatus().name().toLowerCase(),
                savedTransfer.getDestinationAccount(),
                savedTransfer.getAmount(),
                savedTransfer.getCreatedAt()
        );
    }

    @GetMapping("/transfers/{transferId}")
    @SecurityRequirement(name = "bearerAuth")
    public TransferStatusResponse getTransferStatus(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String transferId
    ) {
        User authenticatedUser = authService.authenticateUser(authorizationHeader);

        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));

        Account sourceAccount = accountRepository.findById(transfer.getSourceAccount())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));

        Account destinationAccount = accountRepository.findById(transfer.getDestinationAccount())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

        boolean isSourceOwner = sourceAccount.getUser().getId().equals(authenticatedUser.getId());
        boolean isDestinationOwner = destinationAccount.getUser().getId().equals(authenticatedUser.getId());

        if (!isSourceOwner && !isDestinationOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view transfers related to your own accounts");
        }

        return new TransferStatusResponse(
                transfer.getTransferId(),
                transfer.getSourceAccount(),
                transfer.getDestinationAccount(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getStatus(),
                transfer.getErrorMessage(),
                transfer.getCreatedAt(),
                transfer.getUpdatedAt()
        );
    }

    private TransferResponse toTransferResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getTransferId(),
                transfer.getSourceAccount(),
                transfer.getDestinationAccount(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getStatus(),
                transfer.getErrorMessage(),
                transfer.getCreatedAt()
        );
    }
}