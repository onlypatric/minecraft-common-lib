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
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSessionStatus;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import dev.patric.commonlib.api.port.noop.NoopCommandPort;
import dev.patric.commonlib.api.port.noop.NoopHologramPort;
import dev.patric.commonlib.api.port.noop.NoopNpcPort;
import dev.patric.commonlib.api.port.noop.NoopScoreboardPort;
import dev.patric.commonlib.runtime.adapter.DefaultPortBindingService;
import dev.patric.commonlib.runtime.adapter.DelegatingCommandPort;
import dev.patric.commonlib.runtime.adapter.DelegatingHologramPort;
import dev.patric.commonlib.runtime.adapter.DelegatingNpcPort;
import dev.patric.commonlib.runtime.adapter.DelegatingScoreboardPort;
import dev.patric.commonlib.services.DefaultCapabilityRegistry;
import java.util.List;
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
    void bindAndMarkUnavailableSwitchesDelegatesAndCapabilities() {
        DelegatingCommandPort commandPort = new DelegatingCommandPort(new NoopCommandPort());
        DelegatingScoreboardPort scoreboardPort = new DelegatingScoreboardPort(new NoopScoreboardPort());
        DelegatingHologramPort hologramPort = new DelegatingHologramPort(new NoopHologramPort());
        DelegatingNpcPort npcPort = new DelegatingNpcPort(new NoopNpcPort());
        CapabilityRegistry capabilityRegistry = new DefaultCapabilityRegistry();

        DefaultPortBindingService bindingService = new DefaultPortBindingService(
                commandPort,
                scoreboardPort,
                hologramPort,
                npcPort,
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
        assertEquals(
                "missing-plugin:CommandAPI",
                capabilityRegistry.status(StandardCapabilities.COMMAND).orElseThrow().reason()
        );
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
