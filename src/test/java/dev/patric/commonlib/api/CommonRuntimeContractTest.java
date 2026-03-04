package dev.patric.commonlib.api;

import dev.patric.commonlib.api.hud.BossBarService;
import dev.patric.commonlib.api.hud.ScoreboardSessionService;
import dev.patric.commonlib.api.match.MatchEngineService;
import dev.patric.commonlib.api.arena.ArenaService;
import dev.patric.commonlib.api.persistence.SchemaMigrationService;
import dev.patric.commonlib.api.persistence.SqlPersistencePort;
import dev.patric.commonlib.api.persistence.YamlPersistencePort;
import dev.patric.commonlib.api.team.PartyService;
import dev.patric.commonlib.api.team.TeamService;
import dev.patric.commonlib.testsupport.TestPlugin;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonRuntimeContractTest {

    private ServerMock server;
    private TestPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestPlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void runtimeExposesCoreServicesAndLifecycleContract() {
        List<String> calls = new ArrayList<>();

        CommonRuntime runtime = CommonRuntime.builder(plugin)
                .component(new CommonComponent() {
                    @Override
                    public String id() {
                        return "contract";
                    }

                    @Override
                    public void onLoad(CommonContext context) {
                        calls.add("load");
                    }

                    @Override
                    public void onEnable(CommonContext context) {
                        calls.add("enable");
                    }

                    @Override
                    public void onDisable(CommonContext context) {
                        calls.add("disable");
                    }
                })
                .build();

        runtime.onLoad();
        runtime.onEnable();

        assertNotNull(runtime.services().require(ServiceRegistry.class));
        assertNotNull(runtime.services().require(CommonScheduler.class));
        assertNotNull(runtime.services().require(EventRouter.class));
        assertNotNull(runtime.services().require(RuntimeLogger.class));
        assertNotNull(runtime.services().require(ScoreboardSessionService.class));
        assertNotNull(runtime.services().require(BossBarService.class));
        assertNotNull(runtime.services().require(MatchEngineService.class));
        assertNotNull(runtime.services().require(ArenaService.class));
        assertNotNull(runtime.services().require(TeamService.class));
        assertNotNull(runtime.services().require(PartyService.class));
        assertNotNull(runtime.services().require(YamlPersistencePort.class));
        assertNotNull(runtime.services().require(SqlPersistencePort.class));
        assertNotNull(runtime.services().require(SchemaMigrationService.class));

        runtime.onDisable();

        assertEquals(List.of("load", "enable", "disable"), calls);
    }
}
