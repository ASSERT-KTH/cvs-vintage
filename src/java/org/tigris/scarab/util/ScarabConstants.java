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

import java.util.Locale;
import org.apache.fulcrum.localization.Localization;

/**
 * A place to put public final static strings and other constants.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ScarabConstants.java,v 1.56 2003/04/25 20:01:20 jon Exp $
 */
public interface ScarabConstants
{
    /** 
     * the registration code uses this in order to store an object
     * into the data.getUser().getTemp() hashtable. this is the key 
     * value and is used across several classes.
     */
    String SESSION_REGISTER = "scarab.newUser";

    /**
     * This is the key value that stores the name of the template to 
     * execute next.
     */
    String NEXT_TEMPLATE = "nextTemplate";

    /**
     * This is the key value that stores the name of the template to 
     * cancel to.
     */
    String CANCEL_TEMPLATE = "cancelTemplate";

    /**
     * This is the key value that stores the name of the template to 
     * go back to to (used in wizards)
     */
    String BACK_TEMPLATE = "backTemplate";

    /**
     * This is the key value that stores the name of the template to 
     * go back to to (used in wizards)
     */
    String LAST_TEMPLATE = "lastTemplate";

    /**
     * This is the key value that stores the name of the template
     * that is currently being executed.
     */
    String TEMPLATE = "template";

    /**
     * This is the key value that stores the name of the template
     * other than the next, or cancel, where a user can go
     * depending on an action.
     */
    String OTHER_TEMPLATE = "otherTemplate";
    
    
    /**
     * Primary System Object
     */
    String SCARAB_REQUEST_TOOL = "scarabR";

    /**
     * Collection of useful methods
     */
    String SCARAB_GLOBAL_TOOL = "scarabG";

    /**
     * The name used for the Intake tool
     */
    String INTAKE_TOOL = "intake";

    /**
     * The name used for the Security tool
     */
    String SECURITY_TOOL = "security";

    /**
     * The name used for the Security Admin tool
     */
    String SECURITY_ADMIN_TOOL = "securityAdmin";

    /**
     * The name used for the Localization tool
     */
    String LOCALIZATION_TOOL = "l10n";

    /**
     * Key passed around in the query string which tracks the
     * current module.
     */
    String DEBUG = "debug";
    String CURRENT_MODULE = "curmodule";
    String CURRENT_ISSUE_TYPE = "curit";
    String CURRENT_ADMIN_MENU = "curadminmenu";
    String REPORTING_ISSUE = "rissue";
    String CURRENT_REPORT = "curreport";
    String REMOVE_CURRENT_REPORT = "remcurreport";
    String HISTORY_SCREEN = "oldscreen";
    String NEW_MODULE = "newmodule";
    String NEW_ISSUE_TYPE = "newissuetype";
    String CURRENT_QUERY = "queryString";
    String CURRENT_MITLIST_ID = "curmitlistid";
    String CURRENT_MITLISTITEM = "curmitlistitem";
    String USER_SELECTED_MODULE = "scarab.user.selected.module";
    /** @deprecated No longer used */
    String PROJECT_CHANGE_BOX = "project_change_box";

    String THREAD_QUERY_KEY = "tqk";
    String REMOVE_CURRENT_MITLIST_QKEY = "remcurmitl";

    /**
     * This name will be used to distinguish specific scarab application
     * from other instances that it may interact with (in the future).
     * It is the prefix to all issue id's created in response to an issue
     * entered against a module in this instance's database.
     */
    String DOMAIN_NAME = "scarab.domain.name";

    /**
     *  This is maximum rating for a word.
     *
     */
    int MAX_WORD_RATING = 100000;

    /**
     *  The list of issue id's resulting from a search.
     *
     */
    String ISSUE_ID_LIST = "scarab.issueIdList";

    /**
     *  The message the user sees if they try to perform an action
     *  For which they have no permissions.
     *
     */
    String NO_PERMISSION_MESSAGE = "YouDoNotHavePermissionToAction";

    String ATTACHMENTS_REPO_KEY = "scarab.attachments.repository";

    String ARCHIVE_EMAIL_ADDRESS = "scarab.email.archive.toAddress";

    /**
     * An attribute type
     */
    String DROPDOWN_LIST = "Dropdown list";

    /**
     * Scarab.properties key for roles to be automatically approved.
     */
    String AUTO_APPROVED_ROLES = "scarab.automatic.role.approval";

    /**
     * key used to store session preference for long issue view vs. tabs
     * used in get/setTemp within ScarabUser.
     */
    String TAB_KEY = "scarab.view.issue.details";

    /**
     * Value of the session parameter to view the issue in long form.
     */
    String ISSUE_VIEW_ALL = "all";

    /** 
     * list of invalid characters when doing searches
     */
    String INVALID_SEARCH_CHARACTERS = "\"\t(){}[]!,;:?./*-+=+&|<>\\~^";

    /**
     * format for displaying dates
     */
    String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z";

    /**
     * The default base for resolving ResourceBundles.
     */
    String DEFAULT_BUNDLE_NAME = "ScarabBundle";

    /**
     * Default locale, taken from configuration files.
     */
    Locale DEFAULT_LOCALE =  new Locale(Localization.getDefaultLanguage(), 
                                        Localization.getDefaultCountry());

    Integer INTEGER_0 = new Integer(0);

}    
