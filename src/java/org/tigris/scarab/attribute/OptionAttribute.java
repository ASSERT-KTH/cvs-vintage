package org.tigris.scarab.attribute;

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

import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.RModuleOption;

/**
 * this is a superclass for attributes which use option lists (SelectOne & Voted)
 *
 * @author <a href="mailto:fedor.karpelevitch@home.com">Fedor</a>
 * @version $Revision: 1.17 $ $Date: 2002/01/18 22:26:06 $
 */
public abstract class OptionAttribute extends AttributeValue
{
    public void setOption(RModuleOption option)
        throws Exception
    {
        setOptionIdOnly(option.getOptionId());
        setValueOnly(option.getDisplayValue());
    }

    public Object loadResources() throws Exception
    {
        return null;
    }

    /** this method is used by an Attribute instance
     * to obtain specific resources such as option list for SelectOneAttribute.
     * It may, for example put them into instance variables.
     * Attribute may use common resources as-is or create it's own
     * resources based on common, it should not, however, modify common resources
     * since they will be used by other Attribute instances.
     *
     * @param resources Resources common for Attributes with the specified id.
     */
    public void setResources(Object resources)
    {
    }
}
