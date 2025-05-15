package org.ruyisdk.packages;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Control;




public class PackageExplorerView extends ViewPart {
    private CheckboxTreeViewer viewer;
    private Process bashProcess;
    private BufferedWriter bashWriter;
    private BufferedReader bashReader;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(1, false));

        // 创建按钮容器
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, false));
        buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // 添加刷新按钮
        Button refreshButton = new Button(buttonComposite, SWT.PUSH);
        refreshButton.setText("刷新列表");
        refreshButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        refreshButton.addListener(SWT.Selection, event -> refreshList());

        // 添加下载按钮
        Button downloadButton = new Button(buttonComposite, SWT.PUSH);
        downloadButton.setText("下载");
        downloadButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        downloadButton.addListener(SWT.Selection, event -> {
            System.out.println("下载按钮被点击");
            Object[] checkedElements = viewer.getCheckedElements();
            List<TreeNode> selectedNodes = new ArrayList<>();
            for (Object obj : checkedElements) {
                if (obj instanceof TreeNode) {
                    TreeNode node = (TreeNode) obj;
                    if (node.isLeaf()) {
                        selectedNodes.add(node);
                    }
                }
            }
            if (selectedNodes.isEmpty()) {
                MessageDialog.openInformation(Display.getDefault().getActiveShell(), "提示", "未选中任何节点！");
                return;
            }
            boolean confirmed = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "确认下载", "是否确认下载选中的节点？");
            if (confirmed) {
                for (TreeNode node : selectedNodes) {
                    executeInstallCommand(node.getInstallCommand());
                }
            }
        });

        viewer = new CheckboxTreeViewer(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        Tree tree = viewer.getTree();
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.setContentProvider(new TreeContentProvider());
        viewer.setLabelProvider(new TreeLabelProvider());

        // 只让叶子节点有复选框
        viewer.setCheckStateProvider(new ICheckStateProvider() {
            @Override
            public boolean isChecked(Object element) {
                return false; // 默认不选中
            }
            @Override
            public boolean isGrayed(Object element) {
                // 非叶子节点灰掉（不可选）
                if (element instanceof TreeNode) {
                    return !((TreeNode) element).isLeaf();
                }
                return false;
            }
        });

        // 禁止非叶子节点被选中
        viewer.addCheckStateListener(event -> {
            Object element = event.getElement();
            if (element instanceof TreeNode && !((TreeNode) element).isLeaf()) {
                viewer.setChecked(element, false);
            }
        });
        
        // 启动持久的 Bash 会话并开启实验模式
        startBashSession();

        // 弹出对话框让用户选择硬件类型
        String chosenType = showHardwareTypeSelectionDialog(parent.getShell());
        if (chosenType != null) {
            String command = "ruyi --porcelain list --related-to-entity device:sipeed-" + chosenType + " && echo RUYI_DONE";
            executeCommandInBackground(command);
        }
        Display.getDefault().asyncExec(() -> {
        try {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
        } catch (Exception e) {
            e.printStackTrace();
        }
    });
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    @Override
    public void dispose() {
        closeBashSession();
        super.dispose();
    }

    private void startBashSession() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-i");
            processBuilder.redirectErrorStream(true);
            bashProcess = processBuilder.start();

            bashWriter = new BufferedWriter(new OutputStreamWriter(bashProcess.getOutputStream()));
            bashReader = new BufferedReader(new InputStreamReader(bashProcess.getInputStream()));

            bashWriter.write("export RUYI_EXPERIMENTAL=true\n");
            bashWriter.flush();
            System.out.println("实验模式已开启");

            new Thread(() -> {
                try {
                    String line;
                    while ((line = bashReader.readLine()) != null) {
                        System.out.println("[Bash Output] " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            MessageDialog.openError(Display.getDefault().getActiveShell(), "错误", "无法启动 Bash 会话：" + e.getMessage());
        }
    }

    private void closeBashSession() {
        try {
            if (bashWriter != null) {
                bashWriter.write("exit\n");
                bashWriter.flush();
                bashWriter.close();
            }
            if (bashReader != null) {
                bashReader.close();
            }
            if (bashProcess != null) {
                bashProcess.destroy();
            }
            System.out.println("Bash 会话已关闭");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeCommandInBackground(String command) {
        new Thread(() -> {
            try {
                bashWriter.write(command + "\n");
                bashWriter.flush();
                System.out.println("执行命令: " + command);

                StringBuilder outputBuilder = new StringBuilder();
                String line;

                outputBuilder.append("[");
                while ((line = bashReader.readLine()) != null) {
                    if (line.contains("RUYI_DONE")) {
                        break;
                    }
                    if (!line.trim().isEmpty()) {
                        outputBuilder.append(line).append(",");
                    }
                }

                if (outputBuilder.length() > 1) {
                    outputBuilder.setLength(outputBuilder.length() - 1);
                }
                outputBuilder.append("]");

                String jsonData = outputBuilder.toString();
                System.out.println("接收到的 JSON 数据: " + jsonData);

                Display.getDefault().asyncExec(() -> {
                    try {
                        TreeNode root = JsonParser.parseJson(jsonData);
                        viewer.setInput(root);
                        viewer.expandAll();
                    } catch (Exception e) {
                        MessageDialog.openError(Display.getDefault().getActiveShell(), "错误", "解析 JSON 数据失败：" + e.getMessage());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), "错误", "执行命令失败：" + e.getMessage());
                });
            }
        }).start();
    }
    private void executeInstallCommand(String installCommand) {
        Display.getDefault().asyncExec(() -> {
            OutputLiveDialog dialog = new OutputLiveDialog(Display.getDefault().getActiveShell(), installCommand);
            dialog.open();
        });
    }
    
    // 新的实时输出对话框
    class OutputLiveDialog extends Dialog {
        private String installCommand;
        private Text text;
    
        public OutputLiveDialog(Shell parentShell, String installCommand) {
            super(parentShell);
            this.installCommand = installCommand;
        }
    
        @Override
        protected Control createDialogArea(Composite parent) {
            Composite container = (Composite) super.createDialogArea(parent);
            container.setLayout(new GridLayout(1, false));
            text = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
            text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            text.setText("执行命令:\n" + installCommand + "\n\n输出结果:\n");
            startCommand();
            return container;
        }
    
        private void startCommand() {
            new Thread(() -> {
                try {
                    bashWriter.write(installCommand + "\n");
                    bashWriter.flush();
    
                    String line;
                    while ((line = bashReader.readLine()) != null) {
                        final String outputLine = line + "\n";
                        Display.getDefault().asyncExec(() -> {
                            if (text != null && !text.isDisposed()) {
                                text.append(outputLine);
                            }
                        });
                        if (line.contains("RUYI_DONE")) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    Display.getDefault().asyncExec(() -> {
                        if (text != null && !text.isDisposed()) {
                            text.append("执行安装命令失败：" + e.getMessage() + "\n");
                        }
                    });
                }
            }).start();
        }
    
        @Override
        protected org.eclipse.swt.graphics.Point getInitialSize() {
            return new org.eclipse.swt.graphics.Point(600, 400);
        }
    }
// 自定义对话框
class OutputDialog extends Dialog {
    private String content;
    public OutputDialog(Shell parentShell, String content) {
        super(parentShell);
        this.content = content;
    }
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));
        Text text = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        text.setText(content);
        return container;
    }
    @Override
    protected org.eclipse.swt.graphics.Point getInitialSize() {
        // 设置弹窗初始大小（比如宽600，高400）
        return new org.eclipse.swt.graphics.Point(600, 400);
    }
}

    private void refreshList() {
        System.out.println("开始刷新列表...");
        String command = "ruyi --porcelain list --related-to-entity device:sipeed-lpi4a && echo RUYI_DONE";
        executeCommandInBackground(command);
    }

    private String showHardwareTypeSelectionDialog(Shell shell) {
        String[] hardwareTypes = { "lpi4a" };
        ListDialog dialog = new ListDialog(shell, hardwareTypes);
        return dialog.open();
    }
}
