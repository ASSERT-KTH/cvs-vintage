/**
 * Copyright (C) 2002,2004 - INRIA (www.inria.fr)
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
 * --------------------------------------------------------------------------
 * $Id: JRequestInfo.java,v 1.5 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.jrmp.interceptor;

import java.util.Collection;

/**
 * Class <code>JRequestInfo</code> is the CAROL JRMP general Request info
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public interface JRequestInfo {

    /**
     * Get the context specifie by this id if there is no context corresponding
     * with this id return null
     * @param id the context id
     * @return JServiceContex the specific ServiceContext
     */
    public JServiceContext get_request_service_context(int id);

    /**
     * Get the all the request service context if there is no context return
     * null
     * @return Collection the ServiceContexts
     */
    public Collection get_all_request_service_context();

    /**
     * Get the context specifie by this id if there is no context corresponding
     * with this id return null
     * @param id the context id
     * @return JServiceContex the specific ServiceContext
     */
    public JServiceContext get_reply_service_context(int id);

    /**
     * Get the all the reply service context if there is no context return null
     * @return Collection the ServiceContexts Array
     */
    public Collection get_all_reply_service_context();

    /**
     * true if exit one or more context
     */
    public boolean hasContexts();

    /**
     * clear the service contexts table
     */
    public void clearAllContexts();
}