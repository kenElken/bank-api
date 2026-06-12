package ee.kool.panga_api.transfers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.kool.panga_api.centralbank.CentralBankClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class InterBankTransferService {

    private final CentralBankClient centralBankClient;
    private final ObjectMapper objectMapper;
    private final RestClient.Builder restClientBuilder;

    public InterBankTransferService(
            CentralBankClient centralBankClient,
            ObjectMapper objectMapper,
            RestClient.Builder restClientBuilder
    ) {
        this.centralBankClient = centralBankClient;
        this.objectMapper = objectMapper;
        this.restClientBuilder = restClientBuilder;
    }

    public DestinationBankInfo findDestinationBank(String destinationAccount) {
        if (destinationAccount == null || destinationAccount.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid destination account number");
        }

        String bankPrefix = destinationAccount.substring(0, 3);

        try {
            String banksJson = centralBankClient.getBanks();
            JsonNode root = objectMapper.readTree(banksJson);
            JsonNode banks = root.get("banks");

            if (banks == null || !banks.isArray()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid response from central bank");
            }

            for (JsonNode bank : banks) {
                String bankId = bank.path("bankId").asText();
                String status = bank.path("status").asText();
                String address = bank.path("address").asText();

                if (bankId.startsWith(bankPrefix) && "active".equalsIgnoreCase(status)) {
                    return new DestinationBankInfo(bankId, address);
                }
            }

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination bank not found or not active");

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not read bank list from central bank");
        }
    }

    public String sendTransferToDestinationBank(String destinationBankAddress, String jwt) {
        String receiveUrl = buildReceiveUrl(destinationBankAddress);

        try {
            String response = restClientBuilder
                    .build()
                    .post()
                    .uri(receiveUrl)
                    .body(Map.of("jwt", jwt))
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                return "Destination bank accepted transfer";
            }

            return response;

        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Destination bank error. Status: "
                            + e.getStatusCode()
                            + ", body: "
                            + e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not reach destination bank"
            );
        }
    }

    private String buildReceiveUrl(String destinationBankAddress) {
        if (destinationBankAddress.endsWith("/")) {
            return destinationBankAddress + "transfers/receive";
        }

        return destinationBankAddress + "/transfers/receive";
    }
}