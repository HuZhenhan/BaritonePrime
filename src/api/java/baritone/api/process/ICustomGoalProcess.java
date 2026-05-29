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

import java.util.Collection;
import java.util.List;

public interface ICustomGoalProcess extends IBaritoneProcess {

    /**
     * Sets the pathing goal
     *
     * @param goal The new goal
     */
    void setGoal(Goal goal);

    /**
     * Starts path calculation and execution.
     */
    void path();

    /**
     * @return The current goal
     */
    Goal getGoal();

    /**
     * @return The most recent set goal, which doesn't invalidate upon {@link #onLostControl()}
     */
    Goal mostRecentGoal();

    /**
     * Adds a goal to the end of the queue.
     *
     * @param goal The goal to append
     * @return The created queue entry
     */
    GoalQueueEntry appendGoal(Goal goal);

    /**
     * Adds goals to the end of the queue.
     *
     * @param goals Goals to append
     * @return The created queue entries
     */
    List<GoalQueueEntry> appendGoals(Collection<Goal> goals);

    /**
     * @return A snapshot of the goal queue. Index 0 is the current goal.
     */
    List<GoalQueueEntry> getGoalQueue();

    /**
     * Removes an entry by id.
     *
     * @param id The entry id
     * @return The removed entry, or null when not found
     */
    GoalQueueEntry removeGoal(long id);

    /**
     * Moves an entry to a zero-based index.
     *
     * @param id The entry id
     * @param index The target zero-based index
     * @return Whether the entry was moved
     */
    boolean moveGoal(long id, int index);

    /**
     * Moves an entry to the front of the queue.
     *
     * @param id The entry id
     * @return Whether the entry was found
     */
    boolean setCurrentGoal(long id);

    /**
     * Clears the full goal queue.
     */
    void clearGoalQueue();

    /**
     * Sets the goal and begins the path execution.
     *
     * @param goal The new goal
     */
    default void setGoalAndPath(Goal goal) {
        this.setGoal(goal);
        this.path();
    }
}
