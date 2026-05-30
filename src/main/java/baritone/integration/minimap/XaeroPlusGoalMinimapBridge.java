/*
 * This file is part of Baritone.
 */

package baritone.integration.minimap;

import java.lang.reflect.Method;

public final class XaeroPlusGoalMinimapBridge extends ReflectiveGoalMinimapBridge {

    @Override
    protected int firstSyncedIndex() {
        return 0;
    }

    @Override
    protected Object createWaypoint(int x, int y, int z, String name, String initials, boolean isCurrentGoal)
            throws ReflectiveOperationException {
        Class<?> colorClass = Class.forName("xaero.hud.minimap.waypoint.WaypointColor");
        Method create = Class.forName("xaeroplus.feature.extensions.SyncedWaypoint").getMethod(
                "create",
                int.class,
                int.class,
                int.class,
                String.class,
                String.class,
                colorClass
        );
        Object color = isCurrentGoal
                ? enumValue("xaero.hud.minimap.waypoint.WaypointColor", "GREEN", "LIME", "BRIGHT_GREEN", "LIGHT_GREEN", "DARK_GREEN")
                : enumValue("xaero.hud.minimap.waypoint.WaypointColor", "DARK_GRAY", "GRAY", "BLACK");
        return create.invoke(null, x, y, z, name, initials, color);
    }
}