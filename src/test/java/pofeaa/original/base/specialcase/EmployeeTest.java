package pofeaa.original.base.specialcase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class EmployeeTest {

    @Nested
    @DisplayName("DefaultEmployee Tests")
    class DefaultEmployeeTest {
        
        private DefaultEmployee employee;
        private Contract contract;
        
        @BeforeEach
        void setUp() {
            contract = new Contract(LocalDate.now());
            employee = new DefaultEmployee("John Doe", contract);
        }
        
        @Test
        @DisplayName("Should return correct name")
        void shouldReturnCorrectName() {
            assertThat(employee.getName()).isEqualTo("John Doe");
        }
        
        @Test
        @DisplayName("Should return contract")
        void shouldReturnContract() {
            assertThat(employee.getContract()).isEqualTo(contract);
        }
        
        @Test
        @DisplayName("Should calculate gross to date with no periods")
        void shouldCalculateGrossToDateWithNoPeriods() {
            assertThat(employee.getGrossToDate()).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Should calculate gross to date with single period")
        void shouldCalculateGrossToDateWithSinglePeriod() {
            contract.addPaymentForPeriod(0, new BigDecimal("5000.00"));
            assertThat(employee.getGrossToDate()).isEqualTo(new BigDecimal("5000.00"));
        }
        
        @Test
        @DisplayName("Should calculate gross to date with multiple periods")
        void shouldCalculateGrossToDateWithMultiplePeriods() {
            contract.addPaymentForPeriod(0, new BigDecimal("5000.00"));
            contract.addPaymentForPeriod(1, new BigDecimal("4500.00"));
            contract.addPaymentForPeriod(2, new BigDecimal("4000.00"));
            
            // getGrossToDate() calls calculateGrossFromPeriod(0), which should sum only period 0
            assertThat(employee.getGrossToDate()).isEqualTo(new BigDecimal("5000.00"));
        }
        
        @Test
        @DisplayName("Should calculate gross from specific period")
        void shouldCalculateGrossFromSpecificPeriod() {
            contract.addPaymentForPeriod(0, new BigDecimal("5000.00"));
            contract.addPaymentForPeriod(1, new BigDecimal("4500.00"));
            contract.addPaymentForPeriod(2, new BigDecimal("4000.00"));
            contract.addPaymentForPeriod(3, new BigDecimal("3500.00"));
            
            // calculateGrossFromPeriod(2) sums periods 0, 1, and 2
            BigDecimal grossFromPeriod2 = employee.calculateGrossFromPeriod(2);
            assertThat(grossFromPeriod2).isEqualTo(new BigDecimal("13500.00")); // 5000 + 4500 + 4000
        }
        
        @Test
        @DisplayName("Should return zero for null contract")
        void shouldReturnZeroForNullContract() {
            DefaultEmployee employeeWithNullContract = new DefaultEmployee("Jane Doe", null);
            assertThat(employeeWithNullContract.getGrossToDate()).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Should return zero for Contract.NULL")
        void shouldReturnZeroForContractNull() {
            DefaultEmployee employeeWithNullContract = new DefaultEmployee("Jane Doe", Contract.NULL);
            assertThat(employeeWithNullContract.getGrossToDate()).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Should handle non-contiguous periods")
        void shouldHandleNonContiguousPeriods() {
            contract.addPaymentForPeriod(0, new BigDecimal("5000.00"));
            contract.addPaymentForPeriod(2, new BigDecimal("4000.00")); // Skip period 1
            contract.addPaymentForPeriod(4, new BigDecimal("3000.00")); // Skip period 3
            
            // calculateGrossFromPeriod(3) sums periods 0, 1, 2, and 3
            // Period 1 and 3 don't exist, so they contribute 0
            BigDecimal gross = employee.calculateGrossFromPeriod(3);
            assertThat(gross).isEqualTo(new BigDecimal("9000.00")); // 5000 + 0 + 4000 + 0
        }
    }
    
    @Nested
    @DisplayName("NullEmployee Tests")
    class NullEmployeeTest {
        
        private NullEmployee nullEmployee;
        
        @BeforeEach
        void setUp() {
            nullEmployee = new NullEmployee();
        }
        
        @Test
        @DisplayName("Should return 'Null Employee' as name")
        void shouldReturnNullEmployeeName() {
            assertThat(nullEmployee.getName()).isEqualTo("Null Employee");
        }
        
        @Test
        @DisplayName("Should return zero gross to date")
        void shouldReturnZeroGrossToDate() {
            assertThat(nullEmployee.getGrossToDate()).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Should return Contract.NULL")
        void shouldReturnNullContract() {
            assertThat(nullEmployee.getContract()).isEqualTo(Contract.NULL);
        }
    }
    
    @Nested
    @DisplayName("Contract Tests")
    class ContractTest {
        
        private Contract contract;
        
        @BeforeEach
        void setUp() {
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            contract = new Contract(startDate);
        }
        
        @Test
        @DisplayName("Should store and retrieve payment for period")
        void shouldStoreAndRetrievePaymentForPeriod() {
            BigDecimal amount = new BigDecimal("5000.00");
            contract.addPaymentForPeriod(1, amount);
            
            assertThat(contract.getPaymentForPeriod(1)).isEqualTo(amount);
        }
        
        @Test
        @DisplayName("Should return zero for non-existent period")
        void shouldReturnZeroForNonExistentPeriod() {
            assertThat(contract.getPaymentForPeriod(99)).isEqualTo(BigDecimal.ZERO);
        }
        
        @Test
        @DisplayName("Should check if period exists")
        void shouldCheckIfPeriodExists() {
            contract.addPaymentForPeriod(1, new BigDecimal("5000.00"));
            
            assertThat(contract.hasPeriod(1)).isTrue();
            assertThat(contract.hasPeriod(2)).isFalse();
        }
        
        @Test
        @DisplayName("Should return start date")
        void shouldReturnStartDate() {
            LocalDate expectedDate = LocalDate.of(2024, 1, 1);
            assertThat(contract.getStartDate()).isEqualTo(expectedDate);
        }
        
        @Test
        @DisplayName("Should handle Contract.NULL singleton")
        void shouldHandleContractNullSingleton() {
            Contract nullContract = Contract.NULL;
            
            assertThat(nullContract).isNotNull();
            assertThat(nullContract.getPaymentForPeriod(0)).isEqualTo(BigDecimal.ZERO);
            assertThat(nullContract.hasPeriod(0)).isFalse();
            assertThat(nullContract.getStartDate()).isNull();
        }
        
        @Test
        @DisplayName("Should update payment for existing period")
        void shouldUpdatePaymentForExistingPeriod() {
            contract.addPaymentForPeriod(1, new BigDecimal("5000.00"));
            contract.addPaymentForPeriod(1, new BigDecimal("6000.00"));
            
            assertThat(contract.getPaymentForPeriod(1)).isEqualTo(new BigDecimal("6000.00"));
        }
    }
}