package ee.kool.panga_api.transfers;

public class DestinationBankInfo {

    private final String bankId;
    private final String address;

    public DestinationBankInfo(String bankId, String address) {
        this.bankId = bankId;
        this.address = address;
    }

    public String getBankId() {
        return bankId;
    }

    public String getAddress() {
        return address;
    }
}