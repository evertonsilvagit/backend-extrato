package br.com.everton.backendextrato.dto;

import java.time.Instant;
import java.util.List;

public record OperationRuntimeResponse(
        Instant generatedAt,
        String applicationName,
        SpringContainerSnapshot spring,
        CpuSnapshot cpu,
        JvmSnapshot jvm,
        HostMemorySnapshot hostMemory,
        DiskSnapshot disk,
        ActuatorSnapshot actuator
) {
    public record SpringContainerSnapshot(
            int beanDefinitionCount,
            List<String> activeProfiles,
            Instant startedAt,
            long uptimeMs,
            String uptimeLabel,
            String javaVersion,
            String javaVendor,
            String workingDirectory,
            int cacheCount,
            List<String> cacheNames
    ) {
    }

    public record CpuSnapshot(
            int availableProcessors,
            Double systemUsagePercent,
            Double processUsagePercent,
            Long processCpuTimeMs,
            Double systemLoadAverage
    ) {
    }

    public record JvmSnapshot(
            String runtimeName,
            String vmName,
            String vmVendor,
            String vmVersion,
            List<String> inputArguments,
            ThreadSnapshot threads,
            ClassLoadingSnapshot classes,
            MemoryAreaSnapshot heap,
            MemoryAreaSnapshot nonHeap,
            List<MemoryPoolSnapshot> memoryPools
    ) {
    }

    public record ThreadSnapshot(
            int live,
            int daemon,
            int peak,
            long totalStarted,
            int deadlocked
    ) {
    }

    public record ClassLoadingSnapshot(
            int loaded,
            long totalLoaded,
            long unloaded
    ) {
    }

    public record MemoryAreaSnapshot(
            long usedBytes,
            long committedBytes,
            Long maxBytes,
            Double usagePercent
    ) {
    }

    public record MemoryPoolSnapshot(
            String name,
            long usedBytes,
            long committedBytes,
            Long maxBytes,
            Double usagePercent
    ) {
    }

    public record HostMemorySnapshot(
            long totalPhysicalBytes,
            long usedPhysicalBytes,
            long freePhysicalBytes,
            Double physicalUsagePercent,
            long totalSwapBytes,
            long usedSwapBytes,
            long freeSwapBytes
    ) {
    }

    public record DiskSnapshot(
            String path,
            long totalBytes,
            long usableBytes,
            long freeBytes,
            Double usagePercent
    ) {
    }

    public record ActuatorSnapshot(
            String healthStatus,
            String readinessState,
            String livenessState,
            List<String> exposedEndpoints
    ) {
    }
}
