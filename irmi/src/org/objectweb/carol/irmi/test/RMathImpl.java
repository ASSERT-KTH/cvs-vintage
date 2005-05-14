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

/**
 * RMathImpl
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class RMathImpl implements RMath {

    public int add(byte a, byte b) {
        return a + b;
    }

    public int add(char a, char b) {
        return a + b;
    }

    public int add(short a, short b) {
        return a + b;
    }

    public int add(int a, int b) {
        return a + b;
    }

    public long add(long a, long b) {
        return a + b;
    }

    public float add(float a, float b) {
        return a + b;
    }

    public double add(double a, double b) {
        return a + b;
    }

    public String add(String a, String b) {
        return a + b;
    }

    public int div(int a, int b) {
        return a/b;
    }

}
