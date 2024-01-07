package com.github.mname.analysis.mem.addr.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author <a href="huangzhuojie@wxchina.com">huangzhuojie</a>
 * @date 2024/1/6
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
public class MemInfo {

    private Long startAddr;

    private Long endAddr;

}
