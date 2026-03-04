package dev.patric.commonlib.api.gui;

import java.util.Set;

/**
 * Optional metadata published by GUI adapters.
 *
 * @param backendId backend identifier.
 * @param backendVersion backend version.
 * @param features supported feature set.
 */
public record GuiCapabilityDetails(
        String backendId,
        String backendVersion,
        Set<GuiPortFeature> features
) {
}
