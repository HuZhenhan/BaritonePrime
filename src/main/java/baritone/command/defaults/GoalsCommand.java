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

package baritone.command.defaults;

import baritone.Baritone;
import baritone.api.IBaritone;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandException;
import baritone.api.command.exception.CommandInvalidStateException;
import baritone.api.command.helpers.TabCompleteHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.GoalQueueEntry;
import baritone.api.process.ICustomGoalProcess;
import baritone.command.helpers.GoalListParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static baritone.api.command.IBaritoneChatControl.FORCE_COMMAND_PREFIX;

public class GoalsCommand extends Command {

    private final Deque<GoalQueueEntry> deletedGoals = new ArrayDeque<>();

    public GoalsCommand(IBaritone baritone) {
        super(baritone, "goals", "goalqueue", "gq");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        ICustomGoalProcess goalProcess = baritone.getCustomGoalProcess();
        Action action = args.hasAny() ? Action.getByName(args.getString()) : Action.LIST;
        if (action == null) {
            throw new CommandInvalidStateException("Unknown goals action");
        }
        switch (action) {
            case LIST:
                args.requireMax(0);
                listGoals(label, goalProcess);
                break;
            case ADD:
                List<Goal> goals = GoalListParser.parse(baritone.getCommandManager(), args, ctx.playerFeet());
                goalProcess.appendGoals(goals);
                logDirect(String.format("Added %d goal%s", goals.size(), goals.size() == 1 ? "" : "s"));
                break;
            case REMOVE:
                args.requireExactly(1);
                GoalQueueEntry removed = goalProcess.removeGoal(resolveEntry(goalProcess, args.getString()).id());
                if (removed == null) {
                    throw new CommandInvalidStateException("Goal not found");
                }
                deletedGoals.addLast(removed);
                logDirect(restoreComponent(label, "Goal removed. Click to undo."));
                break;
            case CURRENT:
                args.requireExactly(1);
                if (!goalProcess.setCurrentGoal(resolveEntry(goalProcess, args.getString()).id())) {
                    throw new CommandInvalidStateException("Goal not found");
                }
                logDirect("Current goal updated");
                break;
            case MOVE:
                args.requireExactly(2);
                GoalQueueEntry entry = resolveEntry(goalProcess, args.getString());
                int targetIndex = args.getAs(Integer.class) - 1;
                if (!goalProcess.moveGoal(entry.id(), targetIndex)) {
                    throw new CommandInvalidStateException("Target position is out of range");
                }
                logDirect("Goal moved");
                break;
            case UP:
                args.requireExactly(1);
                moveRelative(goalProcess, args.getString(), -1);
                logDirect("Goal moved up");
                break;
            case DOWN:
                args.requireExactly(1);
                moveRelative(goalProcess, args.getString(), 1);
                logDirect("Goal moved down");
                break;
            case CLEAR:
                args.requireMax(0);
                deletedGoals.addAll(goalProcess.getGoalQueue());
                goalProcess.clearGoalQueue();
                logDirect(restoreComponent(label, "Goals cleared. Click to restore the last removed goal."));
                break;
            case UNDO:
                args.requireMax(0);
                if (deletedGoals.isEmpty()) {
                    throw new CommandInvalidStateException("There is no deleted goal to restore");
                }
                goalProcess.appendGoal(deletedGoals.removeLast().goal());
                logDirect("Restored goal");
                break;
            default:
                throw new IllegalStateException("Unexpected action " + action);
        }
    }

    private void listGoals(String label, ICustomGoalProcess goalProcess) {
        List<GoalQueueEntry> queue = goalProcess.getGoalQueue();
        if (queue.isEmpty()) {
            logDirect("No goals queued");
            return;
        }
        logDirect(String.format("Goal queue (%d):", queue.size()));
        for (int i = 0; i < queue.size(); i++) {
            logDirect(goalComponent(label, queue.get(i), i, queue.size()));
        }
    }

    private MutableComponent goalComponent(String label, GoalQueueEntry entry, int index, int size) {
        MutableComponent line = Component.literal(String.format("#%d %s ", index + 1, roleName(index)));
        line.setStyle(line.getStyle().withColor(index == 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY));

        MutableComponent goal = Component.literal(entry.displayName());
        goal.setStyle(goal.getStyle()
                .withColor(index == 0 ? ChatFormatting.WHITE : ChatFormatting.GRAY)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to set current goal")))
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, FORCE_COMMAND_PREFIX + label + " current id:" + entry.id())));
        line.append(goal);

        line.append(action(label, " [current]", "current", entry.id(), "Set as current goal"));
        line.append(action(label, " [delete]", "remove", entry.id(), "Delete this goal"));
        if (index > 0) {
            line.append(action(label, " [up]", "up", entry.id(), "Move up"));
        }
        if (index + 1 < size) {
            line.append(action(label, " [down]", "down", entry.id(), "Move down"));
        }

        MutableComponent copy = Component.literal(" [copy]");
        copy.setStyle(copy.getStyle()
                .withColor(ChatFormatting.AQUA)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy goal command")))
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, Baritone.settings().prefix.value + "goal " + entry.displayName())));
        line.append(copy);
        return line;
    }

    private MutableComponent action(String label, String text, String action, long id, String hover) {
        MutableComponent component = Component.literal(text);
        component.setStyle(component.getStyle()
                .withColor(ChatFormatting.YELLOW)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hover)))
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, FORCE_COMMAND_PREFIX + label + " " + action + " id:" + id)));
        return component;
    }

    private MutableComponent restoreComponent(String label, String text) {
        MutableComponent component = Component.literal(text);
        component.setStyle(component.getStyle()
                .withColor(ChatFormatting.GRAY)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, FORCE_COMMAND_PREFIX + label + " undo")));
        return component;
    }

    private String roleName(int index) {
        if (index == 0) {
            return "Current";
        }
        if (index == 1) {
            return "Next";
        }
        return "Queued";
    }

    private GoalQueueEntry resolveEntry(ICustomGoalProcess goalProcess, String token) throws CommandException {
        List<GoalQueueEntry> queue = goalProcess.getGoalQueue();
        if (token.startsWith("id:")) {
            long id;
            try {
                id = Long.parseLong(token.substring(3));
            } catch (NumberFormatException ex) {
                throw new CommandInvalidStateException("Goal id must be a number");
            }
            for (GoalQueueEntry entry : queue) {
                if (entry.id() == id) {
                    return entry;
                }
            }
            throw new CommandInvalidStateException("Goal not found");
        }
        int number;
        try {
            number = Integer.parseInt(token);
        } catch (NumberFormatException ex) {
            throw new CommandInvalidStateException("Goal id or index must be a number");
        }
        if (number >= 1 && number <= queue.size()) {
            return queue.get(number - 1);
        }
        for (GoalQueueEntry entry : queue) {
            if (entry.id() == number) {
                return entry;
            }
        }
        throw new CommandInvalidStateException("Goal not found");
    }

    private void moveRelative(ICustomGoalProcess goalProcess, String token, int offset) throws CommandException {
        List<GoalQueueEntry> queue = goalProcess.getGoalQueue();
        GoalQueueEntry entry = resolveEntry(goalProcess, token);
        int currentIndex = queue.indexOf(entry);
        if (!goalProcess.moveGoal(entry.id(), currentIndex + offset)) {
            throw new CommandInvalidStateException("Target position is out of range");
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasExactlyOne()) {
            return new TabCompleteHelper()
                    .append(Action.getAllNames())
                    .sortAlphabetically()
                    .filterPrefix(args.getString())
                    .stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Manage goal queue";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "The goals command manages Baritone's goal queue.",
                "",
                "Usage:",
                "> goals [list] - List queued goals",
                "> goals add <coords>[, <coords>...] - Add one or more goals",
                "> goals current <index/id> - Set a queued goal as current",
                "> goals remove <index/id> - Delete a queued goal",
                "> goals move <index/id> <position> - Move a queued goal",
                "> goals up <index/id> - Move a queued goal up",
                "> goals down <index/id> - Move a queued goal down",
                "> goals clear - Clear all queued goals",
                "> goals undo - Restore the last deleted goal"
        );
    }

    private enum Action {
        LIST("list", "l"),
        ADD("add", "a"),
        REMOVE("remove", "delete", "rm", "d"),
        CURRENT("current", "set", "select", "c"),
        MOVE("move", "m"),
        UP("up", "u"),
        DOWN("down"),
        CLEAR("clear"),
        UNDO("undo", "restore");

        private final String[] names;

        Action(String... names) {
            this.names = names;
        }

        static Action getByName(String name) {
            for (Action action : values()) {
                for (String alias : action.names) {
                    if (alias.equals(name.toLowerCase(Locale.US))) {
                        return action;
                    }
                }
            }
            return null;
        }

        static String[] getAllNames() {
            return Stream.of(values())
                    .flatMap(action -> Arrays.stream(action.names))
                    .toArray(String[]::new);
        }
    }
}