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

import java.util.List;
import java.util.Iterator;

import org.tigris.scarab.om.Module;

import org.apache.oro.text.perl.Perl5Util;

import org.tigris.scarab.util.IssueIdParser;

/**
 * A Utility class for code that doesn't really go other places.
 *   
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @version $Id: ScarabUtil.java,v 1.1 2003/01/31 19:33:38 jon Exp $
 */
public class ScarabUtil
{
    private static final String REGEX_URL =
        "s%\\b(?:[hH][tT]{2}[pP]|[fF][tT][pP]):[^ \\t\\n<>\"]+[\\w/]*%<a href=\"$0\">$0</a>%g";
    private static final String REGEX_MAILTO =
        "s%\\b(?:([mM][aA][iI][lL][tT][oO])):([^ \\t\\n<>\"]+[\\w/])*%<a href=\"$0\">$2</a>%g";
    private static final String REGEX_NEWLINETOBR =
        "s%\\n%<br />%g";

    private static Perl5Util perlUtil = new Perl5Util();

    /**
     * First, it converts all HTML markup into entities.
     * Then it the Jakarta ORO package to convert http:// ftp:// mailto: 
     * links into URL's.
     * Lastly, it uses the IssueIdParser to convert all issue id's into links.
     */
    public static String linkifyText(String input, ScarabLink link, Module module)
        throws Exception
    {
        StringBuffer sb = new StringBuffer(input.length() * 2);
        // first get rid of any HTML crap
        String output = ReferenceInsertionFilter.filter(input);
        output = perlUtil.substitute(REGEX_NEWLINETOBR,output);
        output = perlUtil.substitute(REGEX_MAILTO,output);
        output = perlUtil.substitute(REGEX_URL,output);
        List result = IssueIdParser.tokenizeText(module, output);
        for (Iterator itr = result.iterator(); itr.hasNext();)
        {
            Object tmp = itr.next();
            if (tmp instanceof String)
            {
                sb.append(tmp);
            }
            else
            {
                List tmpList = (List)tmp;
                link.addPathInfo("id", (String)tmpList.get(1));
                link.setLabel((String)tmpList.get(0));
                String bar = link.setAlternateText((String)tmpList.get(0)).toString();
                sb.append(bar);
            }
        }
        return sb.toString();
    }
}
