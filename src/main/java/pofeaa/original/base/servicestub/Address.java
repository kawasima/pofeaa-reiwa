package pofeaa.original.base.servicestub;

import java.util.Objects;

/**
 * Represents a global address that can handle different address formats
 * from various countries.
 * 
 * This class follows the design principles from Martin Fowler's PoEAA,
 * providing a flexible structure that can accommodate various international
 * address formats while maintaining a consistent interface.
 */
public class Address {
    
    private final String line1;
    private final String line2;
    private final String line3;
    private final String city;
    private final String stateOrProvince;
    private final String postalCode;
    private final String country;
    private final String countryCode;
    
    /**
     * Private constructor to enforce use of builder pattern.
     */
    private Address(Builder builder) {
        this.line1 = builder.line1;
        this.line2 = builder.line2;
        this.line3 = builder.line3;
        this.city = builder.city;
        this.stateOrProvince = builder.stateOrProvince;
        this.postalCode = builder.postalCode;
        this.country = builder.country;
        this.countryCode = builder.countryCode;
    }
    
    /**
     * Creates a new Address builder.
     * 
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates an Address for US format.
     * 
     * @param streetAddress The street address (line 1)
     * @param city The city
     * @param state The state abbreviation
     * @param zipCode The ZIP code
     * @return A new Address instance
     */
    public static Address usAddress(String streetAddress, String city, String state, String zipCode) {
        return builder()
                .line1(streetAddress)
                .city(city)
                .stateOrProvince(state)
                .postalCode(zipCode)
                .country("United States")
                .countryCode("US")
                .build();
    }
    
    /**
     * Creates an Address for UK format.
     * 
     * @param line1 First line of address
     * @param line2 Second line of address (optional)
     * @param townOrCity Town or city
     * @param county County (optional)
     * @param postcode UK postcode
     * @return A new Address instance
     */
    public static Address ukAddress(String line1, String line2, String townOrCity, 
                                   String county, String postcode) {
        Builder builder = builder()
                .line1(line1)
                .city(townOrCity)
                .postalCode(postcode)
                .country("United Kingdom")
                .countryCode("GB");
        
        if (line2 != null && !line2.trim().isEmpty()) {
            builder.line2(line2);
        }
        if (county != null && !county.trim().isEmpty()) {
            builder.stateOrProvince(county);
        }
        
        return builder.build();
    }
    
    /**
     * Creates an Address for Japanese format.
     * 
     * @param postalCode Japanese postal code (e.g., "123-4567")
     * @param prefecture Prefecture
     * @param city City, ward, town, or village
     * @param addressLine Remaining address details
     * @return A new Address instance
     */
    public static Address japaneseAddress(String postalCode, String prefecture, 
                                        String city, String addressLine) {
        return builder()
                .postalCode(postalCode)
                .stateOrProvince(prefecture)
                .city(city)
                .line1(addressLine)
                .country("Japan")
                .countryCode("JP")
                .build();
    }
    
    // Getters
    
    public String getLine1() {
        return line1;
    }
    
    public String getLine2() {
        return line2;
    }
    
    public String getLine3() {
        return line3;
    }
    
    public String getCity() {
        return city;
    }
    
    public String getStateOrProvince() {
        return stateOrProvince;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    /**
     * Returns a formatted string representation of the address
     * based on the country format.
     * 
     * @return Formatted address string
     */
    public String format() {
        if ("US".equals(countryCode)) {
            return formatUsAddress();
        } else if ("GB".equals(countryCode)) {
            return formatUkAddress();
        } else if ("JP".equals(countryCode)) {
            return formatJapaneseAddress();
        } else {
            return formatGenericAddress();
        }
    }
    
    private String formatUsAddress() {
        StringBuilder sb = new StringBuilder();
        appendLine(sb, line1);
        appendLine(sb, line2);
        appendLine(sb, line3);
        
        if (city != null && stateOrProvince != null && postalCode != null) {
            sb.append(city).append(", ").append(stateOrProvince).append(" ").append(postalCode);
            sb.append("\n");
        }
        
        if (!"United States".equals(country) && country != null) {
            sb.append(country);
        }
        
        return sb.toString().trim();
    }
    
    private String formatUkAddress() {
        StringBuilder sb = new StringBuilder();
        appendLine(sb, line1);
        appendLine(sb, line2);
        appendLine(sb, line3);
        appendLine(sb, city);
        appendLine(sb, stateOrProvince); // County
        appendLine(sb, postalCode);
        
        if (!"United Kingdom".equals(country) && country != null) {
            sb.append(country);
        }
        
        return sb.toString().trim();
    }
    
    private String formatJapaneseAddress() {
        StringBuilder sb = new StringBuilder();
        
        // Japanese format: postal code first, then large to small
        if (postalCode != null) {
            sb.append("〒").append(postalCode).append("\n");
        }
        
        if (country != null && !"Japan".equals(country)) {
            sb.append(country).append("\n");
        }
        
        appendWithSpace(sb, stateOrProvince); // Prefecture
        appendWithSpace(sb, city);
        appendWithSpace(sb, line1);
        
        if (line2 != null) {
            sb.append("\n");
            sb.append(line2);
        }
        
        return sb.toString().trim();
    }
    
    private String formatGenericAddress() {
        StringBuilder sb = new StringBuilder();
        appendLine(sb, line1);
        appendLine(sb, line2);
        appendLine(sb, line3);
        appendLine(sb, city);
        appendLine(sb, stateOrProvince);
        appendLine(sb, postalCode);
        appendLine(sb, country);
        
        return sb.toString().trim();
    }
    
    private void appendLine(StringBuilder sb, String value) {
        if (value != null && !value.trim().isEmpty()) {
            sb.append(value).append("\n");
        }
    }
    
    private void appendWithSpace(StringBuilder sb, String value) {
        if (value != null && !value.trim().isEmpty()) {
            sb.append(value).append(" ");
        }
    }
    
    /**
     * Validates if the address has the minimum required fields.
     * 
     * @return true if the address is valid, false otherwise
     */
    public boolean isValid() {
        // At minimum, an address should have some location info and country
        boolean hasLocationInfo = (line1 != null && !line1.trim().isEmpty()) ||
                                (city != null && !city.trim().isEmpty());
        boolean hasCountryInfo = (country != null && !country.trim().isEmpty()) ||
                               (countryCode != null && !countryCode.trim().isEmpty());
        
        return hasLocationInfo && hasCountryInfo;
    }
    
    /**
     * Checks if this is a domestic address based on a given country code.
     * 
     * @param compareCountryCode The country code to compare against
     * @return true if this address is in the specified country
     */
    public boolean isDomestic(String compareCountryCode) {
        return compareCountryCode != null && compareCountryCode.equals(this.countryCode);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Address address = (Address) o;
        return Objects.equals(line1, address.line1) &&
               Objects.equals(line2, address.line2) &&
               Objects.equals(line3, address.line3) &&
               Objects.equals(city, address.city) &&
               Objects.equals(stateOrProvince, address.stateOrProvince) &&
               Objects.equals(postalCode, address.postalCode) &&
               Objects.equals(country, address.country) &&
               Objects.equals(countryCode, address.countryCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(line1, line2, line3, city, stateOrProvince, 
                          postalCode, country, countryCode);
    }
    
    @Override
    public String toString() {
        return "Address{" +
               "line1='" + line1 + '\'' +
               ", line2='" + line2 + '\'' +
               ", line3='" + line3 + '\'' +
               ", city='" + city + '\'' +
               ", stateOrProvince='" + stateOrProvince + '\'' +
               ", postalCode='" + postalCode + '\'' +
               ", country='" + country + '\'' +
               ", countryCode='" + countryCode + '\'' +
               '}';
    }
    
    /**
     * Builder class for creating Address instances.
     */
    public static class Builder {
        private String line1;
        private String line2;
        private String line3;
        private String city;
        private String stateOrProvince;
        private String postalCode;
        private String country;
        private String countryCode;
        
        public Builder line1(String line1) {
            this.line1 = line1;
            return this;
        }
        
        public Builder line2(String line2) {
            this.line2 = line2;
            return this;
        }
        
        public Builder line3(String line3) {
            this.line3 = line3;
            return this;
        }
        
        public Builder city(String city) {
            this.city = city;
            return this;
        }
        
        public Builder stateOrProvince(String stateOrProvince) {
            this.stateOrProvince = stateOrProvince;
            return this;
        }
        
        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }
        
        public Builder country(String country) {
            this.country = country;
            return this;
        }
        
        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }
        
        /**
         * Builds the Address instance.
         * 
         * @return A new Address instance
         * @throws IllegalArgumentException if the address is invalid
         */
        public Address build() {
            Address address = new Address(this);
            if (!address.isValid()) {
                throw new IllegalArgumentException(
                    "Address must have at least some location information and country"
                );
            }
            return address;
        }
    }
}
