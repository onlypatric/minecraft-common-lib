package dev.patric.commonlib.api;

import dev.patric.commonlib.api.bootstrap.RuntimeBootstrap;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.capability.CapabilityKey;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.CapabilityStatus;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.arena.ArenaInstance;
import dev.patric.commonlib.api.arena.ArenaOpenRequest;
import dev.patric.commonlib.api.arena.ArenaResetContext;
import dev.patric.commonlib.api.arena.ArenaResetResult;
import dev.patric.commonlib.api.arena.ArenaResetStrategy;
import dev.patric.commonlib.api.arena.ArenaService;
import dev.patric.commonlib.api.arena.ArenaStatus;
import dev.patric.commonlib.api.command.ArgumentType;
import dev.patric.commonlib.api.command.CommandContext;
import dev.patric.commonlib.api.command.CommandExecution;
import dev.patric.commonlib.api.command.CommandMetadata;
import dev.patric.commonlib.api.command.CommandModel;
import dev.patric.commonlib.api.command.CommandNode;
import dev.patric.commonlib.api.command.CommandPermission;
import dev.patric.commonlib.api.command.CommandRegistry;
import dev.patric.commonlib.api.command.CommandResult;
import dev.patric.commonlib.api.command.CommandValidator;
import dev.patric.commonlib.api.command.ExecutionMode;
import dev.patric.commonlib.api.command.PermissionPolicy;
import dev.patric.commonlib.api.command.ValidationIssue;
import dev.patric.commonlib.api.error.ErrorCodes;
import dev.patric.commonlib.api.error.OperationError;
import dev.patric.commonlib.api.error.OperationResult;
import dev.patric.commonlib.api.hud.BossBarOpenRequest;
import dev.patric.commonlib.api.hud.BossBarService;
import dev.patric.commonlib.api.hud.BossBarSession;
import dev.patric.commonlib.api.hud.BossBarState;
import dev.patric.commonlib.api.hud.BossBarUpdateResult;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.HudBarColor;
import dev.patric.commonlib.api.hud.HudBarStyle;
import dev.patric.commonlib.api.hud.HudUpdatePolicy;
import dev.patric.commonlib.api.hud.ScoreboardOpenRequest;
import dev.patric.commonlib.api.hud.ScoreboardSession;
import dev.patric.commonlib.api.hud.ScoreboardSessionService;
import dev.patric.commonlib.api.hud.ScoreboardSessionStatus;
import dev.patric.commonlib.api.hud.ScoreboardSnapshot;
import dev.patric.commonlib.api.hud.ScoreboardUpdateResult;
import dev.patric.commonlib.api.gui.ClickAction;
import dev.patric.commonlib.api.gui.GuiCloseEvent;
import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiDisconnectEvent;
import dev.patric.commonlib.api.gui.GuiEvent;
import dev.patric.commonlib.api.gui.GuiEventResult;
import dev.patric.commonlib.api.gui.GuiOpenRequest;
import dev.patric.commonlib.api.gui.GuiSession;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.api.gui.GuiSessionStatus;
import dev.patric.commonlib.api.gui.GuiState;
import dev.patric.commonlib.api.gui.GuiTimeoutEvent;
import dev.patric.commonlib.api.gui.GuiUpdateResult;
import dev.patric.commonlib.api.dialog.BooleanInputSpec;
import dev.patric.commonlib.api.dialog.CommandTemplateActionSpec;
import dev.patric.commonlib.api.dialog.ConfirmationTypeSpec;
import dev.patric.commonlib.api.dialog.CustomActionSpec;
import dev.patric.commonlib.api.dialog.DialogActionSpec;
import dev.patric.commonlib.api.dialog.DialogAfterAction;
import dev.patric.commonlib.api.dialog.DialogBaseSpec;
import dev.patric.commonlib.api.dialog.DialogBodySpec;
import dev.patric.commonlib.api.dialog.DialogButtonSpec;
import dev.patric.commonlib.api.dialog.DialogCallbacks;
import dev.patric.commonlib.api.dialog.DialogCloseEvent;
import dev.patric.commonlib.api.dialog.DialogCloseReason;
import dev.patric.commonlib.api.dialog.DialogEvent;
import dev.patric.commonlib.api.dialog.DialogEventResult;
import dev.patric.commonlib.api.dialog.DialogInputSpec;
import dev.patric.commonlib.api.dialog.DialogListTypeSpec;
import dev.patric.commonlib.api.dialog.DialogOpenRequest;
import dev.patric.commonlib.api.dialog.DialogResponse;
import dev.patric.commonlib.api.dialog.DialogService;
import dev.patric.commonlib.api.dialog.DialogSession;
import dev.patric.commonlib.api.dialog.DialogSessionStatus;
import dev.patric.commonlib.api.dialog.DialogSubmission;
import dev.patric.commonlib.api.dialog.DialogSubmitEvent;
import dev.patric.commonlib.api.dialog.DialogTemplate;
import dev.patric.commonlib.api.dialog.DialogTemplateRegistry;
import dev.patric.commonlib.api.dialog.DialogTimeoutEvent;
import dev.patric.commonlib.api.dialog.DialogTypeSpec;
import dev.patric.commonlib.api.dialog.ItemBodySpec;
import dev.patric.commonlib.api.dialog.MultiActionTypeSpec;
import dev.patric.commonlib.api.dialog.NoticeTypeSpec;
import dev.patric.commonlib.api.dialog.NumberRangeInputSpec;
import dev.patric.commonlib.api.dialog.PlainMessageBodySpec;
import dev.patric.commonlib.api.dialog.ServerLinksTypeSpec;
import dev.patric.commonlib.api.dialog.SingleOptionEntrySpec;
import dev.patric.commonlib.api.dialog.SingleOptionInputSpec;
import dev.patric.commonlib.api.dialog.StaticClickActionKind;
import dev.patric.commonlib.api.dialog.StaticClickActionSpec;
import dev.patric.commonlib.api.dialog.TextInputSpec;
import dev.patric.commonlib.api.message.FallbackChain;
import dev.patric.commonlib.api.message.MessageRequest;
import dev.patric.commonlib.api.message.PlaceholderResolver;
import dev.patric.commonlib.api.message.PluralRules;
import dev.patric.commonlib.api.persistence.PersistenceRecord;
import dev.patric.commonlib.api.persistence.PersistenceWriteResult;
import dev.patric.commonlib.api.persistence.SchemaMigration;
import dev.patric.commonlib.api.persistence.SchemaMigrationContext;
import dev.patric.commonlib.api.persistence.SchemaMigrationService;
import dev.patric.commonlib.api.persistence.SqlPersistencePort;
import dev.patric.commonlib.api.persistence.YamlPersistencePort;
import dev.patric.commonlib.api.match.DisconnectResult;
import dev.patric.commonlib.api.match.EndReason;
import dev.patric.commonlib.api.match.JoinResult;
import dev.patric.commonlib.api.match.MatchCallbacks;
import dev.patric.commonlib.api.match.MatchCleanup;
import dev.patric.commonlib.api.match.MatchEngineService;
import dev.patric.commonlib.api.match.MatchOpenRequest;
import dev.patric.commonlib.api.match.MatchPolicy;
import dev.patric.commonlib.api.match.MatchSession;
import dev.patric.commonlib.api.match.MatchSessionStatus;
import dev.patric.commonlib.api.match.MatchState;
import dev.patric.commonlib.api.match.MatchTimingPolicy;
import dev.patric.commonlib.api.match.MatchTransitionResult;
import dev.patric.commonlib.api.match.RejoinPolicy;
import dev.patric.commonlib.api.match.RejoinResult;
import dev.patric.commonlib.api.port.ArenaResetPort;
import dev.patric.commonlib.api.port.BossBarPort;
import dev.patric.commonlib.api.port.ClaimsPort;
import dev.patric.commonlib.api.port.CommandPort;
import dev.patric.commonlib.api.port.GuiPort;
import dev.patric.commonlib.api.port.HologramPort;
import dev.patric.commonlib.api.port.MetricsPort;
import dev.patric.commonlib.api.port.NpcPort;
import dev.patric.commonlib.api.port.PacketPort;
import dev.patric.commonlib.api.port.SchematicPort;
import dev.patric.commonlib.api.port.ScoreboardPort;
import dev.patric.commonlib.api.port.noop.NoopClaimsPort;
import dev.patric.commonlib.api.port.noop.NoopBossBarPort;
import dev.patric.commonlib.api.port.noop.NoopCommandPort;
import dev.patric.commonlib.api.port.noop.NoopGuiPort;
import dev.patric.commonlib.api.port.noop.NoopHologramPort;
import dev.patric.commonlib.api.port.noop.NoopMetricsPort;
import dev.patric.commonlib.api.port.noop.NoopNpcPort;
import dev.patric.commonlib.api.port.noop.NoopPacketPort;
import dev.patric.commonlib.api.port.noop.NoopArenaResetPort;
import dev.patric.commonlib.api.port.noop.NoopSchematicPort;
import dev.patric.commonlib.api.port.noop.NoopScoreboardPort;
import dev.patric.commonlib.api.port.options.PasteOptions;
import dev.patric.commonlib.api.packet.PacketDirection;
import dev.patric.commonlib.api.packet.PacketEnvelope;
import dev.patric.commonlib.api.packet.PacketListenerHandle;
import dev.patric.commonlib.api.packet.PacketListenerOptions;
import dev.patric.commonlib.api.packet.PacketListenerPriority;
import dev.patric.commonlib.api.team.FriendlyFirePolicy;
import dev.patric.commonlib.api.team.PartyActionResult;
import dev.patric.commonlib.api.team.PartyService;
import dev.patric.commonlib.api.team.PartySnapshot;
import dev.patric.commonlib.api.team.PartyStatus;
import dev.patric.commonlib.api.team.TeamAssignmentResult;
import dev.patric.commonlib.api.team.TeamDefinition;
import dev.patric.commonlib.api.team.TeamService;
import dev.patric.commonlib.api.team.TeamSnapshot;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                MetricsPort.class,
                PacketPort.class,
                PasteOptions.class,
                PacketDirection.class,
                PacketListenerPriority.class,
                PacketListenerOptions.class,
                PacketEnvelope.class,
                PacketListenerHandle.class,
                CapabilityRegistry.class,
                PortBindingService.class,
                CapabilityKey.class,
                CapabilityStatus.class,
                StandardCapabilities.class,
                NoopNpcPort.class,
                NoopHologramPort.class,
                NoopClaimsPort.class,
                NoopSchematicPort.class,
                NoopCommandPort.class,
                NoopGuiPort.class,
                NoopScoreboardPort.class,
                NoopBossBarPort.class,
                NoopArenaResetPort.class,
                NoopMetricsPort.class,
                NoopPacketPort.class,
                CommandModel.class,
                CommandNode.class,
                CommandExecution.class,
                CommandContext.class,
                CommandResult.class,
                CommandPermission.class,
                CommandValidator.class,
                ValidationIssue.class,
                CommandRegistry.class,
                CommandMetadata.class,
                ArgumentType.class,
                ExecutionMode.class,
                PermissionPolicy.class,
                MessageRequest.class,
                PlaceholderResolver.class,
                FallbackChain.class,
                PluralRules.class,
                GuiState.class,
                GuiCloseReason.class,
                GuiSessionStatus.class,
                ClickAction.class,
                GuiUpdateResult.class,
                GuiEventResult.class,
                GuiOpenRequest.class,
                GuiSession.class,
                GuiEvent.class,
                GuiCloseEvent.class,
                GuiTimeoutEvent.class,
                GuiDisconnectEvent.class,
                GuiSessionService.class,
                DialogService.class,
                DialogTemplateRegistry.class,
                DialogResponse.class,
                DialogCallbacks.class,
                DialogOpenRequest.class,
                DialogSession.class,
                DialogSessionStatus.class,
                DialogCloseReason.class,
                DialogEventResult.class,
                DialogEvent.class,
                DialogSubmitEvent.class,
                DialogTimeoutEvent.class,
                DialogCloseEvent.class,
                DialogSubmission.class,
                DialogTemplate.class,
                DialogBaseSpec.class,
                DialogAfterAction.class,
                DialogBodySpec.class,
                PlainMessageBodySpec.class,
                ItemBodySpec.class,
                DialogInputSpec.class,
                TextInputSpec.class,
                BooleanInputSpec.class,
                NumberRangeInputSpec.class,
                SingleOptionInputSpec.class,
                SingleOptionEntrySpec.class,
                DialogTypeSpec.class,
                ConfirmationTypeSpec.class,
                NoticeTypeSpec.class,
                MultiActionTypeSpec.class,
                DialogListTypeSpec.class,
                ServerLinksTypeSpec.class,
                DialogButtonSpec.class,
                DialogActionSpec.class,
                CommandTemplateActionSpec.class,
                StaticClickActionKind.class,
                StaticClickActionSpec.class,
                CustomActionSpec.class,
                HudAudienceCloseReason.class,
                HudUpdatePolicy.class,
                ScoreboardSnapshot.class,
                ScoreboardSessionStatus.class,
                ScoreboardOpenRequest.class,
                ScoreboardUpdateResult.class,
                ScoreboardSession.class,
                ScoreboardSessionService.class,
                HudBarColor.class,
                HudBarStyle.class,
                BossBarState.class,
                BossBarOpenRequest.class,
                BossBarUpdateResult.class,
                BossBarSession.class,
                BossBarService.class,
                BossBarPort.class,
                MatchState.class,
                EndReason.class,
                MatchSessionStatus.class,
                MatchTimingPolicy.class,
                RejoinPolicy.class,
                MatchPolicy.class,
                MatchOpenRequest.class,
                MatchSession.class,
                MatchTransitionResult.class,
                JoinResult.class,
                DisconnectResult.class,
                RejoinResult.class,
                MatchCallbacks.class,
                MatchCleanup.class,
                MatchEngineService.class,
                ArenaInstance.class,
                ArenaOpenRequest.class,
                ArenaResetContext.class,
                ArenaResetResult.class,
                ArenaResetStrategy.class,
                ArenaService.class,
                ArenaStatus.class,
                FriendlyFirePolicy.class,
                TeamDefinition.class,
                TeamAssignmentResult.class,
                TeamSnapshot.class,
                TeamService.class,
                PartyStatus.class,
                PartyActionResult.class,
                PartySnapshot.class,
                PartyService.class,
                PersistenceRecord.class,
                PersistenceWriteResult.class,
                YamlPersistencePort.class,
                SqlPersistencePort.class,
                SchemaMigration.class,
                SchemaMigrationContext.class,
                SchemaMigrationService.class
        };

        assertTrue(frozenTypes.length >= 48);
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
        assertMethod(ServiceRegistry.class, "find", Optional.class, Class.class);
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

        assertMethod(MessageService.class, "render", Component.class, MessageRequest.class);
        assertMethod(MessageService.class, "render", Component.class, String.class, Locale.class);
        assertMethod(MessageService.class, "render", Component.class, String.class, Map.class, Locale.class);
        assertMethod(MessageService.class, "registerResolver", void.class, PlaceholderResolver.class);
        assertMethod(MessageService.class, "setFallbackChain", void.class, FallbackChain.class);

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
    void frozenCommandModelAndPortsArePresent() throws Exception {
        assertMethod(CommandPort.class, "register", void.class, CommandModel.class);
        assertMethod(CommandPort.class, "unregister", void.class, String.class);
        assertMethod(CommandPort.class, "supportsSuggestions", boolean.class);
        assertMethod(GuiPort.class, "open", boolean.class, GuiSession.class);
        assertMethod(GuiPort.class, "render", boolean.class, UUID.class, GuiState.class);
        assertMethod(GuiPort.class, "close", boolean.class, UUID.class, GuiCloseReason.class);
        assertMethod(GuiPort.class, "supportsPortableEvents", boolean.class);
        assertMethod(ScoreboardPort.class, "open", boolean.class, ScoreboardSession.class);
        assertMethod(ScoreboardPort.class, "render", boolean.class, UUID.class, ScoreboardSnapshot.class);
        assertMethod(ScoreboardPort.class, "close", boolean.class, UUID.class, HudAudienceCloseReason.class);
        assertMethod(BossBarPort.class, "open", boolean.class, BossBarSession.class);
        assertMethod(BossBarPort.class, "render", boolean.class, UUID.class, BossBarState.class);
        assertMethod(BossBarPort.class, "close", boolean.class, UUID.class, HudAudienceCloseReason.class);
        assertMethod(MetricsPort.class, "initialize", boolean.class, JavaPlugin.class, int.class);
        assertMethod(MetricsPort.class, "addSimplePie", boolean.class, String.class, java.util.function.Supplier.class);
        assertMethod(MetricsPort.class, "addSingleLineChart", boolean.class, String.class, java.util.function.IntSupplier.class);
        assertMethod(MetricsPort.class, "shutdown", void.class);
        assertMethod(PacketPort.class, "register", PacketListenerHandle.class, PacketListenerOptions.class, java.util.function.Consumer.class);
        assertMethod(PacketPort.class, "supportsMutation", boolean.class);
        assertMethod(PacketPort.class, "unregisterAll", void.class);

        assertMethod(CommandModel.class, "root", String.class);
        assertMethod(CommandModel.class, "nodes", List.class);
        assertMethod(CommandModel.class, "execution", CommandExecution.class);
        assertMethod(CommandModel.class, "permission", CommandPermission.class);
        assertMethod(CommandModel.class, "metadata", CommandMetadata.class);

        assertMethod(CommandExecution.class, "mode", ExecutionMode.class);
        assertMethod(CommandExecution.class, "run", CompletionStage.class, CommandContext.class);
        assertMethod(CommandValidator.class, "validate", List.class, CommandContext.class, CommandModel.class);

        assertMethod(CommandRegistry.class, "register", void.class, CommandModel.class);
        assertMethod(CommandRegistry.class, "find", Optional.class, String.class);
        assertMethod(CommandRegistry.class, "all", List.class);

        assertMethod(CommandResult.class, "success", CommandResult.class);
        assertMethod(CommandResult.class, "failure", CommandResult.class, String.class, String.class);
        assertMethod(CommandResult.class, "validationFailure", CommandResult.class, String.class, String.class);
    }

    @Test
    void frozenHudServiceModelIsPresent() throws Exception {
        assertMethod(ScoreboardSessionService.class, "open", ScoreboardSession.class, ScoreboardOpenRequest.class);
        assertMethod(ScoreboardSessionService.class, "find", Optional.class, UUID.class);
        assertMethod(ScoreboardSessionService.class, "activeByPlayer", List.class, UUID.class);
        assertMethod(ScoreboardSessionService.class, "update", ScoreboardUpdateResult.class, UUID.class, ScoreboardSnapshot.class);
        assertMethod(ScoreboardSessionService.class, "close", boolean.class, UUID.class, HudAudienceCloseReason.class);
        assertMethod(ScoreboardSessionService.class, "closeAllByPlayer", int.class, UUID.class, HudAudienceCloseReason.class);
        assertMethod(ScoreboardSessionService.class, "closeAll", int.class, HudAudienceCloseReason.class);
        assertMethod(ScoreboardSessionService.class, "onPlayerQuit", void.class, UUID.class);
        assertMethod(ScoreboardSessionService.class, "onPlayerWorldChange", void.class, UUID.class);
        assertMethod(ScoreboardSessionService.class, "policy", HudUpdatePolicy.class);

        assertMethod(BossBarService.class, "open", BossBarSession.class, BossBarOpenRequest.class);
        assertMethod(BossBarService.class, "find", Optional.class, UUID.class);
        assertMethod(BossBarService.class, "activeByPlayer", List.class, UUID.class);
        assertMethod(BossBarService.class, "update", BossBarUpdateResult.class, UUID.class, BossBarState.class);
        assertMethod(BossBarService.class, "close", boolean.class, UUID.class, HudAudienceCloseReason.class);
        assertMethod(BossBarService.class, "closeAllByPlayer", int.class, UUID.class, HudAudienceCloseReason.class);
        assertMethod(BossBarService.class, "closeAll", int.class, HudAudienceCloseReason.class);
        assertMethod(BossBarService.class, "onPlayerQuit", void.class, UUID.class);
        assertMethod(BossBarService.class, "onPlayerWorldChange", void.class, UUID.class);
        assertMethod(BossBarService.class, "policy", HudUpdatePolicy.class);
    }

    @Test
    void frozenMatchEngineModelIsPresent() throws Exception {
        assertMethod(MatchTimingPolicy.class, "competitiveDefaults", MatchTimingPolicy.class);
        assertMethod(RejoinPolicy.class, "competitiveDefaults", RejoinPolicy.class);
        assertMethod(MatchPolicy.class, "competitiveDefaults", MatchPolicy.class);
        assertMethod(MatchCleanup.class, "noop", MatchCleanup.class);

        assertMethod(MatchEngineService.class, "open", MatchSession.class, MatchOpenRequest.class);
        assertMethod(MatchEngineService.class, "find", Optional.class, UUID.class);
        assertMethod(MatchEngineService.class, "active", List.class);
        assertMethod(MatchEngineService.class, "startCountdown", MatchTransitionResult.class, UUID.class);
        assertMethod(MatchEngineService.class, "transition", MatchTransitionResult.class, UUID.class, MatchState.class, EndReason.class);
        assertMethod(MatchEngineService.class, "end", MatchTransitionResult.class, UUID.class, EndReason.class);
        assertMethod(MatchEngineService.class, "join", JoinResult.class, UUID.class, UUID.class);
        assertMethod(MatchEngineService.class, "disconnect", DisconnectResult.class, UUID.class, UUID.class);
        assertMethod(MatchEngineService.class, "rejoin", RejoinResult.class, UUID.class, UUID.class);
        assertMethod(MatchEngineService.class, "closeAll", int.class, EndReason.class);
        assertMethod(MatchEngineService.class, "onPlayerQuit", void.class, UUID.class);
        assertMethod(MatchEngineService.class, "onPlayerWorldChange", void.class, UUID.class);
        assertMethod(MatchEngineService.class, "isIdle", boolean.class);
    }

    @Test
    void frozenGuiSessionModelIsPresent() throws Exception {
        assertMethod(GuiSessionService.class, "open", GuiSession.class, GuiOpenRequest.class);
        assertMethod(GuiSessionService.class, "find", Optional.class, UUID.class);
        assertMethod(GuiSessionService.class, "activeByPlayer", List.class, UUID.class);
        assertMethod(GuiSessionService.class, "update", GuiUpdateResult.class, UUID.class, GuiState.class, long.class);
        assertMethod(GuiSessionService.class, "publish", GuiEventResult.class, GuiEvent.class);
        assertMethod(GuiSessionService.class, "close", boolean.class, UUID.class, GuiCloseReason.class);
        assertMethod(GuiSessionService.class, "closeAllByPlayer", int.class, UUID.class, GuiCloseReason.class);
        assertMethod(GuiSessionService.class, "closeAll", int.class, GuiCloseReason.class);
    }

    @Test
    void frozenDialogModelIsPresent() throws Exception {
        assertMethod(DialogService.class, "open", DialogSession.class, DialogOpenRequest.class);
        assertMethod(DialogService.class, "find", Optional.class, UUID.class);
        assertMethod(DialogService.class, "activeByPlayer", List.class, UUID.class);
        assertMethod(DialogService.class, "publish", DialogEventResult.class, DialogEvent.class);
        assertMethod(DialogService.class, "close", boolean.class, UUID.class, DialogCloseReason.class);
        assertMethod(DialogService.class, "closeAllByPlayer", int.class, UUID.class, DialogCloseReason.class);
        assertMethod(DialogService.class, "closeAll", int.class, DialogCloseReason.class);

        assertMethod(DialogTemplateRegistry.class, "register", void.class, DialogTemplate.class);
        assertMethod(DialogTemplateRegistry.class, "find", Optional.class, String.class);
        assertMethod(DialogTemplateRegistry.class, "all", List.class);
        assertMethod(DialogTemplateRegistry.class, "unregister", boolean.class, String.class);

        assertMethod(DialogResponse.class, "text", Optional.class, String.class);
        assertMethod(DialogResponse.class, "bool", Optional.class, String.class);
        assertMethod(DialogResponse.class, "number", Optional.class, String.class);
        assertMethod(DialogResponse.class, "rawPayload", String.class);
        assertMethod(DialogResponse.class, "asMap", Map.class);
    }

    @Test
    void frozenPluginGenericPortsAndCapabilitiesArePresent() throws Exception {
        assertMethod(ArenaResetPort.class, "resetArena", CompletableFuture.class, String.class);

        assertMethod(NpcPort.class, "spawn", UUID.class, String.class, Location.class, String.class);
        assertMethod(NpcPort.class, "despawn", boolean.class, UUID.class);
        assertMethod(NpcPort.class, "updateDisplayName", boolean.class, UUID.class, String.class);
        assertMethod(NpcPort.class, "teleport", boolean.class, UUID.class, Location.class);

        assertMethod(HologramPort.class, "create", UUID.class, String.class, Location.class, List.class);
        assertMethod(HologramPort.class, "updateLines", boolean.class, UUID.class, List.class);
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
        assertMethod(PortBindingService.class, "bindCommandPort", void.class, CommandPort.class, String.class, String.class);
        assertMethod(
                PortBindingService.class,
                "bindScoreboardPort",
                void.class,
                ScoreboardPort.class,
                String.class,
                String.class
        );
        assertMethod(
                PortBindingService.class,
                "bindHologramPort",
                void.class,
                HologramPort.class,
                String.class,
                String.class
        );
        assertMethod(PortBindingService.class, "bindNpcPort", void.class, NpcPort.class, String.class, String.class);
        assertMethod(PortBindingService.class, "bindClaimsPort", void.class, ClaimsPort.class, String.class, String.class);
        assertMethod(PortBindingService.class, "bindSchematicPort", void.class, SchematicPort.class, String.class, String.class);
        assertMethod(PortBindingService.class, "bindBossBarPort", void.class, BossBarPort.class, String.class, String.class);
        assertMethod(PortBindingService.class, "bindMetricsPort", void.class, MetricsPort.class, String.class, String.class);
        assertMethod(PortBindingService.class, "bindPacketPort", void.class, PacketPort.class, String.class, String.class);
        assertMethod(PortBindingService.class, "markUnavailable", void.class, CapabilityKey.class, String.class);
    }

    @Test
    void frozenArenaTeamAndPersistenceContractsArePresent() throws Exception {
        assertMethod(ArenaResetStrategy.class, "key", String.class);
        assertMethod(ArenaResetStrategy.class, "reset", CompletionStage.class, ArenaInstance.class, ArenaResetContext.class);
        assertMethod(ArenaService.class, "registerStrategy", void.class, ArenaResetStrategy.class);
        assertMethod(ArenaService.class, "open", ArenaInstance.class, ArenaOpenRequest.class);
        assertMethod(ArenaService.class, "find", Optional.class, String.class);
        assertMethod(ArenaService.class, "active", List.class);
        assertMethod(ArenaService.class, "reset", CompletionStage.class, String.class, String.class);
        assertMethod(ArenaService.class, "dispose", boolean.class, String.class);

        assertMethod(TeamService.class, "createRoster", void.class, UUID.class, List.class, FriendlyFirePolicy.class);
        assertMethod(TeamService.class, "assign", TeamAssignmentResult.class, UUID.class, UUID.class, String.class);
        assertMethod(TeamService.class, "autoAssign", TeamAssignmentResult.class, UUID.class, UUID.class);
        assertMethod(TeamService.class, "teamOf", Optional.class, UUID.class, UUID.class);
        assertMethod(TeamService.class, "canDamage", boolean.class, UUID.class, UUID.class, UUID.class);
        assertMethod(TeamService.class, "removePlayer", void.class, UUID.class, UUID.class);
        assertMethod(TeamService.class, "snapshot", Optional.class, UUID.class);
        assertMethod(TeamService.class, "clearRoster", boolean.class, UUID.class);

        assertMethod(PartyService.class, "create", PartySnapshot.class, UUID.class);
        assertMethod(PartyService.class, "invite", PartyActionResult.class, UUID.class, UUID.class, UUID.class);
        assertMethod(PartyService.class, "acceptInvite", PartyActionResult.class, UUID.class, UUID.class);
        assertMethod(PartyService.class, "kick", PartyActionResult.class, UUID.class, UUID.class, UUID.class);
        assertMethod(PartyService.class, "leave", PartyActionResult.class, UUID.class, UUID.class);
        assertMethod(PartyService.class, "disband", PartyActionResult.class, UUID.class, UUID.class);
        assertMethod(PartyService.class, "find", Optional.class, UUID.class);
        assertMethod(PartyService.class, "findByMember", Optional.class, UUID.class);

        assertMethod(YamlPersistencePort.class, "load", Optional.class, String.class, String.class);
        assertMethod(YamlPersistencePort.class, "save", PersistenceWriteResult.class, PersistenceRecord.class);
        assertMethod(YamlPersistencePort.class, "delete", boolean.class, String.class, String.class);
        assertMethod(YamlPersistencePort.class, "list", List.class, String.class);

        assertMethod(SqlPersistencePort.class, "load", CompletionStage.class, String.class, String.class);
        assertMethod(SqlPersistencePort.class, "save", CompletionStage.class, PersistenceRecord.class);
        assertMethod(SqlPersistencePort.class, "delete", CompletionStage.class, String.class, String.class);
        assertMethod(SqlPersistencePort.class, "list", CompletionStage.class, String.class);
        assertMethod(SqlPersistencePort.class, "available", boolean.class);

        assertMethod(SchemaMigration.class, "fromVersion", int.class);
        assertMethod(SchemaMigration.class, "toVersion", int.class);
        assertMethod(SchemaMigration.class, "migrate", void.class, SchemaMigrationContext.class);

        assertMethod(SchemaMigrationService.class, "currentVersion", int.class, String.class);
        assertMethod(SchemaMigrationService.class, "register", void.class, String.class, SchemaMigration.class);
        assertMethod(SchemaMigrationService.class, "migrateToLatest", int.class, String.class);
    }

    @Test
    void legacyDeprecatedTypesWereRemovedBeforeOneZero() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("dev.patric.commonlib.plugin.PluginLifecycle"));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("dev.patric.commonlib.scheduler.Tasks"));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("dev.patric.commonlib.message.MiniMessageService"));
    }

    private static void assertMethod(Class<?> type, String methodName, Class<?> returnType, Class<?>... parameters)
            throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameters);
        assertEquals(returnType, method.getReturnType(), () -> type.getSimpleName() + "#" + methodName + " return type");
    }
}
