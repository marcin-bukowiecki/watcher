package com.watcher;

import com.watcher.events.EventFactory;
import com.watcher.service.ThreadLocalCollector;
import com.watcher.service.WatcherExceptionHandler;

/**
 * @author Marcin Bukowiecki
 *
 * Methods of this class are invoked from the running application thread.
 * Call sites are added during bytecode transformation see {@link com.watcher.asm.WatcherDataCollectorVisitor}
 */
public final class StackValueHandler {

    private static final ThreadLocalCollector threadLocalCollector = ThreadLocalCollector.INSTANCE;

    private static final EventFactory EVENT_FACTORY = EventFactory.INSTANCE;

    //TODO remove place and use new StackWalker API
    /**
     * Thread enters method
     *
     * @param methodName entered method name
     * @param methodDescriptor entered method descriptor
     * @param place owner of entered method
     * @param args entered method arguments
     */
    public static void methodEnter(String methodName, String methodDescriptor, String place, Object[] args) {
        threadLocalCollector.setCurrentPlace(place);
        threadLocalCollector.onEvent(EVENT_FACTORY.methodEnterEvent(methodName, methodDescriptor, place, args));
    }

    /**
     * Thread leaves method
     */
    public static void methodExit() {
        threadLocalCollector.popCurrentPlace();
        threadLocalCollector.popCurrentLine();
    }

    /**
     * Thread started a loop
     *
     * If GOTO goes back
     */
    public static void loopStart() {
        threadLocalCollector.onEvent(EVENT_FACTORY.loopStart(threadLocalCollector.getCurrentPlace()));
    }

    /**
     * Thread leaves a loop
     */
    public static void loopFinish() {
        threadLocalCollector.loopFinish();
    }

    /**
     * Thread finishes single loop iteration
     */
    public static void iterationEnd() {
        threadLocalCollector.iterationEnd();
    }


    /**
     * Thread accesses a static field of Object type
     *
     * @param value value of field
     * @param owner owner of field
     * @param name name of static field
     * @return value of field
     */
    public static Object getStaticField(Object value, String owner, String name) {
        String typeName;
        if (value == null) {
            typeName = "NULL";
            value = "NULL";
        } else {
            typeName = value.getClass().getCanonicalName();
        }
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetStaticFieldEvent(value , owner, name, typeName, threadLocalCollector.getCurrentPlace()));
        return value;
    }

    /**
     * Thread accesses a static field of long type
     *
     * @param value value of field
     * @param owner owner of field
     * @param name name of static field
     * @return value of field
     */
    public static long getStaticField(long value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetStaticFieldEvent(value, owner, name, "long", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    /**
     * Thread accesses a static field of int type
     *
     * @param value value of field
     * @param owner owner of field
     * @param name name of static field
     * @return value of field
     */
    public static int getStaticField(int value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetStaticFieldEvent(value, owner, name, "int", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    /**
     * Thread accesses a static field of boolean type
     *
     * @param value value of field
     * @param owner owner of field
     * @param name name of static field
     * @return value of field
     */
    public static boolean getStaticField(boolean value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetStaticFieldEvent(value, owner, name, "boolean", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    /**
     * Thread accesses a static field of double type
     *
     * @param value value of field
     * @param owner owner of field
     * @param name name of static field
     * @return value of field
     */
    public static double getStaticField(double value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetStaticFieldEvent(value, owner, name, "double", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    /**
     * Thread accesses a static field of float type
     *
     * @param value value of field
     * @param owner owner of field
     * @param name name of static field
     * @return value of field
     */
    public static float getStaticField(float value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetStaticFieldEvent(value, owner, name, "float", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    /**
     * Thread accesses a static field of byte type
     *
     * @param value value of field
     * @param owner owner of field
     * @param name name of static field
     * @return value of field
     */
    public static byte getStaticField(byte value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetStaticFieldEvent(value, owner, name, "byte", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    /**
     * Thread accesses a static field of char type
     *
     * @param value value of field
     * @param owner owner of field
     * @param name name of static field
     * @return value of field
     */
    public static char getStaticField(char value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetStaticFieldEvent(value, owner, name, "char", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    /**
     * Thread accesses a static field of short type
     *
     * @param value value of field
     * @param owner owner of field
     * @param name name of static field
     * @return value of field
     */
    public static short getStaticField(short value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetStaticFieldEvent(value, owner, name, "short", threadLocalCollector.getCurrentPlace()));
        return value;
    }



    public static void reachBreakpoint(String classCanonicalName, int line) {
        threadLocalCollector.breakpointReached(classCanonicalName, line);
    }



    public static Object getInstanceField(Object value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetInstanceFieldEvent(value, owner, name, value != null ? value.getClass().getCanonicalName() : "NULL", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static int getInstanceField(int value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetInstanceFieldEvent(value, owner, name, "int", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static boolean getInstanceField(boolean value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetInstanceFieldEvent(value, owner, name, "boolean", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static float getInstanceField(float value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetInstanceFieldEvent(value, owner, name, "float", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static double getInstanceField(double value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetInstanceFieldEvent(value, owner, name, "double", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static long getInstanceField(long value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetInstanceFieldEvent(value, owner, name, "long", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static byte getInstanceField(byte value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetInstanceFieldEvent(value, owner, name, "byte", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static short getInstanceField(short value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetInstanceFieldEvent(value, owner, name, "short", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static char getInstanceField(char value, String owner, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetInstanceFieldEvent(value, owner, name, "char", threadLocalCollector.getCurrentPlace()));
        return value;
    }



    public static boolean getLocal(boolean value, int index, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetLocalEvent(value, index, "boolean", name, threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static int getLocal(int value, int index, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetLocalEvent(value, index, "int", name, threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static void getLocal(Object value, int index, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetLocalEvent(value, index, value != null ? value.getClass().getCanonicalName() : "NULL", name, threadLocalCollector.getCurrentPlace()));
    }

    public static float getLocal(float value, int index, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetLocalEvent(value, index, "float", name, threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static long getLocal(long value, int index, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetLocalEvent(value, index, "long", name, threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static double getLocal(double value, int index, String name) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getGetLocalEvent(value, index, "double", name, threadLocalCollector.getCurrentPlace()));
        return value;
    }


    /**
     * On bytecode ATHROW
     *
     * @param instance thrown Throwable instance
     * @return {@link Throwable} instance
     */
    public static Throwable exceptionThrown(Throwable instance) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getExceptionEvent(instance.getClass().getCanonicalName(), threadLocalCollector.getCurrentPlace()));
        if (WatcherContext.getInstance().getWatcherSession().isForceDumpOnException()) {
            WatcherExceptionHandler.dumpEvents(Thread.currentThread(), instance);
        }
        return instance;
    }


    public static boolean methodReturn(boolean value) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getMethodReturnEvent(value, "boolean", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static int methodReturn(int value) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getMethodReturnEvent(value, "int", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static Object methodReturn(Object value) {
        if (value == null) {
            threadLocalCollector.onEvent(EVENT_FACTORY.getMethodReturnEvent("NULL", "NullType", threadLocalCollector.getCurrentPlace()));
            return null;
        } else {
            threadLocalCollector.onEvent(EVENT_FACTORY.getMethodReturnEvent(value, value.getClass().getCanonicalName(), threadLocalCollector.getCurrentPlace()));
            return value;
        }
    }

    public static float methodReturn(float value) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getMethodReturnEvent(value, "float", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static long methodReturn(long value) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getMethodReturnEvent(value, "long", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static double methodReturn(double value) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getMethodReturnEvent(value, "double", threadLocalCollector.getCurrentPlace()));
        return value;
    }

    public static void methodReturn() {
        threadLocalCollector.onEvent(EVENT_FACTORY.getMethodReturnEvent( null,"void", threadLocalCollector.getCurrentPlace()));
    }



    public static void methodInvoke(String owner, String name, String desc) {
        threadLocalCollector.onEvent(EVENT_FACTORY.getMethodInvokeEvent(owner, name, desc, threadLocalCollector.getCurrentPlace()));
    }

    /**
     * Sets current line execution
     *
     * @param line given line number
     */
    public static void currentLine(int line) {
        threadLocalCollector.setCurrentLine(line);
    }

    public static String getDescriptor() {
        return StackValueHandler.class.getCanonicalName().replace('.','/');
    }

    public static void handlePrintStackTrace(Throwable t) {
        System.out.println("Dupa");
    }
}
