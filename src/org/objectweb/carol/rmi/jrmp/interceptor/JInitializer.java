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
 * $Id: JInitializer.java,v 1.2 2004/09/01 11:02:41 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.jrmp.interceptor;

/**
 * Interface <code>JInitializer</code> is the CAROL JRMP Initializer interface
 * This interface is use to register Interceptor
 * @author Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 */
public interface JInitializer {

    /**
     * In JRMP the 2 method( per and post init have the same consequences ...
     * @param JInitInfo the JInit Information
     */
    public void pre_init(JInitInfo info);

    /**
     * In JRMP the 2 method( per and post init have the same consequences ...
     * @param JInitInfo the JInit Information
     */
    public void post_init(JInitInfo info);

}