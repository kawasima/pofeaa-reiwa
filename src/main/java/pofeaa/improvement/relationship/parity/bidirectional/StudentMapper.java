package pofeaa.improvement.relationship.parity.bidirectional;

import org.jooq.DSLContext;
import org.jooq.Record;
import pofeaa.combination.domain.model.Identity;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class StudentMapper {
    private final DSLContext ctx;

    public StudentMapper(DSLContext ctx) {
        this.ctx = ctx;
    }

    public Optional<Student> findById(Identity id) {
        Record studentRec = ctx.select()
                .from(table("students"))
                .where(field("id").eq(id.asLong()))
                .fetchOne();
        if (studentRec == null) {
            return Optional.empty();
        }
        List<Course> courses = ctx.select()
                .from(table("courses"))
                .join(table("enrollments"))
                .on(table("courses").field("id", Long.class).eq(table("enrollments").field("course_id", Long.class)))
                .where(field("student_id").eq(id.asLong()))
                .fetch()
                .map(rec -> new Course(
                        Identity.of(rec.get("id", Long.class)),
                        rec.get("name", String.class),
                        null // Students are not loaded here, assuming unidirectional relationship
                ));
        return Optional.of(new Student(
                Identity.of(studentRec.get("id", Long.class)),
                studentRec.get("name", String.class),
                courses
        ));
    }
}
