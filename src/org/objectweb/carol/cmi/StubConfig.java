/**
 * Copyright (C) 2002-2005 - Bull S.A.
 *
 * CMI : Cluster Method Invocation
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: StubConfig.java,v 1.2 2005/10/21 14:33:27 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.WeakHashMap;

import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * Used to get information on a remote object. Searches for CMI configuration
 * information association with the remote class.
 *
 * @see Distributor
 * @author Simon Nieuviarts
 *
 */
public class StubConfig {

    /**
     * Distributor constructor
     */
    private Constructor distribConstr;

    /**
     * Distributor class
     */
    private Distributor distrib;

    /**
     * Stub class
     */
    private Class clusterStubClass;

    /**
     * Default constructor
     *
     */
    private StubConfig() {
    }

    /**
     * Map remote object to config object
     */
    private static WeakHashMap remoteToConfig = new WeakHashMap();

    /**
     * No stub config object
     */
    private static Object noStubConfig = new Object();

    /**
     * Creates a new stub config object
     * @param remoteObjClass remote object class
     * @return StubConfig object
     * @throws StubConfigException if an exception is encountered
     */
    private static StubConfig createStubConfig(Class remoteObjClass)
            throws StubConfigException {
        String clName = remoteObjClass.getName();
        if (clName.endsWith("_Stub")) {
            clName = clName.substring(0, clName.length() - 5);
        }
        else if (clName.endsWith("_OWStub")) {
            clName = clName.substring(0, clName.length() - 7);
        }
        ClassLoader loader = remoteObjClass.getClassLoader();
        Class confClass;
        try {
            confClass = (loader != null) ? loader.loadClass(clName + "_Cmi")
                    : Class.forName(clName+ "_Cmi");
        } catch (ClassNotFoundException e) {
            remoteToConfig.put(remoteObjClass, noStubConfig);
            if (TraceCarol.isDebugCmiDes())
                TraceCarol.debugCmiDes("Class <"+clName+"> has not been recognized as a clustered object");
            return null;
        }
        StubConfig sc = new StubConfig();
        try {
            sc.distribConstr = confClass.getConstructor(new Class[0]);
        } catch (Exception e) {
            throw new StubConfigException(
                    "The distributor class provide a valid default constructor", e);
        }
        sc.distrib = sc.getNewDistributorInstance();
        StubClassLoader scl = new StubClassLoader(loader);
        sc.clusterStubClass = scl.generateClusterStub(remoteObjClass, sc.distrib);
        return sc;
    }

    /**
     * Get the stub config object for a given remote object class
     * @param remoteObjClass remote object class
     * @return StubConfig object or null
     * @throws StubConfigException if an exception occurs
     */
    private static StubConfig getStubConfigOrNull(Class remoteObjClass)
            throws StubConfigException {
        synchronized (remoteToConfig) {
            Object o = remoteToConfig.get(remoteObjClass);
            if (o == null) {
                StubConfig sc = createStubConfig(remoteObjClass);
                Object put = (sc == null) ? noStubConfig : sc;
                remoteToConfig.put(remoteObjClass, put);
                return sc;
            } else if (o == noStubConfig) {
                return null;
            } else {
                return (StubConfig) o;
            }
        }
    }

    /**
     * Get the stub config object for a given remote object class
     * @param remoteObjClass remote object class
     * @return StubConfig object
     * @throws StubConfigException  if an exception occurs or if no confif found
     */
    private static StubConfig getStubConfig(Class remoteObjClass)
            throws StubConfigException {
        StubConfig sc = getStubConfigOrNull(remoteObjClass);
        if (sc != null) {
            return sc;
        }
        throw new StubConfigException(
                "No cluster configuration found for class "
                        + remoteObjClass.getName());
    }

    /**
     * Creates a new instance for the Distributor class
     * @return Distributor instance
     * @throws StubConfigException if error during instanciation
     */
    private Distributor getNewDistributorInstance() throws StubConfigException {
        try {
            return (Distributor) distribConstr.newInstance(new Object[0]);
        } catch (Exception e) {
            throw new StubConfigException(
                    "Could no instanciate a " + distribConstr.getDeclaringClass().getName());
        }
    }

    /**
     * Test if the object is to be clustered and if it has to be advertised at
     * bind().
     *
     * @return null if the object is not clustered.
     * @throws StubConfigException
     */
    public static Boolean clusterEquivAtBind(Remote obj)
            throws StubConfigException {
        StubConfig sc = getStubConfigOrNull(obj.getClass());
        if (sc == null) {
            return null;
        }
        return new Boolean(sc.distrib.equivAtBind());
    }

    /**
     * Test if the object is to be clustered and if it has to be advertised at
     * export(). Return the name to use.
     *
     * @return null if the object is not clustered.
     * @throws StubConfigException if config error
     */
    public static String clusterEquivAtExport(Remote obj)
            throws StubConfigException {
        StubConfig sc = getStubConfigOrNull(obj.getClass());
        if (sc == null) {
            return null;
        }
        if (sc.distrib.equivAtExport()) {
            return sc.clusterStubClass.getName();
        }
        return null;
    }

    /**
     * @return The cluster stub class corresponding to the parameter class.
     */
    /*
     * public static Class getClusterStubClass(Class remoteObjClass) throws
     * ClassNotFoundException { String cstubName =
     * getClusterStubName(remoteObjClass); ClassLoader loader =
     * remoteObjClass.getClassLoader(); Class cstub_class = (loader != null) ?
     * loader.loadClass(cstubName) : Class.forName(cstubName); return
     * cstub_class; }
     */

    /**
     * Check the compatibility between the cluster stub and a remote object class
     * @param remoteObjClass remote object class
     * @param cs cluster stub
     * @throws RemoteException if the stub aren't compliant
     */
    public static void checkClusterStub(Class remoteObjClass, ClusterStub cs)
            throws RemoteException {
        String csName = cs.getClass().getName();
        String rName = remoteObjClass.getName();
        if (!csName.equals(rName)) { //XXX Seems to be a wrong comparison
            throw new RemoteException("Invalid cluster stub " + csName
                    + " for stub " + rName);
        }
    }

    /**
     * Array of classes used to identify the constructor of a ClusterStub class
     */
    private static Class[] cnstrParams = new Class[] { Distributor.class,
            ServerStubList.class };

    /**
     * @param remoteObjClass remote object class
     * @param stubList stubs list
     * @return a cluster stub corresponding to the parameter class
     * @throws StubConfigException if exception during instanciation of the stub class
     */
    public static ClusterStub instanciateClusterStub(Class remoteObjClass,
            ServerStubList stubList) throws StubConfigException {
        StubConfig sc = getStubConfig(remoteObjClass);
        Constructor cnstr;
        try {
            cnstr = sc.clusterStubClass.getConstructor(cnstrParams);
            return (ClusterStub) cnstr.newInstance(new Object[] {sc.getNewDistributorInstance(), stubList});
        } catch (Exception e) {
            throw new StubConfigException(
                    "Can not instanciate cluster stub for class "
                            + remoteObjClass.getName() + " : ", e);
        }
    }

    /**
     * For test purposes
     * @param args program args
     * @throws Exception if an error occurs
     */
    public static void main(String[] args) throws Exception {
        Class rem = Class.forName(args[0]);
        StubConfig sc = getStubConfig(rem);
        FileOutputStream fos = new FileOutputStream("code.class");
        CodeGenerator cg = new CodeGenerator(rem, sc.distrib);
        fos.write(cg.generate());
        fos.close();
    }
}