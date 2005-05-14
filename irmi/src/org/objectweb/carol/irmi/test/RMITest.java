/**
 * Copyright (c) 2004 Red Hat, Inc. All rights reserved.
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
 * Component of: Red Hat Application Server
 *
 * Initial Developers: Rafael H. Schloming
 */
package org.objectweb.carol.irmi.test;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.server.RMISocketFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 * RMITest
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public abstract class RMITest extends TestCase {

    private static final TraceSocketFactory TSF =
        new TraceSocketFactory(RMISocketFactory.getDefaultSocketFactory());

    static {
        try {
            RMISocketFactory.setSocketFactory(TSF);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String CONTEXT;
    private static String REGISTRY;

    static void setContext(String ctx) {
        CONTEXT = ctx;
    }

    static void setRegistry(String reg) {
        REGISTRY = reg;
    }

    public String getContext() {
        return CONTEXT;
    }

    public String getRegistry() {
        return REGISTRY;
    }

    public String getName() {
        return getContext() + "/" + super.getName();
    }

    private Object lookup(String url) {
        try {
            return new InitialContext().lookup(url);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public Remote getObject(String type) {
        return (Remote) lookup(getRegistry() + "/" + type);
    }

    protected void setUp() throws Exception {
        TSF.getClientTraces().clear();
        TSF.getServerTraces().clear();
    }

    protected void tearDown() throws Exception {
        System.out.println(getName() + ": " + TSF.getClientTraces());
        System.out.println(getName() + ": " + TSF.getServerTraces());
    }

}
