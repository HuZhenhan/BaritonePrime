/*
 * This file is part of Baritone.
 */

package baritone.integration.minimap;

public final class NoopGoalMinimapBridge implements GoalMinimapBridge {

    @Override
    public void sync(baritone.Baritone baritone, java.util.List<baritone.api.process.GoalQueueEntry> goalQueue) {}

    @Override
    public void removeById(long id) {}

    @Override
    public void refresh(baritone.Baritone baritone, java.util.List<baritone.api.process.GoalQueueEntry> goalQueue) {}

    @Override
    public void clear() {}
}