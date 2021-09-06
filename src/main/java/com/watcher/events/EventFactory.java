package com.watcher.events;

/**
 * @author Marcin Bukowiecki
 */
public class EventFactory {

    public static final EventFactory INSTANCE = new EventFactory();

    public MethodReturnEvent getMethodReturnEvent(Object value, String typeName, String place) {
        return new MethodReturnEvent(value.toString(), typeName, place);
    }

    public ExceptionThrowEvent getExceptionEvent(String typeName, String place) {
        return new ExceptionThrowEvent(typeName, place);
    }

    public GetLocalEvent getGetLocalEvent(Object value, int index, String typeName, String name, String place) {
        return new GetLocalEvent(index, value.toString(), typeName, name, place);
    }

    public MethodInvokeEvent getMethodInvokeEvent(String owner, String name, String desc, String place) {
        return new MethodInvokeEvent(owner, name, desc, place);
    }

    public GetInstanceField getGetInstanceFieldEvent(Object value, String owner, String name, String typeName, String place) {
        return new GetInstanceField(owner, name, value.toString(), typeName, place);
    }

    public GetStaticField getGetStaticFieldEvent(Object value, String owner, String name, String typeName, String place) {
        return new GetStaticField(value.toString(), owner, name, typeName, place);
    }

    public BaseEvent methodEnterEvent(String methodName, String methodDescriptor, String place, Object[] args) {
        return new MethodEnterEvent(methodName, methodDescriptor, place, args);
    }

    public BaseEvent loopStart(String place) {
        return new LoopStart(place);
    }

    public BaseEvent iterationEnd(String place) {
        return new LoopStart(place);
    }
}
