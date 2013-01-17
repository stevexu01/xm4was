package com.googlecode.xm4was.clmon;

import java.lang.ref.WeakReference;

public class ClassLoaderInfo {
    private final WeakReference<ClassLoader> ref;
    private final ClassLoaderGroup group;
    private boolean stopped;

    public ClassLoaderInfo(ClassLoader classLoader, ClassLoaderGroup group) {
        ref = new WeakReference<ClassLoader>(classLoader);
        this.group = group;
    }
    
    public ClassLoader getClassLoader() {
        return ref.get();
    }

    public ClassLoaderGroup getGroup() {
        return group;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    @Override
    public String toString() {
        return "ClassLoaderInfo[name=" + group.getName() + ",stopped=" + stopped + ",destroyed=" + (ref.get() == null) + "]";
    }
}