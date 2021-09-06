package com.watcher;

import com.watcher.breakpoint.BreakpointFeature;
import com.watcher.data.CollectingDataFeature;
import com.watcher.events.BaseEvent;
import com.watcher.handlers.DebugSessionHandler;
import com.watcher.handlers.RemoveBreakpointHandler;
import com.watcher.handlers.SetBreakpointHandler;
import com.watcher.messages.BreakpointStatusMessage;
import com.watcher.messages.DebugSessionMessage;
import com.watcher.messages.RemoveBreakpointMessage;
import com.watcher.messages.SetBreakpointMessage;
import com.watcher.model.BreakpointData;
import com.watcher.model.WatcherSession;
import com.watcher.security.DefaultSecurityProvider;
import com.watcher.security.KnownHostsSecurityProvider;
import com.watcher.security.SecurityProvider;
import com.watcher.service.FeaturesPipeline;
import io.vertx.core.Vertx;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author Marcin Bukowiecki
 */
public class WatcherContext {

    public static final int API = Opcodes.ASM7;

    public final boolean logEnabled;

    //for simple startup purpose
    public final String mainPackage;

    //Watcher API port, default 10001
    @Getter
    public final int port;

    public boolean printASM;

    public static final ConcurrentHashMap<Long, Thread> RUNNING_THREADS = new ConcurrentHashMap<>();

    public static final String SOURCE_ROOT_DIR = "./src/main/java/";

    private static volatile WatcherContext instance;

    private final AtomicInteger classCounter = new AtomicInteger(0);

    private final Map<String, LoadedClassContext> loadedClasses = new ConcurrentHashMap<>();

    private final WatcherSession watcherSession;

    private final Instrumentation instrumentation;

    private final Vertx vertx;

    private final ReentrantLock loadedClassesContextLock = new ReentrantLock();

    private boolean eventBusInitialized = false;

    private final Set<String> knownHosts;

    @Getter
    private final DebugSessionHandler debugSessionHandler = new DebugSessionHandler(this);

    @Getter
    private final RemoveBreakpointHandler removeBreakpointHandler = new RemoveBreakpointHandler(this);

    @Getter
    private final SetBreakpointHandler setBreakpointHandler = new SetBreakpointHandler(this);

    public WatcherContext(Instrumentation instrumentation, ContextProvider contextProvider) {
        this.instrumentation = instrumentation;
        this.watcherSession = new WatcherSession(this);
        this.vertx = Vertx.vertx();
        this.port = Integer.getInteger("watcher.server.port", 10001);
        instance = this;
        this.logEnabled = Boolean.getBoolean("watcher.logger.enabled");
        this.printASM = Boolean.getBoolean("watcher.asm.print");
        this.mainPackage = System.getProperty("watcher.main.package", "").replace('/','.');
        final String prop = System.getProperty("watcher.security.known.hosts", "");
        if (StringUtils.isEmpty(prop)) {
            this.knownHosts = Collections.emptySet();
        } else {
            this.knownHosts = Arrays.stream(prop.split(",")).collect(Collectors.toSet());
        }
        initEventBus(contextProvider);
    }

    public static WatcherContext init(Instrumentation instrumentation, WatcherContextProvider watcherContextProvider) {
        if (instance != null) {
            throw new IllegalArgumentException("Watcher Context already initialized.");
        }

        synchronized (WatcherContext.class) {
            if (instance != null) {
                throw new IllegalArgumentException("Watcher Context already initialized.");
            }
            instance = new WatcherContext(instrumentation, watcherContextProvider);
        }

        return instance;
    }

    public static boolean logEnabled() {
        return getInstance().logEnabled;
    }

    public String getMainPackage() {
        return mainPackage;
    }

    public void initEventBus(ContextProvider contextProvider) {
        if (isEventBusInitialized()) {
            throw new UnsupportedOperationException("Event bus already initialized");
        }
        this.vertx.eventBus().consumer("breakpoint.set", setBreakpointHandler);
        this.vertx.eventBus().consumer("breakpoint.remove", removeBreakpointHandler);
        this.vertx.eventBus().consumer("debug.session", debugSessionHandler);

        this.vertx.eventBus().registerDefaultCodec(SetBreakpointMessage.class, contextProvider.getCodec(SetBreakpointMessage.class));
        this.vertx.eventBus().registerDefaultCodec(RemoveBreakpointMessage.class, contextProvider.getCodec(RemoveBreakpointMessage.class));
        this.vertx.eventBus().registerDefaultCodec(BreakpointStatusMessage.class, contextProvider.getCodec(BreakpointStatusMessage.class));
        this.vertx.eventBus().registerDefaultCodec(DebugSessionMessage.class, contextProvider.getCodec(DebugSessionMessage.class));
        this.vertx.eventBus().registerDefaultCodec(BreakpointData.class, contextProvider.getCodec(BreakpointData.class));

        this.eventBusInitialized = true;
    }

    public boolean isEventBusInitialized() {
        return eventBusInitialized;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public static WatcherContext getInstance() {
        if (instance == null) {
            throw new IllegalArgumentException("Watcher Context not initialized. Call init() method first.");
        }
        return instance;
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public WatcherSession getWatcherSession() {
        return watcherSession;
    }

    public LoadedClassContext getOrCreateLoadedClassContext(final String canonicalClassName) {
        LoadedClassContext loadedClassContext = loadedClasses.get(canonicalClassName);
        if (loadedClassContext == null) {
            loadedClassesContextLock.lock();
            try {
                var created = LoadedClassContext.builder()
                        .canonicalClassName(canonicalClassName)
                        .classId(classCounter.getAndIncrement())
                        .build();

                //double check
                LoadedClassContext doubleCheck = loadedClasses.get(canonicalClassName);
                if (doubleCheck == null) {
                    loadedClasses.put(canonicalClassName, created);
                    loadedClassContext = created;
                } else {
                    loadedClassContext = doubleCheck;
                }
            } finally {
                loadedClassesContextLock.unlock();
            }
        }
        return loadedClassContext;
    }

    public LoadedClassContext getLoadedClassContext(final String canonicalClassName) {
        return loadedClasses.get(canonicalClassName);
    }

    public Map<String, LoadedClassContext> getLoadedClassContext() {
        return Collections.unmodifiableMap(loadedClasses);
    }

    public FeaturesPipeline featuresPipeline(final String canonicalClassName) {
        final LoadedClassContext loadedClassContext = loadedClasses.get(canonicalClassName);
        if (loadedClassContext == null) {
            throw new UnsupportedOperationException("Loaded class context not created for " + canonicalClassName);
        }

        ArrayList<Feature> features = new ArrayList<>();

        if (watcherSession.isCollectingData()) {
            features.add(new CollectingDataFeature());
        }

        if (watcherSession.getDebugSessionStatus() == DebugSessionStatus.ON) {
            if (features.isEmpty()) {
                features.add(new CollectingDataFeature());
            }
            features.add(new BreakpointFeature());
        }

        return new FeaturesPipeline(features);
    }

    public void publishBreakpointData(BreakpointData breakpointData) {
        for (BaseEvent datum : breakpointData.getData()) {
            System.out.println(datum);
        }
        getVertx().eventBus().publish("debug.data", breakpointData);
    }

    public SecurityProvider getSecurityProvider() {
        if (knownHosts.isEmpty()) {
            return new DefaultSecurityProvider();
        } else {
            return new KnownHostsSecurityProvider(this.knownHosts);
        }
    }
}
