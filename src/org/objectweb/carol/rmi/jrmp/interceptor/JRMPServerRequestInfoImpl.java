/*
 * @(#) JRMPServerRequestInfoImpl.java	1.0 02/07/15
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

//java import
import java.util.Enumeration;
import java.util.Hashtable;

/**
  * Class <code>JRMPServerRequestInfoImpl</code> is the CAROL JRMP Server Request info (JServerRequestInfo) Implementation
 *
 * @see org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInfo
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002  
 */
public class JRMPServerRequestInfoImpl implements JServerRequestInfo {

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
    public JRMPServerRequestInfoImpl () {
	scReplyTable = new Hashtable();
    }

    /**
     * Full constructor 
     * available for Reply Information
     * @param JServiceContext [] the table for Reply SC instantiation 
     */
    public JRMPServerRequestInfoImpl (JServiceContext [] scs) {
	scReplyTable = new Hashtable();
	scRequestTable = new Hashtable();
	for (int i = 0; i < scs.length; i++) {
	    scRequestTable.put(new Integer(scs[i].context_id), scs[i]);
	}
    }    
    /**
     * add a JServicecontext
     * @param JServiceContext the context to add
     * @param boolean replace if true replace the existing service context
     */
    public void add_reply_service_context(JServiceContext jServiceContext, boolean replace) {
	Integer ctxId = new Integer(jServiceContext.context_id);
	if (replace) {
	    scReplyTable.put(ctxId, jServiceContext);
	} else {
	    if (!scReplyTable.containsKey(ctxId)) {
		scReplyTable.put(ctxId, jServiceContext);
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
	return (JServiceContext)scRequestTable.get(new Integer(id));
    }

    /**
     * Get the all the request service context 
     * if there is no context
     * return null
     * @return JServiceContext []  the  ServiceContexts
     */
    public JServiceContext [] get_all_request_service_context() {
	JServiceContext [] result = new JServiceContext [scRequestTable.size()];
	int i =0;
	for (Enumeration e = scRequestTable.elements() ; e.hasMoreElements() ;) {
	    result[i] = (JServiceContext)(e.nextElement());
	    i ++;
	}
	return result; 
    }  

    /**
     * Get the context specifie by this id 
     * if there is no context corresponding with this id
     * return null
     * @param id the context id
     * @return JServiceContex the specific ServiceContext
     */
    public JServiceContext get_reply_service_context(int id) {
	return (JServiceContext)scReplyTable.get(new Integer(id));
    }

    /**
     * Get the all the reply service context 
     * if there is no context
     * return null
     * @return JServiceContext []  the  ServiceContexts
     */
    public JServiceContext [] get_all_reply_service_context() {
	JServiceContext [] result = new JServiceContext [scReplyTable.size()];
	int i = 0;
	for (Enumeration e = scReplyTable.elements() ; e.hasMoreElements() ;) {
	    result[i] = ((JServiceContext)e.nextElement());	 
	    i ++;
	}
	return result;
    }  


}
