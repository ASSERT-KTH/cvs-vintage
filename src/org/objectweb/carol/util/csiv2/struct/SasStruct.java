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
 * $Id: SasStruct.java,v 1.1 2004/12/13 16:24:13 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.csiv2.struct;

import java.io.Serializable;

import org.objectweb.carol.util.csiv2.gss.GSSHelper;

/**
 * Cannot extends final class SAS_ContextSec
 * @author Florent Benoit
 */
public class SasStruct implements Serializable {

    /**
     * Target supports for this mech
     */
    private short targetSupports = 0;

    /**
     * Target requires for this mech
     */
    private short targetRequires = 0;

    /**
     * Identity types supported
     */
    private int supportedIdentityTypes = 0;

    /**
     * Naming mechanisms supported
     */
    private byte[][] supportedNamingMechanisms = new byte[0][0];

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
     * @return Returns the targetSupports.
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

    /**
     * @return Returns the supportedIdentityTypes.
     */
    public int getSupportedIdentityTypes() {
        return supportedIdentityTypes;
    }

    /**
     * @param supportedIdentityTypes The supportedIdentityTypes to set.
     */
    public void setSupportedIdentityTypes(int supportedIdentityTypes) {
        this.supportedIdentityTypes = supportedIdentityTypes;
    }

    /**
     * @return Returns the supportedNamingMechanisms.
     */
    public byte[][] getSupportedNamingMechanisms() {
        return supportedNamingMechanisms;
    }

    /**
     */
    public void enableGSSUPSupportedNamingMechanisms() {

        /**
         * A target that supports identity assertion shall include in its IORs
         * the complete list of GSS mechanisms for which it supports identity
         * assertions using an identity token of type ITTPrincipalName.
         */

        byte[] gssupDerEncoding = GSSHelper.getMechOidDer();

        // One supported (GSSUP)
        this.supportedNamingMechanisms = new byte[1][gssupDerEncoding.length];

        // Copy encoding DER of GSSUP Oid in first index of the array of one element
        System.arraycopy(gssupDerEncoding, 0, this.supportedNamingMechanisms[0], 0, gssupDerEncoding.length);

    }

}