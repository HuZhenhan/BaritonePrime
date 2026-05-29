/*
 * This file is part of Baritone.
 */

package baritone.integration.minimap;

import java.lang.reflect.Method;

public final class XaeroPlusGoalMinimapBridge extends ReflectiveGoalMinimapBridge {

    @Override
    protected int firstSyncedIndex() {
        return 1;
    }

    @Override
    protected Object createWaypoint(int x, int y, int z, String name, String initials) throws ReflectiveOperationException {
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
        return create.invoke(null, x, y, z, name, initials, enumValue("xaero.hud.minimap.waypoint.WaypointColor", "DARK_GRAY", "GRAY", "BLACK"));
    }
}