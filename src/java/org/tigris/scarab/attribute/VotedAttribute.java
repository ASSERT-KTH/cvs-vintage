package org.tigris.scarab.attribute;

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

import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeVote;
import org.tigris.scarab.om.AttributeVotePeer;
import org.tigris.scarab.om.AttributeValuePeer;

import org.apache.torque.util.Criteria;

import org.apache.torque.om.NumberKey;

import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author <a href="mailto:fedor.karpelevitch@home.com">Fedor</a>
 * @version $Revision: 1.16 $ $Date: 2001/10/20 00:51:05 $
 */
public abstract class VotedAttribute extends OptionAttribute
{
    private Hashtable votes;
    private String result;
    private boolean loaded;

    protected Hashtable getVotes()
    {
        return votes;
    }
    
    /** loads Internal Value from the database
     *
     */
    public void init() throws Exception
    {
        // this conditional removes an Exception, it should be
        // re-examined in more detail !!FIXME
        if ( getIssue().getPrimaryKey() != null) 
        {
            
            //note this code is old and has never been used
        int i;
        votes = new Hashtable();
        AttributeVote vote;
        Criteria crit = new Criteria();
        crit.add(AttributeVotePeer.VALUE_ID, getValueId());
        List res = AttributeVotePeer.doSelect(crit);
        for (i=0; i<res.size(); i++)
        {
            vote = (AttributeVote)res.get(i);
            votes.put(vote.getUserId().toString(), AttributeOption
                      .getInstance(vote.getOptionId()));
        }
        Criteria crit1 = new Criteria()
            .add(AttributeValuePeer.VALUE_ID, getValueId());
        if (AttributeValuePeer.doSelect(crit1).size()==1)
            loaded = true;
//        result = computeResult();

        }

    }

    /**
     *  This method calculates result of the vote
     *
    protected abstract String computeResult() throws Exception;
     */
    
    /** Updates both InternalValue and Value of the Attribute object and saves them
     * to database
     * @param newValue String representation of new value.
     * @param data app data. May be needed to get user info for votes and/or for security checks.
     * @throws Exception Generic exception
     *
     * /
    public void setValue(String newValue,RunData data) throws Exception
    {
        ObjectKey userId = ((ScarabUser)data.getUser()).getPrimaryKey();
        AttributeOption vote = getOptionById(newValue);
        Criteria crit = new Criteria();
        crit.add(AttributeVotePeer.ISSUE_ID, 
                 getScarabIssue().getPrimaryKey())
            .add(AttributeVotePeer.ATTRIBUTE_ID, 
                 getAttribute().getPrimaryKey())
            .add(AttributeVotePeer.USER_ID, 
                 ((TurbineUser)data.getUser()).getPrimaryKey());

        if (votes.containsKey(userId))
        {
            if (newValue == null)
            {
                // withdraw the vote
                AttributeVotePeer.doDelete(crit);
                votes.remove(userId);
            }
            else
            {
                //change the vote
                crit.add(AttributeVotePeer.OPTION_ID, 
                         vote.getPrimaryKey()); //FOIXME: is this correct?
                AttributeVotePeer.doUpdate(crit);
                votes.put(userId, vote);
            }
        }
        else
        {
            if (newValue == null)
            {
                //there was no vote and user tries to withdraw it. Do nothing or maybe throw?
                return;
            }
            else
            {
                //new vote
                crit.add(AttributeVotePeer.OPTION_ID, 
                         vote.getPrimaryKey());
                AttributeVotePeer.doInsert(crit);
                votes.put(userId, vote);
            }
        }
        
        result = computeResult();
        Criteria crit1 = new Criteria();
        crit1.add(AttributeValuePeer.ATTRIBUTE_ID, 
                  getAttribute().getPrimaryKey())
            .add(AttributeValuePeer.ISSUE_ID, 
                 getIssue().getPrimaryKey())
            .add(AttributeValuePeer.VALUE, result);
        if (loaded)
        {
            AttributeValuePeer.doUpdate(crit1);
        }
        else
        {
            AttributeValuePeer.doInsert(crit1);
            loaded = true;
        }
    }
    */

}

