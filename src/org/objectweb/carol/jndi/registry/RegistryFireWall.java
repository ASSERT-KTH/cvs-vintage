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
 * $Id: RegistryFireWall.java,v 1.2 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.jndi.registry;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author Guillaume Riviere Feb 7, 2004
 */
public class RegistryFireWall {

    private boolean allowAll = true;

    private Hashtable allowedAddress = new Hashtable(11);

    private Hashtable forbidenAddress = new Hashtable(11);

    public RegistryFireWall() {
    }

    /**
     * Tets if a InetAdress is allow
     * @param i
     * @return
     */
    public boolean isAllow(InetAddress i) {
        if (allowAll) {
            return !(forbidenAddress.contains(i));
        } else {
            return allowedAddress.contains(i);
        }
    }

    /**
     * Allow everybody
     */
    public synchronized void allowAll() {
        allowAll = true;
        allowedAddress.clear();
        forbidenAddress.clear();
    }

    /**
     * Forbid everybody
     */
    public synchronized void forbidAll() {
        allowAll = false;
        allowedAddress.clear();
        forbidenAddress.clear();
    }

    /**
     * add a forbiden address
     * @param i
     */
    public synchronized void addForbidenAddress(InetAddress i) {
        if (allowedAddress.contains(i)) {
            allowedAddress.remove(i);
        }
        forbidenAddress.put(i, i);
    }

    /**
     * remove a forbiden adress
     * @param i
     */
    public synchronized void addAllowedAddress(InetAddress i) {
        if (forbidenAddress.contains(i)) {
            forbidenAddress.remove(i);
        }
        allowedAddress.put(i, i);
    }

    /**
     * list forbiden adress
     * @return
     */
    public InetAddress[] listForbidenAddress() {
        int i = forbidenAddress.size();
        InetAddress[] result = new InetAddress[i];
        Enumeration enum = forbidenAddress.keys();
        while ((--i) >= 0)
            result[i] = (InetAddress) enum.nextElement();
        return result;
    }

    /**
     * list Allowed Adress
     * @return
     */
    public InetAddress[] listAllowedAddress() {
        int i = allowedAddress.size();
        InetAddress[] result = new InetAddress[i];
        Enumeration enum = allowedAddress.keys();
        while ((--i) >= 0)
            result[i] = (InetAddress) enum.nextElement();
        return result;
    }

    /**
     * is allow for all
     * @return
     */
    public boolean isAllowAll() {
        return allowAll;
    }

}