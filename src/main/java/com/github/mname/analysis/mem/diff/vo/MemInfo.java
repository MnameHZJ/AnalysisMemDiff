package com.github.mname.analysis.mem.diff.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author huangzhuojie
 * @date 2024/1/6
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
public class MemInfo {

    private Long startAddr;

    private Long endAddr;

}
