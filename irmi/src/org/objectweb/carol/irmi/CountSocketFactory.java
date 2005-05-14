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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.nio.channels.SocketChannel;
import java.rmi.server.RMISocketFactory;

/**
 * CountSocketFactory is an {@link RMISocketFactory} implementation
 * that uses {@link CountOutputStream} and {@link CountInputStream} to
 * record the time and number of bytes involved in the I/O operations
 * of any RMI implementation that uses {@link RMISocketFactory}. This
 * class uses the {@link Timer} class to record timing information and
 * report results.
 *
 * @see Timer
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class CountSocketFactory extends RMISocketFactory {

    /**
     * <p>Static utility method used to install an instance of
     * CountSocketFactory as the current RMISocketFactory. Note that
     * the {@link RMISocketFactory#setSocketFactory(RMISocketFactory)}
     * can only be set once, and must be set before any RMI operations
     * have been performed. Therefore this method must be called
     * exactly once near the beginning of a programs execution in
     * order for the results reported to be accurate. This method is
     * just a convenience for calling:</p>
     *
     * <code><pre>
     *     RMISocketFactory.setSocketFactory(new CountSocketFactory());
     * </pre></code>
     */

    public static final void install() {
        try {
            RMISocketFactory.setSocketFactory(new CountSocketFactory());
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private CountSocketFactory() {}

    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port) {
            public Socket accept() throws IOException {
                final Socket delegate = super.accept();
                return new Socket((SocketImpl) null) {
                    public void bind(SocketAddress bindpoint) throws IOException {
                        delegate.bind(bindpoint);
                    }
                    public void close() throws IOException {
                        delegate.close();
                    }
                    public void connect(SocketAddress endpoint) throws IOException {
                        delegate.connect(endpoint);
                    }
                    public void connect(SocketAddress endpoint, int timeout)
                        throws IOException {
                        delegate.connect(endpoint, timeout);
                    }
                    public java.nio.channels.SocketChannel getChannel() {
                        return delegate.getChannel();
                    }
                    public InetAddress getInetAddress() {
                        return delegate.getInetAddress();
                    }
                    private InputStream in = null;
                    public InputStream getInputStream() throws IOException {
                        if (in == null) {
                            in = new CountInputStream(delegate.getInputStream());
                        }
                        return in;
                    }
                    public boolean getKeepAlive() throws SocketException {
                        return delegate.getKeepAlive();
                    }
                    public InetAddress getLocalAddress() {
                        return delegate.getLocalAddress();
                    }
                    public int getLocalPort() {
                        return delegate.getLocalPort();
                    }
                    public SocketAddress getLocalSocketAddress() {
                        return delegate.getLocalSocketAddress();
                    }
                    public boolean getOOBInline() throws SocketException {
                        return delegate.getOOBInline();
                    }
                    private OutputStream out = null;
                    public OutputStream getOutputStream() throws IOException {
                        if (out == null) {
                            out = new CountOutputStream(delegate.getOutputStream());
                        }
                        return out;
                    }
                    public int getPort() {
                        return delegate.getPort();
                    }
                    public int getReceiveBufferSize() throws SocketException {
                        return delegate.getReceiveBufferSize();
                    }
                    public SocketAddress getRemoteSocketAddress() {
                        return delegate.getRemoteSocketAddress();
                    }
                    public boolean getReuseAddress() throws SocketException {
                        return delegate.getReuseAddress();
                    }
                    public int getSendBufferSize() throws SocketException {
                        return delegate.getSendBufferSize();
                    }
                    public int getSoLinger() throws SocketException {
                        return delegate.getSoLinger();
                    }
                    public int getSoTimeout() throws SocketException {
                        return delegate.getSoTimeout();
                    }
                    public boolean getTcpNoDelay() throws SocketException {
                        return delegate.getTcpNoDelay();
                    }
                    public int getTrafficClass() throws SocketException {
                        return delegate.getTrafficClass();
                    }
                    public boolean isBound() {
                        return delegate.isBound();
                    }
                    public boolean isClosed() {
                        return delegate.isClosed();
                    }
                    public boolean isConnected() {
                        return delegate.isConnected();
                    }
                    public boolean isInputShutdown() {
                        return delegate.isInputShutdown();
                    }
                    public boolean isOutputShutdown() {
                        return delegate.isOutputShutdown();
                    }
                    public void sendUrgentData(int data) throws IOException {
                        delegate.sendUrgentData(data);
                    }
                    public void setKeepAlive(boolean on) throws SocketException {
                        delegate.setKeepAlive(on);
                    }
                    public void setOOBInline(boolean on) throws SocketException {
                        delegate.setOOBInline(on);
                    }
                    public void setReceiveBufferSize(int size) throws SocketException {
                        delegate.setReceiveBufferSize(size);
                    }
                    public void setReuseAddress(boolean on) throws SocketException {
                        delegate.setReuseAddress(on);
                    }
                    public void setSendBufferSize(int size) throws SocketException {
                        delegate.setSendBufferSize(size);
                    }
                    public void setSoLinger(boolean on, int linger) throws SocketException {
                        delegate.setSoLinger(on, linger);
                    }
                    public void setSoTimeout(int timeout) throws SocketException {
                        delegate.setSoTimeout(timeout);
                    }
                    public void setTcpNoDelay(boolean on) throws SocketException {
                        delegate.setTcpNoDelay(on);
                    }
                    public void setTrafficClass(int tc) throws SocketException {
                        delegate.setTrafficClass(tc);
                    }
                    public void shutdownInput() throws IOException {
                        delegate.shutdownInput();
                    }
                    public void shutdownOutput() throws IOException {
                        delegate.shutdownOutput();
                    }
                    public String toString() {
                        return "<delegate for: " + delegate + ">";
                    }
                };
            }
        };
    }

    public Socket createSocket(String host, int port) throws IOException {
        return new Socket(host, port) {
            private OutputStream out = null;
            private InputStream in = null;
            public OutputStream getOutputStream() throws IOException {
                if (out == null) {
                    out = new CountOutputStream(super.getOutputStream());
                }
                return out;
            }
            public InputStream getInputStream() throws IOException {
                if (in == null) {
                    in = new CountInputStream(super.getInputStream());
                }
                return in;
            }
        };
    }

}
