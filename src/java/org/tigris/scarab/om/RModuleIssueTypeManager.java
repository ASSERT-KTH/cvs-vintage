package org.tigris.scarab.om;

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
import java.util.HashMap;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.SimpleKey;
import org.apache.torque.om.ComboKey;

/** 
 * This class manages RModuleIssueType objects.  
 *
 * @version $Id: RModuleIssueTypeManager.java,v 1.8 2003/03/25 16:57:53 jmcnally Exp $
 */
public class RModuleIssueTypeManager
    extends BaseRModuleIssueTypeManager
{
    /**
     * Creates a new <code>RModuleIssueTypeManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public RModuleIssueTypeManager()
        throws TorqueException
    {
        super();
        setRegion(getClassName().replace('.', '_'));
        validFields = new HashMap();
        validFields.put(RModuleIssueTypePeer.MODULE_ID, null);
    }

    protected Persistent putInstanceImpl(Persistent om)
        throws TorqueException
    {
        Persistent oldOm = super.putInstanceImpl(om);
        List listeners = (List)listenersMap
            .get(RModuleIssueTypePeer.MODULE_ID);
        notifyListeners(listeners, oldOm, om);
        return oldOm;
    }

    public static void removeFromCache(RModuleIssueType module)
        throws TorqueException
    {
        ObjectKey key = module.getPrimaryKey();
        getManager().removeInstanceImpl(key);
    }

    public static RModuleIssueType getInstance(String key)
        throws TorqueException
    {
        if (key == null) 
        {
            throw new NullPointerException(
                "Cannot request a RModuleIssueType using a null key.");
        }
        int colonPos = key.indexOf(':');
        if (colonPos == -1) 
        {
            throw new IllegalArgumentException(
                "RModuleIssueType keys must be of the form 1:2, not " + key);
        }
        NumberKey moduleId = new NumberKey(key.substring(1, colonPos));
        NumberKey itId = new NumberKey(key.substring(colonPos+2, key.length()-1));
        SimpleKey[] keyArray = { moduleId, itId };
        return getInstance(new ComboKey(keyArray));
    }
}
