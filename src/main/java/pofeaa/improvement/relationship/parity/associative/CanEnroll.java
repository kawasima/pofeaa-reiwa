package pofeaa.improvement.relationship.parity.associative;

import java.util.function.BiFunction;

public class CanEnroll implements BiFunction<Student, Course, Enrollment> {
    private final EnrollmentMapper enrollmentMapper;

    public CanEnroll(EnrollmentMapper enrollmentMapper) {
        this.enrollmentMapper = enrollmentMapper;
    }

    @Override
    public Enrollment apply(Student student, Course course) {
        if (enrollmentMapper.countEnrollmentsByStudentId(student.id()) >= 50) {
            return null;
        }
        return new Enrollment(student, course);
    }
}
