package org.tigris.scarab.util;

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

import org.tigris.scarab.om.AttributePeer;
import org.apache.torque.om.NumberKey;

/**
 * A place to put public final static strings and other constants.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ScarabConstants.java,v 1.28 2002/01/18 22:26:13 jon Exp $
 */
public interface ScarabConstants
{
    /** 
     * the registration code uses this in order to store an object
     * into the data.getUser().getTemp() hashtable. this is the key 
     * value and is used across several classes.
     */
    public static final String SESSION_REGISTER = "scarab.newUser";

    /**
     * This is the key value that stores the name of the template to 
     * execute next.
     */
    public static final String NEXT_TEMPLATE = "nextTemplate";

    /**
     * This is the key value that stores the name of the template to 
     * cancel to.
     */
    public static final String CANCEL_TEMPLATE = "cancelTemplate";

    /**
     * This is the key value that stores the name of the template to 
     * go back to to.
     */
    public static final String BACK_TEMPLATE = "backTemplate";

    /**
     * This is the key value that stores the name of the template
     * that is currently being executed.
     */
    public static final String TEMPLATE = "template";

    /**
     * This is the key value that stores the name of the template
     * other than the next, or cancel, where a user can go
     * depending on an action.
     */
    public static final String OTHER_TEMPLATE = "otherTemplate";
    
    
    /**
     * Primary System Object
     */
    public static final String SCARAB_REQUEST_TOOL = "scarabR";

    /**
     * The name used for the Intake tool
     */
    public static final String INTAKE_TOOL = "intake";

    /**
     * The name used for the Security tool
     */
    public static final String SECURITY_TOOL = "security";


    /**
     * Key passed around in the query string which tracks the
     * current module.
     */
    public static final String CURRENT_MODULE = "curmodule";
    public static final String CURRENT_ISSUE_TYPE = "curissuetype";
    public static final String CURRENT_ADMIN_MENU = "curadminmenu";
    public static final String REPORTING_ISSUE = "rissue";
    public static final String HISTORY_SCREEN = "oldscreen";
    public static final String NEW_MODULE = "newmodule";
    public static final String NEW_ISSUE_TYPE = "newissuetype";
    public static final String CURRENT_QUERY = "queryString";
    public static final String USER_SELECTED_MODULE = "scarab.user.selected.module";
    /** @deprecated No longer used */
    public static final String PROJECT_CHANGE_BOX = "project_change_box";

    /**
     * This name will be used to distinguish specific scarab application
     * from other instances that it may interact with (in the future).
     * It is the prefix to all issue id's created in response to an issue
     * entered against a module in this instance's database.
     */
    public static final String DOMAIN_NAME = "scarab.domain.name";

    /**
     *  This is maximum rating for a word.
     *
     */
    public static final int MAX_WORD_RATING = 100000;

    /**
     *  The list of issue id's resulting from a search.
     *
     */
    public static final String ISSUE_ID_LIST = "scarab.issueIdList";

    /**
     *  The message the user sees if they try to perform an action
     *  For which they have no permissions.
     *
     */
    public static final String NO_PERMISSION_MESSAGE = "You do not have " +
                               "permissions to perform this action.";

    /**
     * Name of the global module.
     */
    public static final String GLOBAL_MODULE_NAME = "Global";

    /**
     * The primary key of the assigned_to attribute.  We need this because
     * there is a special screen for the assigned to attribute.
     */
    NumberKey ASSIGNED_TO__PK = AttributePeer.ASSIGNED_TO__PK;
}    
