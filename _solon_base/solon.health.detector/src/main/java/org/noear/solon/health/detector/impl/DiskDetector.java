package org.noear.solon.health.detector.impl;

import org.noear.solon.core.event.EventBus;
import org.noear.solon.health.detector.AbstractDetector;
import org.noear.solon.health.detector.util.CmdUtil;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 磁盘 探测器
 *
 * @author 夜の孤城
 * @since 1.2
 * */
public class DiskDetector extends AbstractDetector {
    private static final Pattern linuxDiskPattern = Pattern.compile("\\s+([\\d]+)\\s+([\\d]+)\\s+([\\d]+)\\s+([\\d]+%)\\s+(/.*)", 42);
    private static final Pattern macDiskPattern = Pattern.compile("\\s+([\\d]+)\\s+([\\d]+)\\s+([\\d]+)\\s+([\\d]+%)\\s+\\d+\\s+\\d+\\s+\\d+%\\s+(/.*)", 42);

    @Override
    public String getName() {
        return "disk";
    }

    @Override
    public Map<String, Object> getInfo() {
        Map<String, Object> details = new LinkedHashMap<>();


        long totals = 0;
        long totalUsed = 0;
        if (osName.indexOf("windows") > -1) {//is windows
            for (File file : File.listRoots()) {
                Map<String, Object> disk = new HashMap<>();

                long total = file.getTotalSpace();
                long free = file.getFreeSpace();
                long used = total - free;
                totals += total;
                totalUsed += used;
                String ratio = (total > 0L ? used * 100L / total : 0L) + "%";

                disk.put("total", formatByteSize(total));
                disk.put("free", formatByteSize(free));
                disk.put("used", formatByteSize(used));
                disk.put("ratio", ratio);

                details.put(file.getPath(), disk);
            }
        } else {
            try {
                String text = CmdUtil.execute("df", "-m");
                boolean isMac = osName.indexOf("mac os") > -1;
                List<String[]> disks = this.matcher(isMac ? macDiskPattern : linuxDiskPattern, text);

                for (String[] disk : disks) {
                    Map<String, Object> diskInfo = new HashMap<>();
                    long total = Long.valueOf(disk[1]) * 1024L * 1024L;
                    long used = Long.valueOf(disk[2]) * 1024L * 1024L;
                    long free = Long.valueOf(disk[3]) * 1024L * 1024L;
                    String ratio = disk[4];

                    diskInfo.put("total", formatByteSize(total));
                    diskInfo.put("free", formatByteSize(free));
                    diskInfo.put("used", formatByteSize(used));
                    diskInfo.put("ratio", ratio);
                    details.put(disk[5], diskInfo);

                    totals += total;
                    totalUsed += used;
                }
            } catch (Exception e) {
                EventBus.pushTry(e);
                details.put("error", "Get Disk Failed:" + e.getMessage());
            }

        }

        Map<String, Object> info = new LinkedHashMap<>();

        info.put("total", formatByteSize(totals));
        info.put("used", formatByteSize(totalUsed));
        info.put("details", details);

        return info;
    }
}
