/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.io.*;
import java.util.*;
import org.jboss.metadata.aggregate.AggregateServer;
import org.jboss.metadata.io.*;

/**
 * Serves as the global repository for metadata plugins, and can construct
 * sets of metadata from the available plugins.  This does not handle a
 * situation where some groups of plugins may be mutually exclusive.
 * @see org.jboss.metadata.MetaDataPlugin
 */
public class MetaDataFactory {

    /**
     * Holds the class names and Class variables for primitives.  Used to set
     * or decode properties that are stored in primitive variables.
     */
    public final static HashMap primitives = new HashMap();
    static {
        primitives.put("int", Integer.TYPE);
        primitives.put("float", Float.TYPE);
        primitives.put("double", Double.TYPE);
        primitives.put("long", Long.TYPE);
        primitives.put("byte", Byte.TYPE);
        primitives.put("short", Short.TYPE);
        primitives.put("boolean", Boolean.TYPE);
        primitives.put("char", Character.TYPE);
    }
    private static MetaDataPlugin[] plugins = new MetaDataPlugin[0];

    /**
     * Gets the number of plugins that have registered.
     */
    public static int getPluginCount() {
        return plugins.length;
    }

    /**
     * Gets the plugin at a specified index.  Once registered, a plugin will
     * have the same index until the JVM is shut down.
     */
    public static MetaDataPlugin getPlugin(int index) {
        return plugins[index];
    }

    /**
     * Registers a new plugin.
     */
    public static void addPlugin(MetaDataPlugin plugin) {
        for(int i=0; i<plugins.length; i++)
            if(plugins[i].getClass().equals(plugin.getClass()))
                return;
        LinkedList list = new LinkedList(Arrays.asList(plugins));
        list.add(plugin);
        plugins = (MetaDataPlugin[])list.toArray(new MetaDataPlugin[list.size()]);
    }

    /**
     * Loads XML files from a specified directory.  Probably should be enhanced
     * to handle URLs and JARs to be truly useful.
     */
    public static ServerMetaData loadXMLFile(File directory) {
        ServerMetaData[] list = new ServerMetaData[plugins.length];
        for(int i=0; i<list.length; i++) {
            XMLReader reader = plugins[i].getXMLReader();
            File source = new File(directory, reader.getFileName());
            try {
                BufferedReader input = new BufferedReader(new FileReader(source));
                list[i] = reader.readXML(input);
                input.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return new AggregateServer(list);
    }

    /**
     * Sample - you give it a directory, it loads XML files form there and
     * dumps metadata to the console.
     */
    public static void main(String params[]) {
        addPlugin(org.jboss.metadata.ejbjar.EJBPlugin.instance());
        addPlugin(org.jboss.metadata.jboss.JBossPlugin.instance());
        addPlugin(org.jboss.metadata.jaws.JAWSPlugin.instance());
        File f = new File(params[0]);
        ServerMetaData cont = loadXMLFile(f);
        dumpMetaData("", cont);
        Set beans = cont.getBeans();
        System.out.println("Found "+beans.size()+" beans:");
        Iterator it = beans.iterator();
        while(it.hasNext()) {
            BeanMetaData bmd = (BeanMetaData)it.next();
            System.out.println("  Bean '"+bmd.getName()+"'");
            dumpMetaData("    ", bmd);
            System.out.println("    Container");
            dumpMetaData("    ", bmd.getContainer());

            Set methods = bmd.getMethods();
            System.out.println("    Found "+methods.size()+" methods:");
            Iterator itm = methods.iterator();
            while(itm.hasNext()) {
                MethodMetaData mmd = (MethodMetaData)itm.next();
                System.out.print("    Method '"+mmd.getName()+"(");
                String[] args = mmd.getParameterTypes();
                for(int i=0; i<args.length; i++) {
                    if(i > 0) System.out.print(", ");
                    System.out.print(args[i]);
                }
                System.out.println(")'");
                dumpMetaData("      ", mmd);
            }

            methods = bmd.getHomeMethods();
            System.out.println("    Found "+methods.size()+" home methods:");
            itm = methods.iterator();
            while(itm.hasNext()) {
                MethodMetaData mmd = (MethodMetaData)itm.next();
                System.out.print("    Home Method '"+mmd.getName()+"(");
                String[] args = mmd.getParameterTypes();
                for(int i=0; i<args.length; i++) {
                    if(i > 0) System.out.print(", ");
                    System.out.print(args[i]);
                }
                System.out.println(")'");
                dumpMetaData("      ", mmd);
            }

            Set fields = bmd.getFields();
            System.out.println("    Found "+fields.size()+" fields:");
            itm = fields.iterator();
            while(itm.hasNext()) {
                FieldMetaData fmd = (FieldMetaData)itm.next();
                System.out.println("    Field '"+fmd.getName()+"'");
                dumpMetaData("      ", fmd);
            }
        }
    }

    private static void dumpMetaData(String prefix, MetaData md) {
        System.out.println(prefix+"Found "+md.size()+" properties:");
        String props[] = md.getPropertyNames();
        for(int i=0; i<props.length; i++)
            System.out.println(prefix+(i+1)+": "+props[i]+"="+md.getProperty(props[i]));
    }
}