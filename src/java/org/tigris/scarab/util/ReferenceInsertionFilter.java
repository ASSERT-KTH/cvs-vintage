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

import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.app.event.NullSetEventHandler;

import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.screens.SelectModule;

/**
 * This is a Velocity EventCartridge Filter which is responsible
 * for processing $ variables when they are rendered in a template.
 * The current purpose of this filter is to process out CSS 
 * (cross site scripting) vulnerabilities. There is some commented 
 * out code that adds a bit of timing information to make sure that
 * the processing doesn't add to much overhead. In limited testing,
 * it looks like this class only adds about 0-2ms of processing time to
 * each request.
 *
 * <p>
 * This class also implements the NullSetEventHandler and returns
 * false from the shouldLogOnNullSet because we don't need that stuff
 * showing up in the log files.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ReferenceInsertionFilter.java,v 1.16 2003/02/04 11:26:03 jon Exp $
 */
public class ReferenceInsertionFilter
    implements ReferenceInsertionEventHandler, NullSetEventHandler
{


    public boolean shouldLogOnNullSet(String lhs, String rhs)
    {
        return false;
    }

    public Object referenceInsert(String reference, Object value)
    {
        // if value is null, we don't want to filter it of course!
        if (value == null)
        {
            return null;
        }
        
//    System.out.println ("reference: '" + reference + 
//                        "' type: '" + value.getClass().getName() + "'");

//        long start = System.currentTimeMillis();
        Object result = value;
        if (value instanceof String)
        {
            if (
                // don't filter renderer because it will get filtered
                // when the actual rendering is done.
                !reference.startsWith("$renderer") && 
                // don't want to filter this because it outputs HTML
                !reference.startsWith("$intake.declare") &&
                // localization tool pre-filters data
                !reference.startsWith("$l10n")
              )
            {
                // we are already a String
                result = filter((String)value);
            }
        }
        // don't filter links and some other known to be safe elements
        else if (!(value instanceof SkipFiltering))
        {
            // We convert the object to a string and output the result
            result = filter(value.toString());
        }
/*        
        long stop = System.currentTimeMillis();
        System.out.println ("start: " + start);
        System.out.println ("stop: " + stop);        
        long time = stop - start;
        System.out.println ("reference: '" + reference + 
                            "': " + time);
*/
        return result;
    }

    /**
     * This method is borrowed from Struts. It converts
     * &lt; &gt; &amp; &quot; into the appropriate entities.
     */
    public static String filter(String value)
    {
        if (value == null)
        {
            return (null);
        }
        char content[] = new char[value.length()];
        value.getChars(0, value.length(), content, 0);
        StringBuffer result = new StringBuffer(content.length + 50);
        for (int i = 0; i < content.length; i++)
        {
            switch (content[i])
            {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    if (i+1 < content.length && content[i+1] == '#') 
                    {
                        result.append('&');
                    }
                    else 
                    {
                        result.append("&amp;");
                    }
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                default:
                    result.append(content[i]);
            }
        }
        return (result.toString());
    }
}
