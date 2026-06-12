package ee.kool.panga_api.transfers;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import ee.kool.panga_api.centralbank.BankKeyService;
import org.springframework.stereotype.Service;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class InterBankJwtService {

    private static final String SOURCE_BANK_ID = "KEN001";

    private final BankKeyService bankKeyService;

    public InterBankJwtService(BankKeyService bankKeyService) {
        this.bankKeyService = bankKeyService;
    }

    public String createTransferJwt(
            TransferRequest request,
            String destinationBankId,
            String currency
    ) {
        try {
            ECPublicKey publicKey = (ECPublicKey) bankKeyService.getPublicKey();
            ECPrivateKey privateKey = (ECPrivateKey) bankKeyService.getPrivateKey();

            ECKey ecKey = new ECKey.Builder(Curve.P_256, publicKey)
                    .privateKey(privateKey)
                    .keyID(SOURCE_BANK_ID)
                    .build();

            Instant now = Instant.now();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .claim("transferId", request.getTransferId())
                    .claim("sourceAccount", request.getSourceAccount())
                    .claim("destinationAccount", request.getDestinationAccount())
                    .claim("amount", request.getAmount().toPlainString())
                    .claim("currency", currency)
                    .claim("sourceBankId", SOURCE_BANK_ID)
                    .claim("destinationBankId", destinationBankId)
                    .claim("timestamp", now.toString())
                    .claim("nonce", UUID.randomUUID().toString())
                    .issueTime(Date.from(now))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.ES256)
                            .type(JOSEObjectType.JWT)
                            .keyID(SOURCE_BANK_ID)
                            .build(),
                    claims
            );

            signedJWT.sign(new ECDSASigner(ecKey));

            return signedJWT.serialize();

        } catch (Exception e) {
            throw new IllegalStateException("Could not create inter-bank transfer JWT", e);
        }
    }
}