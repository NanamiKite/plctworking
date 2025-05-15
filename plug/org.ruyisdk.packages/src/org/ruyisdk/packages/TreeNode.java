package org.ruyisdk.packages;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private String name;
    private String details;
    private String installCommand;
    private boolean selected; // 记录复选框是否选中
    private boolean isLeaf; // 标记是否为最末级节点
    private List<TreeNode> children;

    public TreeNode(String name, String details) {
        this(name, details, null);
    }

    public TreeNode(String name, String details, String installCommand) {
        this.name = name;
        this.details = details;
        this.installCommand = installCommand;
        this.children = new ArrayList<>();
        this.selected = false; // 默认未选中
        this.isLeaf = false; // 默认不是叶子节点
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }

    public String getInstallCommand() {
        return installCommand;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }
}