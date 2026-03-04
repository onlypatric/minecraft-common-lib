package dev.patric.commonlib.api.dialog;

import java.util.List;
import java.util.Optional;

/**
 * Registry for reusable dialog templates.
 */
public interface DialogTemplateRegistry {

    /**
     * Registers a template.
     *
     * @param template template.
     */
    void register(DialogTemplate template);

    /**
     * Finds a template by key.
     *
     * @param templateKey template key.
     * @return optional template.
     */
    Optional<DialogTemplate> find(String templateKey);

    /**
     * Returns all templates.
     *
     * @return templates snapshot.
     */
    List<DialogTemplate> all();

    /**
     * Unregisters a template.
     *
     * @param templateKey template key.
     * @return true when removed.
     */
    boolean unregister(String templateKey);
}
