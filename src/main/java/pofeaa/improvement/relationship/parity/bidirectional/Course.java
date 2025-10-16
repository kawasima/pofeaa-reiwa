package pofeaa.improvement.relationship.parity.bidirectional;

import pofeaa.combination.domain.model.Identity;

import java.util.List;

public record Course(Identity id, String name, List<Student> students) {
}
