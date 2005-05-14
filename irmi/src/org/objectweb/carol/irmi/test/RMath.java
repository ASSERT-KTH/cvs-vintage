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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMath
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public interface RMath extends Remote {

    // maybe pow would be better since arg reversal would be detected

    int add(byte a, byte b) throws RemoteException;

    int add(char a, char b) throws RemoteException;

    int add(short a, short b) throws RemoteException;

    int add(int a, int b) throws RemoteException;

    long add(long a, long b) throws RemoteException;

    float add(float a, float b) throws RemoteException;

    double add(double a, double b) throws RemoteException;

    String add(String a, String b) throws RemoteException;

    int div(int a, int b) throws RemoteException;

}
