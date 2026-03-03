package dev.patric.commonlib.api.port;

import dev.patric.commonlib.api.port.options.PasteOptions;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NewPortsContractTest {

    @Test
    void npcPortContractMatchesExpectedSignatures() throws Exception {
        assertMethod(NpcPort.class, "spawn", UUID.class, String.class, Location.class, String.class);
        assertMethod(NpcPort.class, "despawn", boolean.class, UUID.class);
        assertMethod(NpcPort.class, "updateDisplayName", boolean.class, UUID.class, String.class);
        assertMethod(NpcPort.class, "teleport", boolean.class, UUID.class, Location.class);
    }

    @Test
    void hologramPortContractMatchesExpectedSignatures() throws Exception {
        assertMethod(HologramPort.class, "create", UUID.class, String.class, Location.class, List.class);
        assertMethod(HologramPort.class, "updateLines", boolean.class, UUID.class, List.class);
        assertMethod(HologramPort.class, "move", boolean.class, UUID.class, Location.class);
        assertMethod(HologramPort.class, "delete", boolean.class, UUID.class);
    }

    @Test
    void claimsPortContractMatchesExpectedSignatures() throws Exception {
        assertMethod(ClaimsPort.class, "isInsideClaim", boolean.class, UUID.class, Location.class);
        assertMethod(ClaimsPort.class, "claimIdAt", Optional.class, Location.class);
        assertMethod(ClaimsPort.class, "hasBuildPermission", boolean.class, UUID.class, String.class);
        assertMethod(ClaimsPort.class, "hasCombatPermission", boolean.class, UUID.class, String.class);
    }

    @Test
    void schematicPortContractMatchesExpectedSignatures() throws Exception {
        assertMethod(SchematicPort.class, "paste", CompletableFuture.class, String.class, Location.class, PasteOptions.class);
        assertMethod(SchematicPort.class, "resetRegion", CompletableFuture.class, String.class, String.class, PasteOptions.class);
    }

    private static void assertMethod(Class<?> type, String methodName, Class<?> returnType, Class<?>... parameters)
            throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameters);
        assertEquals(returnType, method.getReturnType());
    }
}
