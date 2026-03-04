package dev.patric.commonlib.adapter;

import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.noop.NoopBossBarPort;
import dev.patric.commonlib.api.port.noop.NoopClaimsPort;
import dev.patric.commonlib.api.port.noop.NoopCommandPort;
import dev.patric.commonlib.api.port.noop.NoopHologramPort;
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
import dev.patric.commonlib.runtime.adapter.DelegatingMetricsPort;
import dev.patric.commonlib.runtime.adapter.DelegatingNpcPort;
import dev.patric.commonlib.runtime.adapter.DelegatingPacketPort;
import dev.patric.commonlib.runtime.adapter.DelegatingSchematicPort;
import dev.patric.commonlib.runtime.adapter.DelegatingScoreboardPort;
import dev.patric.commonlib.services.DefaultCapabilityRegistry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchematicBindingPriorityTest {

    @Test
    void faweBindingOverridesWorldEditWhenBothArePresent() {
        CapabilityRegistry capabilityRegistry = new DefaultCapabilityRegistry();
        DelegatingSchematicPort schematicPort = new DelegatingSchematicPort(new NoopSchematicPort());

        DefaultPortBindingService bindingService = new DefaultPortBindingService(
                new DelegatingCommandPort(new NoopCommandPort()),
                new DelegatingScoreboardPort(new NoopScoreboardPort()),
                new DelegatingHologramPort(new NoopHologramPort()),
                new DelegatingNpcPort(new NoopNpcPort()),
                new DelegatingClaimsPort(new NoopClaimsPort()),
                schematicPort,
                new DelegatingBossBarPort(new NoopBossBarPort()),
                new DelegatingMetricsPort(new NoopMetricsPort()),
                new DelegatingPacketPort(new NoopPacketPort()),
                capabilityRegistry
        );

        AtomicInteger worldEditCalls = new AtomicInteger();
        AtomicInteger faweCalls = new AtomicInteger();

        SchematicPort worldEditPort = new SchematicPort() {
            @Override
            public CompletableFuture<Void> paste(String schematicKey, Location origin, PasteOptions options) {
                worldEditCalls.incrementAndGet();
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletableFuture<Void> resetRegion(String regionKey, String templateKey, PasteOptions options) {
                worldEditCalls.incrementAndGet();
                return CompletableFuture.completedFuture(null);
            }
        };

        SchematicPort fawePort = new SchematicPort() {
            @Override
            public CompletableFuture<Void> paste(String schematicKey, Location origin, PasteOptions options) {
                faweCalls.incrementAndGet();
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletableFuture<Void> resetRegion(String regionKey, String templateKey, PasteOptions options) {
                faweCalls.incrementAndGet();
                return CompletableFuture.completedFuture(null);
            }
        };

        bindingService.bindSchematicPort(worldEditPort, "worldedit", "7.3.0");
        bindingService.bindSchematicPort(fawePort, "fawe", "2.11.0");

        schematicPort.paste("spawn", new Location(null, 0, 0, 0), new PasteOptions(false, false, false, 1000)).join();

        assertEquals(0, worldEditCalls.get());
        assertEquals(1, faweCalls.get());
        assertEquals("fawe:2.11.0", capabilityRegistry.status(StandardCapabilities.SCHEMATIC).orElseThrow().metadata());

        // lower priority bind must not override fawe.
        bindingService.bindSchematicPort(worldEditPort, "worldedit", "7.3.1");
        schematicPort.resetRegion("r1", "tpl", new PasteOptions(false, false, false, 1000)).join();

        assertEquals(0, worldEditCalls.get());
        assertEquals(2, faweCalls.get());
    }
}
