package dev.patric.commonlib.api;

import dev.patric.commonlib.services.DefaultServiceRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceRegistryContractTest {

    @Test
    void serviceRegistryContractIsRespected() {
        ServiceRegistry registry = new DefaultServiceRegistry();

        registry.register(String.class, "value");
        assertEquals("value", registry.require(String.class));

        assertThrows(IllegalStateException.class, () -> registry.register(String.class, "other"));
        assertThrows(IllegalStateException.class, () -> registry.require(Integer.class));
    }
}
