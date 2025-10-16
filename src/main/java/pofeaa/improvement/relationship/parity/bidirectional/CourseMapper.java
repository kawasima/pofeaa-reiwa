package pofeaa.improvement.relationship.parity.bidirectional;

import org.jooq.DSLContext;
import org.jooq.Record;
import pofeaa.combination.domain.model.Identity;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class CourseMapper {
    private final DSLContext ctx;

    public CourseMapper(DSLContext ctx) {
        this.ctx = ctx;
    }

    public Optional<Course> findById(Identity courseId) {
        Record courseRec = ctx.select()
                .from(table("courses"))
                .where(field("id").eq(courseId.asLong()))
                .fetchOne();
        if (courseRec == null) {
            return Optional.empty();
        }

        List<Student> students = ctx.select()
                .from(table("students"))
                .join(table("enrollments"))
                .on(table("students").field("id", Long.class).eq(table("enrollments").field("student_id", Long.class)))
                .where(field("course_id").eq(courseId.asLong()))
                .fetch()
                .map(rec -> new Student(
                        Identity.of(rec.get("student_id", Long.class)),
                        rec.get("name", String.class),
                        null
                ));
        return Optional.of(new Course(
                Identity.of(courseRec.get("id", Long.class)),
                courseRec.get("name", String.class),
                students
        ));
    }
    public void update(Course course) {
        ctx.transaction(configuration -> {
            DSLContext txCtx = configuration.dsl();
            
            // Update course name
            txCtx.update(table("courses"))
                .set(field("name"), course.name())
                .where(field("id").eq(course.id().asLong()))
                .execute();
            
            // Get current enrollments
            List<Long> currentStudentIds = txCtx.select(field("student_id", Long.class))
                .from(table("enrollments"))
                .where(field("course_id").eq(course.id().asLong()))
                .fetch()
                .getValues(field("student_id", Long.class));
            
            // Get new student IDs from the course object
            List<Long> newStudentIds = course.students().stream()
                .map(student -> student.id().asLong())
                .toList();
            
            // Find students to remove
            List<Long> studentsToRemove = currentStudentIds.stream()
                .filter(id -> !newStudentIds.contains(id))
                .toList();
            
            // Find students to add
            List<Long> studentsToAdd = newStudentIds.stream()
                .filter(id -> !currentStudentIds.contains(id))
                .toList();
            
            // Delete removed enrollments
            if (!studentsToRemove.isEmpty()) {
                txCtx.deleteFrom(table("enrollments"))
                    .where(field("course_id").eq(course.id().asLong()))
                    .and(field("student_id").in(studentsToRemove))
                    .execute();
            }
            
            // Insert new enrollments
            if (!studentsToAdd.isEmpty()) {
                var insertQuery = txCtx.insertInto(
                    table("enrollments"),
                    field("course_id"),
                    field("student_id")
                );
                
                for (Long studentId : studentsToAdd) {
                    insertQuery = insertQuery.values(course.id().asLong(), studentId);
                }
                
                insertQuery.execute();
            }
        });
    }
}
