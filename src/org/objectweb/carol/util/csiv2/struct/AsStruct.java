/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004 Bull S.A.
 * Contact: jonas-team@objectweb.org
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
 * $Id: AsStruct.java,v 1.1 2004/12/13 16:24:13 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.csiv2.struct;

import java.io.Serializable;

import org.objectweb.carol.util.csiv2.gss.GSSHelper;

/**
 * Cannot extends final class AS_ContextSec
 * @author Florent Benoit
*/
public class AsStruct implements Serializable {

    /**
     * Target supports for this mech
     */
    private short targetSupports = 0;

    /**
     * Target requires for this mech
     */
    private short targetRequires = 0;

    /**
     * Name of target
     */
    private byte[] targetName = new byte[0];

    /**
     * @return the targetName.
     */
    public byte[] getTargetName() {
        return targetName;
    }

    /**
     * @param targetName The targetName to set.
     */
    public void setTargetName(String targetName) {
        this.targetName = GSSHelper.encodeExported(targetName);
    }

    /**
     * @return client authentication mech
     */
    public byte[] getClientAuthenticationMech() {
        if (targetName.length != 0) {
            return GSSHelper.getMechOidDer();
        } else {
            return new byte[0];
        }
    }

    /**
     * @return the targetRequires.
     */
    public short getTargetRequires() {
        return targetRequires;
    }

    /**
     * @param targetRequires The targetRequires to set.
     */
    public void setTargetRequires(int targetRequires) {
        this.targetRequires = (short) targetRequires;
    }

    /**
     * @return the targetSupports.
     */
    public short getTargetSupports() {
        return targetSupports;
    }

    /**
     * @param targetSupports The targetSupports to set.
     */
    public void setTargetSupports(int targetSupports) {
        this.targetSupports = (short) targetSupports;
    }

}