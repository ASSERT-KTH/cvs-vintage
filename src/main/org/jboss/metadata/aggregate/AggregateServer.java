package org.jboss.metadata.aggregate;

import java.lang.reflect.*;
import java.util.*;
import org.jboss.metadata.*;

public class AggregateServer extends AggregateMetaData implements ServerMetaData {
    private AggregateBean[] beans;

    public AggregateServer() {
        super();
        beans = new AggregateBean[0];
    }

    public AggregateServer(ServerMetaData[] plugins) {
        this();
        for(int i=0; i<plugins.length; i++)
            addPlugin(plugins[i]);
    }

    public void addPlugin(ServerMetaData plugin) {
        super.addPlugin(plugin);
        Set incoming = plugin.getBeans();
        for(int i=0; i<beans.length; i++) {
            String name = beans[i].getName();
            try {
                BeanMetaData bmd = plugin.getBean(name);
                beans[i].addPlugin(bmd);
                incoming.remove(bmd);
            } catch(IllegalArgumentException e) {
                try {
                    Class cls = plugin.getManager().getBeanClass();
                    Object instance = cls.getConstructor(new Class[]{String.class}).newInstance(new Object[]{name});
                    beans[i].addPlugin((BeanMetaData)instance);
                } catch(Exception e2) {e.printStackTrace();}
            }
        }
        Vector v = new Vector(Arrays.asList(beans));
        for(Iterator it = incoming.iterator(); it.hasNext();) {
            BeanMetaData bmd = (BeanMetaData)it.next();
            String name = bmd.getName();
            BeanMetaData[] list = new BeanMetaData[MetaDataFactory.getPluginCount()];
            for(int i=0; i<list.length; i++) {
                Class cls = MetaDataFactory.getPlugin(i).getBeanClass();
                if(cls.equals(bmd.getClass()))
                    list[i] = bmd;
                else
                    try {
                        list[i] = (BeanMetaData)cls.newInstance();
                        Method m = cls.getMethod("setName", new Class[]{String.class});
                        m.invoke(list[i], new Object[]{name});
                    } catch(Exception e) {e.printStackTrace();}
            }
            v.addElement(new AggregateBean(name, list));
        }
        if(v.size() > beans.length)
            beans = (AggregateBean[])v.toArray(new AggregateBean[v.size()]);
    }

    public BeanMetaData getBean(String name) {
        for(int i=0; i<beans.length; i++)
            if(beans[i].getName().equals(name))
                return beans[i];
        throw new IllegalArgumentException("Can't find bean '"+name+"'");
    }

    public Set getBeans() {
        return new HashSet(Arrays.asList(beans));
    }
}