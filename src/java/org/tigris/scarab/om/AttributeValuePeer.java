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

import java.util.Map;
import java.util.HashMap;

// Village
import com.workingdogs.village.Record;

// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import com.workingdogs.village.Record;
import com.workingdogs.village.DataSetException;

/** 
 * The Peer class for an AttributeValue
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: AttributeValuePeer.java,v 1.14 2003/02/14 18:28:20 jon Exp $
 */
public class AttributeValuePeer 
    extends BaseAttributeValuePeer
{
    private static Map classMap = new HashMap();

    private static final String COUNT = 
        "count(" + VALUE_ID + ')';
    private static final String COUNT_DISTINCT = 
        "count(DISTINCT " + VALUE_ID + ')';

    /**
     * Adds count(AttributeValuePeer.VALUE_ID) to the select clause and returns the 
     * number of rows resulting from the given Criteria.  If the criteria will
     * lead to duplicate VALUE_ID's they will be counted.  However, if the
     * criteria is known not to lead to this, or unique VALUE_ID's are not
     * required, this method is cheaper than {@link #countDistinct(Criteria)}
     *
     * @param crit a <code>Criteria</code> value
     * @return an <code>int</code> value
     * @exception TorqueException if an error occurs
     * @exception DataSetException if an error occurs
     */
    public static int count(Criteria crit)
        throws TorqueException, DataSetException
    {
        crit.addSelectColumn(COUNT);
        return ((Record)AttributeValuePeer.doSelectVillageRecords(crit).get(0))
            .getValue(1).asInt();
    }

    /**
     * Adds count(DISTINCT AttributeValuePeer.VALUE_ID) to the select clause and returns
     * the number of rows resulting from the given Criteria.  The returned
     * value will be the number of unique VALUE_ID's.
     *
     * @param crit a <code>Criteria</code> value
     * @return an <code>int</code> value
     * @exception TorqueException if an error occurs
     * @exception DataSetException if an error occurs
     */
    public static int countDistinct(Criteria crit)
        throws TorqueException, DataSetException
    {
        crit.addSelectColumn(COUNT_DISTINCT);
        return ((Record)AttributeValuePeer.doSelectVillageRecords(crit).get(0))
            .getValue(1).asInt();
    }


    /** 
     * Get the className appropriate for a row in the 
     * SCARAB_ISSUE_ATTRIBUTE_VALUE table
     */
    public static Class getOMClass(Record record, int offset) 
        throws TorqueException
    {
        Class c = null;
        try
        {
            NumberKey attId = new NumberKey(record.getValue(offset-1 + 3)
                                            .asString());
            Attribute attribute = AttributeManager.getInstance(attId);
            String className = attribute.getAttributeType().getJavaClassName();

            c = (Class)classMap.get(className);
            if (c == null) 
            {
                c = Class.forName(className);
                classMap.put(className, c);            
            }
        }
        catch (Exception e)
        {
            throw new TorqueException(e);
        }
        
        return c;
    }
}

