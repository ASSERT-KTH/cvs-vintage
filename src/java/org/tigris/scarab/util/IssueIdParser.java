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

// JDK classes
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RE;

// Turbine classes
import org.apache.torque.TorqueException;

// Scarab classes
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.Issue;

/**
 * This class contains logic for finding issue ids in generic text.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: IssueIdParser.java,v 1.4 2003/01/24 19:10:15 jmcnally Exp $
 */
public class IssueIdParser
{
    private static REProgram idREProgram = null;
        
    static
    {
        try
        {
            RECompiler rec = new RECompiler();
            idREProgram = rec.compile("[:alpha:]*\\d+");
        }
        catch (Exception e)
        {
            Log.get().error("An npe is going to occur because I could not " +
                            "compile regex: [:alpha:]*\\d+", e);
        }
    }

    /**
     * Parses text for any valid issue ids and returns matches.
     * The regular expression to determine ids is given by the module
     */
    public static List getIssueIdTokens(Module module, String text)
        throws TorqueException
    {
        List result = new ArrayList();
        RE re = new RE(module.getIssueRegex());
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        int pos = 0;
        while (re.match(text, pos))
        {
            Log.get().debug(re.getParen(0) + " found at " + re.getParenStart(0));
            result.add(re.getParen(0));
            pos = re.getParenEnd(0);
        }

        return result;
    }

    /**
     * Parses text for any valid issue ids.  The text is broken up
     * into tokens at potential id boundaries.  if a token corresponds
     * to a valid issue id, a String[2] is returned with [0] as the
     * token and [1] is the id.  if a token does not contain an id
     * the text is added as a simple string.
     */
    public static List tokenizeText(Module module, String text)
        throws TorqueException
    {
        List result = new ArrayList();
        RE re = new RE(module.getIssueRegex());
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        int pos = 0;
        while (re.match(text, pos))
        {
            Log.get().debug(re.getParen(0) +" found at "+ re.getParenStart(0));
            // Add any text that did not contain an id
            if (re.getParenStart(0) > pos) 
            {
                result.add(text.substring(pos, re.getParenStart(0))); 
            }
                
            String token = re.getParen(0);
            String id = getIssueIdFromToken(module, token);
            if (id == null)
            {
                result.add(token);
            }
            else 
            {
                List tokenId = new ArrayList(2);
                tokenId.add(token);
                tokenId.add(id);
                result.add(tokenId);
            }
            
            pos = re.getParenEnd(0);
        }

        if (pos < text.length()) 
        {
            result.add(text.substring(pos));
        }
            
        return result;
    }

    /**
     * Assumption is the token will contain at most one issue id.
     * A number is a potential valid id within the given module.
     * A syntactically correct id will be checked against the db
     * to make sure a valid issue exists.  if the token does not
     * result in a valid issue, null is returned.  Otherwise the
     * id will be returned including the module code.
     *
     * @param module a <code>Module</code> value
     * @param token a <code>String</code> value
     * @return a <code>String</code> value
     */
    public static String getIssueIdFromToken(Module module, String token)
    {
        RE re = new RE(idREProgram);
        re.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        String id = null;
        if (re.match(token)) 
        {
            id = re.getParen(0);
            if (id.charAt(0) >= '0' && id.charAt(0) <= '9') 
            {
                id = module.getCode() + id;
            }
            Issue issue = Issue.getIssueById(id);
            if (issue == null || issue.getDeleted()) 
            {
                id = null;
            }
        }

        return id;
    }

    /**
     * A Map of ids where the keys are the tokens such as "issue#35" and the
     * value is the unique id, "PACS35".
     */
    public static Map getIssueIds(Module module, String text)
        throws TorqueException
    {
        List tokens = getIssueIdTokens(module, text);
        Map idMap = new HashMap(presizeMap(tokens.size()));
        Iterator i = tokens.iterator();
        while (i.hasNext()) 
        {
            String token = (String)i.next();
            String id = getIssueIdFromToken(module, token);
            if (id != null) 
            {
                idMap.put(token, id);
            }
        }
        return idMap;
    }

    private static int presizeMap(int size)
    {
        return (int) (1.25*size + 1.0);
    }
}
