package org.jboss.metadata.aggregate;

import org.jboss.metadata.*;
import java.util.*;

public class AggregateMetaData implements MetaData {
    MetaData[] plugins;

    public AggregateMetaData() {
        plugins = new MetaData[0];
    }

    public AggregateMetaData(MetaData[] plugins) {
        this.plugins = plugins;
        validate();
    }

    protected void addPlugin(MetaData plugin) {
        for(int i=0; i<plugins.length; i++) {
            if(plugins[i].getManager().equals(plugin.getManager())) {
                plugins[i] = plugin;
                return;
            }
        }
        LinkedList list = new LinkedList(Arrays.asList(plugins));
        list.add(plugin);
        plugins = (MetaData[])list.toArray(new MetaData[list.size()]);
        validate();
    }

    public boolean hasProperty(String name) {
        for(int i=0; i<plugins.length; i++)
            if(plugins[i].hasProperty(name))
                return true;
        return false;
    }

    public Object getProperty(String name) {
        for(int i=0; i<plugins.length; i++)
            if(plugins[i].hasProperty(name))
                return plugins[i].getProperty(name);
        throw new IllegalArgumentException("No such property '"+name+"'");
    }

    public void setProperty(String name, Object value) {
        for(int i=0; i<plugins.length; i++)
            if(plugins[i].hasProperty(name)) {
                plugins[i].setProperty(name, value);
                return;
            }
        throw new IllegalArgumentException("No such property '"+name+"'");
    }

    public String[] getPropertyNames() {
        String[][] names = new String[plugins.length][];
        int total = 0;
        for(int i=0; i<plugins.length; i++) {
            names[i] = plugins[i].getPropertyNames();
            total += names[i].length;
        }
        String[] result = new String[total];
        total = 0;
        for(int i=0; i<names.length; i++)
            for(int j=0; j<names[i].length; j++)
                result[total++] = names[i][j];
        return result;
    }

    public MetaDataPlugin getManager() {
        return null;
    }

    public void clear() {
        for(int i=0; i<plugins.length; i++)
            plugins[i].clear();
    }

    public boolean containsKey(Object key) {
        for(int i=0; i<plugins.length; i++)
            if(plugins[i].containsKey(key))
                return true;
        return false;
    }

    public boolean containsValue(Object value) {
        for(int i=0; i<plugins.length; i++)
            if(plugins[i].containsValue(value))
                return true;
        return false;
    }

    public Set entrySet() {
        HashSet set = new HashSet();
        for(int i=0; i<plugins.length; i++)
            set.addAll(plugins[i].entrySet());
        return set;
    }

    public Object get(Object key) {
        return getProperty((String)key);
    }

    public boolean isEmpty() {
        return plugins.length > 0;
    }

    public Set keySet() {
        HashSet set = new HashSet();
        for(int i=0; i<plugins.length; i++)
            set.addAll(plugins[i].keySet());
        return set;
    }

    public Object put(Object key, Object value) {
        for(int i=0; i<plugins.length; i++)
            if(plugins[i].containsKey(key))
                return plugins[i].put(key, value);
        throw new IllegalArgumentException("No such property '"+key+"'");
    }

    public void putAll(Map stuff) {
        Iterator it = stuff.keySet().iterator();
        while(it.hasNext()) {
            Object key = it.next();
            setProperty((String)key, stuff.get(key));
        }
    }

    public Object remove(Object key) {
        for(int i=0; i<plugins.length; i++)
            if(plugins[i].containsKey(key))
                return plugins[i].remove(key);
        throw new IllegalArgumentException("No such property '"+key+"'");
    }

    public int size() {
        int total = 0;
        for(int i=0; i<plugins.length; i++)
            total += plugins[i].size();
        return total;
    }

    public Collection values() {
        return new AbstractCollection() {
            public int size() {
                return AggregateMetaData.this.size();
            }

            public Iterator iterator() {
                return new AggregateIterator();
            }
        };
    }

    private void validate() {
        HashSet set = new HashSet();
        for(int i=0; i<plugins.length; i++) {
            String[] names = plugins[i].getPropertyNames();
            for(int j=0; j<names.length; j++)
                if(!set.add(names[j]))
                    throw new IllegalArgumentException("Found two properties named '"+names[j]+"'!");
        }
    }

    class AggregateIterator implements Iterator {
        int pos = 0;
        Iterator current;

        public boolean hasNext() {
            while((current == null || !current.hasNext())
                  && pos < plugins.length)
                current = plugins[pos++].values().iterator();
            return current != null && current.hasNext();
        }

        public Object next() {
            if(current == null || !current.hasNext()) hasNext();
            if(current == null) throw new NoSuchElementException();
            return current.next();
        }

        public void remove() {
            if(current != null)
                current.remove();
            else throw new IllegalStateException();
        }
    }
}