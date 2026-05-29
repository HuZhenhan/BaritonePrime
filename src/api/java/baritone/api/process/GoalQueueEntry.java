/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.api.process;

import baritone.api.pathing.goals.Goal;

public final class GoalQueueEntry {

    private final long id;
    private final Goal goal;
    private final long createdAt;
    private final String displayName;

    public GoalQueueEntry(long id, Goal goal, long createdAt, String displayName) {
        this.id = id;
        this.goal = goal;
        this.createdAt = createdAt;
        this.displayName = displayName;
    }

    public long id() {
        return this.id;
    }

    public Goal goal() {
        return this.goal;
    }

    public long createdAt() {
        return this.createdAt;
    }

    public String displayName() {
        return this.displayName;
    }
}