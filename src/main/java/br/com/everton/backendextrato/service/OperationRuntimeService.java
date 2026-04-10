package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.OperationRuntimeResponse;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class OperationRuntimeService {

    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final ObjectProvider<CacheManager> cacheManagerProvider;
    private final ApplicationAvailability applicationAvailability;
    private final String exposedActuatorEndpoints;

    public OperationRuntimeService(
            ApplicationContext applicationContext,
            Environment environment,
            ObjectProvider<CacheManager> cacheManagerProvider,
            ApplicationAvailability applicationAvailability
    ) {
        this.applicationContext = applicationContext;
        this.environment = environment;
        this.cacheManagerProvider = cacheManagerProvider;
        this.applicationAvailability = applicationAvailability;
        this.exposedActuatorEndpoints = environment.getProperty("management.endpoints.web.exposure.include", "");
    }

    public OperationRuntimeResponse snapshot() {
        Instant now = Instant.now();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        java.lang.management.OperatingSystemMXBean baseOperatingSystemMxBean = ManagementFactory.getOperatingSystemMXBean();
        OperatingSystemMXBean operatingSystemMXBean = baseOperatingSystemMxBean instanceof OperatingSystemMXBean cast
                ? cast
                : null;

        MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();

        List<String> cacheNames = cacheManagerProvider.getIfAvailable() == null
                ? List.of()
                : cacheManagerProvider.getIfAvailable().getCacheNames().stream().sorted().toList();

        return new OperationRuntimeResponse(
                now,
                environment.getProperty("spring.application.name", "application"),
                new OperationRuntimeResponse.SpringContainerSnapshot(
                        applicationContext.getBeanDefinitionCount(),
                        Arrays.stream(environment.getActiveProfiles()).sorted().toList(),
                        Instant.ofEpochMilli(runtimeMXBean.getStartTime()),
                        runtimeMXBean.getUptime(),
                        formatDuration(runtimeMXBean.getUptime()),
                        System.getProperty("java.version"),
                        System.getProperty("java.vendor"),
                        Path.of("").toAbsolutePath().normalize().toString(),
                        cacheNames.size(),
                        cacheNames
                ),
                new OperationRuntimeResponse.CpuSnapshot(
                        baseOperatingSystemMxBean.getAvailableProcessors(),
                        toPercent(operatingSystemMXBean == null ? null : operatingSystemMXBean.getSystemCpuLoad()),
                        toPercent(operatingSystemMXBean == null ? null : operatingSystemMXBean.getProcessCpuLoad()),
                        operatingSystemMXBean == null ? null : operatingSystemMXBean.getProcessCpuTime() / 1_000_000L,
                        sanitizeDouble(baseOperatingSystemMxBean.getSystemLoadAverage())
                ),
                new OperationRuntimeResponse.JvmSnapshot(
                        runtimeMXBean.getName(),
                        runtimeMXBean.getVmName(),
                        runtimeMXBean.getVmVendor(),
                        runtimeMXBean.getVmVersion(),
                        runtimeMXBean.getInputArguments(),
                        new OperationRuntimeResponse.ThreadSnapshot(
                                threadMXBean.getThreadCount(),
                                threadMXBean.getDaemonThreadCount(),
                                threadMXBean.getPeakThreadCount(),
                                threadMXBean.getTotalStartedThreadCount(),
                                deadlockedThreads == null ? 0 : deadlockedThreads.length
                        ),
                        new OperationRuntimeResponse.ClassLoadingSnapshot(
                                classLoadingMXBean.getLoadedClassCount(),
                                classLoadingMXBean.getTotalLoadedClassCount(),
                                classLoadingMXBean.getUnloadedClassCount()
                        ),
                        toMemoryArea(heapUsage),
                        toMemoryArea(nonHeapUsage),
                        ManagementFactory.getMemoryPoolMXBeans().stream()
                                .sorted(Comparator.comparing(MemoryPoolMXBean::getType).thenComparing(MemoryPoolMXBean::getName))
                                .map(this::toMemoryPool)
                                .toList()
                ),
                new OperationRuntimeResponse.HostMemorySnapshot(
                        operatingSystemMXBean == null ? 0L : operatingSystemMXBean.getTotalPhysicalMemorySize(),
                        computeUsed(
                                operatingSystemMXBean == null ? 0L : operatingSystemMXBean.getTotalPhysicalMemorySize(),
                                operatingSystemMXBean == null ? 0L : operatingSystemMXBean.getFreePhysicalMemorySize()
                        ),
                        operatingSystemMXBean == null ? 0L : operatingSystemMXBean.getFreePhysicalMemorySize(),
                        calculateUsagePercent(
                                operatingSystemMXBean == null ? 0L : operatingSystemMXBean.getTotalPhysicalMemorySize(),
                                computeUsed(
                                        operatingSystemMXBean == null ? 0L : operatingSystemMXBean.getTotalPhysicalMemorySize(),
                                        operatingSystemMXBean == null ? 0L : operatingSystemMXBean.getFreePhysicalMemorySize()
                                )
                        ),
                        operatingSystemMXBean == null ? 0L : operatingSystemMXBean.getTotalSwapSpaceSize(),
                        computeUsed(
                                operatingSystemMXBean == null ? 0L : operatingSystemMXBean.getTotalSwapSpaceSize(),
                                operatingSystemMXBean == null ? 0L : operatingSystemMXBean.getFreeSwapSpaceSize()
                        ),
                        operatingSystemMXBean == null ? 0L : operatingSystemMXBean.getFreeSwapSpaceSize()
                ),
                buildDiskSnapshot(),
                new OperationRuntimeResponse.ActuatorSnapshot(
                        resolveHealthStatus(),
                        applicationAvailability.getReadinessState().name(),
                        applicationAvailability.getLivenessState().name(),
                        Arrays.stream(exposedActuatorEndpoints.split(","))
                                .map(String::trim)
                                .filter(value -> !value.isBlank())
                                .sorted()
                                .toList()
                )
        );
    }

    private OperationRuntimeResponse.MemoryAreaSnapshot toMemoryArea(MemoryUsage usage) {
        Long max = usage.getMax() < 0 ? null : usage.getMax();
        return new OperationRuntimeResponse.MemoryAreaSnapshot(
                usage.getUsed(),
                usage.getCommitted(),
                max,
                calculateUsagePercent(max, usage.getUsed())
        );
    }

    private OperationRuntimeResponse.MemoryPoolSnapshot toMemoryPool(MemoryPoolMXBean pool) {
        MemoryUsage usage = pool.getUsage();
        if (usage == null) {
            return new OperationRuntimeResponse.MemoryPoolSnapshot(pool.getName(), 0L, 0L, null, null);
        }

        Long max = usage.getMax() < 0 ? null : usage.getMax();
        return new OperationRuntimeResponse.MemoryPoolSnapshot(
                pool.getType() == MemoryType.HEAP ? "Heap / " + pool.getName() : "Non-heap / " + pool.getName(),
                usage.getUsed(),
                usage.getCommitted(),
                max,
                calculateUsagePercent(max, usage.getUsed())
        );
    }

    private OperationRuntimeResponse.DiskSnapshot buildDiskSnapshot() {
        try {
            Path path = Path.of("").toAbsolutePath().normalize();
            FileStore fileStore = java.nio.file.Files.getFileStore(path);
            long total = fileStore.getTotalSpace();
            long usable = fileStore.getUsableSpace();
            long free = fileStore.getUnallocatedSpace();

            return new OperationRuntimeResponse.DiskSnapshot(
                    path.toString(),
                    total,
                    usable,
                    free,
                    calculateUsagePercent(total, total - usable)
            );
        } catch (IOException ex) {
            return new OperationRuntimeResponse.DiskSnapshot(
                    Path.of("").toAbsolutePath().normalize().toString(),
                    0L,
                    0L,
                    0L,
                    null
            );
        }
    }

    private String resolveHealthStatus() {
        ReadinessState readinessState = applicationAvailability.getReadinessState();
        LivenessState livenessState = applicationAvailability.getLivenessState();

        if (livenessState != LivenessState.CORRECT) {
            return "DOWN";
        }

        if (readinessState == ReadinessState.ACCEPTING_TRAFFIC) {
            return "UP";
        }

        return "OUT_OF_SERVICE";
    }

    private Double toPercent(Double fraction) {
        if (fraction == null || fraction.isNaN() || fraction < 0) {
            return null;
        }

        return Math.round(fraction * 1000d) / 10d;
    }

    private Double sanitizeDouble(double value) {
        if (Double.isNaN(value) || value < 0) {
            return null;
        }

        return Math.round(value * 10d) / 10d;
    }

    private Double calculateUsagePercent(Long total, long used) {
        if (total == null || total <= 0) {
            return null;
        }

        return Math.round(((double) used / total) * 1000d) / 10d;
    }

    private long computeUsed(long total, long free) {
        if (total <= 0) {
            return 0L;
        }

        return Math.max(total - Math.max(free, 0L), 0L);
    }

    private String formatDuration(long milliseconds) {
        Duration duration = Duration.ofMillis(milliseconds);
        long days = duration.toDays();
        duration = duration.minusDays(days);
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        long minutes = duration.toMinutes();
        duration = duration.minusMinutes(minutes);
        long seconds = duration.getSeconds();

        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            builder.append(minutes).append("m ");
        }
        builder.append(seconds).append("s");
        return builder.toString().trim();
    }
}
