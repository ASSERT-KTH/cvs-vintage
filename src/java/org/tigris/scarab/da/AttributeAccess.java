package org.tigris.scarab.da;

/* ================================================================
 * Copyright (c) 2000 CollabNet.  All rights reserved.
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
 * software developed by CollabNet (http://www.collab.net/)."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" name
 * nor may "Tigris" appear in their names without prior written
 * permission of CollabNet.
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
 * individuals on behalf of CollabNet.
 */

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.torque.util.Criteria;

/**
 * Access to data relating to attributes.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 */
public interface AttributeAccess
{
    /**
     * Retrieves a list of attribute identifiers for use in
     * determining which columns to display for a user's query
     * results.
     *
     * @param userID The associated user (must be
     * non-<code>null</code>).
     * @param listID The associated artifact type list (can be
     * <code>null</code>).
     * @param moduleID The associated module (ignored if <code>null</code>).
     * @param artifactTypeID The associated artifact type (ignored if
     * <code>null</code>).
     * @return A list of attribute identifiers.
     * @throws DAException If any problems are encountered.
     */
    List retrieveQueryColumnIDs(String userID, String listID,
                                String moduleID, String artifactTypeID);


    /**
     * Deletes the persisted choice of issue list display columns for
     * the given user and artifact type(s).
     *
     * @param userID The associated user (must be
     * non-<code>null</code>).
     * @param listID The associated artifact type list (can be
     * <code>null</code>).
     * @param moduleID The associated module (ignored if <code>null</code>).
     * @param artifactTypeID The associated artifact type (ignored if
     * <code>null</code>).
     * @throws DAException If any problems are encountered.
     */
    void deleteQueryColumnIDs(String userID, String listID,
                              String moduleID, String artifactTypeID);

    /**
     * Set of attributeIDs which are active and required within the given 
     * module for the given issue type and whose attribute group's are 
     * also active.
     * @param moduleID The associated module (must be
     * non-<code>null</code>).
     * @param artifactTypeID The associated artifact type (must be
     * non-<code>null</code>).
     * @return an <code>String</code> of String attribute ids
     */
    public Set retrieveRequiredAttributeIDs(String moduleID, 
                                            String artifactTypeID);

    /**
     * Set of attributeIDs which are active and marked for custom search 
     * within the given 
     * module for the given issue type and whose attribute group's are 
     * also active.
     * @param moduleID The associated module (must be
     * non-<code>null</code>).
     * @param artifactTypeID The associated artifact type (must be
     * non-<code>null</code>).
     * @return an <code>Set</code> of String attribute ids
     */
    public Set retrieveQuickSearchAttributeIDs(String moduleID, 
                                               String issueTypeID);

    /**
     * Torque <code>Attribute</code>s which are active within the 
     * given module for the given issue type 
     * <strike>and whose attribute group's are also active</strike>.  
     *
     * @param moduleID The associated module (must be
     * non-<code>null</code>).
     * @param artifactTypeID The associated artifact type (must be
     * non-<code>null</code>).
     * @param isOrdered indication whether an iterator over the Attributes 
     * should return them in their natural order.
     * @return an <code>Collection</code> of torque Attribute objects.  The
     * collection will be a List if isOrdered is true, otherwise a Set is
     * returned.
     */
    public Collection retrieveActiveAttributeOMs(String moduleID,
                                                 String artifactTypeID, 
                                                 boolean isOrdered);


    /**
     * Retrieves the attribute ID which is active and marked as the 
     * default text attribute within the given 
     * module for the given issue type and whose attribute group is 
     * also active.
     * @param moduleID The associated module (must be
     * non-<code>null</code>).
     * @param artifactTypeID The associated artifact type (must be
     * non-<code>null</code>).
     * @return an <code>String</code> attribute ID
     */
    public String retrieveDefaultTextAttributeID(String moduleID, 
                                                 String artifactTypeID);

    /**
     * Retrieves the attribute ID which is active and is the first id returned
     * when results are ordered based on numerical preferred order and/or 
     * alphabetical by name within the given 
     * module for the given issue type and whose attribute group is 
     * also active.
     * @param moduleID The associated module (must be
     * non-<code>null</code>).
     * @param artifactTypeID The associated artifact type (must be
     * non-<code>null</code>).
     * @return an <code>String</code> attribute ID
     */
    public String retrieveFirstActiveTextAttributeID(String moduleID, 
                                                     String artifactTypeID);
}
