package com.github.mname.analysis.mem.diff.util;

import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.HexUtil;
import com.github.mname.analysis.mem.diff.vo.MemInfo;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author huangzhuojie
 * @date 2024/1/9
 */
public class MemUtil {

    /**
     * 样例：[0x0000000775000000 - 0x00000007f8000000]
     */
    private static final Pattern NMT_PATTERN = Pattern.compile("^\\[0x\\S+ - 0x\\S+\\]");

    public static List<MemInfo> removeOverlapping(List<MemInfo> pmapMemList, List<MemInfo> nmtMemList) {
        List<MemInfo> subList = new ArrayList<>();
        int p = 0;
        int n = 0;
        long lastSplit = 0;
        while (p < pmapMemList.size() && n < nmtMemList.size()) {

            MemInfo pmapMem = pmapMemList.get(p);
            MemInfo nmtMem = nmtMemList.get(n);

            if (nmtMem.getStartAddr() >= pmapMem.getEndAddr()) {
                // pmapMem: -----------
                // nmtMem :             -----------
                p++;
                subList.add(pmapMem);
            } else if (nmtMem.getEndAddr() <= pmapMem.getStartAddr()) {
                // pmapMem:              -----------
                // nmtMem : -----------
                n++;
            } else if (nmtMem.getStartAddr() > pmapMem.getStartAddr()) {
                // pmapMem: -----------
                // nmtMem :     -----------
                // 或者
                // pmapMem: ---------------------------------
                // nmtMem :     -----------    -----------
                long max = Math.max(lastSplit, pmapMem.getStartAddr());
                subList.add(new MemInfo(max, nmtMem.getStartAddr()));
                lastSplit = nmtMem.getEndAddr();
                n++;
            } else if (nmtMem.getEndAddr() < pmapMem.getEndAddr()) {
                // pmapMem:     -----------
                // nmtMem : -----------
                subList.add(new MemInfo(nmtMem.getEndAddr(), pmapMem.getEndAddr()));
                n++;
            } else {
                p++;
                n++;
            }
        }
        return subList;
    }

    public static List<MemInfo> getPmapMemList(String pmapFilePath) {
        List<String> pmapLines = FileUtil.readLines(new File(pmapFilePath), StandardCharsets.UTF_8);

        List<MemInfo> memInfoList = new ArrayList<>(pmapLines.size());
        for (int i = 0; i < pmapLines.size(); i++) {
            if (i <= 1 || i >= (pmapLines.size() - 3)) {
                continue;
            }

            String line = pmapLines.get(i);
            line = line.trim();
            String[] columns = line.split("\\s+");
            String startAddrStr = columns[0];

            BigInteger startAddr = HexUtil.toBigInteger(startAddrStr);
            String perm = columns[1];
            String device = columns[3];
            if ("---p".equals(perm) || !"00:00".equals(device)) {
                continue;
            }

            int size = Integer.parseInt(columns[5]);
            BigInteger endAddr = startAddr.add(BigInteger.valueOf(size * 1024L));

            memInfoList.add(new MemInfo(startAddr.longValue(), endAddr.longValue()));

        }

        return mergeMemAddr(memInfoList);
    }

    public static List<MemInfo> getNmtMemList(String nmtFilePath) {
        List<String> nmtLines = FileUtil.readLines(new File(nmtFilePath), StandardCharsets.UTF_8);

        List<MemInfo> memInfoList = new ArrayList<>();
        for (String line : nmtLines) {
            Matcher matcher = NMT_PATTERN.matcher(line);
            if (matcher.find()) {
                String group = matcher.group();
                String[] addrs = group.split(" - ");
                String startAddrStr = addrs[0].replace("[", "")
                                              .replace("0x", "");
                String endAddrStr = addrs[1].replace("]", "")
                                            .replace("0x", "");

                memInfoList.add(new MemInfo(HexUtil.toBigInteger(startAddrStr).longValue(), HexUtil.toBigInteger(endAddrStr).longValue()));
            }
        }

        System.out.println();
        memInfoList = mergeMemAddr(memInfoList);

        return memInfoList;

    }

    public static List<MemInfo> mergeMemAddr(List<MemInfo> orgList) {
        sortMemInfoList(orgList);

        List<MemInfo> mergeList = new ArrayList<>(orgList.size());
        for (MemInfo memInfo : orgList) {
            if (mergeList.isEmpty()) {
                mergeList.add(memInfo);
                continue;
            }

            MemInfo lastOne = mergeList.get(mergeList.size() - 1);
            if (lastOne.getEndAddr() >= memInfo.getStartAddr()) {
                lastOne.setEndAddr(memInfo.getEndAddr());
            } else {
                mergeList.add(memInfo);
            }
        }

        return mergeList;
    }

    private static void sortMemInfoList(List<MemInfo> memInfoList) {
        memInfoList.sort((m1, m2) -> CompareUtil.compare(m1.getStartAddr(), m2.getStartAddr()));
    }

}
