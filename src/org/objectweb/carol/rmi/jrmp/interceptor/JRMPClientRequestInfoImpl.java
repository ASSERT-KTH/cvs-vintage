/*
 * @(#) JRMPClientRequestInfoImpl.java	1.0 02/07/15
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
package org.objectweb.carol.rmi.jrmp.interceptor;



import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Class <code>JRMPClientRequestInfoImpl</code> is the CAROL JRMP Client Request info (JClientRequestInfo) Implementation
 *
 * @see org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInfo
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public class JRMPClientRequestInfoImpl implements JClientRequestInfo {
 
    /**
     * exiting contexts, true if there is one or more context 
     */
    public boolean contexts = false;

    /**
     * Request Service Context HasTable
     */
    protected Hashtable scRequestTable = null;  

    /**
     * Reply Service Context HasTable
     */
    protected Hashtable scReplyTable = null;  

    /**
     * Empty constructor
     * available for Request Information
     */
    public JRMPClientRequestInfoImpl () {
	scRequestTable = new Hashtable();
    }

    /**
     * Full constructor 
     * available for Reply Information
     * @param JServiceContext [] the table for Reply SC instantiation 
     */
    public JRMPClientRequestInfoImpl (JServiceContext [] scs) {
	if (scs==null) {
	    // empty  JRMPClientRequestInfoImpl, no context
	} else {
	    scRequestTable = new Hashtable();
	    scReplyTable = new Hashtable();
	    for (int i = 0; i < scs.length; i++) {
		scReplyTable.put(new Integer(scs[i].context_id), scs[i]);
		contexts=true;	
	    }
	}
    }    
    /**
     * add a JServicecontext
     * @param JServiceContext the context to add
     * @param boolean replace if true replace the existing service context
     */
    public void add_request_service_context(JServiceContext jServiceContext, boolean replace) {
	if (scRequestTable!=null) {
	    Integer ctxId = new Integer(jServiceContext.context_id);
	    contexts=true;	
	    if (replace) {
		scRequestTable.put(ctxId, jServiceContext);
	    } else {
		if (!scRequestTable.containsKey(ctxId)) {
		    scRequestTable.put(ctxId, jServiceContext);
		}	
	    }
	}
    }

    /**
     * Get the context specifie by this id 
     * if there is no context corresponding with this id
     * return null
     * @param id the context id
     * @return JServiceContex the specific ServiceContext
     */
    public JServiceContext get_request_service_context(int id) {
	if (scRequestTable!=null) {
	    return (JServiceContext)scRequestTable.get(new Integer(id));
	} else {
	    return null;
	}
    }

    /**
     * Get the all the request service context 
     * if there is no context
     * return null
     * @return JServiceContext []  the  ServiceContexts
     */
    public JServiceContext [] get_all_request_service_context() {
	if (scRequestTable!=null) {
	    JServiceContext [] result = new JServiceContext [scRequestTable.size()];
	    int i =0;
	    for (Enumeration e = scRequestTable.elements() ; e.hasMoreElements() ;) {
		result[i] = (JServiceContext)(e.nextElement());
		i ++;
	    }
	    return result;
	} else {
	    return null;
	} 
    }  

    /**
     * Get the context specifie by this id 
     * if there is no context corresponding with this id
     * return null
     * @param id the context id
     * @return JServiceContex the specific ServiceContext
     */
    public JServiceContext get_reply_service_context(int id) {
	if (scReplyTable!=null) {
	    return (JServiceContext)scReplyTable.get(new Integer(id));
	} else {
	    return null;
	}
    }

    /**
     * Get the all the reply service context 
     * if there is no context
     * return null
     * @return JServiceContext []  the  ServiceContexts
     */
    public JServiceContext [] get_all_reply_service_context() {
	if (scReplyTable!=null) {
	    JServiceContext [] result = new JServiceContext [scReplyTable.size()];
	    int i = 0;
	    for (Enumeration e = scReplyTable.elements() ; e.hasMoreElements() ;) {
		result[i] = ((JServiceContext)e.nextElement());	 
		i ++;
	    }
	    return result;
	} else {
	    return null;
	}
    }
    
    /** 
     * true if exit one or more context
     */
    public boolean hasContexts() {
	return contexts;
    }

}
