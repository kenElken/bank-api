package ee.kool.panga_api.centralbank;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

@Service
public class BankKeyService {

    private KeyPair keyPair;

    @PostConstruct
    public void generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            this.keyPair = generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate bank key pair", e);
        }
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public String getPublicKeyPem() {
        String base64PublicKey = Base64.getMimeEncoder(
                64,
                "\n".getBytes(StandardCharsets.UTF_8)
        ).encodeToString(keyPair.getPublic().getEncoded());

        return "-----BEGIN PUBLIC KEY-----\n"
                + base64PublicKey
                + "\n-----END PUBLIC KEY-----";
    }
}