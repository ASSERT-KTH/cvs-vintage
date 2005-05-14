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

import java.io.ObjectOutput;
import java.util.List;

/**
 * ObjectOutputList implements the {@link ObjectOutput} interface, but
 * rather than serializing objects to an {@link java.io.OutputStream},
 * this implementation appends to a list. This class is used in
 * concert with {@link ObjectInputList} in order to provide support
 * for the deprecated {@link java.rmi.server.RemoteCall} interface
 * required by early versions of RMI.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class ObjectOutputList implements ObjectOutput {

    /**
     * The list used for output.
     */
    private List list;

    /**
     * Constructs a new {@link ObjectOutputList} that appends its
     * output to the given list.
     *
     * @param list the list to use for output
     */

    public ObjectOutputList(List list) {
        this.list = list;
    }

    /**
     * Adds an object to the output list.
     *
     * @param obj the object to add to the output list
     */

    private void add(Object obj) {
        list.add(obj);
    }

    public void writeObject(Object obj) {
        add(obj);
    }

    public void writeUTF(String str) {
        add(str);
    }

    public void writeChars(String str) {
        add(str);
    }

    public void writeBytes(String str) {
        add(str);
    }

    public void writeBoolean(boolean b) {
        add(Boolean.valueOf(b));
    }

    public void writeLong(long l) {
        add(new Long(l));
    }

    public void writeShort(int s) {
        writeInt(s);
    }

    public void writeInt(int i) {
        add(new Integer(i));
    }

    public void writeChar(int c) {
        add(new Character((char) c));
    }

    public void writeByte(int b) {
        writeInt(b);
    }

    public void writeFloat(float f) {
        add(new Float(f));
    }

    public void writeDouble(double d) {
        add(new Double(d));
    }

    /**
     * <font color="red">UNSUPPORTED</font>
     * @throws UnsupportedOperationException
     */

    public void write(byte[] b, int off, int len) {
        throw new UnsupportedOperationException();
    }

    /**
     * <font color="red">UNSUPPORTED</font>
     * @throws UnsupportedOperationException
     */

    public void write(byte[] b) {
        throw new UnsupportedOperationException();
    }

    /**
     * <font color="red">UNSUPPORTED</font>
     * @throws UnsupportedOperationException
     */

    public void write(int b) {
        throw new UnsupportedOperationException();
    }

    public void flush() {}

    public void close() {}

}
