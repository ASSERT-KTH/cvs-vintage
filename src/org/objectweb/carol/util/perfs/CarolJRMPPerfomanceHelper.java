/*
 * @(#) .java	1.0 02/07/15
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
 *
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
 *
 */
package org.objectweb.carol.util.perfs;

import java.io.ByteArrayOutputStream;

import sun.rmi.server.MarshalOutputStream;

/**
 * Class <code>CarolJRMPPerfomanceHelper</code>Provide an Helper for perfs mesures
 * 
 * 
 * @author  Simon Nieuviarts 
 * @version 1.0, 15/03/2003
 *
 */
public class CarolJRMPPerfomanceHelper {

    public static String getMarshalBytes(Object obj) {
	try {
	    String result = "";
	    // Print the Context value and size
	    ByteArrayOutputStream ostream = new ByteArrayOutputStream();
	    MarshalOutputStream p = new MarshalOutputStream(ostream);
	    p.writeObject(obj);
	    p.flush();
	    byte[] b = ostream.toByteArray();
	    for (int i=0; i<b.length; i++) {
		if ((b[i] >= 0) && (b[i] < 32)) {
		    result+="<"+b[i]+">";
		} else
		    result+=(char)b[i];
	    }
	    return result;
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public static int getMarshalSize(Object obj) {
	try {
	    // Print the Context value and size
	    ByteArrayOutputStream ostream = new ByteArrayOutputStream();
	    MarshalOutputStream p = new MarshalOutputStream(ostream);
	    p.writeObject(obj);
	    p.flush();
	    return ostream.size();
	} catch (Exception e) {
	    e.printStackTrace();
	    return 0;
	}
    }
}
