/**
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
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
 * --------------------------------------------------------------------------
 * $Id: CarolConfig.java,v 1.2 2005/02/08 22:45:36 el-vadimo Exp $
 * --------------------------------------------------------------------------
 */


package org.objectweb.carol.ant;

import java.util.List;
import java.util.Arrays;

import org.apache.tools.ant.BuildException;

public final class CarolConfig {
    public final static String IIOP    = "iiop";
    public final static String JEREMIE = "jeremie";
    public final static String JRMP    = "jrmp";

    private final static List PROTOCOLS =
        Arrays.asList(new String[] {IIOP, JEREMIE, JRMP});

    private String proto1;
    private String proto2;

    public void setName(String name) throws BuildException {
        if (name == null) {
            throw new NullPointerException("name");
        }

        String trimmed = name.trim();
        if ("".equals(trimmed)) {
            throw new BuildException("name not set");
        }

        int dotIdx = trimmed.indexOf('.');
        if (dotIdx < 0) {
            proto1 = trimmed;
        } else {
            if (dotIdx == trimmed.length()-1) {
                throw new BuildException("name can't end in a dot: " + name);
            }
            proto1 = trimmed.substring(0, dotIdx);
            proto2 = trimmed.substring(dotIdx+1);
        }
        
        
        if (!PROTOCOLS.contains(proto1)) {
            throw new BuildException
                ("Unknown protocol: " + proto1 + "; known protocols are " + PROTOCOLS);
        }
        if (proto2 != null && !PROTOCOLS.contains(proto2)) {
            throw new BuildException
                ("Unknown protocol: " + proto2 + "; known protocols are " + PROTOCOLS);
        }
    }

    public String getProto1() {
        return proto1;
    }

    public String getProto2() {
        return proto2;
    }
}
