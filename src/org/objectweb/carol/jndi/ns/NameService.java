/*
 * @(#)NameService.java	1.0 02/07/15
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
 */
package org.objectweb.carol.jndi.ns;

/*
 * Interface <code>NameService</code> is the CAROL Name Service generic interface.
 * This is the carol SPI for a Name Service
 *
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/01/2003
 */
public interface NameService {

    /**
     * start Method, Start a new NameService or do nothing if the name service is all ready start
     * @param int port is port number
     * @throws NameServiceException if a problem occure 
     */
    public void start() throws NameServiceException;

    /**
     * stop Method, Stop a NameService or do nothing if the name service is all ready stop
     * @throws NameServiceException if a problem occure 
     */
    public void stop() throws NameServiceException;

    /**
     * isStarted Method, check if a name service is started
     * @return boolean true if the name service is started
     */
    public boolean isStarted();

	/**
	 * set port method, set the port for the name service
	 * @param int port number
	 */
	public void setPort(int p);   
	
	/**
	* get port method, get the port for the name service
	* @return int port number
	*/
   public int getPort();
}
