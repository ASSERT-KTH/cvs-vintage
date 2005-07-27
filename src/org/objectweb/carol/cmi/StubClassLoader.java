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
 * $Id: StubClassLoader.java,v 1.1 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;


/**
 * Used to build the class of a cluster stub.
 *
 * @author Simon Nieuviarts
 */
public class StubClassLoader extends ClassLoader {

    /**
     * Creates a new class loader
     * @param parent parent classloader
     */
    public StubClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Generates a new class for a cluster stub
     * @param remoteObjClass remote object class
     * @param conf Distributor class
     * @return new class
     */
    Class generateClusterStub(Class remoteObjClass, Distributor conf) {
        CodeGenerator cg = new CodeGenerator(remoteObjClass, conf);
        byte[] data = cg.generate();
        return defineClass(null, data, 0, data.length);
    }
}