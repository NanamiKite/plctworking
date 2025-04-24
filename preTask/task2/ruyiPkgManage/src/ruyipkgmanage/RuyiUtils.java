package ruyipkgmanage;

import java.io.*;

public class RuyiUtils {
    // 检查 ruyi 是否已安装
    public static boolean isRuyiInstalled() {
        File ruyi = new File("/usr/local/bin/ruyi");
        return ruyi.exists() && ruyi.canExecute();
    }

    // 获取本地 ruyi 版本
    public static String getLocalVersion() {
        try {
            Process proc = Runtime.getRuntime().exec("/usr/local/bin/ruyi --version");
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = reader.readLine();
            if (line != null && line.contains("version")) {
                String[] parts = line.trim().split(" ");
                return parts[parts.length - 1];
            }
        } catch (Exception e) {
            // ignore
        }
        return "unknown";
    }

    // 获取服务器最新版本（实际可通过网络请求，这里写死）
    public static String getRemoteVersion() {
        return "0.32.0";
    }

    // 判断是否需要升级
    public static boolean isRuyiNeedUpgrade() {
        String local = getLocalVersion();
        String remote = getRemoteVersion();
        return !local.equals(remote) && !"unknown".equals(local);
    }

    // 获取安装命令
    public static String getInstallCommand() {
        String arch = System.getProperty("os.arch");
        String url;
        if ("x86_64".equals(arch) || "amd64".equals(arch)) {
            url = "https://mirror.iscas.ac.cn/ruyisdk/ruyi/releases/0.32.0/ruyi.amd64";
        } else if ("aarch64".equals(arch)) {
            url = "https://mirror.iscas.ac.cn/ruyisdk/ruyi/releases/0.32.0/ruyi.arm64";
        } else if ("riscv64".equals(arch)) {
            url = "https://mirror.iscas.ac.cn/ruyisdk/ruyi/releases/0.32.0/ruyi.riscv64";
        } else {
            return null;
        }
        return "wget " + url + " -O /usr/local/bin/ruyi && chmod +x /usr/local/bin/ruyi";
    }

    // 自动升级（无需sudo）
    public static void autoUpgradeRuyi() throws IOException, InterruptedException {
        String cmd = getInstallCommand();
        if (cmd == null) throw new IOException("不支持的架构");
        String[] shellCmd = {"/bin/sh", "-c", cmd};
        Process proc = Runtime.getRuntime().exec(shellCmd);
        proc.waitFor();
    }
}
