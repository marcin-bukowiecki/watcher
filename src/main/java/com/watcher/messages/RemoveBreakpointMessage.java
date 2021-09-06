package com.watcher.messages;

import com.watcher.model.Breakpoint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Marcin Bukowiecki
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoveBreakpointMessage {

    private Breakpoint breakpoint;
}
