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

import junit.framework.TestCase;

/**
 * CommandTest
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class CommandTest extends TestCase {

    public CommandTest(String name) {
        super(name);
    }

    public void testExclude() throws Exception {
        String cls = Run.class.getName();
        Command cmd = new Command
            ("java -cp build/classes " + cls +
             " --exclude " + cls + " " + cls);
        cmd.start();
        if (!cmd.expectError(cls + " is excluded")) {
            fail(cmd.getResult());
        }
    }

}
