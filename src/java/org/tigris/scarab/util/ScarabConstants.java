package org.tigris.scarab.util;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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


/**
    A place to put public final static strings.
    
    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: ScarabConstants.java,v 1.12 2001/07/17 21:40:31 jon Exp $
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
     * This is the key value that stores the name of the template
     * that is currently being executed.
     */
    public static final String TEMPLATE = "template";
    
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
     * Not really used. May be removed in the future.
     */
    public static final String CURRENT_PROJECT = "cur_project_id";
    public static final String USER_SELECTED_MODULE = "scarab.user.selected.module";
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
}    

