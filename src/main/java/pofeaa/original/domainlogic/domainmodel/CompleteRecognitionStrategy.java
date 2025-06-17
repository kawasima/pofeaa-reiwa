package pofeaa.original.domainlogic.domainmodel;

public class CompleteRecognitionStrategy implements RecognitionStrategy {
    @Override
    public void calculateRevenueRecognitions(Contract contract) {
        contract.addRevenueRecognition(new RevenueRecognition(contract.getRevenue(), contract.getWhenSigned()));
    }

}
