package com.linghang.backend.mywust_basic.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.linghang.backend.mywust_basic.Utils.SystemMonitor;

@Tag(name = "服务管理接口", description = "用于管理所有微服务及查看日志")
@RestController
@RequestMapping("/service")
public class ServiceManagementController {
    private static final Logger logger = LoggerFactory.getLogger(ServiceManagementController.class);

    @Autowired
    private DiscoveryClient discoveryClient;

    private static final String PROJECT_ROOT = calculateProjectRoot();

    private static String calculateProjectRoot() {
        String userDir = System.getProperty("user.dir", ".");
        try {
            File dir = new File(userDir).getAbsoluteFile();
            if (dir.getName().equals("wust-mywust-basic")) {
                File parent = dir.getParentFile();
                return (parent != null) ? parent.getAbsolutePath() : userDir;
            }
            return dir.getAbsolutePath();
        } catch (Exception e) {
            return userDir;
        }
    }
    
    @Value("${logging.file.path:logs}")
    private String logPath;

    @Value("${logging.file.name:}")
    private String logName;

    private static final Map<String, ServiceConfig> SERVICE_CONFIGS = initServiceConfigs();

    private static Map<String, ServiceConfig> initServiceConfigs() {
        Map<String, ServiceConfig> configs = new LinkedHashMap<>();
        configs.put("wust-mywust-basic", new ServiceConfig("wust-mywust-basic", "wust-mywust-basic/target/wust-mywust-basic-0.0.5-SNAPSHOT.jar"));
        configs.put("wust-gateway", new ServiceConfig("wust-gateway", "wust-gateway/target/wust-gateway-0.0.5-SNAPSHOT.jar"));
        configs.put("wust-campus-mate", new ServiceConfig("wust-campus-mate", "wust-campus-mate/target/wust-campus-mate-0.0.5-SNAPSHOT.jar"));
        configs.put("wust-second-hand", new ServiceConfig("wust-second-hand", "wust-second-hand/target/wust-second-hand-0.0.5-SNAPSHOT.jar"));
        configs.put("wust-campus-cat", new ServiceConfig("wust-campus-cat", "wust-campus-cat/target/wust-campus-cat-0.0.5-SNAPSHOT.jar"));
        configs.put("wust-competition", new ServiceConfig("wust-competition", "wust-competition/target/wust-competition-0.0.5-SNAPSHOT.jar"));
        return configs;
    }

    private static class ServiceConfig {
        String serviceName;
        String relativeJarPath;
        public ServiceConfig(String serviceName, String relativeJarPath) {
            this.serviceName = serviceName;
            this.relativeJarPath = relativeJarPath;
        }
    }

    @Operation(summary = "系统运行概览")
    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        try {
            int totalServices = SERVICE_CONFIGS.size();
            int runningServices = 0;
            for (String name : SERVICE_CONFIGS.keySet()) {
                List<ServiceInstance> instances = discoveryClient.getInstances(name);
                if (instances != null && !instances.isEmpty()) {
                    runningServices++;
                }
            }
            overview.put("totalServices", totalServices);
            overview.put("runningServices", runningServices);
            
            com.sun.management.OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                    com.sun.management.OperatingSystemMXBean.class);
            double cpuLoad = osBean.getSystemCpuLoad();
            overview.put("cpuUsage", (cpuLoad < 0 ? "获取中" : SystemMonitor.df.format(cpuLoad * 100) + "%"));

            long totalMemory = osBean.getTotalPhysicalMemorySize();
            long freeMemory = osBean.getFreePhysicalMemorySize();
            long usedMemory = totalMemory - freeMemory;
            overview.put("memoryUsage", SystemMonitor.df.format((double) usedMemory / totalMemory * 100) + "%");
            overview.put("memoryDetail", String.format("%s / %s", SystemMonitor.formatSize(usedMemory), SystemMonitor.formatSize(totalMemory)));
            overview.put("envStatus", "良好");
        } catch (Exception e) {
            overview.put("envStatus", "异常");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", overview);
        return result;
    }

    @Operation(summary = "查看所有服务状态")
    @GetMapping("/status")
    public Map<String, Object> getAllServiceStatus() {
        List<Map<String, Object>> statusList = new ArrayList<>();
        for (String name : SERVICE_CONFIGS.keySet()) {
            statusList.add(getServiceStatus(name));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", statusList);
        return result;
    }

    public Map<String, Object> getServiceStatus(String serviceName) {
        Map<String, Object> status = new HashMap<>();
        status.put("serviceName", serviceName);
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            boolean isRegistered = instances != null && !instances.isEmpty();
            String pid = getPid(serviceName);
            boolean isRunning = isRegistered || (pid != null);
            status.put("running", isRunning);
            status.put("pid", pid);
            status.put("nacosRegistered", isRegistered);
        } catch (Exception e) {
            status.put("running", false);
        }
        return status;
    }

    private String getPid(String serviceName) throws IOException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if (isWindows) {
            Process p = Runtime.getRuntime().exec("cmd.exe /c jps -l");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(serviceName) && !line.contains("sun.tools.jps.Jps")) {
                        return line.trim().split("\\s+")[0];
                    }
                }
            }
        }
        return null;
    }

    @Operation(summary = "查看服务日志")
    @GetMapping("/log/{serviceName}")
    public Map<String, Object> viewLog(@PathVariable("serviceName") String serviceName, @RequestParam(value = "lines", defaultValue = "100") int lines) {
        Map<String, Object> result = new HashMap<>();
        try {
            File logFile = getLogFile(serviceName);
            if (logFile == null || !logFile.exists()) {
                result.put("code", 404);
                result.put("message", "未找到日志文件");
                result.put("path", logFile != null ? logFile.getAbsolutePath() : "所有预设路径均失败");
                result.put("content", Collections.singletonList("日志文件不存在，请检查 Nacos 中的配置"));
                return result;
            }
            List<String> allLines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(logFile), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) allLines.add(line);
            }
            int start = Math.max(0, allLines.size() - lines);
            result.put("code", 200);
            result.put("content", allLines.subList(start, allLines.size()));
        } catch (Exception e) {
            result.put("code", 500);
        }
        return result;
    }

    @GetMapping("/system/log")
    public Map<String, Object> viewSystemLog(@RequestParam(value = "lines", defaultValue = "100") int lines) {
        return viewLog("wust-mywust-basic", lines);
    }

    private File getLogFile(String serviceName) {
        if ("wust-mywust-basic".equals(serviceName) && logName != null && !logName.isEmpty()) {
            File file = new File(logName);
            if (file.exists()) return file;
        }

        File baseDir = (logPath != null && !logPath.equals("logs")) ? new File(logPath) : 
                      (logName != null && !logName.isEmpty()) ? new File(logName).getParentFile() : new File(PROJECT_ROOT);

        String pure = serviceName.contains("-") ? serviceName.substring(serviceName.lastIndexOf("-") + 1) : serviceName;
        String camelPure = pure.substring(0, 1).toLowerCase() + pure.substring(1);
        String pascalPure = pure.substring(0, 1).toUpperCase() + pure.substring(1);

        File[] candidates = {
            new File(baseDir, serviceName + ".log"),
            new File(baseDir, pure + "campus.log"), // 适配 catcampus.log
            new File(baseDir, pure + ".log"),       // 适配 gateway.log, competition.log
            new File(baseDir, pure + "mate.log"),   // 适配 campusmate.log 的变体
            new File(baseDir, "campus" + pascalPure + ".log"), // 适配 campusMate.log
            new File(baseDir, "secondhand.log"),    // 适配 second-hand -> secondhand.log
            new File(baseDir, "mywust" + pascalPure + ".log")
        };

        for (File f : candidates) if (f.exists()) return f;
        
        // 深度模糊匹配
        if (baseDir.exists() && baseDir.isDirectory()) {
            File[] allFiles = baseDir.listFiles((dir, name) -> name.endsWith(".log"));
            if (allFiles != null) {
                for (File f : allFiles) {
                    String n = f.getName().toLowerCase();
                    if (n.contains(pure.toLowerCase().replace("-", ""))) return f;
                }
            }
        }
        
        return candidates[0];
    }
}
