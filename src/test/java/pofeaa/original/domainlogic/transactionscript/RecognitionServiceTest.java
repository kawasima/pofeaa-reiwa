package pofeaa.original.domainlogic.transactionscript;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

class RecognitionServiceTest {
    RecognitionService recognitionService;
    DSLContext ctx;

    @BeforeEach
    void setup() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        ctx = DSL.using(ds, SQLDialect.H2);
        ctx.createTable(table("contracts"))
                .column(field("id", SQLDataType.BIGINT.identity(true)))
                .column(field("product_id", SQLDataType.BIGINT))
                .column(field("revenue", SQLDataType.DECIMAL.precision(10, 2)))
                .column(field("date_signed", SQLDataType.TIMESTAMP))
                .execute();

        ctx.createTable(table("products"))
                .column(field("id", SQLDataType.BIGINT.identity(true)))
                .column(field("name", SQLDataType.VARCHAR))
                .column(field("type", SQLDataType.VARCHAR))
                .execute();

        ctx.createTable(table("revenue_recognitions"))
                .column(field("id", SQLDataType.BIGINT.identity(true)))
                .column(field("contract_id", SQLDataType.BIGINT))
                .column(field("amount", SQLDataType.DECIMAL.precision(10, 2)))
                .column(field("recognized_on", SQLDataType.DATE))
                .execute();

        ctx.insertInto(table("products"))
                .set(field("name"), "Spreadsheet")
                .set(field("type"), "S")
                .execute();
        ctx.insertInto(table("products"))
                .set(field("name"), "Word Processor")
                .set(field("type"), "W")
                .execute();
        ctx.insertInto(table("products"))
                .set(field("name"), "Database")
                .set(field("type"), "D")
                .execute();
        ctx.insertInto(table("contracts"))
                .set(field("product_id"), 1L)
                .set(field("revenue"), 1000.00)
                .set(field("date_signed"), "2023-01-01")
                .execute();
        ctx.insertInto(table("contracts"))
                .set(field("product_id"), 2L)
                .set(field("revenue"), 1000.00)
                .set(field("date_signed"), "2023-01-01")
                .execute();
        ctx.insertInto(table("contracts"))
                .set(field("product_id"), 3L)
                .set(field("revenue"), 1000.00)
                .set(field("date_signed"), "2023-01-01")
                .execute();
    }

    @AfterEach
    void tearDown() {
        ctx.dropTable(table("revenue_recognitions")).execute();
        ctx.dropTable(table("contracts")).execute();
        ctx.dropTable(table("products")).execute();
    }

    @Test
    public void recognitionSpreadSheet() {
        Gateway gateway = new Gateway(ctx);
        recognitionService = new RecognitionService(gateway);
        recognitionService.calculateRevenueRecognitions(1L);
        LocalDate dateSigned = LocalDate.of(2023, 1, 1);
        assertThat(recognitionService.recognizedRevenue(1, dateSigned))
                .isEqualTo(Money.dollars(new BigDecimal("333.34")));
        assertThat(recognitionService.recognizedRevenue(1, dateSigned.plusDays(59)))
                .isEqualTo(Money.dollars(new BigDecimal("333.34")));
        assertThat(recognitionService.recognizedRevenue(1, dateSigned.plusDays(60)))
                .isEqualTo(Money.dollars(new BigDecimal("666.67")));
        assertThat(recognitionService.recognizedRevenue(1, dateSigned.plusDays(89)))
                .isEqualTo(Money.dollars(new BigDecimal("666.67")));
        assertThat(recognitionService.recognizedRevenue(1, dateSigned.plusDays(90)))
                .isEqualTo(Money.dollars(new BigDecimal("1000.00")));
    }

    @Test
    public void recognitionWordProcessor() {
        Gateway gateway = new Gateway(ctx);
        recognitionService = new RecognitionService(gateway);
        recognitionService.calculateRevenueRecognitions(2L);
        LocalDate dateSigned = LocalDate.of(2023, 1, 1);
        assertThat(recognitionService.recognizedRevenue(2L, dateSigned))
                .isEqualTo(Money.dollars(new BigDecimal("1000.00")));
        assertThat(recognitionService.recognizedRevenue(2L, dateSigned.plusDays(100)))
                .isEqualTo(Money.dollars(new BigDecimal("1000.00")));
    }

    @Test
    public void recognitionDatabase() {
        Gateway gateway = new Gateway(ctx);
        recognitionService = new RecognitionService(gateway);
        recognitionService.calculateRevenueRecognitions(3L);
        LocalDate dateSigned = LocalDate.of(2023, 1, 1);
        assertThat(recognitionService.recognizedRevenue(3L, dateSigned))
                .isEqualTo(Money.dollars(new BigDecimal("333.34")));
        assertThat(recognitionService.recognizedRevenue(3L, dateSigned.plusDays(29)))
                .isEqualTo(Money.dollars(new BigDecimal("333.34")));
        assertThat(recognitionService.recognizedRevenue(3L, dateSigned.plusDays(30)))
                .isEqualTo(Money.dollars(new BigDecimal("666.67")));
        assertThat(recognitionService.recognizedRevenue(3L, dateSigned.plusDays(59)))
                .isEqualTo(Money.dollars(new BigDecimal("666.67")));
        assertThat(recognitionService.recognizedRevenue(3L, dateSigned.plusDays(60)))
                .isEqualTo(Money.dollars(new BigDecimal("1000.00")));
    }

    @Test
    public void beforeDateSigned() {
        Gateway gateway = new Gateway(ctx);
        recognitionService = new RecognitionService(gateway);
        recognitionService.calculateRevenueRecognitions(1L);
        LocalDate dateSigned = LocalDate.of(2023, 1, 1);
        assertThat(recognitionService.recognizedRevenue(1, dateSigned.minusDays(1)))
                .isEqualTo(Money.dollars(BigDecimal.ZERO));
    }
}