package dev.patric.commonlib.adapter.invui;

import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiItemView;
import dev.patric.commonlib.api.gui.GuiPortFeature;
import dev.patric.commonlib.api.gui.SlotDefinition;
import dev.patric.commonlib.api.gui.render.GuiRenderModel;
import dev.patric.commonlib.api.gui.render.GuiRenderPatch;
import dev.patric.commonlib.api.port.GuiPort;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

/**
 * GUI port implementation for the InvUI adapter module.
 *
 * <p>Runtime behavior is intentionally graceful: if backend-specific classes are unavailable,
 * the port still behaves deterministically and never throws unchecked exceptions.</p>
 */
public final class InvUiGuiPort implements GuiPort {

    private final Backend backend;
    private final Map<UUID, SessionView> views = new ConcurrentHashMap<>();

    /**
     * Creates a GUI port backed by the default InvUI runtime backend.
     */
    public InvUiGuiPort() {
        this(new RealInvUiBackend());
    }

    /**
     * Creates a GUI port with injected backend for deterministic tests.
     *
     * @param backend backend strategy.
     */
    InvUiGuiPort(Backend backend) {
        this.backend = Objects.requireNonNull(backend, "backend");
    }

    @Override
    public boolean open(GuiRenderModel renderModel) {
        Objects.requireNonNull(renderModel, "renderModel");

        Player player = Bukkit.getPlayer(renderModel.playerId());
        if (player == null || !player.isOnline()) {
            return false;
        }

        try {
            close(renderModel.sessionId(), GuiCloseReason.REPLACED);

            BackendView view = backend.open(player, renderModel);
            if (view == null) {
                return false;
            }
            views.put(renderModel.sessionId(), new SessionView(renderModel.playerId(), view));
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public boolean render(UUID sessionId, GuiRenderPatch patch) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(patch, "patch");

        SessionView view = views.get(sessionId);
        if (view == null) {
            return false;
        }

        Player player = Bukkit.getPlayer(view.playerId());
        if (player == null || !player.isOnline()) {
            return false;
        }

        try {
            return backend.render(view.view(), patch);
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public boolean close(UUID sessionId, GuiCloseReason reason) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(reason, "reason");

        SessionView view = views.remove(sessionId);
        if (view == null) {
            return true;
        }

        Player player = Bukkit.getPlayer(view.playerId());
        if (player == null || !player.isOnline()) {
            return true;
        }

        try {
            return backend.close(view.view(), reason);
        } catch (Throwable ex) {
            return false;
        } finally {
            views.remove(sessionId);
        }
    }

    @Override
    public boolean supports(GuiPortFeature feature) {
        Objects.requireNonNull(feature, "feature");
        return switch (feature) {
            case CLICK, DRAG, SHIFT_TRANSFER, HOTBAR_SWAP, DOUBLE_CLICK -> true;
            case DIALOG_BRIDGE -> false;
        };
    }

    private static ItemStack toItemStack(GuiItemView view) {
        Material material = Material.matchMaterial(view.materialKey());
        if (material == null) {
            material = Material.STONE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(view.displayName());
            if (!view.lore().isEmpty()) {
                meta.setLore(view.lore());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    interface Backend {
        BackendView open(Player player, GuiRenderModel renderModel);

        boolean render(BackendView view, GuiRenderPatch patch);

        boolean close(BackendView view, GuiCloseReason reason);
    }

    interface BackendView {
    }

    private static final class RealInvUiBackend implements Backend {

        @Override
        public BackendView open(Player player, GuiRenderModel renderModel) {
            int rows = Math.max(1, renderModel.definition().layout().size() / 9);
            Gui gui = Gui.empty(9, rows);
            applySlots(gui, renderModel.definition().slots());

            String rawTitle = renderModel.definition().title().value();
            String title = rawTitle.length() > 32 ? rawTitle.substring(0, 32) : rawTitle;
            Window window = Window.single()
                    .setViewer(player)
                    .setGui(gui)
                    .setTitle(title)
                    .build();
            window.open();
            return new RealBackendView(gui, window);
        }

        @Override
        public boolean render(BackendView view, GuiRenderPatch patch) {
            return true;
        }

        @Override
        public boolean close(BackendView view, GuiCloseReason reason) {
            if (view instanceof RealBackendView realView) {
                realView.window().close();
            }
            return true;
        }
    }

    private record SessionView(UUID playerId, BackendView view) {
    }

    private record RealBackendView(Gui gui, Window window) implements BackendView {
    }

    private static void applySlots(Gui gui, Map<Integer, SlotDefinition> slots) {
        for (Map.Entry<Integer, SlotDefinition> entry : slots.entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot >= gui.getSize()) {
                continue;
            }
            GuiItemView view = entry.getValue().item();
            if (view == null) {
                continue;
            }
            gui.setItem(slot, new SimpleItem(toItemStack(view)));
        }
    }
}
