package org.tigris.scarab.services.hsql;
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */



import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.hsqldb.HsqlProperties;
import org.hsqldb.Server;

/**
 * An implementation of HSQLService component which configures a single hsqldb 
 * database and allows it to be started and stopped under program control. 
 * 
 * The implementation has been kept to a minimum, so only a single filebased
 * database can be configured. This has also the advantage that the configuration
 * is kept to a minimum.
 * 
 * <p>
 * The component is configured from the componentConfig.xml file by specifying 
 * attributes on the service element
 * </p>
 * <p>
 * 
 * <dl>
 * <dt>database</dt>
 * <dd>The directory path where the database files will be stored</dd>
 * <dt>dbname</dt>
 * <dd>The alial path used to refer to the database from the JDBC url.</dd>
 * <dt>trace</dt>
 * <dd>(true/false) a flag enabling tracing in the hsql server.</dd>
 * <dt>silent</dt>
 * <dd>(true/false) a flag to control the logging output oh thr hsql server.</dd>
 * <dt>start</dt>
 * <dd>(true/false) when true the database is started at configuration time, and does
 * not need to be started under application control.</dd>
 * </dl>
 * 
 * Example:
 *  ...
 * 	<HSQLService database="./target" dbname="test" trace="true" silent="false" start="true"/>
 *  ...
 * 
 * @author <a href="mailto:pti@elex.be">Peter Tillemans</a>
 * @version $Id: HSQLServiceImpl.java,v 1.1 2004/10/27 11:19:46 dep4b Exp $
 */
public class HSQLServiceImpl extends AbstractLogEnabled implements HSQLService, Configurable  {

	private boolean started = false;
	private Server server;
	private Logger logger;
	
	
	/* (non-Javadoc)
	 * @see org.tigris.scarab.test.HSQLService#start()
	 */
	public void start() {
		server.start();
		started = true;
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.test.HSQLService#stop()
	 */
	public void stop() {
		server.stop();
		started = false;
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.test.HSQLService#isStarted()
	 */
	public boolean isStarted() {
		return started;
	}


	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
	 */
	public void configure(Configuration cfg) throws ConfigurationException {
		
		String[] names = cfg.getAttributeNames();
		for (int i = 0; i < names.length; i++) {
			getLogger().debug(names[i] + " --> " + cfg.getAttribute(names[i]));
		}

		HsqlProperties props = new HsqlProperties();
		props.setProperty("server.database.0", cfg.getAttribute("database"));
		props.setProperty("server.dbname.0", cfg.getAttribute("dbname"));
		props.setProperty("server.trace", cfg.getAttributeAsBoolean("trace"));
		props.setProperty("server.silent", cfg.getAttributeAsBoolean("silent"));
		
		server = new Server(); 
		server.setProperties(props);
		if (cfg.getAttributeAsBoolean("start")) {
			start();
		}
	}


}
