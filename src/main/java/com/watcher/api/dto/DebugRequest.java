package com.watcher.api.dto;

import com.watcher.DebugSessionStatus;
import lombok.Data;

/**
 * @author Marcin Bukowiecki
 *
 * Dto for debug session request ON or OFF. With basePackge field see {@link com.watcher.model.TransformationContext}
 *
 */
@Data
public class DebugRequest {

    private DebugSessionStatus debugSessionStatus;

    //supported package
    private String basePackages;
}
