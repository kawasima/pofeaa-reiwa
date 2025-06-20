package pofeaa.original.domainlogic.tablemodule;

import org.jooq.DSLContext;
import org.jooq.Record;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static org.jooq.impl.DSL.table;

public class Contract extends TableModule {
    public Contract(DSLContext ctx) {
        super(table("contracts"), ctx);
    }

    public void calculateRecognitions(long contractId) {
        Record record = find(contractId);
        BigDecimal amount = record.getValue("AMOUNT", BigDecimal.class);
        RevenueRecognition rr = new RevenueRecognition(ctx);
        Product product = new Product(ctx);
        long productId = getProductId(contractId);
        switch(product.getProductType(productId)) {
            case WORD_PROCESSOR -> {
                rr.insert(contractId, amount, getWhenSigned(contractId).toLocalDate());
            }
            case SPREADSHEET -> {
                BigDecimal[] allocation = allocate(amount,3);
                rr.insert(contractId, allocation[0], getWhenSigned(contractId).toLocalDate());
                rr.insert(contractId, allocation[1], getWhenSigned(contractId).toLocalDate()
                        .plusDays(60));
                rr.insert(contractId, allocation[2], getWhenSigned(contractId).toLocalDate()
                        .plusDays(90));
            }
            case DATABASE -> {
                BigDecimal[] allocation = allocate(amount, 3);
                rr.insert(contractId, allocation[0], getWhenSigned(contractId).toLocalDate());
                rr.insert(contractId, allocation[1], getWhenSigned(contractId).toLocalDate()
                        .plusDays(30));
                rr.insert(contractId, allocation[2], getWhenSigned(contractId).toLocalDate()
                        .plusDays(60));
            }
            default -> throw new RuntimeException("Invalid product id");
        }
    }

    public Long getProductId(long id) {
        return find(id).getValue("PRODUCT_ID", Long.class);
    }

    public LocalDateTime getWhenSigned(long id) {
        return find(id).getValue("DATE_SIGNED", LocalDateTime.class);
    }

    private BigDecimal[] allocate(BigDecimal amount, int by) {
        BigDecimal[] result = new BigDecimal[by];
        BigDecimal lowAmount = amount.divide(BigDecimal.valueOf(by), 2, RoundingMode.DOWN);
        BigDecimal highAmount = lowAmount.add(new BigDecimal("0.01"));
        
        // Calculate total of low amounts
        BigDecimal lowTotal = lowAmount.multiply(BigDecimal.valueOf(by));
        
        // Calculate remainder in cents
        BigDecimal remainder = amount.subtract(lowTotal);
        int remainderCents = remainder.multiply(new BigDecimal("100")).intValue();
        
        // Distribute the remainder
        for (int i = 0; i < by; i++) {
            if (i < remainderCents) {
                result[i] = highAmount;
            } else {
                result[i] = lowAmount;
            }
        }
        
        return result;
    }
}
