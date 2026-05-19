# Flutter Hi Json

An IntelliJ IDEA / Android Studio plugin that converts JSON data into Dart entity classes with `json_serializable` annotations.

## Features

- **Right-click to generate**: Right-click any directory → New → Flutter Hi Json
- **Live preview**: See the generated Dart class as you configure options
- **JSON tree view**: Inspect JSON structure with a collapsible tree (Format Only)
- **Configurable options**:
  - `nullable` — types use `?` suffix, no `required` in constructor
  - `default value` — adds `@JsonKey(defaultValue: ...)` based on type, `required` in constructor
  - `use JsonKey.name` — all fields annotated with `@JsonKey(name: 'original_key')`
- **JSON validation**: English error messages for invalid JSON
- **Auto build_runner**: Runs `dart run build_runner build --delete-conflicting-outputs` after generation
- **Nested object support**: Automatically generates nested Dart classes
- **Customizable settings**: Dialog title and prompt text configurable in Settings → Tools → Flutter Hi Json

## Usage

1. Right-click on a directory in the Project view
2. Select **New → Flutter Hi Json**
3. Paste your JSON data in the left editor
4. Click **Format** to preview the generated Dart class, or **Format Only** to view the JSON tree
5. Enter a class name (auto-converted to UpperCamelCase)
6. Configure options (nullable / default value / use JsonKey.name)
7. Click **OK** to generate the `.dart` file and run `build_runner`

## Example

**Input JSON:**
```json
{
  "user_name": "John",
  "age": 25,
  "is_active": true
}
```

**Class Name:** `user`

**Generated `user.dart`:**
```dart
import 'package:json_annotation/json_annotation.dart';

part 'user.g.dart';

@JsonSerializable()
class User {
  @JsonKey(name: 'user_name', defaultValue: '')
  final String userName;
  @JsonKey(name: 'age', defaultValue: 0)
  final int age;
  @JsonKey(name: 'is_active', defaultValue: false)
  final bool isActive;

  User({
    required this.userName,
    required this.age,
    required this.isActive,
  });

  factory User.fromJson(Map<String, dynamic> json) =>
      _$UserFromJson(json);

  Map<String, dynamic> toJson() => _$UserToJson(this);
}
```

## Settings

Go to **Settings → Tools → Flutter Hi Json** to customize:

| Setting | Default | Description |
|---------|---------|-------------|
| Dialog Title | `Flutter Hi Json` | Title shown in the dialog window |
| Prompt Text | `Paste your JSON here...` | Hint text above the JSON editor |
| nullable selected by default | ✅ | Initial state of the nullable radio button |
| default value selected by default | ☐ | Initial state of the default value radio button |
| use JsonKey.name checked by default | ✅ | Initial state of the use JsonKey.name checkbox |

## Requirements

- IntelliJ IDEA 2022.3+ or Android Studio Giraffe 2022.3+
- Flutter project with `json_serializable` and `build_runner` as dev dependencies

## Installation

1. Download `flutter-hi-json-plugin-1.0.0.zip` from the [Releases](https://github.com/hijson/flutter-hi-json/releases) page
2. Open IntelliJ IDEA / Android Studio
3. Go to **Settings → Plugins → ⚙️ → Install Plugin from Disk**
4. Select the downloaded zip file
5. Restart the IDE

---

# Flutter Hi Json（中文）

一款 IntelliJ IDEA / Android Studio 插件，将 JSON 数据转化为基于 `json_serializable` 的 Dart 实体类。

## 功能特性

- **右键生成**：在项目目录右键 → New → Flutter Hi Json
- **实时预览**：配置选项时即时预览生成的 Dart 类
- **JSON 树状视图**：通过可折叠树查看 JSON 结构（Format Only 模式）
- **可配置选项**：
  - `nullable` — 属性可空，构造函数不使用 `required`
  - `default value` — 根据字段类型自动添加 `@JsonKey(defaultValue: ...)`，构造函数使用 `required`
  - `use JsonKey.name` — 所有字段添加 `@JsonKey(name: '原始键名')` 注解
- **JSON 校验**：无效 JSON 时弹出英文错误提示
- **自动执行 build_runner**：生成文件后自动运行 `dart run build_runner build --delete-conflicting-outputs`
- **支持嵌套对象**：自动生成嵌套 Dart 类
- **可自定义设置**：在 Settings → Tools → Flutter Hi Json 中配置弹窗标题和提示文案

## 使用方法

1. 在项目视图中右键点击目标目录
2. 选择 **New → Flutter Hi Json**
3. 在左侧编辑器粘贴 JSON 数据
4. 点击 **Format** 预览生成的 Dart 类，或点击 **Format Only** 查看 JSON 树状结构
5. 输入类名（自动转为大驼峰命名）
6. 配置选项（nullable / default value / use JsonKey.name）
7. 点击 **OK** 生成 `.dart` 文件并自动运行 `build_runner`

## 示例

**输入 JSON：**
```json
{
  "user_name": "张三",
  "age": 25,
  "is_active": true
}
```

**类名输入：** `user`

**生成 `user.dart`：**
```dart
import 'package:json_annotation/json_annotation.dart';

part 'user.g.dart';

@JsonSerializable()
class User {
  @JsonKey(name: 'user_name', defaultValue: '')
  final String userName;
  @JsonKey(name: 'age', defaultValue: 0)
  final int age;
  @JsonKey(name: 'is_active', defaultValue: false)
  final bool isActive;

  User({
    required this.userName,
    required this.age,
    required this.isActive,
  });

  factory User.fromJson(Map<String, dynamic> json) =>
      _$UserFromJson(json);

  Map<String, dynamic> toJson() => _$UserToJson(this);
}
```

## 设置项

在 **Settings → Tools → Flutter Hi Json** 中可自定义：

| 设置项 | 默认值 | 说明 |
|--------|--------|------|
| Dialog Title | `Flutter Hi Json` | 弹窗标题文字 |
| Prompt Text | `Paste your JSON here...` | JSON 编辑器上方的提示文字 |
| nullable selected by default | ✅ | nullable 单选按钮默认是否选中 |
| default value selected by default | ☐ | default value 单选按钮默认是否选中 |
| use JsonKey.name checked by default | ✅ | use JsonKey.name 复选框默认是否勾选 |

## 环境要求

- IntelliJ IDEA 2022.3+ 或 Android Studio Giraffe 2022.3+
- Flutter 项目，需在 `pubspec.yaml` 中添加 `json_serializable` 和 `build_runner` 开发依赖

## 安装方式

1. 从 [Releases](https://github.com/hijson/flutter-hi-json/releases) 页面下载 `flutter-hi-json-plugin-1.0.0.zip`
2. 打开 IntelliJ IDEA / Android Studio
3. 进入 **Settings → Plugins → ⚙️ → Install Plugin from Disk**
4. 选择下载的 zip 文件
5. 重启 IDE
