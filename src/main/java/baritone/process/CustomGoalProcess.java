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

package baritone.process;

import baritone.Baritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.GoalQueueEntry;
import baritone.api.process.ICustomGoalProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.utils.BaritoneProcessHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * As set by ExampleBaritoneControl or something idk
 *
 * @author leijurv
 */
public final class CustomGoalProcess extends BaritoneProcessHelper implements ICustomGoalProcess {

    private final List<GoalQueueEntry> goalQueue = new ArrayList<>();
    private long nextGoalId;

    /**
     * The most recent goal. Not invalidated upon {@link #onLostControl()}
     */
    private Goal mostRecentGoal;

    /**
     * The current process state.
     *
     * @see State
     */
    private State state = State.NONE;

    public CustomGoalProcess(Baritone baritone) {
        super(baritone);
    }

    @Override
    public void setGoal(Goal goal) {
        if (goal == null) {
            clearGoalQueue();
            return;
        }
        appendGoal(goal);
        if (baritone.getElytraProcess().isActive()) {
            baritone.getElytraProcess().pathTo(goal);
        }
    }

    @Override
    public GoalQueueEntry appendGoal(Goal goal) {
        GoalQueueEntry entry = new GoalQueueEntry(++this.nextGoalId, goal, System.currentTimeMillis(), goal.toString());
        this.goalQueue.add(entry);
        this.mostRecentGoal = goal;
        if (this.state == State.NONE) {
            this.state = State.GOAL_SET;
        }
        return entry;
    }

    @Override
    public List<GoalQueueEntry> appendGoals(Collection<Goal> goals) {
        List<GoalQueueEntry> entries = new ArrayList<>();
        for (Goal goal : goals) {
            entries.add(appendGoal(goal));
        }
        return entries;
    }

    @Override
    public List<GoalQueueEntry> getGoalQueue() {
        return Collections.unmodifiableList(new ArrayList<>(this.goalQueue));
    }

    @Override
    public GoalQueueEntry removeGoal(long id) {
        for (int i = 0; i < this.goalQueue.size(); i++) {
            GoalQueueEntry entry = this.goalQueue.get(i);
            if (entry.id() == id) {
                this.goalQueue.remove(i);
                if (i == 0 || this.goalQueue.isEmpty()) {
                    syncStateAfterQueueChange();
                }
                return entry;
            }
        }
        return null;
    }

    @Override
    public boolean moveGoal(long id, int index) {
        if (index < 0 || index >= this.goalQueue.size()) {
            return false;
        }
        for (int i = 0; i < this.goalQueue.size(); i++) {
            GoalQueueEntry entry = this.goalQueue.get(i);
            if (entry.id() == id) {
                this.goalQueue.remove(i);
                this.goalQueue.add(index, entry);
                if (i == 0 || index == 0) {
                    syncStateAfterQueueChange();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setCurrentGoal(long id) {
        return moveGoal(id, 0);
    }

    @Override
    public void clearGoalQueue() {
        this.goalQueue.clear();
        this.mostRecentGoal = null;
        this.state = State.NONE;
    }

    @Override
    public void setGoalAndPath(Goal goal) {
        if (goal == null) {
            clearGoalQueue();
            return;
        }
        GoalQueueEntry entry = appendGoal(goal);
        setCurrentGoal(entry.id());
        path();
    }

    @Override
    public void path() {
        if (getGoal() != null) {
            this.state = State.PATH_REQUESTED;
        }
    }

    @Override
    public Goal getGoal() {
        return this.goalQueue.isEmpty() ? null : this.goalQueue.get(0).goal();
    }

    @Override
    public Goal mostRecentGoal() {
        Goal current = getGoal();
        return current != null ? current : this.mostRecentGoal;
    }

    @Override
    public boolean isActive() {
        return this.state != State.NONE;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        Goal goal = getGoal();
        switch (this.state) {
            case GOAL_SET:
                return new PathingCommand(goal, PathingCommandType.CANCEL_AND_SET_GOAL);
            case PATH_REQUESTED:
                PathingCommand ret = new PathingCommand(goal, PathingCommandType.FORCE_REVALIDATE_GOAL_AND_PATH);
                this.state = State.EXECUTING;
                return ret;
            case EXECUTING:
                if (calcFailed) {
                    onLostControl();
                    return new PathingCommand(goal, PathingCommandType.CANCEL_AND_SET_GOAL);
                }
                if (goal == null || (goal.isInGoal(ctx.playerFeet()) && goal.isInGoal(baritone.getPathingBehavior().pathStart()))) {
                    advanceQueueAfterArrival();
                    if (Baritone.settings().disconnectOnArrival.value) {
                        ctx.world().disconnect();
                    }
                    if (Baritone.settings().notificationOnPathComplete.value) {
                        logNotification("Pathing complete", false);
                    }
                    return new PathingCommand(getGoal(), PathingCommandType.CANCEL_AND_SET_GOAL);
                }
                return new PathingCommand(goal, PathingCommandType.SET_GOAL_AND_PATH);
            default:
                throw new IllegalStateException("Unexpected state " + this.state);
        }
    }

    private void advanceQueueAfterArrival() {
        Goal arrivedGoal = getGoal();
        if (!this.goalQueue.isEmpty()) {
            this.goalQueue.remove(0);
        }
        this.mostRecentGoal = getGoal() != null ? getGoal() : arrivedGoal;
        syncStateAfterQueueChange();
    }

    private void syncStateAfterQueueChange() {
        if (this.goalQueue.isEmpty()) {
            this.state = State.NONE;
            return;
        }
        this.mostRecentGoal = getGoal();
        this.state = State.GOAL_SET;
    }

    @Override
    public void onLostControl() {
        this.state = State.NONE;
    }

    @Override
    public String displayName0() {
        return "Custom Goal " + getGoal();
    }

    protected enum State {
        NONE,
        GOAL_SET,
        PATH_REQUESTED,
        EXECUTING
    }
}