package ruyipkgmanage;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Activator extends AbstractUIPlugin {
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        Display.getDefault().asyncExec(() -> {
            final Shell shell = Display.getDefault().getActiveShell() != null
                    ? Display.getDefault().getActiveShell()
                    : new Shell(Display.getDefault());
            if (!RuyiUtils.isRuyiInstalled()) {
                new RuyiInstallConfigDialog(shell).open();
            } else {
                new Thread(() -> {
                    if (RuyiUtils.isRuyiNeedUpgrade()) {
                        Display.getDefault().asyncExec(() -> {
                            new RuyiUpgradeDialog(shell).open();
                        });
                    }
                }).start();
            }
        });
    }
}