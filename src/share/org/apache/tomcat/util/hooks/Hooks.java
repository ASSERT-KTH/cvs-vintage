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

package org.apache.tomcat.util.hooks;

import java.util.Vector;

/** Hooks support. Hooks implement a chain-of-command pattern, and
 * are commonly used in most web servers as a mechanism of extensibility.
 *
 * This class doesn't deal with hook invocation - the program is expected
 * to use interfaces or base classes with the hooks beeing methods that
 * are invoked. Reflection-based invocation is very expensive and shouldn't 
 * be used.
 * 
 * The Hooks class will provide support for registering and maintaining
 * a list of modules implementing each hook.
 *
 * The module can be added automatically to the right lists by using
 * introspection ( if the module implements a certain method, it'll
 * be added to the chain for the hook with the same name ). It is also
 * possible for a module to explicitely register hooks.
 *
 * This class is modeled after Apache2.0 hooks - most web servers are using
 * this pattern, but so far A2.0 has the most flexible and powerfull
 * implementation
 */
public class Hooks {
    static org.apache.commons.logging.Log logger =
	org.apache.commons.logging.LogFactory.getLog(Hooks.class);

    public static final int INITIAL_HOOKS=24;
    int hookCount;
    String hookNames[];
    Vector hooksV[];

    Object hooks[][];

    Vector allModulesV;
    Object allModules[];

    private static final int dL=0;

    public Hooks() {
	this.hookCount=INITIAL_HOOKS; // XXX TODO: resizing
	hooksV=new Vector[hookCount];
	for( int i=0; i<hookCount ; i++ )
	    hooksV[i]=new Vector();
	allModulesV=new Vector();
	hookNames=new String[hookCount];
	hooks=new Object[hookCount][];
    }

    /** Allow direct access to hooks. You must call resetCache()
	if you change the hooks
    */
    public Vector getHooksVector( int type ) {
	return hooksV[type];
    }

    public Vector getHooksVector() {
	return allModulesV;
    }

    public void resetCache() {
	for( int i=0; i<hookCount; i++ )
	    hooks[i]=null;
	allModules=null;
    }
    
    public int registerHook( String name ) {
	for( int i=0; i<hookNames.length; i++ ) {
	    if( hookNames[i]==null ) {
		hookNames[i]=name;
		return i;
	    }
	}
	throw new RuntimeException("Too many hooks");
    }

    public void registerHook( String name, int id ) {
	hookNames[id]=name;
    }

    public String getHookName( int id ) {
	return hookNames[id];
    }

    public int getHookId( String hookName ) {
	for( int i=0; i< hookNames.length; i++ ) {
	    if( hookName.equals(hookNames[i]))
		return i;
	}
	return -1;
    }
    

    /** Add the module to all the hook chains it's implements
     *  The hook name should match a method defined in the module
     *  ( not inherited - explicitely defined there )
     */
    public void addModule( Object bi ) {
	// the module wants to register the hooks itself
	for( int i=0; i< hookNames.length ; i++ ) {
	    if( hookNames[i]==null ) continue;
	    if( hasHook( bi, hookNames[i] )) {
		if( logger.isDebugEnabled() ) 
		    logger.debug( "Adding " + hookNames[i] + " " +bi );
		hooksV[i].addElement( bi );
		hooks[i]=null;
	    }
	}
	allModules=null;
	allModulesV.addElement( bi );
    }

    public void addModule( String type, Object bi ) {
	int typeId=getHookId( type );
	hooksV[typeId].addElement( bi );
	hooks[typeId]=null;
    }

    public void removeModule( Object bi ) {
	for( int i=0; i<hookNames.length; i++ ) {
	    if( hooksV[i].contains( bi )) {
		hooksV[i].removeElement( bi );
		hooks[i]=null;
	    }
	}
	allModulesV.removeElement( bi );
	allModules=null;
    }

    public Object[] getModules( int type )
    {
	if( hooks[type] != null ) {
	    return hooks[type];
	}

	hooks[type]=new Object[hooksV[type].size()];
	hooksV[type].copyInto( hooks[type] );
	return hooks[type];
    }
    
    /** Get all interceptors
     */
    public Object[] getModules()
    {
	if( allModules!=null ) {
	    return allModules;
	}
	allModules=new Object[allModulesV.size()];
	allModulesV.copyInto( allModules );
	return allModules;
    }

    /** Test if the interceptor implements a particular
     *  method
     */
    private static boolean hasHook( Object obj, String methodN ) {
	if( hookFinder==null ) 
	    return true;
	return hookFinder.hasHook( obj, methodN );

    }

    // -------------------- hook for hook detection --------------------
    
    /** Interface that decouples the Hooks from the introspection code.
	We want to allow future modes that are not based on introspection -
	for example declarative	( using modules.xml declarations ) or
	based on code generation ( introspection done at deploy time ).
    */
    public static interface HookFinder {
	public boolean hasHook( Object obj, String hookName );
    }
    
    // 
    static HookFinder hookFinder=null;
    
    public static void setHookFinder( HookFinder hf ) {
	hookFinder=hf;
    }
    
}
