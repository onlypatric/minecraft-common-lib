package dev.patric.commonlib.api.message;

/**
 * Extensible placeholder resolver.
 */
public interface PlaceholderResolver {

    /**
     * Indicates whether resolver supports a placeholder key.
     *
     * @param placeholderKey placeholder key.
     * @return true when supported.
     */
    boolean supports(String placeholderKey);

    /**
     * Resolves placeholder value.
     *
     * @param placeholderKey placeholder key.
     * @param request message request.
     * @return resolved value.
     */
    String resolve(String placeholderKey, MessageRequest request);
}
