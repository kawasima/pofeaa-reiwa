package pofeaa.original.domainlogic.tablemodule;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

class ContractTest {
    private DSLContext ctx;
    private Contract contract;

    @BeforeEach
    void setup() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        ctx = DSL.using(ds, SQLDialect.H2);
        
        // Create tables
        ctx.createTable(table("contracts"))
                .column(field("id", SQLDataType.BIGINT.identity(true)))
                .column(field("product_id", SQLDataType.BIGINT))
                .column(field("amount", SQLDataType.DECIMAL.precision(10, 2)))
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

        // Insert test products
        ctx.insertInto(table("products"))
                .set(field("id"), 1L)
                .set(field("name"), "Spreadsheet")
                .set(field("type"), "S")
                .execute();
        
        ctx.insertInto(table("products"))
                .set(field("id"), 2L)
                .set(field("name"), "Word Processor")
                .set(field("type"), "W")
                .execute();
        
        ctx.insertInto(table("products"))
                .set(field("id"), 3L)
                .set(field("name"), "Database")
                .set(field("type"), "D")
                .execute();

        // Initialize modules
        contract = new Contract(ctx);
    }

    @AfterEach
    void tearDown() {
        ctx.dropTable(table("revenue_recognitions")).execute();
        ctx.dropTable(table("contracts")).execute();
        ctx.dropTable(table("products")).execute();
    }

    @Nested
    @DisplayName("Word Processor Tests")
    class WordProcessorTests {
        
        @Test
        @DisplayName("Should recognize full revenue on contract date for word processor")
        void shouldRecognizeFullRevenueOnContractDate() {
            // Given
            LocalDateTime dateSigned = LocalDateTime.of(2023, 1, 1, 0, 0);
            ctx.insertInto(table("contracts"))
                    .set(field("id"), 1L)
                    .set(field("product_id"), 2L) // Word Processor
                    .set(field("amount"), new BigDecimal("1000.00"))
                    .set(field("date_signed"), dateSigned)
                    .execute();

            // When
            contract.calculateRecognitions(1L);

            // Then
            var recognitions = ctx.select()
                    .from(table("revenue_recognitions"))
                    .where(field("contract_id").eq(1L))
                    .orderBy(field("recognized_on"))
                    .fetch();

            assertThat(recognitions).hasSize(1);
            assertThat(recognitions.get(0).getValue("AMOUNT", BigDecimal.class))
                    .isEqualTo(new BigDecimal("1000.00"));
            assertThat(recognitions.get(0).getValue("RECOGNIZED_ON", LocalDate.class))
                    .isEqualTo(dateSigned.toLocalDate());
        }

        @Test
        @DisplayName("Should handle different amounts for word processor")
        void shouldHandleDifferentAmounts() {
            // Given
            LocalDateTime dateSigned = LocalDateTime.of(2023, 2, 15, 0, 0);
            ctx.insertInto(table("contracts"))
                    .set(field("id"), 2L)
                    .set(field("product_id"), 2L) // Word Processor
                    .set(field("amount"), new BigDecimal("2500.50"))
                    .set(field("date_signed"), dateSigned)
                    .execute();

            // When
            contract.calculateRecognitions(2L);

            // Then
            var recognitions = ctx.select()
                    .from(table("revenue_recognitions"))
                    .where(field("contract_id").eq(2L))
                    .fetch();

            assertThat(recognitions).hasSize(1);
            assertThat(recognitions.get(0).getValue("AMOUNT", BigDecimal.class))
                    .isEqualTo(new BigDecimal("2500.50"));
        }
    }

    @Nested
    @DisplayName("Database Tests")
    class DatabaseTests {
        
        @Test
        @DisplayName("Should split revenue into three installments for database")
        void shouldSplitRevenueIntoThreeInstallments() {
            // Given
            LocalDateTime dateSigned = LocalDateTime.of(2023, 1, 1, 0, 0);
            ctx.insertInto(table("contracts"))
                    .set(field("id"), 3L)
                    .set(field("product_id"), 3L) // Database
                    .set(field("amount"), new BigDecimal("1000.00"))
                    .set(field("date_signed"), dateSigned)
                    .execute();

            // When
            contract.calculateRecognitions(3L);

            // Then
            var recognitions = ctx.select()
                    .from(table("revenue_recognitions"))
                    .where(field("contract_id").eq(3L))
                    .orderBy(field("recognized_on"))
                    .fetch();

            assertThat(recognitions).hasSize(3);
            
            // First installment on contract date
            assertThat(recognitions.get(0).getValue("AMOUNT", BigDecimal.class))
                    .isEqualTo(new BigDecimal("333.34"));
            assertThat(recognitions.get(0).getValue("RECOGNIZED_ON", LocalDate.class))
                    .isEqualTo(dateSigned.toLocalDate());
            
            // Second installment after 30 days
            assertThat(recognitions.get(1).getValue("AMOUNT", BigDecimal.class))
                    .isEqualTo(new BigDecimal("333.33"));
            assertThat(recognitions.get(1).getValue("RECOGNIZED_ON", LocalDate.class))
                    .isEqualTo(dateSigned.toLocalDate().plusDays(30));
            
            // Third installment after 60 days
            assertThat(recognitions.get(2).getValue("AMOUNT", BigDecimal.class))
                    .isEqualTo(new BigDecimal("333.33"));
            assertThat(recognitions.get(2).getValue("RECOGNIZED_ON", LocalDate.class))
                    .isEqualTo(dateSigned.toLocalDate().plusDays(60));
            
            // Verify total equals original amount
            BigDecimal total = recognitions.stream()
                    .map(r -> r.getValue("AMOUNT", BigDecimal.class))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(total).isEqualTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Should handle amounts that don't divide evenly by three")
        void shouldHandleUnevenDivision() {
            // Given
            LocalDateTime dateSigned = LocalDateTime.of(2023, 3, 15, 0, 0);
            ctx.insertInto(table("contracts"))
                    .set(field("id"), 4L)
                    .set(field("product_id"), 3L) // Database
                    .set(field("amount"), new BigDecimal("100.00"))
                    .set(field("date_signed"), dateSigned)
                    .execute();

            // When
            contract.calculateRecognitions(4L);

            // Then
            var recognitions = ctx.select()
                    .from(table("revenue_recognitions"))
                    .where(field("contract_id").eq(4L))
                    .orderBy(field("recognized_on"))
                    .fetch();

            assertThat(recognitions).hasSize(3);
            
            // First gets the extra penny
            assertThat(recognitions.get(0).getValue("AMOUNT", BigDecimal.class))
                    .isEqualTo(new BigDecimal("33.34"));
            assertThat(recognitions.get(1).getValue("AMOUNT", BigDecimal.class))
                    .isEqualTo(new BigDecimal("33.33"));
            assertThat(recognitions.get(2).getValue("AMOUNT", BigDecimal.class))
                    .isEqualTo(new BigDecimal("33.33"));
            
            // Verify total
            BigDecimal total = recognitions.stream()
                    .map(r -> r.getValue("AMOUNT", BigDecimal.class))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(total).isEqualTo(new BigDecimal("100.00"));
        }
    }

    @Nested
    @DisplayName("Spreadsheet Tests")
    class SpreadsheetTests {
        
        @Test
        @DisplayName("Should split revenue with different schedule for spreadsheet")
        void shouldSplitRevenueForSpreadsheet() {
            // Given
            LocalDateTime dateSigned = LocalDateTime.of(2023, 1, 1, 0, 0);
            ctx.insertInto(table("contracts"))
                    .set(field("id"), 5L)
                    .set(field("product_id"), 1L) // Spreadsheet
                    .set(field("amount"), new BigDecimal("1000.00"))
                    .set(field("date_signed"), dateSigned)
                    .execute();

            // When
            contract.calculateRecognitions(5L);

            // Then
            var recognitions = ctx.select()
                    .from(table("revenue_recognitions"))
                    .where(field("contract_id").eq(5L))
                    .orderBy(field("recognized_on"))
                    .fetch();

            assertThat(recognitions).hasSize(3);
            
            // First installment on contract date
            assertThat(recognitions.get(0).getValue("RECOGNIZED_ON", LocalDate.class))
                    .isEqualTo(dateSigned.toLocalDate());
            
            // Second installment after 60 days
            assertThat(recognitions.get(1).getValue("RECOGNIZED_ON", LocalDate.class))
                    .isEqualTo(dateSigned.toLocalDate().plusDays(60));
            
            // Third installment after 90 days
            assertThat(recognitions.get(2).getValue("RECOGNIZED_ON", LocalDate.class))
                    .isEqualTo(dateSigned.toLocalDate().plusDays(90));
        }
    }

    @Nested
    @DisplayName("Allocation Tests")
    class AllocationTests {
        
        @Test
        @DisplayName("Should allocate amount correctly preserving total")
        void shouldAllocateAmountCorrectly() {
            // Test various amounts to ensure allocation works correctly
            testAllocation(new BigDecimal("1000.00"), 3, 
                    new BigDecimal("333.34"), new BigDecimal("333.33"), new BigDecimal("333.33"));
            
            testAllocation(new BigDecimal("100.00"), 3,
                    new BigDecimal("33.34"), new BigDecimal("33.33"), new BigDecimal("33.33"));
            
            testAllocation(new BigDecimal("10.00"), 3,
                    new BigDecimal("3.34"), new BigDecimal("3.33"), new BigDecimal("3.33"));
            
            testAllocation(new BigDecimal("1.00"), 3,
                    new BigDecimal("0.34"), new BigDecimal("0.33"), new BigDecimal("0.33"));
        }

        private void testAllocation(BigDecimal amount, int parts, BigDecimal... expected) {
            // Create contract for testing allocation
            LocalDateTime dateSigned = LocalDateTime.now();
            Long contractId = ctx.insertInto(table("contracts"))
                    .set(field("product_id"), 3L) // Database for 3-way split
                    .set(field("amount"), amount)
                    .set(field("date_signed"), dateSigned)
                    .returning(field("id", Long.class))
                    .fetchOne()
                    .getValue(field("id", Long.class));

            contract.calculateRecognitions(contractId);

            var recognitions = ctx.select()
                    .from(table("revenue_recognitions"))
                    .where(field("contract_id").eq(contractId))
                    .orderBy(field("recognized_on"))
                    .fetch();

            for (int i = 0; i < expected.length; i++) {
                assertThat(recognitions.get(i).getValue("AMOUNT", BigDecimal.class))
                        .isEqualTo(expected[i]);
            }

            // Verify total
            BigDecimal total = recognitions.stream()
                    .map(r -> r.getValue("AMOUNT", BigDecimal.class))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(total).isEqualTo(amount);
        }
    }
}