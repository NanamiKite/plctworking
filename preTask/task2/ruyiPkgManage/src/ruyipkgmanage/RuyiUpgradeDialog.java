package ruyipkgmanage;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class RuyiUpgradeDialog {

    private Shell shell;

    public RuyiUpgradeDialog(Shell shell) {
        this.shell = shell;
    }

    public void open() {
        String localVersion = RuyiUtils.getLocalVersion();
        String remoteVersion = RuyiUtils.getRemoteVersion();
        boolean upgrade = MessageDialog.openQuestion(shell, "Ruyi升级",
                "当前版本：" + localVersion + "\n最新版本：" + remoteVersion + "\n是否立即升级？");
        if (upgrade) {
            upgradeRuyi();
        }
    }

    private void upgradeRuyi() {
        try {
            RuyiUtils.autoUpgradeRuyi();
            MessageDialog.openInformation(shell, "升级完成", "Ruyi 已自动升级到最新版本！");
        } catch (Exception e) {
            MessageDialog.openError(shell, "升级失败", "自动升级失败，请手动执行命令：\n" + RuyiUtils.getInstallCommand());
        }
    }
}