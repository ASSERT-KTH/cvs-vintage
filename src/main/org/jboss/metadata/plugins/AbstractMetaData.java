package org.jboss.metadata.plugins;

import org.jboss.metadata.*;
import java.lang.reflect.*;
import java.util.*;

public abstract class AbstractMetaData implements MetaData {
    private Field[] savedFields;

    public AbstractMetaData() {
    }

    public boolean hasProperty(String name) {
        try {
            getClass().getField(name);
            return true;
        } catch(NoSuchFieldException e) {}
        return false;
    }

    public Object getProperty(String name) {
        try {
            return getClass().getField(name).get(this);
        } catch(NoSuchFieldException e) {
        } catch(IllegalAccessException e) {
        }
        return null;
    }

    public void setProperty(String name, Object value) {
        try {
            getClass().getField(name).set(this, value);
        } catch(NoSuchFieldException e) {
            throw new IllegalArgumentException("No such field '"+name+"'");
        } catch(IllegalAccessException e) {
            throw new IllegalArgumentException("No such field '"+name+"'");
        }
    }

    public String[] getPropertyNames() {
        Vector list = new Vector();
        Field[] fields = getPublicNonStaticFields();
        for(int i=0; i<fields.length; list.addElement(fields[i++].getName()));
        return (String [])list.toArray(new String[list.size()]);
    }

    public void clear() {
        String[] list = getPropertyNames();
        for(int i=0; i<list.length; i++)
            remove(list[i]);
    }

    public boolean containsKey(Object key) {
        return key instanceof String && hasProperty((String)key);
    }

    public boolean containsValue(Object value) {
        Field[] fields = getPublicNonStaticFields();
        try {
            for(int i=0; i<fields.length; i++)
                if(fields[i].get(this).equals(value))
                    return true;
        } catch(IllegalAccessException e) {
        }
        return false;
    }

    public Set entrySet() {
        HashSet set = new HashSet();
        Field[] fields = getPublicNonStaticFields();
        for(int i=0; i<fields.length; set.add(new FieldEntry(fields[i++])));
        return set;
    }

    public Object get(Object key) {
        if(!(key instanceof String))
            return null;
        return getProperty((String)key);
    }

    public boolean isEmpty() {
        return false;
    }

    public Set keySet() {
        HashSet set = new HashSet();
        Field[] fields = getPublicNonStaticFields();
        for(int i=0; i<fields.length; set.add(fields[i++].getName()));
        return set;
    }

    public Object put(Object key, Object value) {
        Object old = getProperty((String)key);
        setProperty((String)key, value);
        return old;
    }

    public void putAll(Map source) {
        Iterator it = source.keySet().iterator();
        while(it.hasNext()) {
            Object key = it.next();
            setProperty((String)key, source.get(key));
        }
    }

    public Object remove(Object key) {
        Object old = getProperty((String)key);
        setProperty((String)key, null);
        return old;
    }

    public int size() {
        return getPropertyNames().length;
    }

    public Collection values() {
        return new FieldCollection();
    }

    private Field[] getPublicNonStaticFields() {
        if(savedFields != null)
            return savedFields;

        Field[] fields = getClass().getFields();
        Vector list = new Vector();
        for(int i=0; i<fields.length; i++)
            if(Modifier.isPublic(fields[i].getModifiers()) &&
               !Modifier.isStatic(fields[i].getModifiers()))
                list.addElement(fields[i]);
        savedFields = (Field[])list.toArray(new Field[list.size()]);
        return savedFields;
    }

    class FieldEntry implements Map.Entry {
        Field field;

        FieldEntry(Field field) {
            this.field = field;
        }

        public Object setValue(Object o) {
            try {
                Object old = field.get(AbstractMetaData.this);
                field.set(AbstractMetaData.this, o);
                return old;
            } catch(IllegalAccessException e) {
            }
            return null;
        }

        public Object getValue() {
            try {
                return field.get(AbstractMetaData.this);
            } catch(IllegalAccessException e) {
            }
            return null;
        }

        public Object getKey() {
            return field.getName();
        }
    }

    class FieldCollection extends AbstractCollection {
        int i=0;
        public FieldCollection() {
            getPublicNonStaticFields();
        }

        public Iterator iterator() {
            final AbstractMetaData md = AbstractMetaData.this;
            return new Iterator() {
                public boolean hasNext() {
                    return i < savedFields.length;
                }

                public Object next() {
                    try {
                        return savedFields[i++].get(md);
                    } catch(ArrayIndexOutOfBoundsException e) {
                        throw new NoSuchElementException();
                    } catch(IllegalAccessException e) {
                    }
                    return null;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public int size() {
            return savedFields.length;
        }
    }
}