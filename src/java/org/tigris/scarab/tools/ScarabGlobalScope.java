package org.tigris.scarab.tools;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

import java.util.List;

import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.velocity.app.FieldMethodizer;


/**
 * This scope is an object that is made available as a global
 * object within the system.
 * This object must be thread safe as multiple
 * requests may access it at the same time. The object is made
 * available in the context as: $scarabG
 * <p>
 * The design goals of the Scarab*API is to enable a <a
 * href="http://jakarta.apache.org/turbine/pullmodel.html">pull based
 * methodology</a> to be implemented.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ScarabGlobalScope.java,v 1.17 2003/03/15 21:56:59 jon Exp $
 */
public interface ScarabGlobalScope extends ApplicationTool
{
    /**
     * holds the Scarab constants. it will be available to the template system
     * as $scarabG.Constant.CONSTANT_NAME.
     */
    FieldMethodizer getConstant();

    /**
     * holds the Scarab permission constants.  It will be available to 
     * the template system as $scarabG.PERMISSION_NAME.
     */
    FieldMethodizer getPermission();
        
    /**
     * Gets a List of all of user objects
     * By attribute Type : either user, or non-user.
     */
    List getUserAttributes()
        throws Exception;

    /**
     * Gets a List of all of user Attribute objects.
     */
    List getAttributes(String attributeType)
        throws Exception;

    /**
     * gets a list of all Issue Types 
     */
    List getAllIssueTypes()
        throws Exception;
    
    /**
     * Gets a List of all of the Attribute objects.
     */
    List getAllAttributes() 
        throws Exception;
    
    /**
     * Gets a List of users based on the specified search criteria.
     */
    List getSearchUsers(String searchField, String searchCriteria)
        throws Exception;
    
    /**
     * Gets a List of users based on the specified search criteria and
     * orders the list on the specified field.
     */
    List getSearchUsers(String searchField, String searchCriteria, 
                               String orderByField, String ascOrDesc)
        throws Exception;

    /**
     * Creates a new array with elements reversed from the given array.
     *
     * @param a the orginal <code>Object[]</code>
     * @return a new <code>Object[]</code> with values reversed from the 
     * original
     */
    Object[] reverse(Object[] a);

    /**
     * Creates a new List with elements reversed from the given List.
     *
     * @param a the orginal <code>List</code>
     * @return a new <code>List</code> with values reversed from the 
     * original
     */
    List reverse(List a);

    /**
     * Creates  a view of the portion of the given
     * List between the specified fromIndex, inclusive, and toIndex, exclusive
     * The list returned by this method is backed by the original, so changes
     * to either affect the other.
     *
     * @param a the orginal <code>List</code>
     * @param fromIndex the start index of the returned subset
     * @param toIndex the end index of the returned subset
     * @return a derived <code>List</code> with a view of the original
     */
    List subset(List a, Integer fromIndex, Integer toIndex);

    /**
     * Creates a new array with a view of the portion of the given array
     * between the specified fromIndex, inclusive, and toIndex, exclusive
     *
     * @param a the orginal <code>Object[]</code>
     * @param fromIndex the start index of the returned subset
     * @param toIndex the end index of the returned subset
     * @return a new <code>Object[]</code> with a view of the original
     */
    Object[] subset(Object[] a, Integer fromIndex, Integer toIndex);

    /**
     * Velocity has no way of getting the size of an <code>Object[]</code>
     * easily. Usually this would be done by calling obj.length
     * but this doesn't work in Velocity.
     * @param obj the <code>Object[]</code>
     * @return the number of objects in the <code>Object[]</code>  or -1 if obj is null
     */
    int sizeOfArray(Object[] obj);

    /**
     * Logs a message at the debug level.  Useful for "I am here" type 
     * messages. The category is "org.tigris.scarab". 
     *
     * @param s message to log
     */
    void log(String s);

    /**
     * Logs a message at the debug level.  Useful for "I am here" type 
     * messages. The category in which to log is also specified. 
     *
     * @param category log4j Category
     * @param s message to log
     */
    void log(String category, String s);

    /**
     * Prints a message to standard out.  Useful for "I am here" type 
     * messages. 
     *
     * @param s message to log
     */
    void print(String s);

    /**
     * Provides the site name for the top banner.
     *
     * @return the configured site name
     */
    String getSiteName();

    /**
     * Returns an <code>int</code> representation of the given
     * <code>Object</code> whose toString method should be a valid integer.
     * If the <code>String</code> cannot be parsed <code>0</code> is returned.
     * @param obj the object
     * @return the <code>int</code> representation of the <code>Object</code>
     *  if possible or <code>0</code>.
     */
    int getInt(Object obj);
}
