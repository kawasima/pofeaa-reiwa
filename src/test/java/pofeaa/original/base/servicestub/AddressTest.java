package pofeaa.original.base.servicestub;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for the global Address class that demonstrates its ability
 * to handle different international address formats.
 */
@DisplayName("Global Address Tests")
class AddressTest {

    @Test
    @DisplayName("Should create US address with convenience method")
    void shouldCreateUsAddressWithConvenienceMethod() {
        // Given/When
        Address address = Address.usAddress(
            "123 Main Street",
            "New York",
            "NY",
            "10001"
        );
        
        // Then
        assertThat(address.getLine1()).isEqualTo("123 Main Street");
        assertThat(address.getCity()).isEqualTo("New York");
        assertThat(address.getStateOrProvince()).isEqualTo("NY");
        assertThat(address.getPostalCode()).isEqualTo("10001");
        assertThat(address.getCountry()).isEqualTo("United States");
        assertThat(address.getCountryCode()).isEqualTo("US");
        assertThat(address.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should format US address correctly")
    void shouldFormatUsAddressCorrectly() {
        // Given
        Address address = Address.usAddress(
            "123 Main Street",
            "New York",
            "NY",
            "10001"
        );
        
        // When
        String formatted = address.format();
        
        // Then
        String expected = "123 Main Street\nNew York, NY 10001";
        assertThat(formatted).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should create UK address with convenience method")
    void shouldCreateUkAddressWithConvenienceMethod() {
        // Given/When
        Address address = Address.ukAddress(
            "10 Downing Street",
            null,
            "London",
            "Westminster",
            "SW1A 2AA"
        );
        
        // Then
        assertThat(address.getLine1()).isEqualTo("10 Downing Street");
        assertThat(address.getLine2()).isNull();
        assertThat(address.getCity()).isEqualTo("London");
        assertThat(address.getStateOrProvince()).isEqualTo("Westminster");
        assertThat(address.getPostalCode()).isEqualTo("SW1A 2AA");
        assertThat(address.getCountry()).isEqualTo("United Kingdom");
        assertThat(address.getCountryCode()).isEqualTo("GB");
    }

    @Test
    @DisplayName("Should format UK address correctly")
    void shouldFormatUkAddressCorrectly() {
        // Given
        Address address = Address.ukAddress(
            "10 Downing Street",
            "Cabinet Office",
            "London",
            "Westminster",
            "SW1A 2AA"
        );
        
        // When
        String formatted = address.format();
        
        // Then
        String expected = "10 Downing Street\nCabinet Office\nLondon\nWestminster\nSW1A 2AA";
        assertThat(formatted).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should create Japanese address with convenience method")
    void shouldCreateJapaneseAddressWithConvenienceMethod() {
        // Given/When
        Address address = Address.japaneseAddress(
            "100-0001",
            "東京都",
            "千代田区",
            "千代田1-1"
        );
        
        // Then
        assertThat(address.getPostalCode()).isEqualTo("100-0001");
        assertThat(address.getStateOrProvince()).isEqualTo("東京都");
        assertThat(address.getCity()).isEqualTo("千代田区");
        assertThat(address.getLine1()).isEqualTo("千代田1-1");
        assertThat(address.getCountry()).isEqualTo("Japan");
        assertThat(address.getCountryCode()).isEqualTo("JP");
    }

    @Test
    @DisplayName("Should format Japanese address correctly")
    void shouldFormatJapaneseAddressCorrectly() {
        // Given
        Address address = Address.japaneseAddress(
            "100-0001",
            "東京都",
            "千代田区",
            "千代田1-1"
        );
        
        // When
        String formatted = address.format();
        
        // Then
        String expected = "〒100-0001\n東京都 千代田区 千代田1-1";
        assertThat(formatted).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should create address with builder pattern")
    void shouldCreateAddressWithBuilderPattern() {
        // Given/When
        Address address = Address.builder()
            .line1("Via Roma 123")
            .city("Milano")
            .stateOrProvince("MI")
            .postalCode("20121")
            .country("Italy")
            .countryCode("IT")
            .build();
            
        // Then
        assertThat(address.getLine1()).isEqualTo("Via Roma 123");
        assertThat(address.getCity()).isEqualTo("Milano");
        assertThat(address.getCountry()).isEqualTo("Italy");
        assertThat(address.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should format generic address for unknown country")
    void shouldFormatGenericAddressForUnknownCountry() {
        // Given
        Address address = Address.builder()
            .line1("Rua Augusta 123")
            .line2("Apto 45")
            .city("São Paulo")
            .stateOrProvince("SP")
            .postalCode("01305-100")
            .country("Brazil")
            .countryCode("BR")
            .build();
            
        // When
        String formatted = address.format();
        
        // Then
        String expected = "Rua Augusta 123\nApto 45\nSão Paulo\nSP\n01305-100\nBrazil";
        assertThat(formatted).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should validate address correctly")
    void shouldValidateAddressCorrectly() {
        // Invalid - no location or country info
        assertThatThrownBy(() -> Address.builder().build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Address must have at least some location information and country");
            
        // Valid - has line1 and country
        Address valid1 = Address.builder()
            .line1("Some Street")
            .country("Some Country")
            .build();
        assertThat(valid1.isValid()).isTrue();
        
        // Valid - has city and country code
        Address valid2 = Address.builder()
            .city("Some City")
            .countryCode("XX")
            .build();
        assertThat(valid2.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should check domestic address correctly")
    void shouldCheckDomesticAddressCorrectly() {
        // Given
        Address usAddress = Address.usAddress("123 Main St", "New York", "NY", "10001");
        Address ukAddress = Address.ukAddress("10 Downing St", null, "London", null, "SW1A 2AA");
        
        // Then
        assertThat(usAddress.isDomestic("US")).isTrue();
        assertThat(usAddress.isDomestic("GB")).isFalse();
        assertThat(ukAddress.isDomestic("GB")).isTrue();
        assertThat(ukAddress.isDomestic("US")).isFalse();
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        Address address1 = Address.usAddress("123 Main St", "New York", "NY", "10001");
        Address address2 = Address.usAddress("123 Main St", "New York", "NY", "10001");
        Address address3 = Address.usAddress("456 Oak Ave", "New York", "NY", "10001");
        
        // Then
        assertThat(address1).isEqualTo(address2);
        assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
        assertThat(address1).isNotEqualTo(address3);
        assertThat(address1.hashCode()).isNotEqualTo(address3.hashCode());
    }

    @Test
    @DisplayName("Should handle international address formatting")
    void shouldHandleInternationalAddressFormatting() {
        // Given - US address viewed from UK
        Address address = Address.builder()
            .line1("123 Main Street")
            .city("New York")
            .stateOrProvince("NY")
            .postalCode("10001")
            .country("United States")
            .countryCode("US")
            .build();
            
        // When formatting for international mail
        String formatted = address.format();
        
        // Then - Should include country when not domestic
        assertThat(formatted).isEqualTo("123 Main Street\nNew York, NY 10001");
        
        // But if we change the country to something else, it should show
        Address intlAddress = Address.builder()
            .line1("123 Main Street")
            .city("New York")
            .stateOrProvince("NY")
            .postalCode("10001")
            .country("Canada")  // Different country
            .countryCode("CA")
            .build();
            
        String intlFormatted = intlAddress.format();
        assertThat(intlFormatted).contains("Canada");
    }
}