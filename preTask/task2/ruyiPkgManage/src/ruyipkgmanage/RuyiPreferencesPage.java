package ruyipkgmanage;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class RuyiPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    private Text installPathText;
    private Label versionLabel;
    private Text repoText;
    private Label repoUpdateLabel;
    private Button repoSwitchBtn;
    private Label telemetryLabel;
    private Button telemetrySwitchBtn;
    private Link newsLink;

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(3, false));

        // ruyi 安装路径
        Label pathLabel = new Label(container, SWT.NONE);
        pathLabel.setText("ruyi 安装路径：");
        installPathText = new Text(container, SWT.BORDER);
        installPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        installPathText.setText("/usr/local/bin/ruyi");
        Button openDirBtn = new Button(container, SWT.PUSH);
        openDirBtn.setText("打开目录");
        openDirBtn.addListener(SWT.Selection, e -> openInstallDir());

        // ruyi 版本
        Label verLabel = new Label(container, SWT.NONE);
        verLabel.setText("ruyi版本：");
        versionLabel = new Label(container, SWT.NONE);
        versionLabel.setText(RuyiUtils.getLocalVersion());
        Button upgradeBtn = new Button(container, SWT.PUSH);
        upgradeBtn.setText("升级");
        upgradeBtn.addListener(SWT.Selection, e -> upgradeRuyi());

        // 存储库地址
        Label repoLabel = new Label(container, SWT.NONE);
        repoLabel.setText("存储库地址：");
        repoText = new Text(container, SWT.BORDER);
        repoText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        repoText.setText("https://mirror.iscas.ac.cn/ruyisdk/ruyi");
        repoUpdateLabel = new Label(container, SWT.NONE);
        repoUpdateLabel.setText("更新时间: 2024-04-23");
        repoSwitchBtn = new Button(container, SWT.PUSH);
        repoSwitchBtn.setText("切换存储库");
        repoSwitchBtn.addListener(SWT.Selection, e -> switchRepo());

        // 遥测状态
        Label telemetryTextLabel = new Label(container, SWT.NONE);
        telemetryTextLabel.setText("遥测状态：");
        telemetryLabel = new Label(container, SWT.NONE);
        telemetryLabel.setText("on");
        telemetrySwitchBtn = new Button(container, SWT.PUSH);
        telemetrySwitchBtn.setText("切换状态");
        telemetrySwitchBtn.addListener(SWT.Selection, e -> switchTelemetry());

        // 版本动态
        Label newsLabel = new Label(container, SWT.NONE);
        newsLabel.setText("版本动态：");
        newsLink = new Link(container, SWT.NONE);
        newsLink.setText("<a href=\"https://mirror.iscas.ac.cn/ruyisdk/ruyi/news\">查看</a>");
        newsLink.addListener(SWT.Selection, e -> openNewsUrl());
        new Label(container, SWT.NONE); // 占位

        return container;
    }

    private void openInstallDir() {
        try {
            String dir = installPathText.getText();
            Runtime.getRuntime().exec(new String[]{"xdg-open", dir.substring(0, dir.lastIndexOf('/'))});
        } catch (Exception e) {
            MessageDialog.openError(getShell(), "错误", "无法打开目录");
        }
    }

    private void upgradeRuyi() {
        try {
            RuyiUtils.autoUpgradeRuyi();
            versionLabel.setText(RuyiUtils.getLocalVersion());
            MessageDialog.openInformation(getShell(), "升级完成", "Ruyi 已升级到最新版本！");
        } catch (Exception e) {
            MessageDialog.openError(getShell(), "升级失败", "升级失败，请手动执行命令：\n" + RuyiUtils.getInstallCommand());
        }
    }

    private void switchRepo() {
        String current = repoText.getText();
        if (current.contains("mirror.iscas.ac.cn")) {
            repoText.setText("https://main.repo.example.com/ruyi");
        } else {
            repoText.setText("https://mirror.iscas.ac.cn/ruyisdk/ruyi");
        }
        repoUpdateLabel.setText("更新时间: " + java.time.LocalDate.now());
    }

    private void switchTelemetry() {
        String current = telemetryLabel.getText();
        if ("on".equals(current)) {
            telemetryLabel.setText("off");
        } else if ("off".equals(current)) {
            telemetryLabel.setText("local");
        } else {
            telemetryLabel.setText("on");
        }
    }

    private void openNewsUrl() {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("https://mirror.iscas.ac.cn/ruyisdk/ruyi/news"));
        } catch (Exception e) {
            MessageDialog.openError(getShell(), "错误", "无法打开链接");
        }
    }

    @Override
    public void init(IWorkbench workbench) {}
}
