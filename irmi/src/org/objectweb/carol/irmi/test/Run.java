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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Run is used to invoke java code in a restricted classpath.
 *
 * Usage: java test.Run [--exclude <classname1> ... --exclude <classname_n>] <classname> <args>
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class Run {

    /**
     * @see Run
     **/

    public static final void main(String[] args) throws Exception {
        LinkedList remaining = new LinkedList(Arrays.asList(args));
        final List excludes = new ArrayList();
        String cmd = null;
        List cmdargs = new ArrayList();
        while (!remaining.isEmpty()) {
            String arg = (String) remaining.removeFirst();
            if (arg.equals("--exclude")) {
                excludes.add(remaining.removeFirst());
            } else if (cmd == null) {
                cmd = arg;
            } else {
                cmdargs.add(arg);
            }
        }

        ClassLoader loader = new ClassLoader(Run.class.getClassLoader()) {
            protected Class loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
                for (int i = 0; i < excludes.size(); i++) {
                    String exclude = (String) excludes.get(i);
                    if (name.equals(exclude)) {
                        throw new ClassNotFoundException(name + " is excluded");
                    }
                }

                return super.loadClass(name, resolve);
            }
        };

        Class klass = Class.forName(cmd, true, loader);
        Method meth = klass.getMethod("main", new Class[] { String[].class });
        meth.invoke(null, new Object[] { cmdargs.toArray(new String[cmdargs.size()]) });
    }

}
