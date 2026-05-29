/*
 * This file is part of Baritone.
 */

package baritone.integration.minimap;

import baritone.Baritone;
import baritone.api.process.GoalQueueEntry;

import java.util.List;

public interface GoalMinimapBridge {

    void sync(Baritone baritone, List<GoalQueueEntry> goalQueue);

    void clear();
}