/*
 * This file is part of Baritone.
 */

package baritone.integration.minimap;

import baritone.Baritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalComposite;
import baritone.api.pathing.goals.GoalInverted;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.process.GoalQueueEntry;
import baritone.api.utils.IPlayerContext;
import baritone.api.utils.interfaces.IGoalRenderPos;
import net.minecraft.core.BlockPos;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

abstract class ReflectiveGoalMinimapBridge implements GoalMinimapBridge {

    private final List<Object> createdWaypoints = new ArrayList<>();
    private Object waypointSet;
    private String lastSignature = "";

    @Override
    public final void sync(Baritone baritone, List<GoalQueueEntry> goalQueue) {
        try {
            Object currentWaypointSet = currentWaypointSet();
            if (currentWaypointSet == null) {
                clear();
                return;
            }
            String signature = System.identityHashCode(currentWaypointSet) + ":" + queueSignature(baritone.getPlayerContext(), goalQueue);
            if (signature.equals(this.lastSignature)) {
                return;
            }
            clearFromSet(this.waypointSet);
            this.waypointSet = currentWaypointSet;
            this.lastSignature = signature;
            addQueueWaypoints(baritone.getPlayerContext(), currentWaypointSet, goalQueue);
            refreshWaypoints();
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            clear();
        }
    }

    @Override
    public final void clear() {
        try {
            clearFromSet(this.waypointSet);
            refreshWaypoints();
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            this.createdWaypoints.clear();
        }
        this.waypointSet = null;
        this.lastSignature = "";
    }

    protected abstract int firstSyncedIndex();

    protected abstract Object createWaypoint(int x, int y, int z, String name, String initials) throws ReflectiveOperationException;

    protected Class<?> waypointClass() throws ClassNotFoundException {
        return Class.forName("xaero.common.minimap.waypoints.Waypoint");
    }

    private void addQueueWaypoints(IPlayerContext ctx, Object waypointSet, List<GoalQueueEntry> goalQueue) throws ReflectiveOperationException {
        int firstIndex = firstSyncedIndex();
        for (int i = firstIndex; i < goalQueue.size(); i++) {
            BlockPos pos = goalPos(ctx, goalQueue.get(i).goal());
            if (pos == null) {
                continue;
            }
            int order = i + 1;
            String name = firstIndex == 0 && i == 0 ? "Baritone Goal" : "Baritone Queue #" + order;
            String initials = firstIndex == 0 && i == 0 ? "B" : "B" + order;
            Object waypoint = createWaypoint(pos.getX(), pos.getY(), pos.getZ(), name, initials);
            waypointSet.getClass().getMethod("add", waypointClass()).invoke(waypointSet, waypoint);
            this.createdWaypoints.add(waypoint);
        }
    }

    private void clearFromSet(Object set) throws ReflectiveOperationException {
        if (set == null) {
            this.createdWaypoints.clear();
            return;
        }
        Method remove = set.getClass().getMethod("remove", waypointClass());
        for (Object waypoint : this.createdWaypoints) {
            remove.invoke(set, waypoint);
        }
        this.createdWaypoints.clear();
    }

    private String queueSignature(IPlayerContext ctx, List<GoalQueueEntry> goalQueue) {
        StringBuilder builder = new StringBuilder();
        if (ctx.world() != null) {
            builder.append(ctx.world().dimension().location());
        }
        int firstIndex = firstSyncedIndex();
        for (int i = firstIndex; i < goalQueue.size(); i++) {
            BlockPos pos = goalPos(ctx, goalQueue.get(i).goal());
            if (pos != null) {
                builder.append('|').append(i).append(':').append(pos.getX()).append(',').append(pos.getY()).append(',').append(pos.getZ());
            }
        }
        return builder.toString();
    }

    private Object currentWaypointSet() throws ReflectiveOperationException {
        Class<?> modulesClass = Class.forName("xaero.hud.minimap.BuiltInHudModules");
        Object minimap = modulesClass.getField("MINIMAP").get(null);
        Object session = minimap.getClass().getMethod("getCurrentSession").invoke(minimap);
        if (session == null) {
            return null;
        }
        Object worldManager = session.getClass().getMethod("getWorldManager").invoke(session);
        if (worldManager == null) {
            return null;
        }
        Object currentWorld = worldManager.getClass().getMethod("getCurrentWorld").invoke(worldManager);
        if (currentWorld == null) {
            return null;
        }
        return currentWorld.getClass().getMethod("getCurrentWaypointSet").invoke(currentWorld);
    }

    protected Object enumValue(String className, String... names) throws ClassNotFoundException {
        Class<?> enumClass = Class.forName(className);
        for (String name : names) {
            try {
                return Enum.valueOf((Class) enumClass, name);
            } catch (IllegalArgumentException ignored) {}
        }
        Object[] values = enumClass.getEnumConstants();
        return values.length == 0 ? null : values[0];
    }

    protected Object construct(String className, Class<?>[] parameterTypes, Object... args) throws ReflectiveOperationException {
        Constructor<?> constructor = Class.forName(className).getConstructor(parameterTypes);
        return constructor.newInstance(args);
    }

    private void refreshWaypoints() throws ReflectiveOperationException {
        try {
            Class<?> supportModsClass = Class.forName("xaero.map.mods.SupportMods");
            Field field = supportModsClass.getField("xaeroMinimap");
            Object xaeroMinimap = field.get(null);
            if (xaeroMinimap != null) {
                xaeroMinimap.getClass().getMethod("requestWaypointsRefresh").invoke(xaeroMinimap);
            }
        } catch (ClassNotFoundException ignored) {}
    }

    private static BlockPos goalPos(IPlayerContext ctx, Goal goal) {
        if (goal instanceof IGoalRenderPos) {
            return ((IGoalRenderPos) goal).getGoalPos();
        }
        if (goal instanceof GoalXZ) {
            GoalXZ goalXZ = (GoalXZ) goal;
            return new BlockPos(goalXZ.getX(), 64, goalXZ.getZ());
        }
        if (goal instanceof GoalComposite) {
            for (Goal child : ((GoalComposite) goal).goals()) {
                BlockPos pos = goalPos(ctx, child);
                if (pos != null) {
                    return pos;
                }
            }
        }
        if (goal instanceof GoalInverted) {
            return goalPos(ctx, ((GoalInverted) goal).origin);
        }
        return null;
    }
}