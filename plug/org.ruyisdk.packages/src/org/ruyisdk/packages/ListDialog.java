package org.ruyisdk.packages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class ListDialog extends Dialog {
    private String[] items;
    private String selectedItem;

    public ListDialog(Shell parent, String[] items) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.items = items;
    }

    public String open() {
        Shell shell = new Shell(getParent(), getStyle());
        shell.setText("选择开发板");
        shell.setLayout(new GridLayout(2, false));

        // 创建列表
        List list = new List(shell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
        for (String item : items) {
            list.add(item);
        }

        // 创建按钮
        Button okButton = new Button(shell, SWT.PUSH);
        okButton.setText("确定");
        okButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        okButton.addListener(SWT.Selection, e -> {
            int selectionIndex = list.getSelectionIndex();
            if (selectionIndex >= 0) {
                selectedItem = items[selectionIndex];
            }
            shell.close();
        });

        Button cancelButton = new Button(shell, SWT.PUSH);
        cancelButton.setText("取消");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        cancelButton.addListener(SWT.Selection, e -> {
            selectedItem = null;
            shell.close();
        });

        shell.setSize(300, 200);
        shell.open();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return selectedItem;
    }
}
