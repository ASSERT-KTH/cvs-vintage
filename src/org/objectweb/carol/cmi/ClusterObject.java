/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
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
 */
package org.objectweb.carol.cmi;

import java.rmi.Remote;

/**
 * Tell us if an object can be clustered or not.
 */
public final class ClusterObject {
    /**
     * @return The cluster stub class corresponding to the parameter class or
     * <code>null</code> if this object has no cluster stub class.
     */
    public static Class getClusterStubClass(Class cl) {
        String stub_name = cl.getName();
        String cstub_name = stub_name;
        if (stub_name.endsWith("_Stub"))
            cstub_name = cstub_name.substring(0, cstub_name.length() - 5);
        else if (stub_name.endsWith("_OWStub"))
            cstub_name = cstub_name.substring(0, cstub_name.length() - 7);
        cstub_name += "_Cluster";
        ClassLoader loader = cl.getClassLoader();
        try {
            Class cstub_class = loader.loadClass(cstub_name);
            return cstub_class;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Class getClusterConfigClass(Remote obj)
        throws ClassNotFoundException {
        Class cl = obj.getClass();
        String obj_name = obj.getClass().getName();
        String config_name = obj_name;
        if (obj_name.endsWith("_Stub"))
            config_name = config_name.substring(0, config_name.length() - 5);
        else if (obj_name.endsWith("_OWStub"))
            config_name = config_name.substring(0, config_name.length() - 7);
        config_name += "_ClusterConfig";
        ClassLoader loader = cl.getClassLoader();
        return loader.loadClass(config_name);
    }

    /**
     * Call getClusterConfig() on a cluster stub instead of this one whenever
     * possible.
     * @return A ClusterConfig object reflecting the static cluster
     * configuration of <code>stub</code>'s class.
     */
    public static ClusterConfig getClusterConfig(Remote obj)
        throws
            ClassNotFoundException,
            NoSuchMethodException,
            IllegalAccessException,
            java.lang.reflect.InvocationTargetException {
        Class config_class = getClusterConfigClass(obj);
        java.lang.reflect.Method mth =
            config_class.getMethod("getClusterConfig", null);
        return (ClusterConfig) mth.invoke(null, null);
    }
}
