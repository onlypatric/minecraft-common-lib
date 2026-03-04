package dev.patric.commonlib.adapter;

import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.command.CommandExecution;
import dev.patric.commonlib.api.command.CommandMetadata;
import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.command.CommandNode;
import dev.patric.commonlib.api.command.CommandPermission;
import dev.patric.commonlib.api.command.CommandResult;
import dev.patric.commonlib.api.command.ExecutionMode;
import dev.patric.commonlib.api.command.PermissionPolicy;
import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.HudBarColor;
import dev.patric.commonlib.api.hud.HudBarStyle;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSessionStatus;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.packet.PacketDirection;
import dev.patric.commonlib.api.packet.PacketEnvelope;
import dev.patric.commonlib.api.packet.PacketListenerHandle;
import dev.patric.commonlib.api.packet.PacketListenerOptions;
import dev.patric.commonlib.api.packet.PacketListenerPriority;
import dev.patric.commonlib.api.port.BossBarPort;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.api.port.MetricsPort;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.PacketPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import dev.patric.commonlib.api.port.noop.NoopBossBarPort;
import dev.patric.commonlib.api.port.noop.NoopClaimsPort;
import dev.patric.commonlib.api.port.noop.NoopCommandPort;
import dev.patric.commonlib.api.port.noop.NoopHologramPort;
import dev.patric.commonlib.api.port.noop.NoopGuiPort;
import dev.patric.commonlib.api.port.noop.NoopMetricsPort;
import dev.patric.commonlib.api.port.noop.NoopNpcPort;
import dev.patric.commonlib.api.port.noop.NoopPacketPort;
import dev.patric.commonlib.api.port.noop.NoopSchematicPort;
import dev.patric.commonlib.api.port.noop.NoopScoreboardPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import dev.patric.commonlib.runtime.adapter.DefaultPortBindingService;
import dev.patric.commonlib.runtime.adapter.DelegatingBossBarPort;
import dev.patric.commonlib.runtime.adapter.DelegatingClaimsPort;
import dev.patric.commonlib.runtime.adapter.DelegatingCommandPort;
import dev.patric.commonlib.runtime.adapter.DelegatingHologramPort;
import dev.patric.commonlib.runtime.adapter.DelegatingGuiPort;
import dev.patric.commonlib.runtime.adapter.DelegatingMetricsPort;
import dev.patric.commonlib.runtime.adapter.DelegatingNpcPort;
import dev.patric.commonlib.runtime.adapter.DelegatingPacketPort;
import dev.patric.commonlib.runtime.adapter.DelegatingSchematicPort;
import dev.patric.commonlib.runtime.adapter.DelegatingScoreboardPort;
import dev.patric.commonlib.services.DefaultCapabilityRegistry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortBindingServiceTest {

    @Test
    void bindAndMarkUnavailableSwitchesDelegatesAndCapabilitiesAcrossAllPorts() {
        DelegatingCommandPort commandPort = new DelegatingCommandPort(new NoopCommandPort());
        DelegatingScoreboardPort scoreboardPort = new DelegatingScoreboardPort(new NoopScoreboardPort());
        DelegatingHologramPort hologramPort = new DelegatingHologramPort(new NoopHologramPort());
        DelegatingNpcPort npcPort = new DelegatingNpcPort(new NoopNpcPort());
        DelegatingGuiPort guiPort = new DelegatingGuiPort(new NoopGuiPort());
        DelegatingClaimsPort claimsPort = new DelegatingClaimsPort(new NoopClaimsPort());
        DelegatingSchematicPort schematicPort = new DelegatingSchematicPort(new NoopSchematicPort());
        DelegatingBossBarPort bossBarPort = new DelegatingBossBarPort(new NoopBossBarPort());
        DelegatingMetricsPort metricsPort = new DelegatingMetricsPort(new NoopMetricsPort());
        DelegatingPacketPort packetPort = new DelegatingPacketPort(new NoopPacketPort());
        CapabilityRegistry capabilityRegistry = new DefaultCapabilityRegistry();

        DefaultPortBindingService bindingService = new DefaultPortBindingService(
                commandPort,
                scoreboardPort,
                hologramPort,
                npcPort,
                guiPort,
                claimsPort,
                schematicPort,
                bossBarPort,
                metricsPort,
                packetPort,
                capabilityRegistry
        );

        TrackingCommandPort trackingCommandPort = new TrackingCommandPort();
        bindingService.bindCommandPort(trackingCommandPort, "CommandAPI", "11.1.0");

        commandPort.register(testModel("arena"));
        assertEquals(1, trackingCommandPort.registerCount.get());
        assertTrue(commandPort.supportsSuggestions());
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.COMMAND));
        assertEquals("commandapi:11.1.0", capabilityRegistry.status(StandardCapabilities.COMMAND).orElseThrow().metadata());

        bindingService.markUnavailable(StandardCapabilities.COMMAND, "missing-plugin:CommandAPI");
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.COMMAND));
        assertEquals("missing-plugin:CommandAPI", capabilityRegistry.status(StandardCapabilities.COMMAND).orElseThrow().reason());
        assertFalse(commandPort.supportsSuggestions());

        ToggleScoreboardPort toggleScoreboardPort = new ToggleScoreboardPort();
        bindingService.bindScoreboardPort(toggleScoreboardPort, "fastboard", "2.1.5");

        ScoreboardSession session = new ScoreboardSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "main",
                new ScoreboardSnapshot("Title", List.of("L1")),
                ScoreboardSessionStatus.OPEN,
                1L,
                0L
        );

        assertFalse(scoreboardPort.open(session));
        bindingService.markUnavailable(StandardCapabilities.SCOREBOARD, "missing-class:fr.mrmicky.fastboard.FastBoard");
        assertTrue(scoreboardPort.open(session));

        HologramPort customHologramPort = new HologramPort() {
            @Override
            public UUID create(String hologramKey, Location location, List<String> lines) {
                return new UUID(0L, 42L);
            }

            @Override
            public boolean updateLines(UUID hologramId, List<String> lines) {
                return true;
            }

            @Override
            public boolean move(UUID hologramId, Location location) {
                return true;
            }

            @Override
            public boolean delete(UUID hologramId) {
                return true;
            }
        };

        bindingService.bindHologramPort(customHologramPort, "fancyholograms", "2.9.1");
        assertEquals(new UUID(0L, 42L), hologramPort.create("holo", new Location(null, 0, 0, 0), List.of("a")));
        bindingService.markUnavailable(StandardCapabilities.HOLOGRAM, "disabled-plugin:FancyHolograms");
        assertFalse(hologramPort.delete(new UUID(0L, 42L)));

        NpcPort customNpcPort = new NpcPort() {
            @Override
            public UUID spawn(String templateKey, Location location, String displayName) {
                return new UUID(0L, 99L);
            }

            @Override
            public boolean despawn(UUID npcId) {
                return true;
            }

            @Override
            public boolean updateDisplayName(UUID npcId, String displayName) {
                return true;
            }

            @Override
            public boolean teleport(UUID npcId, Location location) {
                return true;
            }
        };

        bindingService.bindNpcPort(customNpcPort, "fancynpcs", "2.9.0");
        assertEquals(new UUID(0L, 99L), npcPort.spawn("villager", new Location(null, 0, 0, 0), "Trader"));
        bindingService.markUnavailable(StandardCapabilities.NPC, "missing-plugin:FancyNpcs");
        assertFalse(npcPort.despawn(new UUID(0L, 99L)));

        GuiPort customGuiPort = new GuiPort() {
            @Override
            public boolean open(dev.patric.commonlib.api.gui.render.GuiRenderModel renderModel) {
                return false;
            }

            @Override
            public boolean render(UUID sessionId, dev.patric.commonlib.api.gui.render.GuiRenderPatch patch) {
                return false;
            }

            @Override
            public boolean close(UUID sessionId, dev.patric.commonlib.api.gui.GuiCloseReason reason) {
                return false;
            }

            @Override
            public boolean supports(dev.patric.commonlib.api.gui.GuiPortFeature feature) {
                return true;
            }
        };
        bindingService.bindGuiPort(customGuiPort, "invui", "unknown");
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.GUI));
        assertFalse(guiPort.open(new dev.patric.commonlib.api.gui.render.GuiRenderModel(
                UUID.randomUUID(),
                UUID.randomUUID(),
                dev.patric.commonlib.api.gui.GuiDefinition.chest("gui.main", 6, "Main"),
                dev.patric.commonlib.api.gui.GuiState.empty()
        )));
        bindingService.markUnavailable(StandardCapabilities.GUI, "missing-plugin:InvUI");
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.GUI));

        ClaimsPort claimsImpl = new ClaimsPort() {
            @Override
            public boolean isInsideClaim(UUID playerId, Location location) {
                return true;
            }

            @Override
            public Optional<String> claimIdAt(Location location) {
                return Optional.of("claim-main");
            }

            @Override
            public boolean hasBuildPermission(UUID playerId, String claimId) {
                return true;
            }

            @Override
            public boolean hasCombatPermission(UUID playerId, String claimId) {
                return false;
            }
        };
        bindingService.bindClaimsPort(claimsImpl, "huskclaims", "4.7.1");
        assertTrue(claimsPort.isInsideClaim(UUID.randomUUID(), new Location(null, 0, 0, 0)));

        bindingService.markUnavailable(StandardCapabilities.CLAIMS, "missing-plugin:HuskClaims");
        assertFalse(claimsPort.isInsideClaim(UUID.randomUUID(), new Location(null, 0, 0, 0)));

        SchematicPort schematicImpl = new SchematicPort() {
            @Override
            public CompletableFuture<Void> paste(String schematicKey, Location origin, PasteOptions options) {
                return CompletableFuture.failedFuture(new IllegalStateException("custom"));
            }

            @Override
            public CompletableFuture<Void> resetRegion(String regionKey, String templateKey, PasteOptions options) {
                return CompletableFuture.failedFuture(new IllegalStateException("custom"));
            }
        };
        bindingService.bindSchematicPort(schematicImpl, "worldedit", "7.3.0");
        assertTrue(capabilityRegistry.isAvailable(StandardCapabilities.SCHEMATIC));

        bindingService.markUnavailable(StandardCapabilities.SCHEMATIC, "disabled-plugin:WorldEdit");
        assertFalse(capabilityRegistry.isAvailable(StandardCapabilities.SCHEMATIC));

        BossBarPort bossImpl = new BossBarPort() {
            @Override
            public boolean open(BossBarSession session) {
                return false;
            }

            @Override
            public boolean render(UUID barId, BossBarState state) {
                return false;
            }

            @Override
            public boolean close(UUID barId, HudAudienceCloseReason reason) {
                return false;
            }
        };
        bindingService.bindBossBarPort(bossImpl, "paper-bossbar", "paper-1.21.11");
        assertFalse(bossBarPort.open(new BossBarSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "hud",
                new BossBarState("Title", 1.0f, HudBarColor.BLUE, HudBarStyle.SOLID, true),
                false,
                System.currentTimeMillis(),
                0L
        )));

        bindingService.markUnavailable(StandardCapabilities.BOSSBAR, "binding-failed:paper-bossbar:RuntimeException");
        assertTrue(bossBarPort.close(UUID.randomUUID(), HudAudienceCloseReason.MANUAL));

        MetricsPort metricsImpl = new MetricsPort() {
            @Override
            public boolean initialize(org.bukkit.plugin.java.JavaPlugin plugin, int pluginId) {
                return false;
            }

            @Override
            public boolean addSimplePie(String chartId, java.util.function.Supplier<String> supplier) {
                return false;
            }

            @Override
            public boolean addSingleLineChart(String chartId, java.util.function.IntSupplier supplier) {
                return false;
            }

            @Override
            public void shutdown() {
                // no-op
            }
        };
        bindingService.bindMetricsPort(metricsImpl, "bstats", "3.1.0");
        assertFalse(metricsPort.addSimplePie("mode", () -> "solo"));

        bindingService.markUnavailable(StandardCapabilities.METRICS, "binding-failed:bstats:initialize");
        assertTrue(metricsPort.addSimplePie("mode", () -> "solo"));

        PacketPort packetImpl = new PacketPort() {
            @Override
            public PacketListenerHandle register(PacketListenerOptions options, java.util.function.Consumer<PacketEnvelope> listener) {
                return () -> {
                    // no-op
                };
            }

            @Override
            public boolean supportsMutation() {
                return true;
            }

            @Override
            public void unregisterAll() {
                // no-op
            }
        };
        bindingService.bindPacketPort(packetImpl, "protocollib", "5.3.0");
        assertTrue(packetPort.supportsMutation());

        bindingService.markUnavailable(StandardCapabilities.PACKETS, "missing-plugin:ProtocolLib");
        assertFalse(packetPort.supportsMutation());

        packetPort.register(
                new PacketListenerOptions(PacketDirection.INBOUND, List.of("CHAT"), PacketListenerPriority.NORMAL, false),
                envelope -> {
                    // no-op
                }
        ).close();
    }

    private static CommandModel testModel(String root) {
        return new CommandModel() {
            @Override
            public String root() {
                return root;
            }

            @Override
            public List<CommandNode> nodes() {
                return List.of(new CommandNode("target", dev.patric.commonlib.api.command.ArgumentType.STRING, false, List.of()));
            }

            @Override
            public CommandExecution execution() {
                return new CommandExecution() {
                    @Override
                    public ExecutionMode mode() {
                        return ExecutionMode.SYNC;
                    }

                    @Override
                    public CompletionStage<CommandResult> run(dev.patric.commonlib.api.command.CommandContext context) {
                        return CompletableFuture.completedFuture(CommandResult.success());
                    }
                };
            }

            @Override
            public CommandPermission permission() {
                return new CommandPermission("commonlib.test", PermissionPolicy.OPTIONAL);
            }

            @Override
            public CommandMetadata metadata() {
                return new CommandMetadata("desc", "/" + root);
            }
        };
    }

    private static final class TrackingCommandPort implements CommandPort {

        private final AtomicInteger registerCount = new AtomicInteger();

        @Override
        public void register(CommandModel model) {
            registerCount.incrementAndGet();
        }

        @Override
        public void unregister(String root) {
            // no-op
        }

        @Override
        public boolean supportsSuggestions() {
            return true;
        }
    }

    private static final class ToggleScoreboardPort implements ScoreboardPort {

        @Override
        public boolean open(ScoreboardSession session) {
            return false;
        }

        @Override
        public boolean render(UUID sessionId, ScoreboardSnapshot snapshot) {
            return false;
        }

        @Override
        public boolean close(UUID sessionId, HudAudienceCloseReason reason) {
            return false;
        }
    }
}
