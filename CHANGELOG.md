# Changelog

## 未发布

- Elytra 飞行现在支持在下界以外的维度进行（会尽量使用各世界的高度范围）。
- 小地图目标队列同步现在会投影完整的多目标，当前目标为绿色，后续目标为灰色，并支持按队列条目标识进行删除/刷新。
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
