package org.jboss.metadata.plugins;

import java.util.*;
import org.jboss.metadata.*;

public abstract class AbstractBean extends AbstractMetaData implements BeanMetaData {
    private String name;
    private HashSet methods;
    private HashSet homeMethods;
    private HashSet fields;
    private ContainerMetaData container;

    public AbstractBean() {
        methods = new HashSet();
        homeMethods = new HashSet();
        fields = new HashSet();
        try {
            container = (ContainerMetaData)getManager().getContainerClass().newInstance();
        } catch(Exception e) {e.printStackTrace();}
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContainerMetaData getContainer() {
        return container;
    }

    public void setContainerMetaData(ContainerMetaData container) {
        this.container = container;
    }

    public Set getMethods() {
        return methods;
    }

    public Set getHomeMethods() {
        return homeMethods;
    }

    public Set getFields() {
        return fields;
    }

    public void addMethod(MethodMetaData method) {
        methods.add(method);
    }

    public void addHomeMethod(MethodMetaData method) {
        homeMethods.add(method);
    }

    public void addField(FieldMetaData field) {
        fields.add(field);
    }

    public MethodMetaData getMethod(String name, Class[] paramTypes) {
        Iterator it = methods.iterator();
        while(it.hasNext()) {
            MethodMetaData mmd = (MethodMetaData)it.next();
            if(mmd.getName().equals(name) && sameParams(paramTypes, mmd.getParameterTypes()))
                return mmd;
        }
        throw new IllegalArgumentException("No such method!");
    }

    public MethodMetaData getHomeMethod(String name, Class[] paramTypes) {
        Iterator it = homeMethods.iterator();
        while(it.hasNext()) {
            MethodMetaData mmd = (MethodMetaData)it.next();
            if(mmd.getName().equals(name) && sameParams(paramTypes, mmd.getParameterTypes()))
                return mmd;
        }
        throw new IllegalArgumentException("No such method!");
    }

    public FieldMetaData getField(String name) {
        Iterator it = fields.iterator();
        while(it.hasNext()) {
            FieldMetaData fmd = (FieldMetaData)it.next();
            if(fmd.getName().equals(name))
                return fmd;
        }
        throw new IllegalArgumentException("No such field!");
    }

    private boolean sameParams(Class[] list1, Class[] list2) {
        if(list1.length != list2.length) return false;
        for(int i=0; i<list1.length; i++)
            if(!list1[i].equals(list2[i]))
                return false;
        return true;
    }
}