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
package org.objectweb.carol.irmi;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * ClientLogger
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class ClientLogger implements ClientInterceptor {

    public void receive(byte code, ObjectInput in)
        throws IOException, ClassNotFoundException {
        System.out.println("code: " + code);
        System.out.println("context: " + in.readUTF());
    }

    public void send(byte code, ObjectOutput out) throws IOException {
        switch (code) {
        case METHOD_CALL:
            out.writeUTF("METHOD_CALL");
            break;
        case METHOD_RESULT:
            out.writeUTF("METHOD_RESULT");
            break;
        case METHOD_ERROR:
            out.writeUTF("METHOD_ERROR");
            break;
        case SYSTEM_ERROR:
            out.writeUTF("SYSTEM_ERROR");
            break;
        default:
            out.writeUTF("null");
            break;
        }
    }

}
