/*
 *  Copyright 2001-2004 The Apache Software Foundation
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

package org.apache.tomcat.modules.loggers.log4j;

import java.util.Hashtable;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RepositorySelector;
import org.apache.log4j.spi.RootCategory;

/**
 * Implimentation to add context-seperation support for log4j logging.
 * Code taken from <a href="http://www.qos.ch/logging/sc.html">
 * Support for log4j in Servlet Containers</a> by Ceki Gülcü.
 * @author Ceki Gülcü.
 */
public class CRS implements RepositorySelector {
    private Hashtable ht = new Hashtable();
    private Level defLevel=Level.DEBUG;

    public void setDefaultLevel(Level lv) {
	if(lv != null)
	    defLevel = lv;
    }
    public Level getDefaultLevel() {
	return defLevel;
    }

    void addRepository(ClassLoader cl) {
	if( ht.get(cl) != null ) {
	    throw new IllegalStateException("ClassLoader " + cl + " already registered");
	}
	ht.put(cl, new Hierarchy(new RootCategory( defLevel)));
    }
				 
    // the returned value is guaranteed to be non-null
    public LoggerRepository getLoggerRepository() {
	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	Hierarchy hierarchy = (Hierarchy) ht.get(cl);
	
	if(hierarchy == null) {
	    hierarchy = new Hierarchy(new RootCategory(defLevel));
	    ht.put(cl, hierarchy);
	} 
	return hierarchy;
    }

    /** 
     * The Container should remove the entry when the web-application
     * is removed or restarted.
     * */
    public void remove(ClassLoader cl) {
	ht.remove(cl); 
    } 
}


