package dev.patric.commonlib.api;

import dev.patric.commonlib.api.bootstrap.RuntimeBootstrap;
import dev.patric.commonlib.api.capability.CapabilityKey;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.CapabilityStatus;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.error.ErrorCodes;
import dev.patric.commonlib.api.error.OperationError;
import dev.patric.commonlib.api.error.OperationResult;
import dev.patric.commonlib.api.port.ArenaResetPort;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.noop.NoopClaimsPort;
import dev.patric.commonlib.api.port.noop.NoopHologramPort;
import dev.patric.commonlib.api.port.noop.NoopNpcPort;
import dev.patric.commonlib.api.port.noop.NoopSchematicPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicApiFreezeContractTest {

    @Test
    void frozenPublicTypesAreLoadable() {
        Class<?>[] frozenTypes = {
                CommonRuntime.class,
                CommonComponent.class,
                CommonContext.class,
                ServiceRegistry.class,
                CommonScheduler.class,
                TaskHandle.class,
                ConfigService.class,
                MessageService.class,
                EventRouter.class,
                RuntimeLogger.class,
                RuntimeBootstrap.class,
                OperationResult.class,
                OperationError.class,
                ErrorCodes.class,
                CommandPort.class,
                GuiPort.class,
                ScoreboardPort.class,
                ArenaResetPort.class,
                NpcPort.class,
                HologramPort.class,
                ClaimsPort.class,
                SchematicPort.class,
                PasteOptions.class,
                CapabilityRegistry.class,
                CapabilityKey.class,
                CapabilityStatus.class,
                StandardCapabilities.class,
                NoopNpcPort.class,
                NoopHologramPort.class,
                NoopClaimsPort.class,
                NoopSchematicPort.class
        };

        assertEquals(31, frozenTypes.length);
        for (Class<?> type : frozenTypes) {
            assertTrue(type.getName().startsWith("dev.patric.commonlib.api"));
        }
    }

    @Test
    void frozenCoreMethodSignaturesArePresent() throws Exception {
        assertMethod(CommonRuntime.class, "onLoad", void.class);
        assertMethod(CommonRuntime.class, "onEnable", void.class);
        assertMethod(CommonRuntime.class, "onDisable", void.class);
        assertMethod(CommonRuntime.class, "services", ServiceRegistry.class);

        Method builder = CommonRuntime.class.getMethod("builder", JavaPlugin.class);
        assertEquals(CommonRuntime.Builder.class, builder.getReturnType());
        assertTrue(Modifier.isStatic(builder.getModifiers()));

        assertMethod(CommonComponent.class, "id", String.class);
        assertMethod(CommonComponent.class, "onLoad", void.class, CommonContext.class);
        assertMethod(CommonComponent.class, "onEnable", void.class, CommonContext.class);
        assertMethod(CommonComponent.class, "onDisable", void.class, CommonContext.class);

        assertMethod(CommonContext.class, "plugin", JavaPlugin.class);
        assertMethod(CommonContext.class, "logger", java.util.logging.Logger.class);
        assertMethod(CommonContext.class, "scheduler", CommonScheduler.class);
        assertMethod(CommonContext.class, "services", ServiceRegistry.class);

        assertMethod(ServiceRegistry.class, "register", void.class, Class.class, Object.class);
        assertMethod(ServiceRegistry.class, "find", java.util.Optional.class, Class.class);
        assertMethod(ServiceRegistry.class, "require", Object.class, Class.class);

        assertMethod(CommonScheduler.class, "runSync", TaskHandle.class, Runnable.class);
        assertMethod(CommonScheduler.class, "runSyncLater", TaskHandle.class, long.class, Runnable.class);
        assertMethod(CommonScheduler.class, "runSyncRepeating", TaskHandle.class, long.class, long.class, Runnable.class);
        assertMethod(CommonScheduler.class, "runAsync", TaskHandle.class, Runnable.class);
        assertMethod(CommonScheduler.class, "supplyAsync", CompletableFuture.class, Supplier.class);
        assertMethod(CommonScheduler.class, "isPrimaryThread", boolean.class);
        assertMethod(CommonScheduler.class, "requirePrimaryThread", void.class, String.class);

        assertMethod(TaskHandle.class, "cancel", void.class);
        assertMethod(TaskHandle.class, "isCancelled", boolean.class);

        assertMethod(ConfigService.class, "main", FileConfiguration.class);
        assertMethod(ConfigService.class, "load", FileConfiguration.class, String.class);
        assertMethod(ConfigService.class, "reloadAll", void.class);

        assertMethod(MessageService.class, "render", Component.class, String.class, Map.class, Locale.class);
        assertMethod(MessageService.class, "render", Component.class, String.class);

        assertMethod(EventRouter.class, "registerPolicy", void.class, Class.class, dev.patric.commonlib.guard.PolicyHook.class);
        assertMethod(EventRouter.class, "route", dev.patric.commonlib.guard.PolicyDecision.class, Event.class);
    }

    @Test
    void frozenBootstrapAndErrorHelpersArePresent() throws Exception {
        assertMethod(RuntimeBootstrap.class, "build", OperationResult.class, JavaPlugin.class, java.util.function.Consumer.class);
        assertMethod(RuntimeBootstrap.class, "build", OperationResult.class, JavaPlugin.class, CommonComponent[].class);
        assertMethod(RuntimeBootstrap.class, "safeLoad", OperationResult.class, CommonRuntime.class);
        assertMethod(RuntimeBootstrap.class, "safeEnable", OperationResult.class, CommonRuntime.class);
        assertMethod(RuntimeBootstrap.class, "safeDisable", OperationResult.class, CommonRuntime.class);

        assertMethod(OperationResult.class, "isSuccess", boolean.class);
        assertMethod(OperationResult.class, "isFailure", boolean.class);
        assertMethod(OperationResult.class, "valueOrNull", Object.class);
        assertMethod(OperationResult.class, "errorOrNull", OperationError.class);

        assertMethod(OperationError.class, "of", OperationError.class, ErrorCodes.class, String.class);
        assertMethod(OperationError.class, "of", OperationError.class, ErrorCodes.class, String.class, Throwable.class);
        assertMethod(OperationError.class, "code", ErrorCodes.class);
        assertMethod(OperationError.class, "message", String.class);
        assertMethod(OperationError.class, "cause", Throwable.class);
    }

    @Test
    void frozenPluginGenericPortsAndCapabilitiesArePresent() throws Exception {
        assertMethod(NpcPort.class, "spawn", UUID.class, String.class, Location.class, String.class);
        assertMethod(NpcPort.class, "despawn", boolean.class, UUID.class);
        assertMethod(NpcPort.class, "updateDisplayName", boolean.class, UUID.class, String.class);
        assertMethod(NpcPort.class, "teleport", boolean.class, UUID.class, Location.class);

        assertMethod(HologramPort.class, "create", UUID.class, String.class, Location.class, java.util.List.class);
        assertMethod(HologramPort.class, "updateLines", boolean.class, UUID.class, java.util.List.class);
        assertMethod(HologramPort.class, "move", boolean.class, UUID.class, Location.class);
        assertMethod(HologramPort.class, "delete", boolean.class, UUID.class);

        assertMethod(ClaimsPort.class, "isInsideClaim", boolean.class, UUID.class, Location.class);
        assertMethod(ClaimsPort.class, "claimIdAt", Optional.class, Location.class);
        assertMethod(ClaimsPort.class, "hasBuildPermission", boolean.class, UUID.class, String.class);
        assertMethod(ClaimsPort.class, "hasCombatPermission", boolean.class, UUID.class, String.class);

        assertMethod(SchematicPort.class, "paste", CompletableFuture.class, String.class, Location.class, PasteOptions.class);
        assertMethod(SchematicPort.class, "resetRegion", CompletableFuture.class, String.class, String.class, PasteOptions.class);

        assertMethod(CapabilityRegistry.class, "publish", void.class, CapabilityKey.class, CapabilityStatus.class);
        assertMethod(CapabilityRegistry.class, "status", Optional.class, CapabilityKey.class);
        assertMethod(CapabilityRegistry.class, "isAvailable", boolean.class, CapabilityKey.class);

        assertMethod(CapabilityKey.class, "of", CapabilityKey.class, String.class, Class.class);
        assertMethod(CapabilityStatus.class, "available", CapabilityStatus.class, Object.class);
        assertMethod(CapabilityStatus.class, "unavailable", CapabilityStatus.class, String.class);
    }

    private static void assertMethod(Class<?> type, String methodName, Class<?> returnType, Class<?>... parameters)
            throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameters);
        assertEquals(returnType, method.getReturnType(), () -> type.getSimpleName() + "#" + methodName + " return type");
    }
}
