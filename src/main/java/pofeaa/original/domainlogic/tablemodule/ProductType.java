package pofeaa.original.domainlogic.tablemodule;

import java.util.Arrays;
import java.util.Objects;

public enum ProductType {
    WORD_PROCESSOR("W"),
    SPREADSHEET("S"),
    DATABASE("D");

    private final String code;

    ProductType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ProductType fromCode(String code) {
        return Arrays.stream(ProductType.values())
                .filter(pt -> Objects.equals(code, pt.code))
                .findAny()
                .orElseThrow();
    }
}
