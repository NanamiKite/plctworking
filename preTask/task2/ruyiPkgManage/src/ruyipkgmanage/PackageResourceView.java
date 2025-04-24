package ruyipkgmanage;

import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Menu;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class PackageResourceView extends ViewPart {

    public static final String ID = "com.ruyi.packageManager.PackageResourceView";

    private CheckboxTreeViewer treeViewer;
    private Action downloadAction;

    @Override
    public void createPartControl(Composite parent) {
        treeViewer = new CheckboxTreeViewer(parent, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL);
        Tree tree = treeViewer.getTree();
        tree.setHeaderVisible(true);

        // 设置内容提供者和标签提供者
        treeViewer.setContentProvider(new PackageContentProvider());
        treeViewer.setLabelProvider(new PackageLabelProvider());

        // 创建数据模型
        createPackageTree();

        // 创建右键菜单
        createContextMenu();
    }

    private void createPackageTree() {
        // 从命令获取 JSON 数据
        String json = executeRuyiListCommand();

        // 解析 JSON 数据
        JsonReader reader = Json.createReader(new StringReader(json));
        JsonObject jsonObject = reader.readObject();

        String deviceName = jsonObject.getString("device", "未知设备");
        JsonArray packages = jsonObject.getJsonArray("packages");

        // 创建根节点
        DeviceNode rootNode = new DeviceNode(deviceName);

        // 按类型分类并解析版本信息
        for (JsonValue pkg : packages) {
            JsonObject obj = pkg.asJsonObject();
            String type = obj.getString("type", "未知类型");
            String packageName = obj.getString("name", "未知软件包");

            // 创建 PackageNode
            PackageNode packageNode = new PackageNode(packageName, type);

            // 解析 versions 数组
            JsonArray versions = obj.getJsonArray("versions");
            if (versions != null) {
                for (JsonValue versionValue : versions) {
                    JsonObject versionObj = versionValue.asJsonObject();
                    String version = versionObj.getString("version", "未知版本");
                    JsonArray tagsArray = versionObj.getJsonArray("tags");
                    List<String> tags = new ArrayList<>();
                    if (tagsArray != null) {
                        for (JsonValue tagValue : tagsArray) {
                            tags.add(tagValue.toString());
                        }
                    }
                    packageNode.versions.add(new VersionNode(version, tags));
                }
            }

            // 查找或创建类型节点
            TypeNode typeNode = rootNode.getTypeNode(type);
            if (typeNode == null) {
                typeNode = new TypeNode(type);
                rootNode.children.add(typeNode);
            }
            typeNode.children.add(packageNode);
        }

        // 设置新的输入并刷新视图
        treeViewer.setInput(rootNode);
        treeViewer.refresh();
    }

    private String executeRuyiListCommand() {
        StringBuilder output = new StringBuilder();
        try {
            // 执行命令
            Process process = Runtime.getRuntime().exec("ruyi list --related-to-entity device:sipeed-lpi4a");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // 读取命令输出
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 返回模拟的 JSON 数据（如果命令失败，可以返回默认数据）
        if (output.length() == 0) {
            return getMockJsonData();
        }
        return output.toString();
    }

    private String getMockJsonData() {
        return "{"
                + "\"device\": \"sipeed-lpi4a\","
                + "\"packages\": ["
                + "  {\"type\": \"source\", \"name\": \"coremark\", \"versions\": ["
                + "    {\"version\": \"1.0.2-pre.20230125\", \"tags\": [\"prerelease\", \"latest-prerelease\"]},"
                + "    {\"version\": \"1.0.1\", \"tags\": [\"latest\"]}"
                + "  ]},"
                + "  {\"type\": \"source\", \"name\": \"ruyisdk-demo\", \"versions\": ["
                + "    {\"version\": \"0.20231114.0\", \"tags\": [\"latest\"]}"
                + "  ]},"
                + "  {\"type\": \"board-image\", \"name\": \"debian-desktop-sdk-milkv-mars-sd\", \"versions\": ["
                + "    {\"version\": \"1.0.6+3.6.1\", \"tags\": [\"latest\"]}"
                + "  ]},"
                + "  {\"type\": \"board-image\", \"name\": \"freebsd-riscv64-mini-live\", \"versions\": ["
                + "    {\"version\": \"14.0.0\", \"tags\": [\"latest\"]}"
                + "  ]}"
                + "]"
                + "}";
    }

    private void createContextMenu() {
        // 创建右键菜单管理器
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(treeViewer.getControl());
        treeViewer.getControl().setMenu(menu);

        // 创建下载操作
              downloadAction = new Action("下载选中软件包") {
            @Override
            public void run() {
                Object[] checkedElements = treeViewer.getCheckedElements();
                List<String> commands = new ArrayList<>();
        
                for (Object element : checkedElements) {
                    if (element instanceof PackageNode) {
                        PackageNode packageNode = (PackageNode) element;
                        for (VersionNode version : packageNode.versions) {
                            // 修改命令格式为 ruyi install 'coremark(版本号)'
                            String command = "ruyi install '" + packageNode.name + "(" + version.version + ")'";
                            commands.add(command);
                        }
                    } else if (element instanceof VersionNode) {
                        VersionNode versionNode = (VersionNode) element;
                        PackageNode parentPackage = findParentPackage(versionNode);
                        if (parentPackage != null) {
                            // 修改命令格式为 ruyi install 'coremark(版本号)'
                            String command = "ruyi install '" + parentPackage.name + "(" + versionNode.version + ")'";
                            commands.add(command);
                        }
                    }
                }
        

                if (!commands.isEmpty()) {
                    for (String command : commands) {
                        executeCommandInEclipseConsole(command);
                    }
                } else {
                    System.out.println("未选中任何软件包！");
                }
            }
        };

        // 将操作添加到菜单管理器
        menuManager.add(downloadAction);
    }

    private PackageNode findParentPackage(VersionNode versionNode) {
        Object input = treeViewer.getInput();
        if (input instanceof DeviceNode) {
            for (TypeNode typeNode : ((DeviceNode) input).children) {
                for (PackageNode packageNode : typeNode.children) {
                    if (packageNode.versions.contains(versionNode)) {
                        return packageNode;
                    }
                }
            }
        }
        return null;
    }

    private void executeCommandInEclipseConsole(String command) {
        try {
            MessageConsole console = findConsole("Ruyi Install Console");
            MessageConsoleStream out = console.newMessageStream();

            out.println("执行命令: " + command);

            Process process = Runtime.getRuntime().exec(command);

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.println(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            process.waitFor();
            out.println("命令执行完成: " + command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MessageConsole findConsole(String name) {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager consoleManager = plugin.getConsoleManager();
        IConsole[] existing = consoleManager.getConsoles();
        for (IConsole console : existing) {
            if (name.equals(console.getName())) {
                return (MessageConsole) console;
            }
        }
        MessageConsole newConsole = new MessageConsole(name, null);
        consoleManager.addConsoles(new IConsole[] { newConsole });
        return newConsole;
    }

    @Override
    public void setFocus() {}

    static class DeviceNode {
        String name;
        List<TypeNode> children = new ArrayList<>();

        DeviceNode(String name) {
            this.name = name;
        }

        TypeNode getTypeNode(String type) {
            for (TypeNode node : children) {
                if (node.name.equals(type)) {
                    return node;
                }
            }
            return null;
        }
    }

    static class TypeNode {
        String name;
        List<PackageNode> children = new ArrayList<>();

        TypeNode(String name) {
            this.name = name;
        }
    }

    static class PackageNode {
        String name, type;
        List<VersionNode> versions = new ArrayList<>();

        PackageNode(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    static class VersionNode {
        String version;
        List<String> tags;

        VersionNode(String version, List<String> tags) {
            this.version = version;
            this.tags = tags;
        }
    }

    static class PackageContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof DeviceNode) {
                return ((DeviceNode) inputElement).children.toArray();
            }
            return new Object[0];
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof TypeNode) {
                return ((TypeNode) parentElement).children.toArray();
            } else if (parentElement instanceof PackageNode) {
                return ((PackageNode) parentElement).versions.toArray();
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof TypeNode) {
                return !((TypeNode) element).children.isEmpty();
            } else if (element instanceof PackageNode) {
                return !((PackageNode) element).versions.isEmpty();
            }
            return false;
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

        @Override
        public void dispose() {}
    }

    static class PackageLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof TypeNode) {
                return "type: " + ((TypeNode) element).name;
            } else if (element instanceof PackageNode) {
                PackageNode pkg = (PackageNode) element;
                return "pkg: " + pkg.name + " (type: " + pkg.type + ")";
            } else if (element instanceof VersionNode) {
                VersionNode version = (VersionNode) element;
                return "version: " + version.version + " - tags: " + version.tags;
            }
            return super.getText(element);
        }
    }
}