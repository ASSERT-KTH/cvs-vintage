package org.tigris.scarab.screens.admin;

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

import java.util.Iterator;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

// Turbine Stuff 
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.ParameterParser;
import org.apache.torque.util.Criteria;

// Scarab Stuff
import org.tigris.scarab.screens.Default;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.om.ActivitySetPeer;

/**
 * Screen creates an iterator of ActivitySets for the ActivityList.vm template
 * with no parameters it returns all ActivitySets sorted in reverse 
 * creation date order.  Parameters can be used to limit results
 * sort=pk will sort in reverse pk order
 * frompk and topk can be used to specify integers to limit results
 * fromdate and todate can be entered in "yyyy-MM-dd HH:mm:ss" format
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: ActivityList.java,v 1.5 2003/05/03 22:37:24 jon Exp $
 */
public class ActivityList extends Default
{
    /**
     * builds up the context for display of variables on the page.
     */
    protected void doBuildTemplate(RunData data, TemplateContext context)
        throws Exception 
    {
        super.doBuildTemplate(data, context);
        ParameterParser pp = data.getParameters();
        String fromPk = pp.getString("frompk");
        String toPk = pp.getString("topk");
        String fromDateString = pp.getString("fromdate");
        String toDateString = pp.getString("todate");
        boolean isByPk = "pk".equals(pp.get("sort")) || (fromPk != null)
            || (toPk != null);
        // max=# will give the number of records, -1 will return all records
        //int max = pp.getInt("max", 100);

        Criteria crit = new Criteria();
        Criteria.Criterion c = null;
        if (isByPk) 
        {
            crit.addAscendingOrderByColumn(ActivitySetPeer.TRANSACTION_ID);
            if (fromPk != null) 
            {
                c = crit.getNewCriterion(ActivitySetPeer.TRANSACTION_ID, 
                                         fromPk, Criteria.GREATER_EQUAL);
            }
            if (toPk != null) 
            {
                if (c == null) 
                {
                    c = crit.getNewCriterion(ActivitySetPeer.TRANSACTION_ID, 
                                             toPk, Criteria.LESS_EQUAL);
                }
                else 
                {
                    c.and(crit.getNewCriterion(ActivitySetPeer.TRANSACTION_ID, 
                                               toPk, Criteria.LESS_EQUAL));
                    
                }
            }
        }
        else 
        {
            crit.addAscendingOrderByColumn(ActivitySetPeer.CREATED_DATE);

            if (fromDateString != null) 
            {
                c = crit.getNewCriterion(ActivitySetPeer.CREATED_DATE, 
                                         parseDate(fromDateString), 
                                         Criteria.GREATER_EQUAL);
            }

            if (toDateString != null) 
            {
                if (c == null) 
                {
                    c = crit.getNewCriterion(ActivitySetPeer.CREATED_DATE, 
                                             parseDate(toDateString), 
                                             Criteria.LESS_EQUAL);
                }
                else 
                {
                    c.and(crit.getNewCriterion(ActivitySetPeer.CREATED_DATE, 
                                               parseDate(toDateString), 
                                               Criteria.LESS_EQUAL));
                }
            }
        }
        if (c != null) 
        {
            crit.add(c);
        }
        
        final List sets = ActivitySetPeer.doSelect(crit);
        // the following iterator starts at the end of the list and
        // iterates toward the beginning, removing items from the list
        // as it goes.  This helps with memory usage.  The original list
        // size can still be too large, but if it is not, processing by
        // the template should not add additional memory burden.
        Iterator setIterator = new Iterator()
            {
                public boolean hasNext()
                {
                    return !sets.isEmpty();
                }

                public Object next()
                {
                    ScarabCache.clear();
                    return sets.remove(sets.size()-1);
                }

                public void remove()
                {
                    //not implemented
                }
            };
        context.put("activitySets", setIterator);
    }

    private Date parseDate(String s)
        throws ParseException
    {
        SimpleDateFormat[] sdf = {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm"),
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("HH:mm:ss"),
            new SimpleDateFormat("HH:mm")
                };

        Date date = null;
        for (int i=0; i<sdf.length-1; i++) 
        {
            try 
            {
                date = sdf[i].parse(s);
            }
            catch (ParseException e)
            {
                // try the next one
            }
        }

        if (date == null) 
        {
            throw new ParseException(s + " could not be parsed as a date", 0);
        }
        return date;
    }
}
