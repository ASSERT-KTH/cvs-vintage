package org.jboss.dependencies;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.sun.xml.parser.Parser;
import com.sun.xml.parser.Resolver;

public class DependencyManager {
    private final static boolean DEBUG=false;
    private HashMap dependencies;
    private HashSet loadedBeans;
    private HashMap remainingBeans;
    private HashSet pendingBeans;
    private MBeanServer server;

    public DependencyManager() {
        dependencies = new HashMap();
        loadedBeans = new HashSet();
        remainingBeans = new HashMap();
        pendingBeans = new HashSet();
    }

    public void loadXML(String source) {
        dependencies.clear();
        Parser parser = new Parser();
        try {
            parser.setDocumentHandler(new SAXHandler());
            InputSource input = new InputSource(new StringReader(source));
            parser.parse(input);
        } catch(IOException e) {
            e.printStackTrace();
        } catch(SAXException e) {
            e.printStackTrace();
        }
    }

    public void startMBeans(MBeanServer server) {
        this.server = server;
        loadedBeans.clear();
        remainingBeans.clear();
        pendingBeans.clear();
        Iterator it = server.queryNames(null, null).iterator();
        while(it.hasNext()) {
            ObjectName name = (ObjectName)it.next();
            String service = getService(name);
            if(service == null)
                continue;
            Set set = (Set)remainingBeans.get(service);
            if(set == null)
                set = new HashSet();
            set.add(name);
            remainingBeans.put(service, set);
        }
        it = ((Map)remainingBeans.clone()).keySet().iterator();
        while(it.hasNext()) {
            String next = (String)it.next();
            if(!processMBean(next)) {
                System.out.println("Unable to load service '"+next+"'");
            }
        }
        System.out.println(loadedBeans.size()+" services loaded.");
        loadedBeans.clear();
        dependencies.clear();
    }

    private boolean processMBean(String target) {
        if(pendingBeans.contains(target))
            throw new RuntimeException("Circular dependencies!");
        if(loadedBeans.contains(target))
            return true;
        if(DEBUG) System.out.println("Processing service '"+target+"'");
        pendingBeans.add(target);
        Set set = (Set)dependencies.get(target);
        if(set == null) {
            return loadMBean(target);
        } else {
            Iterator it = set.iterator();
            while(it.hasNext()) {
                Dependency dep = (Dependency)it.next();
                if(DEBUG) System.out.println("  Service '"+target+"' depends on '"+dep.name+"'");
                if(!loadedBeans.contains(dep.name)) {
                    boolean result = processMBean(dep.name);
                    if(!result && dep.required)
                        return false;
                }
            }
            return loadMBean(target);
        }
    }

    private boolean loadMBean(String target) {
        boolean loaded = false;
        Set set = (Set)remainingBeans.get(target);
        if(set != null) {
            if(DEBUG) System.out.println("Starting service '"+target+"'");
            Iterator it = set.iterator();
            while(it.hasNext()) {
                ObjectName name = (ObjectName)it.next();
                if(DEBUG) System.out.println("Starting instance '"+name.getCanonicalName()+"'");
                try {
                    server.invoke(name, "start", new Object[0], new String[0]);
                    loaded = true;
                } catch(ReflectionException e) {
                    if(e.getTargetException() instanceof NoSuchMethodException) {
                        loaded = true;  // This bean doesn't have a start!
                    } else {
                        System.out.println("Error starting service '"+name.getCanonicalName()+"': "+e.getTargetException());
                        loaded = false;
                        break;
                    }
                } catch(Throwable t) {
                    System.out.println("Error starting service '"+name.getCanonicalName()+"': "+t);
                    loaded = false;
                    break;
                }
            }
            remainingBeans.remove(target);
            dependencies.remove(target);
        }
        loadedBeans.add(target);
        pendingBeans.remove(target);
        return loaded;
    }

    private String getService(ObjectName oName) {
        String name = oName.getCanonicalName();
        int pos = name.indexOf("service=");
        if(pos < 0)
            return null;
        int test, end = name.length();
        test = name.indexOf(',', pos+8);
        if(test > -1 && test < end)
            end = test;
        test = name.indexOf(';', pos+8);
        if(test > -1 && test < end)
            end = test;
        return name.substring(pos+8, end);
    }

    public void dump() {
        Iterator it = dependencies.keySet().iterator();
        while(it.hasNext()) {
            String key = (String)it.next();
            Iterator child = ((HashSet)dependencies.get(key)).iterator();
            while(child.hasNext()) {
                Dependency dep = (Dependency)child.next();
                System.out.println(key+" depends on "+dep.name+(dep.required ? " REQUIRED" : ""));
            }
        }
    }

    public static void main(String args[]) {
        StringBuffer total = new StringBuffer();
        try{
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(args[0])));
            String line;
            while((line = reader.readLine()) != null)
                total.append(line).append('\n');
            DependencyManager dm = new DependencyManager();
            dm.loadXML(total.toString());
            dm.dump();
        } catch(IOException e) {e.printStackTrace();}
    }

    private class SAXHandler extends HandlerBase {
        private String currentService;
        public void endElement(String name) throws SAXException {
            if(name.equals("service"))
                currentService = null;
        }
        public void startElement(String name, AttributeList atts) throws SAXException {
            if(name.equals("service")) {
                currentService = atts.getValue("name");
                dependencies.put(currentService, new HashSet());
            } else if(name.equals("dependency")) {
                HashSet set = (HashSet)dependencies.get(currentService);
                set.add(new Dependency(atts.getValue("service"), atts.getValue("required")));
            }
        }
    }
}

class Dependency {
    public String name;
    public boolean required;

    public Dependency(String name, String required) {
        this.name = name;
        this.required = new Boolean(required).booleanValue();
    }
}
