package dev.patric.commonlib.api.team;

import java.util.Objects;

/**
 * Team definition used to build match rosters.
 *
 * @param teamId team identifier.
 * @param displayName display name.
 * @param maxMembers max members allowed in this team.
 */
public record TeamDefinition(String teamId, String displayName, int maxMembers) {

    /**
     * Creates a team definition.
     */
    public TeamDefinition {
        teamId = requireText(teamId, "teamId");
        displayName = requireText(displayName, "displayName");
        if (maxMembers <= 0) {
            throw new IllegalArgumentException("maxMembers must be > 0");
        }
    }

    private static String requireText(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
