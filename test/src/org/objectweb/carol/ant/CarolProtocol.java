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
 * $Id: CarolProtocol.java,v 1.1 2005/02/09 19:47:48 el-vadimo Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.ant;

final class CarolProtocol {
    public final static CarolProtocol IIOP    = protocol("iiop");
    public final static CarolProtocol JEREMIE = protocol("jeremie");
    public final static CarolProtocol JRMP11  = protocol("jrmp", "1.1");
    public final static CarolProtocol JRMP12  = protocol("jrmp", "1.2");

    private final String name;
    private final String version;

    private CarolProtocol(String name, String version) {
        this.name = name;
        if (version == null) {
            this.version = name;
        } else {
            this.version = name + version;
        }
    }

    private static CarolProtocol protocol(String name) {
        return new CarolProtocol(name, null);
    }

    private static CarolProtocol protocol(String name, String version) {
        return new CarolProtocol(name, version);
    }

    public String getName() {
        return name;
    }

    public String getNameVersion() {
        return version;
    }
}
