package com.linghang.backend.mywust_basic.Controller;

import com.linghang.backend.mywust_basic.Utils.R;
import com.linghang.backend.mywust_basic.Utils.SystemMonitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Tag(name = "系统监控接口", description = "获取JVM、系统内存、磁盘、CPU等监控信息")
@RestController
@RequestMapping("/system/monitor")
public class SystemMonitorController {
    private static final Logger logger = LoggerFactory.getLogger(SystemMonitorController.class);

    /**
     * 获取JVM内存信息
     */
    @Operation(summary = "获取JVM内存使用情况", description = "返回JVM总内存、已用内存、空闲内存等信息")
    @GetMapping("/jvm-memory")
    public R<Map<String, String>> getJvmMemoryInfo() {
        try {
            Map<String, String> jvmMemoryMap = new HashMap<>();
            Runtime runtime = Runtime.getRuntime();

            long totalJvmMemory = runtime.totalMemory();
            long freeJvmMemory = runtime.freeMemory();
            long usedJvmMemory = totalJvmMemory - freeJvmMemory;
            long maxJvmMemory = runtime.maxMemory();

            jvmMemoryMap.put("total", SystemMonitor.formatSize(totalJvmMemory));
            jvmMemoryMap.put("used", SystemMonitor.formatSize(usedJvmMemory));
            jvmMemoryMap.put("free", SystemMonitor.formatSize(freeJvmMemory));
            jvmMemoryMap.put("max", SystemMonitor.formatSize(maxJvmMemory));

            return R.success(jvmMemoryMap);
        } catch (Exception e) {
            logger.error("获取JVM内存信息失败", e);
            return R.failure(500, "获取JVM内存信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取系统物理内存信息
     */
    @Operation(summary = "获取系统物理内存使用情况", description = "返回系统总内存、已用内存、可用内存及使用率")
    @GetMapping("/system-memory")
    public R<Map<String, String>> getSystemMemoryInfo() {
        try {
            Map<String, String> systemMemoryMap = new HashMap<>();
            com.sun.management.OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                    com.sun.management.OperatingSystemMXBean.class);

            long totalSystemMemory = osBean.getTotalMemorySize();
            long freeSystemMemory = osBean.getFreeMemorySize();
            long usedSystemMemory = totalSystemMemory - freeSystemMemory;
            double usage = (double) usedSystemMemory / totalSystemMemory * 100;

            systemMemoryMap.put("total", SystemMonitor.formatSize(totalSystemMemory));
            systemMemoryMap.put("used", SystemMonitor.formatSize(usedSystemMemory));
            systemMemoryMap.put("free", SystemMonitor.formatSize(freeSystemMemory));
            systemMemoryMap.put("usage", SystemMonitor.df.format(usage) + "%");

            return R.success(systemMemoryMap);
        } catch (Exception e) {
            logger.error("获取系统内存信息失败", e);
            return R.failure(500, "获取系统内存信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取磁盘空间信息
     */
    @Operation(summary = "获取磁盘空间信息", description = "返回各磁盘分区的总空间、可用空间及使用率")
    @GetMapping("/disk")
    public R<Map<String, Map<String, String>>> getDiskInfo() {
        try {
            Map<String, Map<String, String>> diskMap = new HashMap<>();
            java.io.File[] roots = java.io.File.listRoots();

            for (java.io.File root : roots) {
                Map<String, String> partitionInfo = new HashMap<>();
                long totalSpace = root.getTotalSpace();
                long freeSpace = root.getFreeSpace();
                long usedSpace = totalSpace - freeSpace;
                double usage = (double) usedSpace / totalSpace * 100;

                partitionInfo.put("total", SystemMonitor.formatSize(totalSpace));
                partitionInfo.put("used", SystemMonitor.formatSize(usedSpace));
                partitionInfo.put("free", SystemMonitor.formatSize(freeSpace));
                partitionInfo.put("usage", SystemMonitor.df.format(usage) + "%");

                diskMap.put(root.getAbsolutePath(), partitionInfo);
            }

            return R.success(diskMap);
        } catch (Exception e) {
            logger.error("获取磁盘信息失败", e);
            return R.failure(500, "获取磁盘信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取CPU使用率信息
     * 注意：接口会阻塞1秒（用于计算CPU使用率）
     */
    @Operation(summary = "获取CPU使用率信息", description = "返回CPU核心数、JVM进程ID及系统CPU使用率（接口会阻塞1秒）")
    @GetMapping("/cpu-usage")
    public R<Map<String, Object>> getCpuUsage() {
        try {
            Map<String, Object> cpuMap = new HashMap<>();
            com.sun.management.OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                    com.sun.management.OperatingSystemMXBean.class);

            // CPU核心数
            int cpuCores = osBean.getAvailableProcessors();
            cpuMap.put("cpuCores", cpuCores);

            // JVM进程ID
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            String jvmName = runtimeBean.getName();
            long pid = Long.valueOf(jvmName.split("@")[0]);
            cpuMap.put("jvmPid", pid);

            // 计算CPU使用率（需要1秒采样）
            long startCpuTime = osBean.getProcessCpuTime();
            long startTime = System.nanoTime();
            TimeUnit.SECONDS.sleep(1); // 阻塞1秒，用于计算
            long endCpuTime = osBean.getProcessCpuTime();
            long endTime = System.nanoTime();

            long cpuTimeUsed = endCpuTime - startCpuTime;
            long elapsedTime = endTime - startTime;
            double cpuUsage = (double) cpuTimeUsed / elapsedTime / cpuCores * 100;
            cpuMap.put("cpuUsage", SystemMonitor.df.format(cpuUsage) + "%");

            return R.success(cpuMap);
        } catch (InterruptedException e) {
            logger.error("CPU使用率计算被中断", e);
            Thread.currentThread().interrupt(); // 恢复中断状态
            return R.failure(500, "CPU使用率计算被中断：" + e.getMessage());
        } catch (Exception e) {
            logger.error("获取CPU信息失败", e);
            return R.failure(500, "获取CPU信息失败：" + e.getMessage());
        }
    }
}