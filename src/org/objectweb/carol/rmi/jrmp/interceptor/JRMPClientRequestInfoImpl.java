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



import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Class <code>JRMPClientRequestInfoImpl</code> is the CAROL JRMP Client Request info (JClientRequestInfo) Implementation
 *
 * @see org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInfo
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public class JRMPClientRequestInfoImpl implements JClientRequestInfo {
 
    /**
     * Request Service Context HasTable
     */
    protected ArrayList scTable = new ArrayList();  

    /**
     * Empty constructor
     * available for Request Information
     */
    public JRMPClientRequestInfoImpl () {
    }
    
    /**
     * add a JServicecontext
     * @param JServiceContext the context to add
     * @param boolean replace if true replace the existing service context
     */
    public void add_request_service_context(JServiceContext jServiceContext) {
		scTable.add(jServiceContext);
    }

    /**
     * Get the context specifie by this id 
     * if there is no context corresponding with this id
     * return null
     * @param id the context id
     * @return JServiceContex the specific ServiceContext
     */
    public JServiceContext get_request_service_context(int id) {
    		JServiceContext jc = null;
			for (Iterator i = scTable.iterator() ; i.hasNext() ;) {
				jc = (JServiceContext)i.next(); 
				if (jc.getContextId()==id) {
					return jc;
			    }
			}
			return null;
	}
 
    /**
     * Get the all the request service context 
     * if there is no context
     * return null
     * @return Collection  the  ServiceContexts ArrayList
     */
    public Collection get_all_request_service_context() {
	return scTable;
    }  

    /**
     * Get the context specifie by this id 
     * if there is no context corresponding with this id
     * return null
     * @param id the context id
     * @return JServiceContex the specific ServiceContext
     */
    public JServiceContext get_reply_service_context(int id) {
		JServiceContext jc = null;
		for (Iterator i = scTable.iterator() ; i.hasNext() ;) {
			jc = (JServiceContext)i.next(); 
			if (jc.getContextId()==id) {
				return jc;
			}
		}
		return null;
    }

    /**
     * Get the all the reply service context 
     * if there is no context
     * return null
     * @return Collection  the  ServiceContexts ArrayList
     */
    public Collection get_all_reply_service_context() {
		return scTable;
    }
    
	/**
	 *Add the all the request service context 
	 * @param c Services contexts
	 */
	public void add_all_request_service_context(Collection c) {
		scTable.addAll(c);
	}
    
    /** 
     * true if exit one or more context
     */
    public boolean hasContexts() {
    		return !(scTable.isEmpty());
    }
    
    /**
     * clear the service contexts table
     *
     */
    public void clearAllContexts() {
    		scTable.clear();
    }
}
