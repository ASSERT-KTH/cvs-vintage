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
 * @version $Id: ScarabUtil.java,v 1.2 2003/02/07 03:35:48 jon Exp $
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

    /**
     * URL encodes <code>in</code> and writes it to <code>out</code>. If the
     * string is null, 'null' will be written. Code 'borrowed' from DynamicURI.java
     * in the Jakarta Turbine 3 package. We use this code instead of java.net.Encoder
     * because Encoder.encode is deprecated and we don't feel like putting a dependency
     * on JDK 1.4.1. This should work fine for our purposes.
     *
     * @param in String to write.
     * @param out Buffer to write to. Null if nothing in in.
     */
    public static final String urlEncode(String in)
    {
        if (in == null || in.length() == 0)
        {
            return null;
        }

        StringBuffer out = new StringBuffer(in.length()+128);
        // This is the most expensive operation:
        byte[] bytes = in.getBytes();

        for (int i = 0; i < bytes.length; i++)
        {
            char c = (char) bytes[i];

            if ( c < 128 && safe[ c ] )
            {
                out.append(c);
            }
            else if (c == ' ')
            {
                out.append('+');
            }
            else
            {
                byte toEscape = bytes[i];
                out.append('%');
                int low = (int) (toEscape & 0x0f);
                int high = (int) ((toEscape & 0xf0) >> 4);
                out.append(hexadecimal[high]);
                out.append(hexadecimal[low]);
            }
        }
        return out.toString();
    }

    // ------------------------------------- private constants for url encoding

    /**
     * Array mapping hexadecimal values to the corresponding ASCII characters.
     */
    private static final char[] hexadecimal =
        {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
        };

    /**
     * Characters that need not be encoded. This is much faster than using a
     * BitSet, and for such a small array the space cost seems justified.
     */
    private static boolean[] safe = new boolean[ 128 ];

    /** Static initializer for {@link #safe} */
    static
    {
        for (int i = 'a'; i <= 'z'; i++)
        {
            safe[ i ] = true;
        }
        for (int i = 'A'; i <= 'Z'; i++)
        {
            safe[ i ] = true;
        }
        for (int i = '0'; i <= '9'; i++)
        {
            safe[ i ] = true;
        }

        safe['-'] = true;
        safe['_'] = true;
        safe['.'] = true;
        safe['!'] = true;
        safe['~'] = true;
        safe['*'] = true;
        safe['\''] = true;
        safe['('] = true;
        safe[')'] = true;
    }

}
