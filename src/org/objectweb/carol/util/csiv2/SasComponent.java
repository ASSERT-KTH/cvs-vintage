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
 * $Id: SasComponent.java,v 1.1 2004/12/13 16:24:13 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.csiv2;

import org.objectweb.carol.util.csiv2.struct.AsStruct;
import org.objectweb.carol.util.csiv2.struct.SasStruct;
import org.objectweb.carol.util.csiv2.struct.TransportStruct;

/**
 * Class which allow to define compound sec mech object
 * @author Florent Benoit
 */
public class SasComponent implements org.omg.CORBA.portable.IDLEntity {

    /**
     * Transport mech
     */
    private TransportStruct transport = null;

    /**
     * As mech
     */
    private AsStruct as = null;

    /**
     * Sas mech
     */
    private SasStruct sas = null;

    /**
     * Constructor
     * @param transport the given transport mech
     * @param as the given as mech
     * @param sas the given sas mech
     */
    public SasComponent(TransportStruct transport, AsStruct as, SasStruct sas) {
        this.transport = transport;
        this.as = as;
        this.sas = sas;
    }

    /**
     * @return Returns the as.
     */
    public AsStruct getAs() {
        return as;
    }
    /**
     * @return Returns the sas.
     */
    public SasStruct getSas() {
        return sas;
    }
    /**
     * @return Returns the transport.
     */
    public TransportStruct getTransport() {
        return transport;
    }
}