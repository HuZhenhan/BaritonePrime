/*
 * This file is part of Baritone.
 */

package baritone.integration.minimap;

public final class GoalMinimapBridgeFactory {

    private GoalMinimapBridgeFactory() {}

    public static GoalMinimapBridge create() {
        if (isPresent("xaeroplus.feature.waypoint.WaypointAPI") && isPresent("xaeroplus.feature.extensions.SyncedWaypoint")) {
            return new XaeroPlusGoalMinimapBridge();
        }
        if (isPresent("xaero.hud.minimap.BuiltInHudModules") && isPresent("xaero.common.minimap.waypoints.Waypoint")) {
            return new XaeroDirectGoalMinimapBridge();
        }
        return new NoopGoalMinimapBridge();
    }

    private static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}