/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.dependencies;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Manages dependencies between MBeans.  Loads an XML configuration file,
 * and then starts a list of MBeans according to the dependencies in the
 * file.
 * @author Aaron Mulder <ammulder@alumni.princeton.edu>
 * @version $Revision: 1.5 $
 */
public class DependencyManager {
    // Static --------------------------------------------------------

    /**
     * Set this to true to enable verbose console output (as each MBean
     * is loaded)
     */
    private final static boolean DEBUG=false;

    // Attributes ----------------------------------------------------

    private HashMap dependencies;
    private HashSet loadedBeans;
    private HashMap remainingBeans;
    private HashSet pendingBeans;
    private HashSet otherMBeans;
    private Vector mbeanStartOrder;
    private MBeanServer server;

    // Constructors --------------------------------------------------

    /**
     * Initializes a DependencyManager.
     */
    public DependencyManager() {
        dependencies = new HashMap();
        loadedBeans = new HashSet();
        remainingBeans = new HashMap();
        pendingBeans = new HashSet();
        otherMBeans = new HashSet();
        mbeanStartOrder = new Vector();
    }

    // Public --------------------------------------------------------

    /**
     * Initializes all the MBeans in a server in an order consistant with the
     * dependencies.
     */
    public void initMBeans(MBeanServer server) {
        this.server = server;

        // Init all the MBeans
        Iterator it = server.queryNames(null, null).iterator();
        while(it.hasNext()) {
            ObjectName name = (ObjectName)it.next();
            if(!initMBean(name)) {
                System.out.println("Unable to init MBean '"+name.getCanonicalName()+"'");
            }
        }
    }
    
    /**
     * Starts all the MBeans in a server in an order consistant with the
     * dependencies.
     */
    public void startMBeans(MBeanServer server) {
        this.server = server;
        loadedBeans.clear();
        remainingBeans.clear();
        pendingBeans.clear();
        otherMBeans.clear();
        mbeanStartOrder.clear();
        Iterator it = server.queryNames(null, null).iterator();

        // Identify all the MBeans
        while(it.hasNext()) {
            ObjectName name = (ObjectName)it.next();
            String service = getService(name);
            if(service == null) {
                otherMBeans.add(name);
                continue;
            }
            Set set = (Set)remainingBeans.get(service);
            if(set == null)
                set = new HashSet();
            set.add(name);
            remainingBeans.put(service, set);
        }

        // Start all the MBeans that are services
        it = ((Map)remainingBeans.clone()).keySet().iterator();
        while(it.hasNext()) {
            String next = (String)it.next();
            if(!processService(next, true)) {
                System.out.println("Unable to start service '"+next+"'");
            }
        }

        // Start all the MBeans that are not services
        it = otherMBeans.iterator();
        while(it.hasNext()) {
            ObjectName name = (ObjectName)it.next();
            if(!startMBean(name)) {
                System.out.println("Unable to start MBean '"+name.getCanonicalName()+"'");
            }
        }

        // Clear out remaining temporary data structures.
        System.out.println(loadedBeans.size()+" services and "+otherMBeans.size()+" other MBeans started.");
        loadedBeans.clear();
        otherMBeans.clear();
        mbeanStartOrder.clear();
    }

    /**
     * Stops all the MBeans in a server in an order consistant with the
     * dependencies.
     */
    public void stopMBeans(MBeanServer server) {
        this.server = server;
        loadedBeans.clear();
        remainingBeans.clear();
        pendingBeans.clear();
        otherMBeans.clear();
        mbeanStartOrder.clear();
        Iterator it = server.queryNames(null, null).iterator();

        // Identify all the MBeans
        while(it.hasNext()) {
            ObjectName name = (ObjectName)it.next();
            String service = getService(name);
            if(service == null) {
                otherMBeans.add(name);
                continue;
            }
            Set set = (Set)remainingBeans.get(service);
            if(set == null)
                set = new HashSet();
            set.add(name);
            remainingBeans.put(service, set);
        }

        // Get ordering for all the MBeans that are services
        it = ((Map)remainingBeans.clone()).keySet().iterator();
        while(it.hasNext()) {
            String next = (String)it.next();
            if(!processService(next, false)) {
                System.out.println("Unable to stop service '"+next+"'");
            }
        }

        // Stop all the MBeans that are not services
        it = otherMBeans.iterator();
        while(it.hasNext()) {
            ObjectName name = (ObjectName)it.next();
            if(!stopMBean(name)) {
                System.out.println("Unable to stop MBean '"+name.getCanonicalName()+"'");
            }
        }

        // Stop all the MBeans that are services in the reverse of
        // the valid startup order.
        int max = mbeanStartOrder.size();
        for(int i=max-1; i >= 0; i--) {
            ObjectName name = (ObjectName)mbeanStartOrder.elementAt(i);
            if(!stopMBean(name)) {
                System.out.println("Unable to stop MBean '"+name.getCanonicalName()+"'");
            }
        }

        // Clear out remaining temporary data structures.
        System.out.println(loadedBeans.size()+" services and "+otherMBeans.size()+" other MBeans stopped.");
        loadedBeans.clear();
        otherMBeans.clear();
        mbeanStartOrder.clear();
    }

    /**
     * Prints all the dependencies to the console.
     */
    public void printDependencies() {
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

    // Private -------------------------------------------------------

    /**
     * Loads all the dependencies for a service, and then the service itself.
     * This is a recursive process.
     */
    private boolean processService(String target, boolean executeStart) {
        if(pendingBeans.contains(target))
            throw new RuntimeException("Circular dependencies!");
        if(loadedBeans.contains(target))
            return true;
        if(DEBUG) System.out.println("Processing service '"+target+"'");
        pendingBeans.add(target);
        Set set = (Set)dependencies.get(target);
        if(set == null) {
            return loadService(target, executeStart);
        } else {
            Iterator it = set.iterator();
            while(it.hasNext()) {
                Dependency dep = (Dependency)it.next();
                if(DEBUG) System.out.println("  Service '"+target+"' depends on '"+dep.name+"'");
                if(!loadedBeans.contains(dep.name)) {
                    boolean result = processService(dep.name, executeStart);
                    if(!result && dep.required)
                        return false;
                }
            }
            return loadService(target, executeStart);
        }
    }

    /**
     * Loads all instances of a service.  That is, all MBeans which have
     * "service=target" in the ObjectName.
     */
    private boolean loadService(String target, boolean executeStart) {
        boolean loaded = false;
        Set set = (Set)remainingBeans.get(target);
        if(set != null) {
            if(DEBUG) System.out.println("Checking instances for service '"+target+"'");
            Iterator it = set.iterator();
            while(it.hasNext()) {
                ObjectName oName = (ObjectName)it.next();
                mbeanStartOrder.add(oName);
                if(executeStart) {
                    loaded = startMBean(oName);
                    if(!loaded)
                        break;
                    loaded = true;
                } else {
                    loaded = true;
                }
            }
            remainingBeans.remove(target);
        }
        loadedBeans.add(target);
        pendingBeans.remove(target);
        return loaded;
    }

    /**
     * Calls the "init" method on an MBean.  If no such method is available,
     * that's OK, but if the call fails for another reason, returns false.
     */
    private boolean initMBean(ObjectName name) {
        boolean loaded = false;
        if(DEBUG) System.out.println("Initializing instance '"+name.getCanonicalName()+"'");
        try {
            server.invoke(name, "init", new Object[0], new String[0]);
            loaded = true;
        } catch(ReflectionException e) {
            if(e.getTargetException() instanceof NoSuchMethodException) {
                loaded = true;  // This bean doesn't have a start!
            } else {
                e.getTargetException().printStackTrace(System.err);
                System.out.println("BAR");
                if (e.getTargetException() instanceof RuntimeErrorException)
                {
                  System.out.println("FOO");
                   ((RuntimeErrorException)e.getTargetException()).getTargetError().printStackTrace(System.err);
                }
                
                System.out.println("Error initializing service '"+name.getCanonicalName()+"': "+e.getTargetException());
            }
        } catch(Throwable t) {
            if (t instanceof RuntimeErrorException)
            {
              System.out.println("LGPL");
               ((RuntimeErrorException)t).getTargetError().printStackTrace(System.err);
            }
            System.out.println("Error initializing service '"+name.getCanonicalName()+"': "+t);
        }
        return loaded;
    }
    
    /**
     * Calls the "start" method on an MBean.  If no such method is available,
     * that's OK, but if the call fails for another reason, returns false.
     */
    private boolean startMBean(ObjectName name) {
        boolean loaded = false;
        if(DEBUG) System.out.println("Starting instance '"+name.getCanonicalName()+"'");
        try {
            server.invoke(name, "start", new Object[0], new String[0]);
            loaded = true;
        } catch(ReflectionException e) {
            if(e.getTargetException() instanceof NoSuchMethodException) {
                loaded = true;  // This bean doesn't have a start!
            } else {
                System.out.println("Error starting service '"+name.getCanonicalName()+"': "+e.getTargetException());
            }
        } catch(Throwable t) {
            System.out.println("Error starting service '"+name.getCanonicalName()+"': "+t);
        }
        return loaded;
    }

    /**
     * Calls the "destroy" method on an MBean.  If no such method is available,
     * that's OK, but if the call fails for another reason, returns false.
     */
    private boolean stopMBean(ObjectName name) {
        boolean stopped = false;
        if(DEBUG) System.out.println("Stopping instance '"+name.getCanonicalName()+"'");
        try {
            server.invoke(name, "destroy", new Object[0], new String[0]);
            stopped = true;
        } catch(ReflectionException e) {
            if(e.getTargetException() instanceof NoSuchMethodException) {
                stopped = true;  // This bean doesn't have a destroy!
            } else {
                System.out.println("Error stopping service '"+name.getCanonicalName()+"': "+e.getTargetException());
            }
        } catch(Throwable t) {
            System.out.println("Error stopping service '"+name.getCanonicalName()+"': "+t);
        }
        return stopped;
    }

    /**
     * Finds the substring "service=XXX" in an ObjectName.
     * @return The XXX part, or null if there is no such substring.
     */
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

    // Inner classes -------------------------------------------------
    private class SAXHandler extends HandlerBase {
        private String currentService;

        /**
         * Clears the current service.
         */
        public void endElement(String name) throws SAXException {
            if(name.equals("service"))
                currentService = null;
        }

        /**
         * Records the current service, or a dependency for the current
         * service.
         */
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

/**
 * A record of a dependency.
 */
class Dependency {
    /**
     * The name of the service that should be loaded first.
     */
    public String name;
    /**
     * <B>True</B> if the service <I>must</I> be loaded first, <B>false</B> if
     * the service should be loaded first only if available.
     */
    public boolean required;

    /**
     * Creates a new dependency record.
     */
    public Dependency(String name, String required) {
        this.name = name;
        this.required = new Boolean(required).booleanValue();
    }
}
