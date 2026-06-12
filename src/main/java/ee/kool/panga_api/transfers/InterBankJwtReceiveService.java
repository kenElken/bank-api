package ee.kool.panga_api.transfers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import ee.kool.panga_api.centralbank.BankKeyService;
import ee.kool.panga_api.centralbank.CentralBankClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class InterBankJwtReceiveService {

    private static final String LOCAL_BANK_ID = "KEN001";

    private final CentralBankClient centralBankClient;
    private final ObjectMapper objectMapper;
    private final BankKeyService bankKeyService;

    public InterBankJwtReceiveService(
            CentralBankClient centralBankClient,
            ObjectMapper objectMapper,
            BankKeyService bankKeyService
    ) {
        this.centralBankClient = centralBankClient;
        this.objectMapper = objectMapper;
        this.bankKeyService = bankKeyService;
    }

    public VerifiedInterBankTransfer verifyAndExtract(String jwt) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwt);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            String sourceBankId = claims.getStringClaim("sourceBankId");

            if (sourceBankId == null || sourceBankId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT is missing sourceBankId");
            }

            ECPublicKey publicKey = resolvePublicKey(sourceBankId);

            boolean validSignature = signedJWT.verify(new ECDSAVerifier(publicKey));

            if (!validSignature) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT signature");
            }

            return new VerifiedInterBankTransfer(
                    claims.getStringClaim("transferId"),
                    claims.getStringClaim("sourceAccount"),
                    claims.getStringClaim("destinationAccount"),
                    new BigDecimal(claims.getStringClaim("amount")),
                    claims.getStringClaim("currency"),
                    sourceBankId
            );

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid inter-bank JWT");
        }
    }

    private ECPublicKey resolvePublicKey(String sourceBankId) {
        try {
            if (LOCAL_BANK_ID.equals(sourceBankId)) {
                return (ECPublicKey) bankKeyService.getPublicKey();
            }

            String banksJson = centralBankClient.getBanks();
            JsonNode root = objectMapper.readTree(banksJson);
            JsonNode banks = root.get("banks");

            if (banks == null || !banks.isArray()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid central bank response");
            }

            for (JsonNode bank : banks) {
                String bankId = bank.path("bankId").asText();

                if (sourceBankId.equals(bankId)) {
                    String publicKeyPem = bank.path("publicKey").asText();
                    return parsePublicKey(publicKeyPem);
                }
            }

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source bank public key not found");

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not resolve source bank public key");
        }
    }

    private ECPublicKey parsePublicKey(String publicKeyPem) throws Exception {
        String cleanKey = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decodedKey = Base64.getDecoder().decode(cleanKey);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");

        return (ECPublicKey) keyFactory.generatePublic(keySpec);
    }
}