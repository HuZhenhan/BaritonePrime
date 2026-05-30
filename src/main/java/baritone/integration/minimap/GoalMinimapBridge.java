/*
 * This file is part of Baritone.
 */

package baritone.integration.minimap;

import baritone.Baritone;
import baritone.api.process.GoalQueueEntry;

import java.util.List;

public interface GoalMinimapBridge {

    /**
     * Full-queue sync: current goal (index 0) → green, future goals → gray.
     */
    void sync(Baritone baritone, List<GoalQueueEntry> goalQueue);

    /**
     * Remove a single waypoint by its queue-entry id, then refresh.
     */
    void removeById(long id);

    /**
     * Force a full re-sync regardless of cached signature.
     */
    void refresh(Baritone baritone, List<GoalQueueEntry> goalQueue);

    void clear();
}