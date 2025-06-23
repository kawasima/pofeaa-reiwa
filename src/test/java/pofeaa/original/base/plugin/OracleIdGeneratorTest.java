package pofeaa.original.base.plugin;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for OracleIdGenerator to verify it properly generates IDs from Oracle sequences.
 * 
 * Note: This test uses H2 database to simulate Oracle sequence behavior.
 */
@DisplayName("Oracle ID Generator Tests")
class OracleIdGeneratorTest {

    private DSLContext ctx;

    @BeforeEach
    void setUp() {
        // Setup in-memory H2 database with Oracle compatibility mode
        var ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:oracleidtest" + System.nanoTime() + ";MODE=Oracle;DB_CLOSE_DELAY=-1");
        
        ctx = DSL.using(ds, SQLDialect.H2);
        
        // Create a test sequence
        ctx.execute("CREATE SEQUENCE test_seq START WITH 1 INCREMENT BY 1");
    }

    @Test
    @DisplayName("Should generate sequential IDs from Oracle sequence")
    void shouldGenerateSequentialIdsFromOracleSequence() {
        // Given
        OracleIdGenerator generator = new OracleIdGenerator(ctx, "test_seq");
        
        // When
        Long id1 = generator.nextId();
        Long id2 = generator.nextId();
        Long id3 = generator.nextId();
        
        // Then
        assertThat(id1).isEqualTo(1L);
        assertThat(id2).isEqualTo(2L);
        assertThat(id3).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should generate unique IDs across multiple generator instances")
    void shouldGenerateUniqueIdsAcrossMultipleGeneratorInstances() {
        // Given
        OracleIdGenerator generator1 = new OracleIdGenerator(ctx, "test_seq");
        OracleIdGenerator generator2 = new OracleIdGenerator(ctx, "test_seq");
        
        // When
        Long id1 = generator1.nextId();
        Long id2 = generator2.nextId();
        Long id3 = generator1.nextId();
        
        // Then
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id2).isNotEqualTo(id3);
        assertThat(id1).isNotEqualTo(id3);
        
        // Should be sequential (1, 2, 3) since they're using the same sequence
        assertThat(id2).isEqualTo(id1 + 1);
        assertThat(id3).isEqualTo(id2 + 1);
    }

    @Test
    @DisplayName("Should work with different sequence names")
    void shouldWorkWithDifferentSequenceNames() {
        // Given
        ctx.execute("CREATE SEQUENCE another_seq START WITH 100 INCREMENT BY 5");
        
        OracleIdGenerator generator1 = new OracleIdGenerator(ctx, "test_seq");
        OracleIdGenerator generator2 = new OracleIdGenerator(ctx, "another_seq");
        
        // When
        Long id1 = generator1.nextId(); // Should be from test_seq
        Long id2 = generator2.nextId(); // Should be from another_seq
        
        // Then
        assertThat(id1).isEqualTo(1L); // test_seq starts at 1
        assertThat(id2).isEqualTo(100L); // another_seq starts at 100
    }

    @Test
    @DisplayName("Should throw exception when sequence does not exist")
    void shouldThrowExceptionWhenSequenceDoesNotExist() {
        // Given
        OracleIdGenerator generator = new OracleIdGenerator(ctx, "nonexistent_seq");
        
        // When/Then
        assertThatThrownBy(generator::nextId)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to generate ID from Oracle sequence: nonexistent_seq");
    }

    @Test
    @DisplayName("Should handle sequences with custom increment")
    void shouldHandleSequencesWithCustomIncrement() {
        // Given
        ctx.execute("CREATE SEQUENCE custom_seq START WITH 10 INCREMENT BY 10");
        OracleIdGenerator generator = new OracleIdGenerator(ctx, "custom_seq");
        
        // When
        Long id1 = generator.nextId();
        Long id2 = generator.nextId();
        Long id3 = generator.nextId();
        
        // Then
        assertThat(id1).isEqualTo(10L);
        assertThat(id2).isEqualTo(20L);
        assertThat(id3).isEqualTo(30L);
    }

    @Test
    @DisplayName("Should be thread-safe for concurrent ID generation")
    void shouldBeThreadSafeForConcurrentIdGeneration() throws InterruptedException {
        // Given
        OracleIdGenerator generator = new OracleIdGenerator(ctx, "test_seq");
        final int threadCount = 10;
        final int idsPerThread = 5;
        Long[] generatedIds = new Long[threadCount * idsPerThread];
        Thread[] threads = new Thread[threadCount];
        
        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < idsPerThread; j++) {
                    generatedIds[threadIndex * idsPerThread + j] = generator.nextId();
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then - All IDs should be unique
        assertThat(generatedIds).doesNotContainNull();
        assertThat(generatedIds).doesNotHaveDuplicates();
        assertThat(generatedIds).hasSize(threadCount * idsPerThread);
    }
}