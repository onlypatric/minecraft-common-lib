package dev.patric.commonlib.api.hud;

/**
 * Reasons for closing HUD resources bound to an audience.
 */
public enum HudAudienceCloseReason {
    QUIT,
    WORLD_CHANGE,
    PLUGIN_DISABLE,
    MANUAL,
    REPLACED,
    ERROR
}
