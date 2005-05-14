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

import java.io.ObjectInput;
import java.util.List;

/**
 * ObjectInputList implements the {@link ObjectInput} interface, but
 * rather than deserializing objects from an {@link
 * java.io.InputStream}, this implementation reads objects from a list
 * created using {@link ObjectOutputList}. This class is used in
 * concert with {@link ObjectOutputList} in order to provide support
 * for the deprecated {@link java.rmi.server.RemoteCall} interface
 * required by early versions of RMI.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class ObjectInputList implements ObjectInput {

    /**
     * The source list of objects.
     */
    private List list;

    /**
     * The current position in the source list.
     */
    private int pos = 0;

    /**
     * Creates a new {@link ObjectInputList} instance that reads from
     * the given {@link List}. This list should have been generated
     * using {@link ObjectOutputList}.
     *
     * @param list the list to use for input
     */

    public ObjectInputList(List list) {
        this.list = list;
    }

    /**
     * Returns the object in the list and advances the position pointer.
     *
     * @return the next object
     */

    private Object next() {
        return list.get(pos++);
    }

    public Object readObject() {
        return next();
    }

    public String readUTF() {
        return (String) next();
    }

    public String readLine() {
        return readUTF();
    }

    public boolean readBoolean() {
        return ((Boolean) next()).booleanValue();
    }

    public short readShort() {
        return (short) readInt();
    }

    public long readLong() {
        return ((Long) next()).longValue();
    }

    public int readUnsignedShort() {
        return readInt();
    }

    public int readUnsignedByte() {
        return readInt();
    }

    public int readInt() {
        return ((Integer) next()).intValue();
    }

    public float readFloat() {
        return ((Float) next()).floatValue();
    }

    public double readDouble() {
        return ((Double) next()).doubleValue();
    }

    public char readChar() {
        return ((Character) next()).charValue();
    }

    public byte readByte() {
        return (byte) readInt();
    }

    /**
     * <font color="red">UNSUPPORTED</font>
     * @throws UnsupportedOperationException
     */

    public int available() {
        throw new UnsupportedOperationException();
    }

    /**
     * <font color="red">UNSUPPORTED</font>
     * @throws UnsupportedOperationException
     */

    public void readFully(byte[] b, int off, int len) {
        throw new UnsupportedOperationException();
    }

    /**
     * <font color="red">UNSUPPORTED</font>
     * @throws UnsupportedOperationException
     */

    public void readFully(byte[] b) {
        throw new UnsupportedOperationException();
    }

    /**
     * <font color="red">UNSUPPORTED</font>
     * @throws UnsupportedOperationException
     */

    public int read(byte[] b, int off, int len) {
        throw new UnsupportedOperationException();
    }

    /**
     * <font color="red">UNSUPPORTED</font>
     * @throws UnsupportedOperationException
     */

    public int read(byte[] b) {
        throw new UnsupportedOperationException();
    }

    /**
     * <font color="red">UNSUPPORTED</font>
     * @throws UnsupportedOperationException
     */

    public int read() {
        throw new UnsupportedOperationException();
    }

    /**
     * <font color="red">UNSUPPORTED</font>
     * @throws UnsupportedOperationException
     */

    public long skip(long b) {
        throw new UnsupportedOperationException();
    }

    /**
     * <font color="red">UNSUPPORTED</font>
     * @throws UnsupportedOperationException
     */

    public int skipBytes(int b) {
        throw new UnsupportedOperationException();
    }

    public void close() {}

}
