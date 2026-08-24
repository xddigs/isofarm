package com.isofarm.utils;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Sensors;

import java.util.List;

@Utils
public final class Components {
    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final HardwareAbstractionLayer HARDWARE = SYSTEM_INFO.getHardware();
    private static final CentralProcessor CPU = HARDWARE.getProcessor();
    private static final GlobalMemory MEMORY = HARDWARE.getMemory();
    private static final Sensors SENSORS = HARDWARE.getSensors();

    private Components() {}

    public static String getCpu() {
        return CPU.getProcessorIdentifier().getName();
    }

    public static String getGpu() {
        List<GraphicsCard> graphicsCards = HARDWARE.getGraphicsCards();

        if (graphicsCards.isEmpty()) {
            return "GPU: N/A";
        }

        return graphicsCards.get(0).getName();
    }

    public static String getCpuTemperature() {
        double temperature = SENSORS.getCpuTemperature();
        if (temperature <= 0 || Double.isNaN(temperature)) {
            return "N/A";
        }

        return String.format("%.1f °C", temperature);
    }

    public static String getPhysicalMemory() {
        long total = MEMORY.getTotal();
        long available = MEMORY.getAvailable();

        long used = total - available;

        return String.format(
                "%s / %s (%d%%)",
                formatBytes(used),
                formatBytes(total),
                (int) ((used * 100.0) / total)
        );
    }

    private static String formatBytes(long bytes) {
        double gb = bytes / (1024.0 * 1024.0 * 1024.0);

        if (gb >= 1.0) {
            return String.format("%.1f GB", gb);
        }

        double mb = bytes / (1024.0 * 1024.0);
        return String.format("%.0f MB", mb);
    }
}