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

package baritone.command.helpers;

import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.argument.ICommandArgument;
import baritone.api.command.datatypes.RelativeGoal;
import baritone.api.command.exception.CommandException;
import baritone.api.command.exception.CommandInvalidStateException;
import baritone.api.command.manager.ICommandManager;
import baritone.api.pathing.goals.Goal;
import baritone.api.utils.BetterBlockPos;
import baritone.command.argument.ArgConsumer;
import baritone.command.argument.CommandArguments;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public final class GoalListParser {

    private GoalListParser() {}

    public static List<Goal> parse(ICommandManager manager, IArgConsumer args, BetterBlockPos origin) throws CommandException {
        List<String> tokens = normalizeCommaTokens(args.getArgs());
        if (!tokens.contains(",")) {
            ArgConsumer single = new ArgConsumer(manager, args.getArgs());
            Goal goal = single.getDatatypePost(RelativeGoal.INSTANCE, origin);
            single.requireMax(0);
            List<Goal> goals = new ArrayList<>();
            goals.add(goal);
            return goals;
        }

        List<Goal> goals = new ArrayList<>();
        List<String> group = new ArrayList<>();
        int groupIndex = 1;
        for (String token : tokens) {
            if (token.equals(",")) {
                goals.add(parseGroup(manager, group, origin, groupIndex));
                group.clear();
                groupIndex++;
            } else {
                group.add(token);
            }
        }
        goals.add(parseGroup(manager, group, origin, groupIndex));
        return goals;
    }

    private static Goal parseGroup(ICommandManager manager, List<String> group, BetterBlockPos origin, int groupIndex) throws CommandException {
        if (group.isEmpty()) {
            throw new CommandInvalidStateException(String.format("第 %d 组坐标为空", groupIndex));
        }
        StringJoiner joiner = new StringJoiner(" ");
        group.forEach(joiner::add);
        ArgConsumer consumer = new ArgConsumer(manager, CommandArguments.from(joiner.toString()));
        try {
            Goal goal = consumer.getDatatypePost(RelativeGoal.INSTANCE, origin);
            consumer.requireMax(0);
            return goal;
        } catch (CommandException ex) {
            throw new CommandInvalidStateException(String.format("第 %d 组坐标无法解析: %s", groupIndex, ex.getMessage()));
        }
    }

    private static List<String> normalizeCommaTokens(List<ICommandArgument> args) {
        List<String> tokens = new ArrayList<>();
        for (ICommandArgument arg : args) {
            String value = arg.getValue();
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == ',') {
                    if (current.length() > 0) {
                        tokens.add(current.toString());
                        current.setLength(0);
                    }
                    tokens.add(",");
                } else {
                    current.append(c);
                }
            }
            if (current.length() > 0) {
                tokens.add(current.toString());
            }
        }
        return tokens;
    }
}