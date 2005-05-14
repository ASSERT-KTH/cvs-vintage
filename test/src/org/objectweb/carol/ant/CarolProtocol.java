/**
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
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
 * $Id: CarolProtocol.java,v 1.5 2005/05/14 00:03:58 rhs Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.ant;

/**
 * @author Vadim Nasardinov (vadimn@redhat.com)
 */
final class CarolProtocol {

    /**
     * IIOP protocol
     */
    public static final CarolProtocol IIOP = protocol("iiop");

    /**
     * Jeremie protocol
     */
    public static final CarolProtocol JEREMIE = protocol("jeremie");

    /**
     * JRMP protocol (v 1.1)
     */
    public static final CarolProtocol JRMP11 = protocol("jrmp", "1.1", true);

    /**
     * JRMP protocol (v 1.2)
     */
    public static final CarolProtocol JRMP12 = protocol("jrmp", "1.2", true);

    /**
     * IRMI protocol (v 1.1)
     */
    public static final CarolProtocol IRMI11 = protocol("irmi", "1.1", true);

    /**
     * IRMI protocol (v 1.2)
     */
    public static final CarolProtocol IRMI12 = protocol("irmi", "1.2", true);

    /**
     * Name of the protocol
     */
    private final String name;

    /**
     * Version of the protocol (JRMP)
     */
    private final String version;

    /**
     * True if the protocol uses sun stubs
     */
    private final boolean sunStubs;

    /**
     * Build a new carol protocol with the given name and its version
     * @param name of the protocol
     * @param version of the protocol (JRMP)
     * @param sunStubs true if the protocol uses sun stubs
     */
    private CarolProtocol(String name, String version, boolean sunStubs) {
        this.name = name;
        if (version == null) {
            this.version = name;
        } else {
            this.version = name + version;
        }
        this.sunStubs = sunStubs;
    }

    /**
     * Build a new carol protocol without specified version
     * @param name name of the protocol
     * @return the protocol built
     */
    private static CarolProtocol protocol(String name) {
        return new CarolProtocol(name, null, false);
    }

    /**
     * Build a new carol protocol with a given version
     * @param name of the protocol
     * @param version of the protocol
     * @return the protocol built
     */
    private static CarolProtocol protocol(String name, String version) {
        return new CarolProtocol(name, version, false);
    }

    /**
     * Build a new carol protocol with a given version
     * @param name of the protocol
     * @param version of the protocol
     * @param sunStubs true if the protocol uses sun stubs
     * @return the protocol built
     */
    private static CarolProtocol protocol(String name, String version, boolean sunStubs) {
        return new CarolProtocol(name, version, sunStubs);
    }

    /**
     * @return name of the protocol
     */
    public String getName() {
        return name;
    }

    /**
     * @return version of the protocol
     */
    public String getNameVersion() {
        return version;
    }

    /**
     * @return true if this protocol requires sun stubs
     */
    public boolean useSunStubs() {
        return sunStubs;
    }
}
