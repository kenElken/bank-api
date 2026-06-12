package ee.kool.panga_api.transfers;

import ee.kool.panga_api.accounts.Account;
import ee.kool.panga_api.accounts.AccountRepository;
import ee.kool.panga_api.security.AuthService;
import ee.kool.panga_api.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@Tag(name = "Transfers", description = "Pangasisesed ja pankadevahelised ülekanded")
public class TransferController {

    private static final String BANK_PREFIX = "KEN";

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final AuthService authService;
    private final InterBankTransferService interBankTransferService;
    private final InterBankJwtService interBankJwtService;
    private final InterBankJwtReceiveService interBankJwtReceiveService;

    public TransferController(
            TransferRepository transferRepository,
            AccountRepository accountRepository,
            AuthService authService,
            InterBankTransferService interBankTransferService,
            InterBankJwtService interBankJwtService,
            InterBankJwtReceiveService interBankJwtReceiveService
    ) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.authService = authService;
        this.interBankTransferService = interBankTransferService;
        this.interBankJwtService = interBankJwtService;
        this.interBankJwtReceiveService = interBankJwtReceiveService;
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Algatab uue ülekande")
    public TransferResponse createTransfer(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody TransferRequest request
    ) {
        User authenticatedUser = authService.authenticateUser(authorizationHeader);

        if (transferRepository.existsById(request.getTransferId())) {
            Transfer existingTransfer = transferRepository.findById(request.getTransferId())
                    .orElseThrow();

            ensureUserCanViewTransfer(existingTransfer, authenticatedUser);

            return toTransferResponse(existingTransfer);
        }

        Account sourceAccount = accountRepository.findById(request.getSourceAccount())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));

        if (!sourceAccount.getUser().getId().equals(authenticatedUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only transfer money from your own account");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            Transfer failedTransfer = createTransferRecord(
                    request,
                    sourceAccount.getCurrency(),
                    TransferStatus.FAILED,
                    "Insufficient funds"
            );

            Transfer savedTransfer = transferRepository.save(failedTransfer);
            return toTransferResponse(savedTransfer);
        }

        if (request.getDestinationAccount().startsWith(BANK_PREFIX)) {
            return createInternalTransfer(request, sourceAccount);
        }

        return createPendingInterBankTransfer(request, sourceAccount);
    }

    private TransferResponse createInternalTransfer(TransferRequest request, Account sourceAccount) {
        Account destinationAccount = accountRepository.findById(request.getDestinationAccount())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

        if (!sourceAccount.getCurrency().equals(destinationAccount.getCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Accounts must use the same currency");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transfer transfer = createTransferRecord(
                request,
                sourceAccount.getCurrency(),
                TransferStatus.COMPLETED,
                null
        );

        Transfer savedTransfer = transferRepository.save(transfer);

        return toTransferResponse(savedTransfer);
    }

    private TransferResponse createPendingInterBankTransfer(TransferRequest request, Account sourceAccount) {
        DestinationBankInfo destinationBank = interBankTransferService.findDestinationBank(
                request.getDestinationAccount()
        );

        String jwt = interBankJwtService.createTransferJwt(
                request,
                destinationBank.getBankId(),
                sourceAccount.getCurrency()
        );

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(sourceAccount);

        TransferStatus finalStatus;
        String message;

        try {
            String destinationResponse = interBankTransferService.sendTransferToDestinationBank(
                    destinationBank.getAddress(),
                    jwt
            );

            finalStatus = TransferStatus.COMPLETED;
            message = "Inter-bank transfer sent to "
                    + destinationBank.getBankId()
                    + " at "
                    + destinationBank.getAddress()
                    + ". Response: "
                    + destinationResponse;

        } catch (ResponseStatusException e) {
            finalStatus = TransferStatus.PENDING;
            message = "Inter-bank transfer pending. Destination bank: "
                    + destinationBank.getBankId()
                    + ", address: "
                    + destinationBank.getAddress()
                    + ". Reason: "
                    + e.getReason();
        }

        Transfer transfer = createTransferRecord(
                request,
                sourceAccount.getCurrency(),
                finalStatus,
                message
        );

        Transfer savedTransfer = transferRepository.save(transfer);

        return toTransferResponse(savedTransfer);
    }

    @PostMapping("/transfers/receive")
    @Transactional
    @Operation(summary = "Võtab vastu teisest pangast tuleva JWT-põhise ülekande")
    public InterBankTransferResponse receiveInterBankTransfer(
            @Valid @RequestBody IncomingTransferJwtRequest request
    ) {
        VerifiedInterBankTransfer incomingTransfer = interBankJwtReceiveService.verifyAndExtract(request.getJwt());

        if (transferRepository.existsById(incomingTransfer.getTransferId())) {
            Transfer existingTransfer = transferRepository.findById(incomingTransfer.getTransferId())
                    .orElseThrow();

            return new InterBankTransferResponse(
                    existingTransfer.getTransferId(),
                    existingTransfer.getStatus().name().toLowerCase(),
                    existingTransfer.getDestinationAccount(),
                    existingTransfer.getAmount(),
                    existingTransfer.getCreatedAt()
            );
        }

        Account destinationAccount = accountRepository.findById(incomingTransfer.getDestinationAccount())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

        if (!destinationAccount.getCurrency().equals(incomingTransfer.getCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency does not match destination account currency");
        }

        destinationAccount.setBalance(destinationAccount.getBalance().add(incomingTransfer.getAmount()));
        accountRepository.save(destinationAccount);

        Transfer transfer = new Transfer();
        transfer.setTransferId(incomingTransfer.getTransferId());
        transfer.setSourceAccount(incomingTransfer.getSourceAccount());
        transfer.setDestinationAccount(incomingTransfer.getDestinationAccount());
        transfer.setAmount(incomingTransfer.getAmount());
        transfer.setCurrency(incomingTransfer.getCurrency());
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setErrorMessage("Incoming inter-bank transfer from " + incomingTransfer.getSourceBankId());
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
    @Operation(summary = "Tagastab ülekande staatuse transferId järgi")
    public TransferStatusResponse getTransferStatus(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String transferId
    ) {
        User authenticatedUser = authService.authenticateUser(authorizationHeader);

        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));

        ensureUserCanViewTransfer(transfer, authenticatedUser);

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

    private void ensureUserCanViewTransfer(Transfer transfer, User authenticatedUser) {
        boolean isSourceOwner = false;
        boolean isDestinationOwner = false;

        if (transfer.getSourceAccount().startsWith(BANK_PREFIX)) {
            Account sourceAccount = accountRepository.findById(transfer.getSourceAccount())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));

            isSourceOwner = sourceAccount.getUser().getId().equals(authenticatedUser.getId());
        }

        if (transfer.getDestinationAccount().startsWith(BANK_PREFIX)) {
            Account destinationAccount = accountRepository.findById(transfer.getDestinationAccount())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

            isDestinationOwner = destinationAccount.getUser().getId().equals(authenticatedUser.getId());
        }

        if (!isSourceOwner && !isDestinationOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view transfers related to your own accounts");
        }
    }

    private Transfer createTransferRecord(
            TransferRequest request,
            String currency,
            TransferStatus status,
            String errorMessage
    ) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(request.getTransferId());
        transfer.setSourceAccount(request.getSourceAccount());
        transfer.setDestinationAccount(request.getDestinationAccount());
        transfer.setAmount(request.getAmount());
        transfer.setCurrency(currency);
        transfer.setStatus(status);
        transfer.setErrorMessage(errorMessage);
        transfer.setCreatedAt(Instant.now());
        transfer.setUpdatedAt(Instant.now());

        return transfer;
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