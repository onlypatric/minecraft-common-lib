package dev.patric.commonlib.runtime;

import dev.patric.commonlib.api.CommonComponent;
import dev.patric.commonlib.api.CommonContext;
import dev.patric.commonlib.api.CommonRuntime;
import dev.patric.commonlib.api.CommonScheduler;
import dev.patric.commonlib.api.ConfigService;
import dev.patric.commonlib.api.EventRouter;
import dev.patric.commonlib.api.MessageService;
import dev.patric.commonlib.api.RuntimeLogger;
import dev.patric.commonlib.api.ServiceRegistry;
import dev.patric.commonlib.api.adapter.PortBindingService;
import dev.patric.commonlib.api.dialog.DialogCloseReason;
import dev.patric.commonlib.api.dialog.DialogService;
import dev.patric.commonlib.api.dialog.DialogTemplateRegistry;
import dev.patric.commonlib.api.command.CommandRegistry;
import dev.patric.commonlib.api.command.CommandValidator;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.CapabilityStatus;
import dev.patric.commonlib.api.capability.StandardCapabilities;
import dev.patric.commonlib.api.arena.ArenaService;
import dev.patric.commonlib.api.hud.BossBarService;
import dev.patric.commonlib.api.hud.HudAudienceCloseReason;
import dev.patric.commonlib.api.hud.ScoreboardSessionService;
import dev.patric.commonlib.api.gui.GuiCloseReason;
import dev.patric.commonlib.api.gui.GuiSessionService;
import dev.patric.commonlib.api.match.EndReason;
import dev.patric.commonlib.api.match.MatchEngineService;
import dev.patric.commonlib.api.message.FallbackChain;
import dev.patric.commonlib.api.message.PluralRules;
import dev.patric.commonlib.api.persistence.SchemaMigrationService;
import dev.patric.commonlib.api.persistence.SqlPersistencePort;
import dev.patric.commonlib.api.persistence.YamlPersistencePort;
import dev.patric.commonlib.api.team.PartyService;
import dev.patric.commonlib.api.team.TeamService;
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
import dev.patric.commonlib.api.port.noop.NoopBossBarPort;
import dev.patric.commonlib.api.port.noop.NoopClaimsPort;
import dev.patric.commonlib.api.port.noop.NoopCommandPort;
import dev.patric.commonlib.api.port.noop.NoopGuiPort;
import dev.patric.commonlib.api.port.noop.NoopHologramPort;
import dev.patric.commonlib.api.port.noop.NoopMetricsPort;
import dev.patric.commonlib.api.port.noop.NoopNpcPort;
import dev.patric.commonlib.api.port.noop.NoopArenaResetPort;
import dev.patric.commonlib.api.port.noop.NoopPacketPort;
import dev.patric.commonlib.api.port.noop.NoopSchematicPort;
import dev.patric.commonlib.api.port.noop.NoopScoreboardPort;
import dev.patric.commonlib.command.DefaultCommandValidator;
import dev.patric.commonlib.config.YamlConfigService;
import dev.patric.commonlib.lifecycle.SimpleEventRouter;
import dev.patric.commonlib.message.AdvancedMiniMessageService;
import dev.patric.commonlib.message.DefaultFallbackChain;
import dev.patric.commonlib.message.DefaultPluralRules;
import dev.patric.commonlib.scheduler.BukkitCommonScheduler;
import dev.patric.commonlib.runtime.persistence.DefaultSchemaMigrationService;
import dev.patric.commonlib.runtime.persistence.DefaultYamlPersistencePort;
import dev.patric.commonlib.runtime.persistence.NoopSqlPersistencePort;
import dev.patric.commonlib.runtime.adapter.DefaultPortBindingService;
import dev.patric.commonlib.runtime.adapter.DelegatingCommandPort;
import dev.patric.commonlib.runtime.adapter.DelegatingClaimsPort;
import dev.patric.commonlib.runtime.adapter.DelegatingBossBarPort;
import dev.patric.commonlib.runtime.adapter.DelegatingHologramPort;
import dev.patric.commonlib.runtime.adapter.DelegatingMetricsPort;
import dev.patric.commonlib.runtime.adapter.DelegatingNpcPort;
import dev.patric.commonlib.runtime.adapter.DelegatingPacketPort;
import dev.patric.commonlib.runtime.adapter.DelegatingSchematicPort;
import dev.patric.commonlib.runtime.adapter.DelegatingScoreboardPort;
import dev.patric.commonlib.services.DefaultCapabilityRegistry;
import dev.patric.commonlib.services.DefaultServiceRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Default runtime implementation for orchestration and service wiring.
 */
public final class DefaultCommonRuntime implements CommonRuntime {

    private final CommonContext context;
    private final ServiceRegistry serviceRegistry;
    private final RuntimeLogger runtimeLogger;
    private final BukkitCommonScheduler scheduler;
    private final List<CommonComponent> components;
    private final List<CommonComponent> enabledComponents;
    private final AtomicBoolean loaded;
    private final AtomicBoolean enabled;
    private MatchPlayerLifecycleBridge matchPlayerLifecycleBridge;
    private DialogPlayerLifecycleBridge dialogPlayerLifecycleBridge;

    /**
     * Creates the default runtime with optional built-in components.
     *
     * @param plugin owning plugin.
     * @param customComponents extra components to install.
     * @param mainConfigPath relative path to main config.
     * @param messagesConfigPath relative path to messages config.
     * @param defaultLocale default locale used by message service.
     * @param includeDefaultCoreComponents whether to include config and message components.
     */
    public DefaultCommonRuntime(
            JavaPlugin plugin,
            List<CommonComponent> customComponents,
            String mainConfigPath,
            String messagesConfigPath,
            Locale defaultLocale,
            boolean includeDefaultCoreComponents
    ) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(customComponents, "customComponents");
        Objects.requireNonNull(mainConfigPath, "mainConfigPath");
        Objects.requireNonNull(messagesConfigPath, "messagesConfigPath");
        Objects.requireNonNull(defaultLocale, "defaultLocale");

        this.serviceRegistry = new DefaultServiceRegistry();
        this.runtimeLogger = new DefaultRuntimeLogger(plugin.getLogger());
        this.scheduler = new BukkitCommonScheduler(plugin);
        this.context = new DefaultCommonContext(plugin, plugin.getLogger(), scheduler, serviceRegistry);
        this.components = new ArrayList<>();
        this.enabledComponents = new ArrayList<>();
        this.loaded = new AtomicBoolean(false);
        this.enabled = new AtomicBoolean(false);
        this.matchPlayerLifecycleBridge = null;
        this.dialogPlayerLifecycleBridge = null;

        serviceRegistry.register(ServiceRegistry.class, serviceRegistry);
        serviceRegistry.register(RuntimeLogger.class, runtimeLogger);
        serviceRegistry.register(CommonScheduler.class, scheduler);
        EventRouter eventRouter = new SimpleEventRouter();
        serviceRegistry.register(EventRouter.class, eventRouter);
        serviceRegistry.register(CommandRegistry.class, new DefaultCommandRegistry());
        serviceRegistry.register(CommandValidator.class, new DefaultCommandValidator());
        registerDefaultPortsAndCapabilities(eventRouter);

        if (includeDefaultCoreComponents) {
            YamlConfigService configService = new YamlConfigService(plugin, mainConfigPath, List.of(messagesConfigPath));
            FallbackChain fallbackChain = new DefaultFallbackChain();
            PluralRules pluralRules = new DefaultPluralRules();
            MessageService messageService = new AdvancedMiniMessageService(
                    configService,
                    messagesConfigPath,
                    defaultLocale,
                    fallbackChain,
                    pluralRules
            );

            serviceRegistry.register(ConfigService.class, configService);
            serviceRegistry.register(FallbackChain.class, fallbackChain);
            serviceRegistry.register(PluralRules.class, pluralRules);
            serviceRegistry.register(MessageService.class, messageService);

            components.add(new CoreConfigComponent(configService));
            components.add(new CoreMessagesComponent(messageService));
        }

        components.addAll(customComponents);
    }

    private void registerDefaultPortsAndCapabilities(EventRouter eventRouter) {
        NpcPort fallbackNpcPort = new NoopNpcPort();
        DelegatingNpcPort npcPort = new DelegatingNpcPort(fallbackNpcPort);
        HologramPort fallbackHologramPort = new NoopHologramPort();
        DelegatingHologramPort hologramPort = new DelegatingHologramPort(fallbackHologramPort);
        ClaimsPort fallbackClaimsPort = new NoopClaimsPort();
        DelegatingClaimsPort claimsPort = new DelegatingClaimsPort(fallbackClaimsPort);
        SchematicPort fallbackSchematicPort = new NoopSchematicPort();
        DelegatingSchematicPort schematicPort = new DelegatingSchematicPort(fallbackSchematicPort);
        CommandPort fallbackCommandPort = new NoopCommandPort();
        DelegatingCommandPort commandPort = new DelegatingCommandPort(fallbackCommandPort);
        GuiPort guiPort = new NoopGuiPort();
        ScoreboardPort fallbackScoreboardPort = new NoopScoreboardPort();
        DelegatingScoreboardPort scoreboardPort = new DelegatingScoreboardPort(fallbackScoreboardPort);
        BossBarPort fallbackBossBarPort = new NoopBossBarPort();
        DelegatingBossBarPort bossBarPort = new DelegatingBossBarPort(fallbackBossBarPort);
        MetricsPort fallbackMetricsPort = new NoopMetricsPort();
        DelegatingMetricsPort metricsPort = new DelegatingMetricsPort(fallbackMetricsPort);
        PacketPort fallbackPacketPort = new NoopPacketPort();
        DelegatingPacketPort packetPort = new DelegatingPacketPort(fallbackPacketPort);
        ArenaResetPort arenaResetPort = new NoopArenaResetPort();

        serviceRegistry.register(NpcPort.class, npcPort);
        serviceRegistry.register(HologramPort.class, hologramPort);
        serviceRegistry.register(ClaimsPort.class, claimsPort);
        serviceRegistry.register(SchematicPort.class, schematicPort);
        serviceRegistry.register(CommandPort.class, commandPort);
        serviceRegistry.register(GuiPort.class, guiPort);
        serviceRegistry.register(ScoreboardPort.class, scoreboardPort);
        serviceRegistry.register(BossBarPort.class, bossBarPort);
        serviceRegistry.register(MetricsPort.class, metricsPort);
        serviceRegistry.register(PacketPort.class, packetPort);
        serviceRegistry.register(ArenaResetPort.class, arenaResetPort);

        TeamService teamService = new DefaultTeamService();
        PartyService partyService = new DefaultPartyService();
        YamlPersistencePort yamlPersistencePort = new DefaultYamlPersistencePort(context.plugin());
        SqlPersistencePort sqlPersistencePort = new NoopSqlPersistencePort();
        SchemaMigrationService schemaMigrationService = new DefaultSchemaMigrationService(
                context.plugin(),
                yamlPersistencePort,
                sqlPersistencePort,
                serviceRegistry
        );
        ArenaService arenaService = new DefaultArenaService(scheduler, runtimeLogger, serviceRegistry, arenaResetPort);

        serviceRegistry.register(TeamService.class, teamService);
        serviceRegistry.register(PartyService.class, partyService);
        serviceRegistry.register(YamlPersistencePort.class, yamlPersistencePort);
        serviceRegistry.register(SqlPersistencePort.class, sqlPersistencePort);
        serviceRegistry.register(SchemaMigrationService.class, schemaMigrationService);
        serviceRegistry.register(ArenaService.class, arenaService);
        serviceRegistry.register(
                GuiSessionService.class,
                new DefaultGuiSessionService(scheduler, eventRouter, runtimeLogger, guiPort)
        );
        serviceRegistry.register(
                ScoreboardSessionService.class,
                new DefaultScoreboardSessionService(scheduler, runtimeLogger, scoreboardPort)
        );
        serviceRegistry.register(
                BossBarService.class,
                new DefaultBossBarService(scheduler, runtimeLogger, bossBarPort)
        );
        serviceRegistry.register(
                MatchEngineService.class,
                new DefaultMatchEngineService(scheduler, runtimeLogger, serviceRegistry)
        );

        CapabilityRegistry capabilityRegistry = new DefaultCapabilityRegistry();
        DialogTemplateRegistry dialogTemplateRegistry = new DefaultDialogTemplateRegistry();
        DialogService dialogService = new DefaultDialogService(
                context.plugin(),
                scheduler,
                eventRouter,
                runtimeLogger,
                dialogTemplateRegistry,
                capabilityRegistry
        );
        serviceRegistry.register(DialogTemplateRegistry.class, dialogTemplateRegistry);
        serviceRegistry.register(DialogService.class, dialogService);

        PortBindingService portBindingService = new DefaultPortBindingService(
                commandPort,
                scoreboardPort,
                hologramPort,
                npcPort,
                claimsPort,
                schematicPort,
                bossBarPort,
                metricsPort,
                packetPort,
                capabilityRegistry
        );
        capabilityRegistry.publish(StandardCapabilities.NPC, CapabilityStatus.unavailable("No adapter installed"));
        capabilityRegistry.publish(StandardCapabilities.HOLOGRAM, CapabilityStatus.unavailable("No adapter installed"));
        capabilityRegistry.publish(StandardCapabilities.CLAIMS, CapabilityStatus.unavailable("No adapter installed"));
        capabilityRegistry.publish(StandardCapabilities.SCHEMATIC, CapabilityStatus.unavailable("No adapter installed"));
        capabilityRegistry.publish(StandardCapabilities.GUI, CapabilityStatus.unavailable("No adapter installed"));
        capabilityRegistry.publish(StandardCapabilities.COMMAND, CapabilityStatus.unavailable("No adapter installed"));
        capabilityRegistry.publish(StandardCapabilities.SCOREBOARD, CapabilityStatus.unavailable("No adapter installed"));
        capabilityRegistry.publish(StandardCapabilities.BOSSBAR, CapabilityStatus.unavailable("No adapter installed"));
        capabilityRegistry.publish(StandardCapabilities.METRICS, CapabilityStatus.unavailable("No adapter installed"));
        capabilityRegistry.publish(StandardCapabilities.PACKETS, CapabilityStatus.unavailable("No adapter installed"));
        capabilityRegistry.publish(StandardCapabilities.DIALOG, CapabilityStatus.available("core-default"));
        capabilityRegistry.publish(StandardCapabilities.MATCH_ENGINE, CapabilityStatus.available("core-default"));
        capabilityRegistry.publish(StandardCapabilities.ARENA_RESET, CapabilityStatus.unavailable("No adapter installed"));
        capabilityRegistry.publish(StandardCapabilities.PERSISTENCE_YAML, CapabilityStatus.available("core-default"));
        capabilityRegistry.publish(
                StandardCapabilities.PERSISTENCE_SQL,
                CapabilityStatus.unavailable("No SQL adapter configured")
        );
        capabilityRegistry.publish(StandardCapabilities.TEAMS, CapabilityStatus.available("core-default"));
        capabilityRegistry.publish(StandardCapabilities.PARTIES, CapabilityStatus.available("core-default"));

        serviceRegistry.register(CapabilityRegistry.class, capabilityRegistry);
        serviceRegistry.register(PortBindingService.class, portBindingService);
    }

    @Override
    public void onLoad() {
        if (!loaded.compareAndSet(false, true)) {
            return;
        }

        for (CommonComponent component : components) {
            runtimeLogger.lifecycleEvent("onLoad", component.id());
            component.onLoad(context);
        }
    }

    @Override
    public void onEnable() {
        if (!loaded.get()) {
            onLoad();
        }
        if (!enabled.compareAndSet(false, true)) {
            return;
        }

        try {
            for (CommonComponent component : components) {
                runtimeLogger.lifecycleEvent("onEnable", component.id());
                component.onEnable(context);
                enabledComponents.add(component);
            }
            installMatchLifecycleBridge();
            installDialogLifecycleBridge();
        } catch (RuntimeException ex) {
            runtimeLogger.error("enable failed, running rollback", ex);
            uninstallDialogLifecycleBridge();
            uninstallMatchLifecycleBridge();
            rollbackEnabledComponents();
            enabled.set(false);
            throw ex;
        }
    }

    @Override
    public void onDisable() {
        rollbackEnabledComponents();
        uninstallDialogLifecycleBridge();
        uninstallMatchLifecycleBridge();
        serviceRegistry.find(MatchEngineService.class).ifPresent(service -> service.closeAll(EndReason.PLUGIN_DISABLE));
        serviceRegistry.find(ScoreboardSessionService.class)
                .ifPresent(service -> service.closeAll(HudAudienceCloseReason.PLUGIN_DISABLE));
        serviceRegistry.find(BossBarService.class)
                .ifPresent(service -> service.closeAll(HudAudienceCloseReason.PLUGIN_DISABLE));
        serviceRegistry.find(GuiSessionService.class).ifPresent(service -> service.closeAll(GuiCloseReason.PLUGIN_DISABLE));
        serviceRegistry.find(DialogService.class).ifPresent(service -> service.closeAll(DialogCloseReason.PLUGIN_DISABLE));
        serviceRegistry.find(PacketPort.class).ifPresent(PacketPort::unregisterAll);
        serviceRegistry.find(MetricsPort.class).ifPresent(MetricsPort::shutdown);
        scheduler.cancelAll();
        enabled.set(false);
    }

    @Override
    public ServiceRegistry services() {
        return serviceRegistry;
    }

    private void installMatchLifecycleBridge() {
        if (matchPlayerLifecycleBridge != null) {
            return;
        }

        serviceRegistry.find(MatchEngineService.class).ifPresent(matchEngine -> {
            MatchPlayerLifecycleBridge bridge = new MatchPlayerLifecycleBridge(matchEngine);
            context.plugin().getServer().getPluginManager().registerEvents(bridge, context.plugin());
            matchPlayerLifecycleBridge = bridge;
        });
    }

    private void uninstallMatchLifecycleBridge() {
        if (matchPlayerLifecycleBridge == null) {
            return;
        }
        HandlerList.unregisterAll(matchPlayerLifecycleBridge);
        matchPlayerLifecycleBridge = null;
    }

    private void installDialogLifecycleBridge() {
        if (dialogPlayerLifecycleBridge != null) {
            return;
        }

        serviceRegistry.find(DialogService.class)
                .filter(DefaultDialogService.class::isInstance)
                .map(DefaultDialogService.class::cast)
                .ifPresent(dialogService -> {
                    DialogPlayerLifecycleBridge bridge = new DialogPlayerLifecycleBridge(dialogService);
                    context.plugin().getServer().getPluginManager().registerEvents(bridge, context.plugin());
                    dialogPlayerLifecycleBridge = bridge;
                });
    }

    private void uninstallDialogLifecycleBridge() {
        if (dialogPlayerLifecycleBridge == null) {
            return;
        }
        HandlerList.unregisterAll(dialogPlayerLifecycleBridge);
        dialogPlayerLifecycleBridge = null;
    }

    private void rollbackEnabledComponents() {
        for (int i = enabledComponents.size() - 1; i >= 0; i--) {
            CommonComponent component = enabledComponents.get(i);
            try {
                runtimeLogger.lifecycleEvent("onDisable", component.id());
                component.onDisable(context);
            } catch (RuntimeException ex) {
                runtimeLogger.error("disable failed for " + component.id(), ex);
            }
        }
        enabledComponents.clear();
    }

    private record CoreConfigComponent(ConfigService configService) implements CommonComponent {

        @Override
        public String id() {
            return "core-config";
        }

        @Override
        public void onLoad(CommonContext context) {
            configService.reloadAll();
        }
    }

    private record CoreMessagesComponent(MessageService messageService) implements CommonComponent {

        @Override
        public String id() {
            return "core-messages";
        }

        @Override
        public void onLoad(CommonContext context) {
            messageService.render("commonlib.bootstrap", Locale.ENGLISH);
        }
    }
}
