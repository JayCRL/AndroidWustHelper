package com.linghang.backend.mywust_basic.Utils;

import com.sun.management.OperatingSystemMXBean;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class SystemMonitor {
    public static final DecimalFormat df = new DecimalFormat("#.##");

    public static void main(String[] args) {
        try {
            // 显示内存使用情况
            printMemoryInfo();

            // 显示磁盘空间信息
            printDiskInfo();

            // 显示CPU使用率
            printCpuUsage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印内存使用情况
     */
    private static void printMemoryInfo() {
        System.out.println("=== 内存使用情况 ===");

        Runtime runtime = Runtime.getRuntime();

        // 总内存
        long totalMemory = runtime.totalMemory();
        // 空闲内存
        long freeMemory = runtime.freeMemory();
        // 已使用内存
        long usedMemory = totalMemory - freeMemory;
        // 最大可用内存
        long maxMemory = runtime.maxMemory();

        System.out.println("总内存: " + formatSize(totalMemory));
        System.out.println("已用内存: " + formatSize(usedMemory));
        System.out.println("空闲内存: " + formatSize(freeMemory));
        System.out.println("最大可用内存: " + formatSize(maxMemory));
        System.out.println();
    }

    /**
     * 打印磁盘空间信息
     */
    private static void printDiskInfo() {
        System.out.println("=== 磁盘空间信息 ===");

        // 获取系统根目录
        File[] roots = File.listRoots();

        for (File root : roots) {
            System.out.println("磁盘: " + root.getAbsolutePath());
            System.out.println("  总空间: " + formatSize(root.getTotalSpace()));
            System.out.println("  可用空间: " + formatSize(root.getFreeSpace()));
            System.out.println("  已用空间: " + formatSize(root.getTotalSpace() - root.getFreeSpace()));
            System.out.println("  使用率: " + df.format(
                    (double)(root.getTotalSpace() - root.getFreeSpace()) / root.getTotalSpace() * 100) + "%");
        }
        System.out.println();
    }

    /**
     * 打印CPU使用率
     */
    private static void printCpuUsage() throws InterruptedException {
        System.out.println("=== CPU使用情况 ===");

        // 使用Sun的扩展接口
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);

        // 获取CPU核心数
        int cpuCores = osBean.getAvailableProcessors();
        System.out.println("CPU核心数: " + cpuCores);

        // 获取JVM进程CPU使用率
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        String jvmName = runtimeBean.getName();
        long pid = Long.valueOf(jvmName.split("@")[0]);
        System.out.println("当前JVM进程ID: " + pid);

        // 计算系统CPU使用率
        long startCpuTime = osBean.getProcessCpuTime();
        long startTime = System.nanoTime();

        // 等待1秒
        TimeUnit.SECONDS.sleep(1);

        long endCpuTime = osBean.getProcessCpuTime();
        long endTime = System.nanoTime();

        long cpuTimeUsed = endCpuTime - startCpuTime;
        long elapsedTime = endTime - startTime;

        // 计算CPU使用率
        double cpuUsage = (double) cpuTimeUsed / elapsedTime / cpuCores * 100;
        System.out.println("系统CPU使用率: " + df.format(cpuUsage) + "%");
    }

    /**
     * 将字节数格式化为人易读的单位
     */
    public static String formatSize(long size) {
        if (size <= 0) return "0";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return df.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
    