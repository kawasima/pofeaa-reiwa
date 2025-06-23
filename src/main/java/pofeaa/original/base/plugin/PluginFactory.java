package pofeaa.original.base.plugin;

import java.util.ServiceLoader;

public class PluginFactory {
    /**
     * Gets a plugin instance for the specified plugin interface class using ServiceLoader.
     *
     * The method uses Java's ServiceLoader mechanism to find and instantiate
     * implementations of the plugin interface. Service providers should be
     * declared in META-INF/services files.
     *
     * @param <T> The type of the plugin interface
     * @param pluginClass The interface or abstract class of the plugin
     * @return An instance of the plugin implementation
     * @throws RuntimeException if no plugin implementation is found
     */
    public static <T> T getPlugin(Class<T> pluginClass) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(pluginClass);
        
        for (T service : serviceLoader) {
            return service;
        }
        
        throw new RuntimeException("No implementation found for plugin: " + pluginClass.getName());
    }
}
