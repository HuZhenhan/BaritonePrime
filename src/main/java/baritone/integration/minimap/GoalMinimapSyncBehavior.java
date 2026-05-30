/*
 * This file is part of Baritone.
 */

package baritone.integration.minimap;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.event.events.TickEvent;
import baritone.api.process.GoalQueueEntry;
import baritone.behavior.Behavior;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public final class GoalMinimapSyncBehavior extends Behavior {

    private final GoalMinimapBridge bridge = GoalMinimapBridgeFactory.create();
    private String lastSignature = "";
    private int ticks;

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
                    + "}";

            URL url = new URL("http://127.0.0.1:7359/ingest/56767ac9-a301-457c-bae6-3d2f400d39b2");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Debug-Session-Id", "da64ea");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            // 不关心响应内容，只要能发出即可
            conn.getResponseCode();
        } catch (Exception ignored) {
        }
    }

    public GoalMinimapSyncBehavior(Baritone baritone) {
        super(baritone);
    }

    @Override
    public void onTick(TickEvent event) {
        if (event.getType() == TickEvent.Type.OUT) {
            return;
        }
        if (!BaritoneAPI.getSettings().syncGoalQueueToXaero.value) {
            this.bridge.clear();
            this.lastSignature = "disabled";
            return;
        }
        List<GoalQueueEntry> goalQueue = this.baritone.getCustomGoalProcess().getGoalQueue();
        String signature = signature(goalQueue);
        boolean changed = !signature.equals(this.lastSignature);
        if (changed || ++this.ticks >= 20) {
            this.ticks = 0;
            this.lastSignature = signature;
            // #region agent log
            dbg(
                    "H1",
                    "GoalMinimapSyncBehavior.onTick",
                    changed ? "sync_trigger_queue_changed" : "sync_trigger_periodic_20t",
                    "{\"signature\":\"" + signature.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}"
            );
            // #endregion
            this.bridge.sync(this.baritone, goalQueue);
        }
    }

    private String signature(List<GoalQueueEntry> goalQueue) {
        StringBuilder builder = new StringBuilder();
        if (this.ctx.world() != null) {
            builder.append(this.ctx.world().dimension().location());
        }
        for (GoalQueueEntry entry : goalQueue) {
            builder.append('|').append(entry.id()).append(':').append(entry.goal());
        }
        return builder.toString();
    }
}