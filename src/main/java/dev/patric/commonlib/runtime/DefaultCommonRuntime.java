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
import dev.patric.commonlib.config.YamlConfigService;
import dev.patric.commonlib.lifecycle.SimpleEventRouter;
import dev.patric.commonlib.message.MiniMessageService;
import dev.patric.commonlib.scheduler.BukkitCommonScheduler;
import dev.patric.commonlib.services.DefaultServiceRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
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

        serviceRegistry.register(ServiceRegistry.class, serviceRegistry);
        serviceRegistry.register(RuntimeLogger.class, runtimeLogger);
        serviceRegistry.register(CommonScheduler.class, scheduler);
        serviceRegistry.register(EventRouter.class, new SimpleEventRouter());

        if (includeDefaultCoreComponents) {
            YamlConfigService configService = new YamlConfigService(plugin, mainConfigPath, List.of(messagesConfigPath));
            MiniMessageService messageService = new MiniMessageService(configService, messagesConfigPath, defaultLocale);

            serviceRegistry.register(ConfigService.class, configService);
            serviceRegistry.register(MessageService.class, messageService);

            components.add(new CoreConfigComponent(configService));
            components.add(new CoreMessagesComponent(messageService));
        }

        components.addAll(customComponents);
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
        } catch (RuntimeException ex) {
            runtimeLogger.error("enable failed, running rollback", ex);
            rollbackEnabledComponents();
            enabled.set(false);
            throw ex;
        }
    }

    @Override
    public void onDisable() {
        if (!loaded.get()) {
            return;
        }

        rollbackEnabledComponents();
        scheduler.cancelAll();
        enabled.set(false);
    }

    @Override
    public ServiceRegistry services() {
        return serviceRegistry;
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
            messageService.render("commonlib.bootstrap");
        }
    }
}
