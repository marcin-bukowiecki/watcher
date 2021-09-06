package com.watcher.messages;

import com.watcher.model.Breakpoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Marcin Bukowiecki
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SetBreakpointMessage {

    private Breakpoint breakpoint;
}
