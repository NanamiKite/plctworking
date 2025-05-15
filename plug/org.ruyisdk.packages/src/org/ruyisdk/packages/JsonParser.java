package org.ruyisdk.packages;

import javax.json.*;
import java.io.StringReader;

public class JsonParser {
    public static TreeNode parseJson(String jsonData) {
        try (JsonReader reader = Json.createReader(new StringReader(jsonData))) {
            // 检查 JSON 数据是对象还是数组
            JsonStructure jsonStructure = reader.read();

            // 创建根节点
            TreeNode root = new TreeNode("LPI4A", null);

            if (jsonStructure instanceof JsonObject) {
                // 如果是单个 JSON 对象
                parseJsonObject((JsonObject) jsonStructure, root);
            } else if (jsonStructure instanceof JsonArray) {
                // 如果是 JSON 数组
                JsonArray jsonArray = (JsonArray) jsonStructure;
                for (JsonValue value : jsonArray) {
                    if (value instanceof JsonObject) {
                        parseJsonObject((JsonObject) value, root);
                    }
                }
            }

            return root;
        } catch (Exception e) {
            throw new RuntimeException("解析 JSON 数据失败：" + e.getMessage(), e);
        }
    }

    // private static void parseJsonObject(JsonObject rootObject, TreeNode root) {
    //     // 获取类别（如 emulator, toolchain 等）
    //     String category = rootObject.getString("category", "unknown");

    //     // 检查是否已存在该类别节点
    //     TreeNode categoryNode = findOrCreateCategoryNode(root, category);

    //     // 获取包名
    //     String name = rootObject.getString("name", "unknown");
    //     System.out.println("Package name: " + name);
    //     TreeNode packageNode = new TreeNode(name, null);
    //     categoryNode.addChild(packageNode);

    //     // 获取版本信息
    //     JsonArray versions = rootObject.getJsonArray("vers");
    //     if (versions != null) {
    //         for (JsonValue versionValue : versions) {
    //             JsonObject versionObject = versionValue.asJsonObject();
    //             String semver = versionObject.getString("semver", "unknown");

    //             // 获取备注信息
    //             JsonArray remarks = versionObject.getJsonArray("remarks");
    //             String remark = (remarks != null && !remarks.isEmpty()) ? " [" + remarks.getString(0) + "]" : "";

    //             TreeNode versionNode = new TreeNode(semver + remark, null);
    //             packageNode.addChild(versionNode);
    //         }
    //     }
    // }









    private static void parseJsonObject(JsonObject rootObject, TreeNode root) {
        // 跳过没有 category 或 name 的对象
        if (!rootObject.containsKey("category") || !rootObject.containsKey("name")) {
            return;
        }
        String category = rootObject.getString("category");
        TreeNode categoryNode = findOrCreateCategoryNode(root, category);
    
        String name = rootObject.getString("name");
        System.out.println("Package name: " + name);
        TreeNode packageNode = new TreeNode(name, null);
        categoryNode.addChild(packageNode);
    
        JsonArray versions = rootObject.getJsonArray("vers");
        if (versions != null) {
            for (JsonValue versionValue : versions) {
                JsonObject versionObject = versionValue.asJsonObject();
                // 跳过没有 semver 的对象
                if (!versionObject.containsKey("semver")) {
                    continue;
                }
                String semver = versionObject.getString("semver");
    
                JsonArray remarks = versionObject.getJsonArray("remarks");
                String remark = (remarks != null && !remarks.isEmpty()) ? " [" + remarks.getString(0) + "]" : "";
    
                // 构造安装命令
                String installCommand = "ruyi install '" + name + "(" + semver + ")'";
    
                TreeNode versionNode = new TreeNode(semver + remark, null, installCommand);
                versionNode.setLeaf(true); // 标记为叶子节点
                packageNode.addChild(versionNode);
            }
        }
    }

    private static TreeNode findOrCreateCategoryNode(TreeNode root, String category) {
        // 遍历根节点的子节点，查找是否已存在该类别节点
        for (TreeNode child : root.getChildren()) {
            if (child.getName().equals(category)) {
                return child; // 如果找到，直接返回
            }
        }

        // 如果未找到，创建新的类别节点并添加到根节点
        TreeNode categoryNode = new TreeNode(category, null);
        root.addChild(categoryNode);
        return categoryNode;
    }
}