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

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.TomcatException;

/**
 * Interceptor to add context-seperation support for log4j logging.
 * Based on ideas from <a href="http://www.qos.ch/logging/sc.html">
 * Support for log4j in Servlet Containers</a> by Ceki Gülcü.
 */

public class Log4jHelper extends BaseInterceptor {

    private CRS crs = new CRS();

    public Log4jHelper() {}

    public void setDefaultLevel(String level) {
	Level value = null;
	if("all".equalsIgnoreCase(level))
	    value = Level.ALL;
	else if("debug".equalsIgnoreCase(level))
	    value = Level.DEBUG;
	else if("error".equalsIgnoreCase(level))
	    value =Level.ERROR;
	else if("fatal".equalsIgnoreCase(level))
	    value = Level.FATAL;
	else if("info".equalsIgnoreCase(level))
	    value = Level.INFO;
	else if("off".equalsIgnoreCase(level))
	    value = Level.OFF;
	else if("warn".equalsIgnoreCase(level))
	    value = Level.WARN;
	crs.setDefaultLevel(value);
    }

    public String getDefaultLevel() {
	String value = null;
	Level level = crs.getDefaultLevel();
	if(level == Level.ALL)
	    value = "all";
	else if(level == Level.DEBUG)
	    value = "debug";
	else if(level == Level.ERROR)
	    value = "error";
	else if(level == Level.FATAL)
	    value = "fatal";
	else if(level == Level.INFO)
	    value = "info";
	else if(level == Level.OFF)
	    value = "off";
	else if(level == Level.WARN)
	    value = "warn";
	return value;
    }

    public void contextInit( Context ctx )
	throws TomcatException {
	crs.addRepository(ctx.getClassLoader());
    }

    public void contextShutdown(  Context ctx )
	throws TomcatException {
	crs.remove(ctx.getClassLoader());
    }

    public void reload(Request req, Context ctx) throws TomcatException {
	ClassLoader oldCl = (ClassLoader)req.getNote("oldLoader");
	if(oldCl != null) {
	    crs.remove(oldCl);
	}
	crs.addRepository(ctx.getClassLoader());
    }

    public void copyContext(Request req, Context oldCtx, Context newCtx)
	throws TomcatException {
	crs.remove(oldCtx.getClassLoader());
	crs.addRepository(newCtx.getClassLoader());
    }

    public void engineInit(ContextManager cm) throws TomcatException {
	LogManager.setRepositorySelector(crs, this);
    }
}

	
