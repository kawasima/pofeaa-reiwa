package pofeaa.improvement.relationship.parity.unidirectional;

import pofeaa.combination.domain.model.Identity;

import java.util.List;

public record Student(Identity id, String name, List<Course> courses) {
}
