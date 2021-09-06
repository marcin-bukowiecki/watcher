package com.watcher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.watcher.WatcherContext;
import com.watcher.events.BaseEvent;
import com.watcher.events.ExceptionThrowEvent;
import com.watcher.events.GetLocalEvent;
import com.watcher.events.GetStaticField;
import com.watcher.events.MethodEnterEvent;
import com.watcher.events.MethodInvokeEvent;
import com.watcher.events.MethodReturnEvent;
import com.watcher.model.EntryPoint;
import com.watcher.utils.JavaASTUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Marcin Bukowiecki
 */
public class WatcherExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String[] ignoredPackages = {
            "java\\",
            "com\\fasterxml\\jackson\\",
            "com\\github\\javaparser\\",
            "com\\lmax\\",
            "sun\\",
            "jdk\\",
            "com\\sun\\",
            "org\\slf4j\\",
            "com\\watcher\\",
            "com\\intellij\\",
            "com\\google\\common\\collect\\",
            "org\\jetbrains\\",
            "org\\objectweb\\asm\\"
    };


    public static final WatcherExceptionHandler INSTANCE = new WatcherExceptionHandler();

    public static void dumpEvents(Throwable e) {
        Thread t = Thread.currentThread();
        dumpEvents(t, e);
        cleanup(t);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        dumpEvents(t, e);
        cleanup(t);
    }

    public static void dumpEvents(Thread thread, Throwable ex) {
        try {
            final List<BaseEvent> events = ThreadLocalCollector.THREAD_LOCAL.get().chopEvents();
            final List<StackTraceElement> stackTraceElements = Arrays.asList(ex.getStackTrace());
            Collections.reverse(stackTraceElements);
            final String content = toJson(events);
            final Date now = new Date();
            final Format sdf = new SimpleDateFormat("YYYY-MM-dd_hh-mm-ss");
            final Path file = Files.createFile(Paths.get("./WatcherStackTrace-" + sdf.format(now) + "-" + UUID.randomUUID().toString() + ".json"));
            Files.write(file, content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    private static void printCollectedData(Thread thread, Throwable ex) {
        final List<BaseEvent> events = ThreadLocalCollector.THREAD_LOCAL.get().chopEvents();
        final List<Node> road = new ArrayList<>();
        final List<StackTraceElement> stackTraceElements = Arrays.asList(ex.getStackTrace());
        Collections.reverse(stackTraceElements);
        EntryPoint entryPoint = null;
        int i = 0;
        for (StackTraceElement ste : stackTraceElements) {
            try {
                final String className = ste.getClassName();
                final String canonicalName = StringUtils.replace(className, ".", File.separator);

                if (StringUtils.startsWithAny(canonicalName, ignoredPackages)) {
                    continue;
                }

                final String methodName = ste.getMethodName();
                final int lineNumber = ste.getLineNumber();
                final CompilationUnit cu = JavaParser.parse(new File(WatcherContext.SOURCE_ROOT_DIR + File.separator + canonicalName + ".java"));

                loop:
                for (Node childrenNode : cu.getChildrenNodes()) {
                    if (childrenNode instanceof ClassOrInterfaceDeclaration) {
                        final ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration) childrenNode;
                        if (className.equals(cu.getPackage().getName() + "." + clazz.getName())) {

                            classChildren:
                            for (Node node : clazz.getChildrenNodes()) {
                                if (node instanceof MethodDeclaration) {
                                    final MethodDeclaration methodDeclaration = (MethodDeclaration) node;

                                    if (methodDeclaration.getName().equals(methodName) &&
                                            (methodDeclaration.getBeginLine() <= lineNumber && lineNumber <= methodDeclaration.getEndLine())) {

                                        if (i == 0) {
                                            entryPoint = new EntryPoint(
                                                    className,
                                                    methodDeclaration.getName(),
                                                    methodDeclaration.getNameExpr().getBeginLine(),
                                                    ex.getMessage() == null ? "" : ex.getMessage(),
                                                    ex.getClass().getCanonicalName(),
                                                    thread.getName(),
                                                    thread.getId()
                                            );
                                        }

                                       // road.add(methodDeclaration);

                                        for (Node methodChildren : methodDeclaration.getBody().getChildrenNodes()) {
                                            if (methodChildren.getBeginLine() < lineNumber) {
                                                JavaASTUtils.extractReferenceAccessNodes(methodChildren, road);
                                            }
                                            if (methodChildren.getBeginLine() == lineNumber) {
                                                JavaASTUtils.extractReferenceAccessNodes(methodChildren, road);
                                                break loop;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                i++;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final ArrayList<BaseEvent> filtered = new ArrayList<>();

        try {
            i = 0;
            for (BaseEvent event : events) {
                if (event instanceof MethodReturnEvent) {
                    continue;
                }
                if (event instanceof MethodEnterEvent) {
                    event.setLine(-1);
                    //event.setStartCol(-1);
                    //event.setEndCol(-1);
                    filtered.add(event);
                    continue;
                }

                while (true) {
                    final Node node = road.get(i);

                    if (event.getLine() != node.getBeginLine()) {
                        i++;
                        continue;
                    }

                    if (event instanceof MethodInvokeEvent && !(node instanceof MethodCallExpr)) {
                        break;
                    }

                    if (event instanceof MethodInvokeEvent) {
                        MethodCallExpr methodCallExpr = (MethodCallExpr) node;
                        if (methodCallExpr.getName().equals(((MethodInvokeEvent) event).getName())) {
                            //System.out.println(methodCallExpr.getNameExpr());
                            fillEventData(event, methodCallExpr.getNameExpr());
                            filtered.add(event);
                            i++;
                        }
                        break;
                    } else if (event instanceof GetLocalEvent && node instanceof NameExpr) {
                        String name = ((GetLocalEvent) event).getName();
                        if (name.equals(((NameExpr) node).getName())) {
                            //System.out.println(node);
                            fillEventData(event, node);
                            filtered.add(event);
                            i++;
                            break;
                        } else {
                            i++;
                            continue;
                        }
                    } else if (event instanceof ExceptionThrowEvent && node instanceof ThrowStmt) {
                        fillEventData(event, node);
                        filtered.add(event);
                        i++;
                        break;
                    } else if (event instanceof GetStaticField) {
                        final String name = ((GetStaticField) event).getName();

                        if (node instanceof FieldAccessExpr && ((FieldAccessExpr) node).getField().equals(name)) {
                            //System.out.println(((FieldAccessExpr) node).getFieldExpr());
                            FieldAccessExpr fieldAccess = (FieldAccessExpr) node;
                            fillEventData(event, fieldAccess.getFieldExpr());
                            filtered.add(event);
                            i++;
                            break;
                        } else if (node instanceof NameExpr && ((NameExpr) node).getName().equals(name)) {
                            //System.out.println(node);
                            fillEventData(event, node);
                            filtered.add(event);
                            i++;
                            break;
                        }
                    }
                    i++;
                }
            }

            final String content = toJson(entryPoint, filtered);
            final Path file = Files.createFile(Paths.get("./stackTrace-" + UUID.randomUUID().toString() + ".json"));
            Files.write(file, content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void fillEventData(BaseEvent event, Node node) {
        //event.setStartCol(node.getBeginColumn());
        //event.setEndCol(node.getEndColumn());
    }

    private static void cleanup(Thread t) {
        WatcherContext.RUNNING_THREADS.remove(t.getId());
        ThreadLocalCollector.THREAD_LOCAL.set(null);
    }

    private static String toJson(List<BaseEvent> events) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        final JsonContext jsonContext = new JsonContext();
        jsonContext.setEvents(events);
        return mapper.writeValueAsString(jsonContext);
    }

    private static String toJson(EntryPoint entryPoint, List<BaseEvent> events) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        if (WatcherContext.getInstance().logEnabled) {
            System.out.println(mapper.writeValueAsString(entryPoint));
        }

        for (BaseEvent event : events) {
            if (WatcherContext.getInstance().logEnabled) {
                System.out.println(mapper.writeValueAsString(event));
            }
        }

        final JsonContext jsonContext = new JsonContext();
        jsonContext.setEntryPoint(entryPoint);
        jsonContext.setEvents(events);
        return mapper.writeValueAsString(jsonContext);
    }

    @Data
    public static class JsonContext {

        private EntryPoint entryPoint;

        private List<BaseEvent> events;
    }
}
