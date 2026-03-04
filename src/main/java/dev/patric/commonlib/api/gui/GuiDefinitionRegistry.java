package dev.patric.commonlib.api.gui;

import java.util.List;
import java.util.Optional;

/**
 * Registry for reusable GUI definitions addressable by key.
 */
public interface GuiDefinitionRegistry {

    /**
     * Registers one definition.
     *
     * @param definition definition to register.
     */
    void register(GuiDefinition definition);

    /**
     * Finds one definition by key.
     *
     * @param key definition key.
     * @return optional definition.
     */
    Optional<GuiDefinition> find(String key);

    /**
     * Unregisters one definition by key.
     *
     * @param key definition key.
     * @return true if one definition was removed.
     */
    boolean unregister(String key);

    /**
     * Lists all currently registered definitions.
     *
     * @return immutable list.
     */
    List<GuiDefinition> all();
}
