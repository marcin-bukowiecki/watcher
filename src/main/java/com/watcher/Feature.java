package com.watcher;

import com.watcher.model.TransformationContext;

/**
 * @author Marcin Bukowiecki
 */
public interface Feature {

    void handleTransformation(final TransformationContext transformationContext);

    int getPriority();
}
