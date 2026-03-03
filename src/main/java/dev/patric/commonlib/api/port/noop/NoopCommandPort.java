package dev.patric.commonlib.api.port.noop;

import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.port.CommandPort;

/**
 * No-op command port.
 */
public final class NoopCommandPort implements CommandPort {

    @Override
    public void register(CommandModel model) {
        // no-op
    }

    @Override
    public void unregister(String root) {
        // no-op
    }

    @Override
    public boolean supportsSuggestions() {
        return false;
    }
}
