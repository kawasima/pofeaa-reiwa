package pofeaa.improvement.rr.dependent;

import pofeaa.combination.domain.model.Identity;

import java.util.List;

public record Restaurant(
        Identity id,
        String name,
        List<Table> tables
){
}
