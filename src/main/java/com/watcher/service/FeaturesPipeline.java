package com.watcher.service;

import com.watcher.Feature;
import com.watcher.model.TransformationContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public class FeaturesPipeline {

    private final List<Feature> features;

    public FeaturesPipeline(final List<Feature> features) {
        this.features = new ArrayList<>(features.stream()
                .collect(Collectors.toMap(Feature::getPriority, Function.identity())).values()).stream()
                .sorted(Comparator.comparingInt(Feature::getPriority)).collect(Collectors.toList());
    }

    public boolean handle(final TransformationContext transformationContext) {
        int size = features.size();
        log.info("Got {} features to append", size);
        for (final Feature feature : features) {
            feature.handleTransformation(transformationContext);
        }
        return size > 0;
    }
}
