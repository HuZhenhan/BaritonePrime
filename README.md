# BaritonePrime

BaritonePrime 是基于原版 Baritone 的二次开发版本，目标是在保留 Baritone 自动寻路、挖掘、建造和命令控制能力的基础上，加入更实用的改进、优化现有体验，并持续打磨细节。

当前项目主要面向 Minecraft 1.21 / 1.21.1，支持 Fabric 与 Tweaker 构建。

## 项目状态

- 基于 Baritone 1.11.x 分支继续维护
- 当前模块版本：`1.11.2`
- Minecraft 版本：`1.21`
- Java 版本：`21`
- 可用加载方式：`fabric`、`tweaker`
- 许可证：`LGPL-3.0`

## 主要能力

BaritonePrime 继承了 Baritone 的核心能力，包括：

- 长距离自动寻路
- 方块挖掘、放置与避障
- 自动挖矿、探索、跟随和路径目标控制
- 区域选择与 schematic 构建
- 路径缓存与分段计算
- 聊天命令控制

更多原有能力可参考：

- [功能说明](FEATURES.md)
- [使用说明](USAGE.md)
- [安装与构建说明](SETUP.md)

## BaritonePrime 改动

当前版本已包含以下调整：

- Elytra 飞行支持在下界以外的维度进行。
- 新增 Elytra 高度相关配置，便于控制不同世界中的飞行高度策略。
- 针对现有功能进行实用性优化和细节修正。

更具体的变更记录见：

- [更新日志](baritone-1.21.1/CHANGELOG.md)

## 构建要求

请先确认本机已安装：

- JDK 21
- Git
- Gradle Wrapper 会随项目一起使用，无需单独安装 Gradle

检查 Java 版本：

```bash
java -version
```

## 从源码构建

Windows：

```bash
gradlew.bat build
```

Linux / macOS：

```bash
./gradlew build
```

构建完成后，产物会输出到 `dist/` 目录。

## 运行测试

Windows：

```bash
gradlew.bat test
```

Linux / macOS：

```bash
./gradlew test
```

## 安装使用

Fabric 用户通常可以将构建出的 Fabric 版本 jar 放入 Minecraft 的 `mods` 目录中使用。

进入游戏后，默认可以通过 `#` 前缀使用 Baritone 命令，例如：

```text
#help
#goto 100 64 100
#mine diamond_ore
#stop
```

更多命令请查看 [USAGE.md](USAGE.md)。

## 目录说明

```text
src/api/             Baritone API
src/main/            核心实现
src/launch/          启动与 Mixin 相关内容
src/test/            测试代码
fabric/              Fabric 加载器配置
tweaker/             Tweaker 构建模块
buildSrc/            Gradle 构建辅助任务
scripts/             构建与混淆相关脚本
baritone-1.21.1/     当前分支变更记录
```

## 贡献

欢迎提交 Issue 或 Pull Request。建议在提交前先运行：

```bash
./gradlew test
./gradlew build
```

Windows 下请将命令中的 `./gradlew` 替换为 `gradlew.bat`。

## 说明

BaritonePrime 是基于 Baritone 的二次开发项目。原项目版权、协议和相关说明请以仓库中的 `LICENSE`、`CODE_OF_CONDUCT.md` 以及上游 Baritone 项目为准。