package pofeaa.improvement.relationship.parity.associative;

import org.jooq.DSLContext;
import pofeaa.combination.domain.model.Identity;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class EnrollmentMapper {
    private final DSLContext ctx;

    public EnrollmentMapper(DSLContext ctx) {
        this.ctx = ctx;
    }

    public Integer countEnrollmentsByStudentId(Identity studentId) {
        return ctx.selectCount()
                .from(table("enrollments"))
                .where(field("student_id").eq(studentId))
                .fetchOne(0, Integer.class);
    }
}
