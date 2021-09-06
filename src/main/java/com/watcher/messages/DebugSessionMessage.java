package com.watcher.messages;

import com.watcher.DebugSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Marcin Bukowiecki
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebugSessionMessage {

    private DebugSessionStatus debugSessionStatus;

    private String basePackages;
}
