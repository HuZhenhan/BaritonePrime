# Changelog

## 未发布

- Elytra 飞行现在支持在下界以外的维度进行（会尽量使用各世界的高度范围）。
- 小地图目标队列同步现在会投影完整的多目标，当前目标为绿色，后续目标为灰色，并支持按队列条目标识进行删除/刷新。
- Elytra：在接近终点的 XZ 距离约 250 格范围内开始允许向下规划，避免需要等到几乎抵达 XZ 才下降（减少“贴高不降/强制高空飞行”的等待感）。
- Elytra：延迟安全落点搜索，只有当玩家距离地面约不超过 100 格时才开始计算，减少高空阶段的重复计算与日志刷屏。
- 修复：Elytra 相关逻辑在部分环境下的构建编译失败问题。
- Elytra：非下界在高空阶段允许更快的垂直俯冲（距离 landing 约 30 格以上时不使用烟花），并在接近约 30 格后恢复缓慢下降策略。
- 新增可配置的 Elytra 高度限制与高空偏好：
  - `elytraOverworldAndEndMaxHeightAboveBuildLimit`（默认 100）：主世界/末地允许超过最大建造高度的上限。
  - `elytraOverworldAndEndPreferredHeightAboveBuildLimit`（默认 15）：在没有显式目标 Y 的情况下，优先选择高于建造高度的巡航高度。
- 兼容性修复：由于原生寻路库在非下界维度仅支持有限 Y 范围，当目标/落点 Y 超出范围时会自动夹紧到支持范围，避免 `Invalid y1 or y2` 崩溃。
- 新增：Elytra 垂直起飞模式（默认启用）
  - 新增子命令：`#elytra vertical` / `#elytra old` 切换垂直/旧起飞逻辑
  - 若玩家头部正上方 25 格内无方块碰撞，则在起飞瞬间抬头 pitch=-90，并在同一刻强制使用 1 发烟花实现垂直起飞；否则回退到旧起飞逻辑
- 迁移参考（本次还涉及新增文件/逻辑）：`src/api/java/baritone/api/Settings.java`、`src/main/java/baritone/command/defaults/ElytraCommand.java`、`src/main/java/baritone/process/ElytraProcess.java`、`src/main/java/baritone/process/elytra/ElytraBehavior.java`

迁移参考（本次涉及文件）：
- `src/api/java/baritone/api/Settings.java`
- `src/main/java/baritone/process/ElytraProcess.java`
- `src/main/java/baritone/command/defaults/ElytraCommand.java`
