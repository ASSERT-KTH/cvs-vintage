/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.qlog;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.log.LogManager;
    
/**
 *
 * @author Costin Manolache
 */
public final class QLogFactory extends LogFactory {
    static LogManager logManager;
    static LogDaemon logDaemon;
    
    public QLogFactory() {
        super();

        if( logManager == null ) {
            logManager=new TomcatLogManager();

	    logDaemon=new LogDaemon();
	    logDaemon.start();
	}
    }

    /**
     * The configuration attributes for this {@link LogFactory}.
     */
    private Hashtable attributes = new Hashtable();

    // previously returned instances, to avoid creation of proxies
    private Hashtable instances = new Hashtable();

    // --------------------------------------------------------- Public Methods
    /** Adapter and registry for QueueLoggers
     */
    static class TomcatLogManager extends LogManager {
	TomcatLogManager() {
	    // can't be changed after this
	    LogManager olm=org.apache.tomcat.util.log.Log.setLogManager( this ); 
	    this.loggers=olm.getLoggers();
	    this.channels=olm.getChannels();
	}
    }


    /**
     * Return the configuration attribute with the specified name (if any),
     * or <code>null</code> if there is no such attribute.
     *
     * @param name Name of the attribute to return
     */
    public Object getAttribute(String name) {
        return (attributes.get(name));
    }


    /**
     * Return an array containing the names of all currently defined
     * configuration attributes.  If there are no such attributes, a zero
     * length array is returned.
     */
    public String[] getAttributeNames() {
        Vector names = new Vector();
        Enumeration keys = attributes.keys();
        while (keys.hasMoreElements()) {
            names.addElement((String) keys.nextElement());
        }
        String results[] = new String[names.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = (String) names.elementAt(i);
        }
        return (results);
    }


    /**
     * Convenience method to derive a name from the specified class and
     * call <code>getInstance(String)</code> with it.
     *
     * @param clazz Class for which a suitable Log name will be derived
     *
     * @exception LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public Log getInstance(Class clazz)
        throws LogConfigurationException
    {
        return getInstance( clazz.getName() );
    }
    

    public Log getInstance(String name)
        throws LogConfigurationException
    {
        Log instance = (Log) instances.get(name);
        if( instance != null )
            return instance;

        instance =org.apache.tomcat.util.log.Log.getLog( name, name );
        instances.put( name, instance );
        return instance;
    }


    public void release() {
        instances.clear();
    }


    /**
     * Remove any configuration attribute associated with the specified name.
     * If there is no such attribute, no action is taken.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {
        attributes.remove(name);
    }


    /**
     * Set the configuration attribute with the specified name.  Calling
     * this with a <code>null</code> value is equivalent to calling
     * <code>removeAttribute(name)</code>.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set, or <code>null</code>
     *  to remove any setting for this attribute
     */
    public void setAttribute(String name, Object value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }

}
