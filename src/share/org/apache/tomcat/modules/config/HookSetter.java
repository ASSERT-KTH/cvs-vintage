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

package org.apache.tomcat.modules.config;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.hooks.Hooks;

/**
 * Keep hook chains to minimal, using introspection.
 *
 * @author Costin Manolache
 */
public class HookSetter extends BaseInterceptor {
    
    public HookSetter() {
    }

    // -------------------- Properties --------------------
    
    // -------------------- Hooks --------------------

    /** When this module is added, it'll automatically load
     *  a configuration file and add all global modules.
     */
    public void addInterceptor(ContextManager cm, Context ctx,
			       BaseInterceptor module)
	throws TomcatException
    {
	Hooks.setHookFinder( new IntrospectionHookFinder() );
    }

    static class IntrospectionHookFinder implements Hooks.HookFinder {
	public boolean hasHook( Object o, String hook ) {
	    return IntrospectionUtils.hasHook( o, hook );
	}
    }
}

