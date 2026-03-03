package dev.patric.commonlib;

/**
 * Entry utility class for minecraft-common-lib.
 */
public final class CommonLib {

    private CommonLib() {
        throw new UnsupportedOperationException("CommonLib is a utility class and cannot be instantiated.");
    }

    /**
     * Returns the current library version placeholder.
     *
     * @return semantic version string.
     */
    public static String version() {
        return "0.1.0-SNAPSHOT";
    }
}
