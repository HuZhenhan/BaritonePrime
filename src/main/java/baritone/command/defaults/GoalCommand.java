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

import baritone.api.IBaritone;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.datatypes.RelativeCoordinate;
import baritone.api.command.exception.CommandException;
import baritone.api.command.helpers.TabCompleteHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.ICustomGoalProcess;
import baritone.api.utils.BetterBlockPos;
import baritone.command.helpers.GoalListParser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GoalCommand extends Command {

    public GoalCommand(IBaritone baritone) {
        super(baritone, "goal");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        ICustomGoalProcess goalProcess = baritone.getCustomGoalProcess();
        if (args.hasAny() && Arrays.asList("reset", "clear", "none").contains(args.peekString())) {
            args.requireMax(1);
            if (goalProcess.getGoal() != null) {
                goalProcess.setGoal(null);
                logDirect("已清空当前目标");
            } else {
                logDirect("当前没有可清空的目标");
            }
        } else {
            BetterBlockPos origin = ctx.playerFeet();
            List<Goal> goals = GoalListParser.parse(baritone.getCommandManager(), args, origin);
            goalProcess.appendGoals(goals);
            if (goals.size() == 1) {
                logDirect(String.format("已添加目标：%s", goals.get(0)));
            } else {
                logDirect(String.format("已添加 %d 个目标", goals.size()));
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        TabCompleteHelper helper = new TabCompleteHelper();
        if (args.hasExactlyOne()) {
            helper.append("reset", "clear", "none", "~");
        } else {
            if (args.hasAtMost(3)) {
                while (args.has(2)) {
                    if (args.peekDatatypeOrNull(RelativeCoordinate.INSTANCE) == null) {
                        break;
                    }
                    args.get();
                    if (!args.has(2)) {
                        helper.append("~");
                    }
                }
            }
        }
        return helper.filterPrefix(args.getString()).stream();
    }

    @Override
    public String getShortDesc() {
        return "设置或清空目标";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "goal 用来设置或清空 Baritone 的目标。",
                "",
                "坐标可以直接使用 ~，也可以使用普通数字。",
                "",
                "用法：",
                "> goal - 设置目标为当前位置",
                "> goal <reset/clear/none> - 清空目标",
                "> goal <y> - 设置目标为某个 Y 高度",
                "> goal <x> <z> - 设置目标为 X,Z 坐标",
                "> goal <x> <y> <z> - 设置目标为 X,Y,Z 坐标"
        );
    }
}
