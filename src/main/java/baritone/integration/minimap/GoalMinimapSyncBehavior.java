/*
 * This file is part of Baritone.
 */

package baritone.integration.minimap;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.event.events.TickEvent;
import baritone.api.process.GoalQueueEntry;
import baritone.behavior.Behavior;

import java.util.List;

public final class GoalMinimapSyncBehavior extends Behavior {

    private final GoalMinimapBridge bridge = GoalMinimapBridgeFactory.create();
    private String lastSignature = "";
    private int ticks;

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
        if (!signature.equals(this.lastSignature) || ++this.ticks >= 20) {
            this.ticks = 0;
            this.lastSignature = signature;
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