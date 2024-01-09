package com.github.mname.analysis.mem.diff;

import com.github.mname.analysis.mem.diff.util.MemUtil;
import com.github.mname.analysis.mem.diff.vo.MemInfo;

import java.net.URL;
import java.util.List;

/**
 * @author huangzhuojie
 * @date 2024/1/6
 */
public class Main {

    public static void main(String[] args) {

        URL classpathUrl = Main.class.getClassLoader().getResource("./");
        if (classpathUrl == null) {
            throw new IllegalArgumentException("classpathUrl is null");
        }

        String classpath = classpathUrl.getPath();

        String pmapFilePath = System.getProperty("pmapFilePath", classpath + "/pmap.txt");
        String nmtFilePath = System.getProperty("nmtFilePath", classpath + "/nmt.txt");

        List<MemInfo> pmapMemList = MemUtil.getPmapMemList(pmapFilePath);
        List<MemInfo> nmtMemList = MemUtil.getNmtMemList(nmtFilePath);

        List<MemInfo> subList = MemUtil.removeOverlapping(pmapMemList, nmtMemList);

        subList.forEach(m -> System.out.printf("gdb --batch --pid <pid> -ex \"dump memory ./memory-%s-%s.dump 0x%s 0x%s\" " +
                                                       "&& strings memory-%s-%s.dump > strings-%s-%s.txt \n",
                                               Long.toHexString(m.getStartAddr()), Long.toHexString(m.getEndAddr()),
                                               Long.toHexString(m.getStartAddr()), Long.toHexString(m.getEndAddr()),
                                               Long.toHexString(m.getStartAddr()), Long.toHexString(m.getEndAddr()),
                                               Long.toHexString(m.getStartAddr()), Long.toHexString(m.getEndAddr())
                                              ));

    }

}
