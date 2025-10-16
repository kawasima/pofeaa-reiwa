package pofeaa.improvement.relationship.parity.unidirectional;

import org.jooq.DSLContext;
import pofeaa.combination.domain.model.Identity;

import java.util.List;

import static org.jooq.impl.DSL.table;

public class CourseMapper {
    private final DSLContext ctx;

    public CourseMapper(DSLContext ctx) {
        this.ctx = ctx;
    }

    public List<Course> findByStudentId(Identity studentId) {
        return ctx.select()
                .from(table("courses"))
                .where(table("courses").field("student_id", Long.class).eq(studentId.asLong()))
                .fetch()
                .map(rec -> new Course(
                        Identity.of(rec.get("id", Long.class)),
                        rec.get("name", String.class)
                ));
    }
}
