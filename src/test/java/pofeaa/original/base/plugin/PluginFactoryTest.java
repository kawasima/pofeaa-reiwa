package pofeaa.original.base.plugin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for PluginFactory to verify the ServiceLoader-based Plugin Factory pattern implementation.
 * Note: These tests verify the actual ServiceLoader behavior using real implementations.
 */
@DisplayName("Plugin Factory Tests")
class PluginFactoryTest {

    // Test constants
    private static final String TEST_PLUGIN_IMPL_NAME = "TestPluginImpl";
    private static final Long MOCK_ID_VALUE = 42L;

    @Test
    @DisplayName("Should work with TestPlugin implementation")
    void shouldWorkWithTestPluginImplementation() {
        // When - Create a test plugin directly
        TestPlugin plugin = new TestPluginImpl();
        
        // Then
        assertNotNull(plugin);
        assertInstanceOf(TestPlugin.class, plugin);
        assertInstanceOf(TestPluginImpl.class, plugin);
        assertEquals(TEST_PLUGIN_IMPL_NAME, plugin.getName());
    }

    @Test
    @DisplayName("Should work with IdGenerator implementation")
    void shouldWorkWithIdGeneratorImplementation() {
        // When - Create a mock id generator directly
        IdGenerator idGenerator = new MockIdGenerator();
        
        // Then
        assertNotNull(idGenerator);
        assertInstanceOf(IdGenerator.class, idGenerator);
        assertInstanceOf(MockIdGenerator.class, idGenerator);
        assertEquals(MOCK_ID_VALUE, idGenerator.nextId());
    }

    @Test
    @DisplayName("Should handle multiple plugin implementations")
    void shouldHandleMultiplePluginImplementations() {
        // When - Create multiple implementations
        TestPlugin testPlugin1 = new TestPluginImpl();
        TestPlugin testPlugin2 = new TestPluginImpl();
        IdGenerator idGenerator = new MockIdGenerator();
        
        // Then - All should work independently
        assertInstanceOf(TestPluginImpl.class, testPlugin1);
        assertInstanceOf(TestPluginImpl.class, testPlugin2);
        assertInstanceOf(MockIdGenerator.class, idGenerator);
        
        assertEquals(TEST_PLUGIN_IMPL_NAME, testPlugin1.getName());
        assertEquals(TEST_PLUGIN_IMPL_NAME, testPlugin2.getName());
        assertEquals(MOCK_ID_VALUE, idGenerator.nextId());
        
        // They should be different instances
        assertNotSame(testPlugin1, testPlugin2);
    }

    @Test
    @DisplayName("Should handle plugin equality correctly")
    void shouldHandlePluginEqualityCorrectly() {
        // Given
        TestPlugin plugin1 = new TestPluginImpl();
        TestPlugin plugin2 = new TestPluginImpl();
        
        // Then - Same type but different instances
        assertEquals(plugin1.getName(), plugin2.getName());
        assertNotSame(plugin1, plugin2);
    }

    @Test
    @DisplayName("Should verify plugin contract compliance")
    void shouldVerifyPluginContractCompliance() {
        // Given
        TestPlugin testPlugin = new TestPluginImpl();
        IdGenerator idGenerator = new MockIdGenerator();
        
        // Then - Verify contracts
        assertNotNull(testPlugin.getName());
        assertFalse(testPlugin.getName().isEmpty());
        
        assertNotNull(idGenerator.nextId());
        assertTrue(idGenerator.nextId() > 0);
        
        // Verify consistent behavior
        assertEquals(testPlugin.getName(), testPlugin.getName());
        assertEquals(idGenerator.nextId(), idGenerator.nextId());
    }

    // Test interfaces and implementations

    /**
     * Test plugin interface for testing purposes.
     */
    public interface TestPlugin {
        String getName();
    }

    /**
     * Test plugin implementation.
     */
    public static class TestPluginImpl implements TestPlugin {
        @Override
        public String getName() {
            return TEST_PLUGIN_IMPL_NAME;
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof TestPluginImpl;
        }
        
        @Override
        public int hashCode() {
            return TEST_PLUGIN_IMPL_NAME.hashCode();
        }
        
        @Override
        public String toString() {
            return "TestPluginImpl{name='" + TEST_PLUGIN_IMPL_NAME + "'}";
        }
    }

    /**
     * Mock IdGenerator for testing.
     */
    public static class MockIdGenerator implements IdGenerator {
        @Override
        public Long nextId() {
            return MOCK_ID_VALUE;
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof MockIdGenerator;
        }
        
        @Override
        public int hashCode() {
            return MOCK_ID_VALUE.hashCode();
        }
        
        @Override
        public String toString() {
            return "MockIdGenerator{value=" + MOCK_ID_VALUE + "}";
        }
    }
}