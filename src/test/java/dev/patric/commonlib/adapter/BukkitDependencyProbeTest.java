package dev.patric.commonlib.adapter;

import dev.patric.commonlib.runtime.adapter.BukkitDependencyProbe;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BukkitDependencyProbeTest {

    @Test
    void probeReturnsMissingPluginReasonWhenDependencyIsAbsent() {
        JavaPlugin owner = mock(JavaPlugin.class);
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);

        when(owner.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(pluginManager.getPlugin("CommandAPI")).thenReturn(null);

        BukkitDependencyProbe.ProbeResult result = BukkitDependencyProbe.probe(
                owner,
                "CommandAPI",
                "11.1.0",
                null
        );

        assertFalse(result.available());
        assertEquals("missing-plugin:CommandAPI", result.reason());
    }

    @Test
    void probeReturnsDisabledReasonWhenDependencyIsPresentButDisabled() {
        JavaPlugin owner = mock(JavaPlugin.class);
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        Plugin dependency = mock(Plugin.class);

        when(owner.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(pluginManager.getPlugin("FancyNpcs")).thenReturn(dependency);
        when(dependency.isEnabled()).thenReturn(false);

        BukkitDependencyProbe.ProbeResult result = BukkitDependencyProbe.probe(
                owner,
                "FancyNpcs",
                "2.9.0",
                null
        );

        assertFalse(result.available());
        assertEquals("disabled-plugin:FancyNpcs", result.reason());
    }

    @Test
    void probeReturnsIncompatibleVersionReasonWhenInstalledVersionIsTooOld() {
        JavaPlugin owner = mock(JavaPlugin.class);
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        Plugin dependency = mock(Plugin.class);
        PluginDescriptionFile description = mock(PluginDescriptionFile.class);

        when(owner.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(pluginManager.getPlugin("FancyHolograms")).thenReturn(dependency);
        when(dependency.isEnabled()).thenReturn(true);
        when(dependency.getDescription()).thenReturn(description);
        when(description.getVersion()).thenReturn("2.8.9");

        BukkitDependencyProbe.ProbeResult result = BukkitDependencyProbe.probe(
                owner,
                "FancyHolograms",
                "2.9.1",
                null
        );

        assertFalse(result.available());
        assertEquals("incompatible-version:FancyHolograms:2.8.9<2.9.1", result.reason());
    }

    @Test
    void probeReturnsAvailableForCompatibleDependency() {
        JavaPlugin owner = mock(JavaPlugin.class);
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        Plugin dependency = mock(Plugin.class);
        PluginDescriptionFile description = mock(PluginDescriptionFile.class);

        when(owner.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(pluginManager.getPlugin("CommandAPI")).thenReturn(dependency);
        when(dependency.isEnabled()).thenReturn(true);
        when(dependency.getDescription()).thenReturn(description);
        when(description.getVersion()).thenReturn("11.1.3");

        BukkitDependencyProbe.ProbeResult result = BukkitDependencyProbe.probe(
                owner,
                "CommandAPI",
                "11.1.0",
                null
        );

        assertTrue(result.available());
        assertEquals("11.1.3", result.installedVersion());
    }
}
