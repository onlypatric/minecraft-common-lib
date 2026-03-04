package dev.patric.commonlib.api.gui;

import java.util.UUID;

/**
 * Portable GUI interaction contract used by the v2 interaction pipeline.
 */
public sealed interface GuiInteractionEvent permits
        SlotClickEvent,
        InventoryDragEventPortable,
        HotbarSwapEventPortable,
        DropEventPortable,
        DoubleClickEventPortable,
        CloseEventPortable,
        DisconnectEventPortable,
        TimeoutEventPortable {

    /**
     * Target session id.
     *
     * @return session id.
     */
    UUID sessionId();

    /**
     * Expected current session revision.
     *
     * @return expected revision.
     */
    long expectedRevision();
}
