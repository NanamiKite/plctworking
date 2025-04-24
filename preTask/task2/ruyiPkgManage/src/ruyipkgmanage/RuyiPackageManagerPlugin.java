package ruyipkgmanage;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class RuyiPackageManagerPlugin extends AbstractUIPlugin {
    private static RuyiPackageManagerPlugin plugin;

    public static RuyiPackageManagerPlugin getDefault() {
        return plugin;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        // 确保工作台已初始化
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                // 工作台初始化完成后执行操作
                checkRuyiInstallation();
            }
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    private void checkRuyiInstallation() {
        boolean isRuyiInstalled = checkIfRuyiInstalled();

        if (!isRuyiInstalled) {
            // 弹出安装配置对话框
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            RuyiInstallConfigDialog dialog = new RuyiInstallConfigDialog(shell);
            dialog.open();
        } else {
            // 检查版本
            checkRuyiVersion();
        }
    }

    private boolean checkIfRuyiInstalled() {
        return false; // 假设未安装
    }

    private void checkRuyiVersion() {
        // 检查本地版本，并与服务器最新版本对比
        // 如果需要更新，弹出升级提示
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        RuyiUpgradeDialog upgradeDialog = new RuyiUpgradeDialog(shell);
        upgradeDialog.open();
    }
}

