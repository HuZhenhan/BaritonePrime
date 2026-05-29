/*
 * This file is part of Baritone.
 */

package baritone.integration.minimap;

public final class XaeroDirectGoalMinimapBridge extends ReflectiveGoalMinimapBridge {

    @Override
    protected int firstSyncedIndex() {
        return 0;
    }

    @Override
    protected Object createWaypoint(int x, int y, int z, String name, String initials) throws ReflectiveOperationException {
        Object color = enumValue("xaero.hud.minimap.waypoint.WaypointColor", "Baritone Goal".equals(name) ? "GREEN" : "DARK_GRAY", "GRAY", "BLACK");
        Object purpose = enumValue("xaero.hud.minimap.waypoint.WaypointPurpose", "NORMAL");
        try {
            return construct(
                    "xaero.common.minimap.waypoints.Waypoint",
                    new Class<?>[]{int.class, int.class, int.class, String.class, String.class, color.getClass(), purpose.getClass()},
                    x,
                    y,
                    z,
                    name,
                    initials,
                    color,
                    purpose
            );
        } catch (NoSuchMethodException ignored) {
            return construct(
                    "xaero.common.minimap.waypoints.Waypoint",
                    new Class<?>[]{int.class, int.class, int.class, String.class, String.class, color.getClass(), purpose.getClass(), boolean.class, boolean.class},
                    x,
                    y,
                    z,
                    name,
                    initials,
                    color,
                    purpose,
                    true,
                    true
            );
        }
    }
}