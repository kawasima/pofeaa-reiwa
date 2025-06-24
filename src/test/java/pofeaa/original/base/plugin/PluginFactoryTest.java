package pofeaa.original.base.plugin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.ServiceLoader;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test for PluginFactory to verify the ServiceLoader-based Plugin Factory pattern implementation.
 * 
 * Note: These tests use Mockito to mock ServiceLoader behavior for testing purposes.
 */
@DisplayName("Plugin Factory Tests")
class PluginFactoryTest {

    @Test
    @DisplayName("Should load and instantiate plugin using ServiceLoader")
    void shouldLoadAndInstantiatePluginUsingServiceLoader() {
        // Given - Mock ServiceLoader with a service implementation
        ServiceLoader<TestPlugin> mockServiceLoader = mock(ServiceLoader.class);
        TestPluginImpl testPluginImpl = new TestPluginImpl();
        Iterator<TestPlugin> iterator = List.<TestPlugin>of(testPluginImpl).iterator();
        
        when(mockServiceLoader.iterator()).thenReturn(iterator);
        
        try (MockedStatic<ServiceLoader> mockedServiceLoader = Mockito.mockStatic(ServiceLoader.class)) {
            mockedServiceLoader.when(() -> ServiceLoader.load(TestPlugin.class))
                .thenReturn(mockServiceLoader);
            
            // When
            TestPlugin plugin = PluginFactory.getPlugin(TestPlugin.class);
            
            // Then
            assertNotNull(plugin);
            assertInstanceOf(TestPlugin.class, plugin);
            assertInstanceOf(TestPluginImpl.class, plugin);
            assertEquals("TestPluginImpl", plugin.getName());
        }
    }

    @Test
    @DisplayName("Should throw exception when no service implementation found")
    void shouldThrowExceptionWhenNoServiceImplementationFound() {
        // Given - Mock ServiceLoader with no services
        ServiceLoader<TestPlugin> mockServiceLoader = mock(ServiceLoader.class);
        Iterator<TestPlugin> emptyIterator = Collections.emptyIterator();
        
        when(mockServiceLoader.iterator()).thenReturn(emptyIterator);
        
        try (MockedStatic<ServiceLoader> mockedServiceLoader = Mockito.mockStatic(ServiceLoader.class)) {
            mockedServiceLoader.when(() -> ServiceLoader.load(TestPlugin.class))
                .thenReturn(mockServiceLoader);
            
            // When/Then
            RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> PluginFactory.getPlugin(TestPlugin.class));
            assertEquals("No implementation found for plugin: pofeaa.original.base.plugin.PluginFactoryTest$TestPlugin", 
                exception.getMessage());
        }
    }

    @Test
    @DisplayName("Should return first service when multiple implementations available")
    void shouldReturnFirstServiceWhenMultipleImplementationsAvailable() {
        // Given - Mock ServiceLoader with multiple services
        ServiceLoader<TestPlugin> mockServiceLoader = mock(ServiceLoader.class);
        TestPluginImpl firstImpl = new TestPluginImpl();
        TestPluginImpl secondImpl = new TestPluginImpl();
        Iterator<TestPlugin> iterator = List.<TestPlugin>of(firstImpl, secondImpl).iterator();
        
        when(mockServiceLoader.iterator()).thenReturn(iterator);
        
        try (MockedStatic<ServiceLoader> mockedServiceLoader = Mockito.mockStatic(ServiceLoader.class)) {
            mockedServiceLoader.when(() -> ServiceLoader.load(TestPlugin.class))
                .thenReturn(mockServiceLoader);
            
            // When
            TestPlugin plugin = PluginFactory.getPlugin(TestPlugin.class);
            
            // Then
            assertNotNull(plugin);
            assertSame(firstImpl, plugin);
        }
    }

    @Test
    @DisplayName("Should work with IdGenerator plugin")
    void shouldWorkWithIdGeneratorPlugin() {
        // Given - Mock ServiceLoader with IdGenerator implementation
        ServiceLoader<IdGenerator> mockServiceLoader = mock(ServiceLoader.class);
        MockIdGenerator mockIdGenerator = new MockIdGenerator();
        Iterator<IdGenerator> iterator = List.<IdGenerator>of(mockIdGenerator).iterator();
        
        when(mockServiceLoader.iterator()).thenReturn(iterator);
        
        try (MockedStatic<ServiceLoader> mockedServiceLoader = Mockito.mockStatic(ServiceLoader.class)) {
            mockedServiceLoader.when(() -> ServiceLoader.load(IdGenerator.class))
                .thenReturn(mockServiceLoader);
            
            // When
            IdGenerator plugin = PluginFactory.getPlugin(IdGenerator.class);
            
            // Then
            assertNotNull(plugin);
            assertInstanceOf(IdGenerator.class, plugin);
            assertInstanceOf(MockIdGenerator.class, plugin);
            assertEquals(42L, plugin.nextId());
        }
    }

    @Test
    @DisplayName("Should handle different plugin types independently")
    void shouldHandleDifferentPluginTypesIndependently() {
        // Given - Mock ServiceLoaders for different plugin types
        ServiceLoader<TestPlugin> testPluginServiceLoader = mock(ServiceLoader.class);
        ServiceLoader<IdGenerator> idGeneratorServiceLoader = mock(ServiceLoader.class);
        
        TestPluginImpl testPluginImpl = new TestPluginImpl();
        MockIdGenerator mockIdGenerator = new MockIdGenerator();
        
        when(testPluginServiceLoader.iterator()).thenReturn(List.<TestPlugin>of(testPluginImpl).iterator());
        when(idGeneratorServiceLoader.iterator()).thenReturn(List.<IdGenerator>of(mockIdGenerator).iterator());
        
        try (MockedStatic<ServiceLoader> mockedServiceLoader = Mockito.mockStatic(ServiceLoader.class)) {
            mockedServiceLoader.when(() -> ServiceLoader.load(TestPlugin.class))
                .thenReturn(testPluginServiceLoader);
            mockedServiceLoader.when(() -> ServiceLoader.load(IdGenerator.class))
                .thenReturn(idGeneratorServiceLoader);
            
            // When
            TestPlugin testPlugin = PluginFactory.getPlugin(TestPlugin.class);
            IdGenerator idGenerator = PluginFactory.getPlugin(IdGenerator.class);
            
            // Then
            assertInstanceOf(TestPluginImpl.class, testPlugin);
            assertInstanceOf(MockIdGenerator.class, idGenerator);
            assertEquals("TestPluginImpl", testPlugin.getName());
            assertEquals(42L, idGenerator.nextId());
        }
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
            return "TestPluginImpl";
        }
    }

    /**
     * Mock IdGenerator for testing.
     */
    public static class MockIdGenerator implements IdGenerator {
        @Override
        public Long nextId() {
            return 42L;
        }
    }
}