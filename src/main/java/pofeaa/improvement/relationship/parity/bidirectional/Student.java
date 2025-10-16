package pofeaa.improvement.relationship.parity.bidirectional;

import pofeaa.combination.domain.model.Identity;

import java.util.List;

public record Student(Identity id, String name, List<Course> courses) {
    public void enroll(Course course) {
        if (courses.size() > 50) {
            throw new IllegalStateException("Cannot enroll in more than 50 courses");
        }
        courses.add(course);
        course.students().add(this);
    }
}
