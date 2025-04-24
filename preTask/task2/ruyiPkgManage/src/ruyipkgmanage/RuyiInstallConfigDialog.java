package ruyipkgmanage;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

public class RuyiInstallConfigDialog extends Dialog {

    private Text ruyiInstallPathText;
    private Button installButton;

    protected RuyiInstallConfigDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        //安装路径
        ruyiInstallPathText = new Text(container, SWT.BORDER);
        ruyiInstallPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // 安装按钮
        installButton = new Button(container, SWT.PUSH);
        installButton.setText("Install ruyi");
        installButton.addListener(SWT.Selection, e -> installRuyi());
        return container;
    }

    private void installRuyi() {
        // 执行安装逻辑
        String installPath = ruyiInstallPathText.getText();
        // 使用该路径安装
 
    }
}
