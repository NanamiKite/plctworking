# Eclipse 插件开发中的 `plugin.xml` 和 `MANIFEST.MF` 

在 Eclipse 插件开发中，`plugin.xml` 和 `MANIFEST.MF` 是两个核心配置文件，分别用于定义插件的扩展点和元数据。以下是它们的功能详解及开发中的注意事项。

---

## `plugin.xml` 文件

### 功能
`plugin.xml` 文件是 Eclipse 插件的核心配置文件，用于定义插件的扩展点和功能。它描述了插件如何与 Eclipse 平台交互，以及插件提供的功能。

### 常见结构
以下是一个典型的 `plugin.xml` 文件结构：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<plugin>
    <!-- 定义视图扩展点 -->
    <extension point="org.eclipse.ui.views">
        <view
            name="Package Resource Manager"
            category="com.ruyi.pkgmanage"
            class="ruyipkgmanage.PackageResourceView"
            id="com.ruyi.pkgmanage.PackageResourceView"
            restorable="true"/>
    </extension>

    <!-- 定义首选项页面扩展点 -->
    <extension point="org.eclipse.ui.preferencePages">
        <page
            name="Ruyi Package Manager"
            class="ruyipkgmanage.RuyiPreferencesPage"
            id="ruyipkgmanage.RuyiPreferencesPage"/>
    </extension>
</plugin>
```
### 关键字段：
- 1.`plugin`：
    根元素，包含插件的所有扩展点定义。
- 2.`extension>`：定义插件的扩展点。
    - `point`属性指定扩展点的 ID，例如 `org.eclipse.ui.views`表示视图扩展点。
- 3.视图扩展点 `org.eclipse.ui.views`：

    - `name`：视图的显示名称。
    - `category`：视图所属的分类，用于在 Eclipse 的视图选择器中分组。
    - `class`：实现视图功能的 Java 类。
    - `id`：视图的唯一标识符。
    - `restorable`：是否支持在 Eclipse 重启后恢复视图。
- 4首选项页面扩展点 `org.eclipse.ui.preferencePages)`：

    - `name`：首选项页面的显示名称。
    - `class`：实现首选项页面的 Java 类。
    - `id`：首选项页面的唯一标识符。

## `MANIFEST.MF` 文件

### 功能
`MANIFEST.MF` 文件是插件的元数据文件，描述插件的基本信息、依赖关系和导出的包。

### 常见结构
```
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: Ruyi Package Manager
Bundle-SymbolicName: com.ruyi.pkgmanage;singleton:=true
Bundle-Version: 1.0.0
Bundle-Activator: ruyipkgmanage.Activator
Require-Bundle: org.eclipse.ui,
 org.eclipse.core.runtime
Export-Package: ruyipkgmanage
```
### 关键字段
- 1 `Manifest-Version`：
    描述文件的版本号，通常为 1.0。
- 2 `Bundle-ManifestVersion`：
    OSGi 元数据的版本号，通常为 2。
- 3 `Bundle-Name`：
    插件的名称，用于显示在 Eclipse 插件管理器中。
- 4 `Bundle-SymbolicName`：
    插件的唯一标识符，通常是插件的包名。
    - `singleton:=true` 表示该插件在运行时只能有一个实例。
- 5 `Bundle-Version`：
    插件的版本号，格式为 `major.minor.micro`。
- 6 `Bundle-Activator`：
    插件的激活器类，负责在插件启动和停止时执行特定逻辑。
- 7 `Require-Bundle`：
    插件的依赖项，列出插件运行时所需的其他插件。
     -   例如：
        `org.eclipse.ui`：依赖 Eclipse UI 框架。
        `org.eclipse.core.runtime`：依赖 Eclipse 核心运行。
- 8 `Export-Package`：
    插件导出的包，供其他插件使用。
### 总结
- 1 `plugin.xml`文件：
    用于定义插件的扩展点和功能。
    确保扩展点 ID 和类路径正确。
- 2 `MANIFEST.MF`文件：
    用于描述插件的元数据和依赖关系。
    确保 Bundle-SymbolicName 唯一，依赖项完整。
