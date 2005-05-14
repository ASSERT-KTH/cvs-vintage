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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import junit.extensions.TestDecorator;
import junit.framework.Test;
import junit.framework.TestResult;
import org.objectweb.carol.irmi.PRO;


/**
 * RMITestDecorator
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class RMITestDecorator extends TestDecorator {

    private static final String WS = "\\s+";

    private Command reg = new Command
        (run("org.objectweb.carol.irmi.test.Registry 12345"));
    private Command srv = new Command
        (run("org.objectweb.carol.irmi.test.Server rmi://localhost:12345 " +
             "org.objectweb.carol.irmi.test.RMath " +
             "org.objectweb.carol.irmi.test.Remover"));
    private Collection exported = null;

    public RMITestDecorator(Test test) {
        super(test);
    }

    public int countTestCases() {
        return super.countTestCases() * 2;
    }

    public void run(TestResult result) {
        try {
            System.setProperty("java.naming.factory.url.pkgs",
                               "org.objectweb.carol.irmi.jndi");
            System.setProperty("javax.rmi.CORBA.PortableRemoteObjectClass",
                               PRO.class.getName());

            RMITest.setRegistry("rmi://localhost:12345");

            RMITest.setContext("remote");
            setUpRemote();
            super.run(result);
            tearDownRemote();

            if (result.shouldStop()) {
                return;
            }

            RMITest.setContext("local");
            setUpLocal();
            super.run(result);
            tearDownLocal();
        } catch (Exception e) {
            result.addError(this, e);
        }
    }

    private static String run(String cmd, Collection exclude) {
        StringBuffer result = new StringBuffer();
        result.append("java -cp ");
        result.append(System.getProperty("java.class.path"));
        result.append(" -Djava.naming.factory.url.pkgs=");
        result.append("org.objectweb.carol.irmi.jndi");
        result.append(" -Djavax.rmi.CORBA.PortableRemoteObjectClass=");
        result.append(PRO.class.getName());
        result.append(" -Dtimer.out=build/reports/" + cmd.split(WS, 2)[0] +
                      "-timer.out");

        for (Iterator it = exclude.iterator(); it.hasNext(); ) {
            Class klass = (Class) it.next();
            result.append(" --exclude");
            result.append(klass.getName());
        }

        result.append(" ");
        result.append(cmd);

        return result.toString();
    }

    private static String run(String cmd) {
        return run(cmd, Collections.EMPTY_LIST);
    }

    private void start(Command cmd) throws IOException, InterruptedException {
        cmd.start();
        if (!cmd.expect("started")) {
            cmd.waitFor();
            throw new RuntimeException(cmd.getCommand() + ":\n\n" + cmd.getResult());
        }
    }

    private void stop(Command cmd) throws IOException, InterruptedException {
        PrintWriter out = cmd.getInput();
        out.write('s');
        out.flush();
        cmd.waitFor();
    }

    private void setUpRemote() throws Exception {
        start(reg);
        start(srv);
    }

    private void tearDownRemote() throws Exception {
        stop(reg);
        stop(srv);
    }

    private void setUpLocal() throws Exception {
        start(reg);
        exported = Server.export
            (("rmi://localhost:12345 org.objectweb.carol.irmi.test.RMath " +
              "org.objectweb.carol.irmi.test.Remover").split(WS));
    }

    private void tearDownLocal() throws Exception {
        stop(reg);
        Server.unexport(exported);
    }

}
