package com.watcher.messages;

import com.watcher.DebugSessionStatus;
import lombok.Data;

/**
 * @author Marcin Bukowiecki
 */
@Data
public class SetConfigMessage {

    private DebugSessionStatus debugSessionStatus;
}
