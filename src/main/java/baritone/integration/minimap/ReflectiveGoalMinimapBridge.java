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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

abstract class ReflectiveGoalMinimapBridge implements GoalMinimapBridge {

    // Maps queue-entry id → waypoint object for stable per-id removal
    private final java.util.Map<Long, Object> waypointById = new java.util.LinkedHashMap<>();
    private final List<Object> createdWaypoints = new ArrayList<>();
    private Object waypointSet;
    private String lastSignature = "";

    private static void dbg(String hypothesisId, String location, String message, String dataJson) {
        try {
            long timestamp = System.currentTimeMillis();
            String safeMessage = message == null ? "" : message.replace("\\", "\\\\").replace("\"", "\\\"");
            String safeLocation = location == null ? "" : location.replace("\\", "\\\\").replace("\"", "\\\"");
            String safeHyp = hypothesisId == null ? "" : hypothesisId.replace("\\", "\\\\").replace("\"", "\\\"");
            String safeData = dataJson == null ? "null" : dataJson;
            String json = "{"
                    + "\"sessionId\":\"da64ea\","
                    + "\"runId\":\"debug\","
                    + "\"hypothesisId\":\"" + safeHyp + "\","
                    + "\"location\":\"" + safeLocation + "\","
                    + "\"message\":\"" + safeMessage + "\","
                    + "\"data\":" + safeData + ","
                    + "\"timestamp\":" + timestamp
                    + "}\n";

            URL url = new URL("http://127.0.0.1:7359/ingest/56767ac9-a301-457c-bae6-3d2f400d39b2");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Debug-Session-Id", "da64ea");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            conn.getResponseCode();
        } catch (Exception ignored) {
        }
    }

    @Override
    public final void sync(Baritone baritone, List<GoalQueueEntry> goalQueue) {
        try {
            Object currentWaypointSet = currentWaypointSet();
            if (currentWaypointSet == null) {
                clear();
                return;
            }
            String signature = System.identityHashCode(currentWaypointSet) + ":" + queueSignature(baritone.getPlayerContext(), goalQueue);
            boolean skip = signature.equals(this.lastSignature);
            // #region agent log
            dbg(
                    "H2",
                    "ReflectiveGoalMinimapBridge.sync",
                    skip ? "bridge_skip_due_signature" : "bridge_rebuild_due_signature_change",
                    "{\"signature\":\"" + signature.replace("\\", "\\\\").replace("\"", "\\\"") + "\","
                            + "\"lastSignature\":\"" + (this.lastSignature == null ? "" : this.lastSignature.replace("\\", "\\\\").replace("\"", "\\\"")) + "\"}"
            );
            // #endregion
            if (skip) {
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
    public final void removeById(long id) {
        // #region agent log
        dbg("H3", "ReflectiveGoalMinimapBridge.removeById", "called", "{\"id\":" + id + "}");
        // #endregion
        try {
            if (this.waypointSet == null) {
                return;
            }
            Object waypoint = this.waypointById.remove(id);
            if (waypoint == null) {
                return;
            }

            Method remove = this.waypointSet.getClass().getMethod("remove", waypointClass());
            remove.invoke(this.waypointSet, waypoint);
            this.createdWaypoints.remove(waypoint);
            refreshWaypoints();
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Fallback: inconsistent state will be corrected on next sync().
        }
    }

    @Override
    public final void refresh(Baritone baritone, List<GoalQueueEntry> goalQueue) {
        clear();
        sync(baritone, goalQueue);
    }

    @Override
    public final void clear() {
        try {
            clearFromSet(this.waypointSet);
            refreshWaypoints();
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            this.createdWaypoints.clear();
            this.waypointById.clear();
        }
        this.waypointSet = null;
        this.lastSignature = "";
    }

    protected abstract int firstSyncedIndex();

    protected abstract Object createWaypoint(int x, int y, int z, String name, String initials, boolean isCurrentGoal)
            throws ReflectiveOperationException;

    protected Class<?> waypointClass() throws ClassNotFoundException {
        return Class.forName("xaero.common.minimap.waypoints.Waypoint");
    }

    private void addQueueWaypoints(IPlayerContext ctx, Object waypointSet, List<GoalQueueEntry> goalQueue) throws ReflectiveOperationException {
        int firstIndex = firstSyncedIndex();
        for (int queueIndex = 0; queueIndex < goalQueue.size(); queueIndex++) {
            GoalQueueEntry entry = goalQueue.get(queueIndex);
            int signedIndex = firstIndex + queueIndex;

            BlockPos pos = goalPos(ctx, entry.goal());
            if (pos == null) {
                continue;
            }
            String name = "Baritone Goal";
            String initials = "B";
            boolean isCurrentGoal = signedIndex == firstIndex;
            Object waypoint = createWaypoint(pos.getX(), pos.getY(), pos.getZ(), name, initials, isCurrentGoal);
            waypointSet.getClass().getMethod("add", waypointClass()).invoke(waypointSet, waypoint);

            this.waypointById.put(entry.id(), waypoint);
            this.createdWaypoints.add(waypoint);
        }
    }

    private void clearFromSet(Object set) throws ReflectiveOperationException {
        if (set == null) {
            this.createdWaypoints.clear();
            this.waypointById.clear();
            return;
        }
        Method remove = set.getClass().getMethod("remove", waypointClass());
        for (Object waypoint : this.createdWaypoints) {
            remove.invoke(set, waypoint);
        }
        this.createdWaypoints.clear();
        this.waypointById.clear();
    }

    private String queueSignature(IPlayerContext ctx, List<GoalQueueEntry> goalQueue) {
        StringBuilder builder = new StringBuilder();
        if (ctx.world() != null) {
            builder.append(ctx.world().dimension().location());
        }
        int firstIndex = firstSyncedIndex();
        for (int queueIndex = 0; queueIndex < goalQueue.size(); queueIndex++) {
            int signedIndex = firstIndex + queueIndex;
            BlockPos pos = goalPos(ctx, goalQueue.get(queueIndex).goal());
            if (pos != null) {
                builder.append('|').append(signedIndex).append(':').append(pos.getX()).append(',').append(pos.getY()).append(',').append(pos.getZ());
            } else {
                builder.append('|').append(signedIndex).append(":null");
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