package dev.patric.commonlib.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultServiceRegistryTest {

    @Test
    void registerFindAndRequireWork() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(String.class, "value");

        assertTrue(registry.find(String.class).isPresent());
        assertEquals("value", registry.require(String.class));
    }

    @Test
    void duplicateRegistrationFailsFast() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(String.class, "one");

        assertThrows(IllegalStateException.class, () -> registry.register(String.class, "two"));
    }

    @Test
    void requireFailsWhenMissing() {
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        assertThrows(IllegalStateException.class, () -> registry.require(Integer.class));
    }
}
