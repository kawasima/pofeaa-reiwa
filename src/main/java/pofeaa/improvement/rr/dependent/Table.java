package pofeaa.improvement.rr.dependent;

import pofeaa.combination.domain.model.Identity;

public record Table(
        Identity id,
        Restaurant restaurant,
        int tableNumber,
        int numberOfSeats
) {
}
