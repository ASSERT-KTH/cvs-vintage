package org.tigris.scarab.util.word;

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

// JDK classes
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// Turbine classes
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.commons.util.SequencedHashtable;
import org.apache.commons.util.StringUtils;

// Scarab classes
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.ROptionOptionPeer;
import org.tigris.scarab.om.AttributeValuePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.AttributePeer;

import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.attribute.StringAttribute;


/** 
 * A utility class to build up and carry out a search for 
 * similar issues.  It subclasses Issue for functionality, it is 
 * not a more specific type of Issue.
 */
public class IssueSearch 
    extends Issue
{
    private static final NumberKey ALL_TEXT = new NumberKey("0");

    private static final String PARENT_ID;
    private static final String CHILD_ID;
    private static final String AV_ISSUE_ID;
    private static final String AV_OPTION_ID;

    private String searchWords;
    private NumberKey[] textScope;
    private String minId;
    private String maxId;
    private String minDate;
    private String maxDate;
    private int minVotes;
    
    private int resultsPerPage;

    static 
    {
        PARENT_ID = ROptionOptionPeer.OPTION1_ID;
        CHILD_ID = ROptionOptionPeer.OPTION2_ID;

        // column names only
        AV_OPTION_ID = AttributeValuePeer.OPTION_ID.substring(
            AttributeValuePeer.OPTION_ID.indexOf('.')+1);
        AV_ISSUE_ID = AttributeValuePeer.ISSUE_ID.substring(
            AttributeValuePeer.ISSUE_ID.indexOf('.')+1);

    }

    /**
     * Get the value of searchWords.
     * @return value of searchWords.
     */
    public String getSearchWords() 
    {
        return searchWords;
    }
    
    /**
     * Set the value of searchWords.
     * @param v  Value to assign to searchWords.
     */
    public void setSearchWords(String  v) 
    {
        this.searchWords = v;
    }

    /**
     * Get the value of textScope.  if the scope is not set then all
     * text attributes are returned.  if there are no relevant text
     * attributes null will be returned.
     * @return value of textScope.
     */
    public NumberKey[] getTextScope()
        throws Exception
    {
        if ( textScope == null ) 
        {
            setTextScopeToAll();
        }
        else
        {
            for ( int i=textScope.length-1; i>=0; i-- ) 
            {
                if ( textScope[i].equals(ALL_TEXT) ) 
                {
                    setTextScopeToAll();
                    break;
                }       
            }
        }
        return textScope;
    }


    /**
     * Sets the text search scope to all quick search text attributes.
     */
    private void setTextScopeToAll()
        throws Exception
    {
        List textAttributes = getQuickSearchTextAttributeValues();
        if ( textAttributes != null ) 
        {
            textScope = new NumberKey[textAttributes.size()];
            for ( int j=textAttributes.size()-1; j>=0; j-- ) 
            {
                textScope[j] = ((Attribute)
                                textAttributes.get(j)).getAttributeId();
            }
        }
    }

    /**
     * Set the value of textScope.
     * @param v  Value to assign to textScope.
     */
    public void setTextScope(NumberKey[]  v) 
    {
        this.textScope = v;
    }


    /**
     * Get the value of minId.
     * @return value of minId.
     */
    public String getMinId() 
    {
        return minId;
    }
    
    /**
     * Set the value of minId.
     * @param v  Value to assign to minId.
     */
    public void setMinId(String  v) 
    {
        this.minId = v;
    }

    
    /**
     * Get the value of maxId.
     * @return value of maxId.
     */
    public String getMaxId() 
    {
        return maxId;
    }
    
    /**
     * Set the value of maxId.
     * @param v  Value to assign to maxId.
     */
    public void setMaxId(String  v) 
    {
        this.maxId = v;
    }
    
    
    /**
     * Get the value of minDate.
     * @return value of minDate.
     */
    public String getMinDate() 
    {
        return minDate;
    }
    
    /**
     * Set the value of minDate.
     * @param v  Value to assign to minDate.
     */
    public void setMinDate(String  v) 
    {
        this.minDate = v;
    }

    
    /**
     * Get the value of maxDate.
     * @return value of maxDate.
     */
    public String getMaxDate() 
    {
        return maxDate;
    }
    
    /**
     * Set the value of maxDate.
     * @param v  Value to assign to maxDate.
     */
    public void setMaxDate(String  v) 
    {
        this.maxDate = v;
    }
    
    /**
     * Get the value of minVotes.
     * @return value of minVotes.
     */
    public int getMinVotes() 
    {
        return minVotes;
    }
    
    /**
     * Set the value of minVotes.
     * @param v  Value to assign to minVotes.
     */
    public void setMinVotes(int  v) 
    {
        this.minVotes = v;
    }
    

    /**
     * Get the value of resultsPerPage.
     * @return value of resultsPerPage.
     */
    public int getResultsPerPage() 
    {
        return resultsPerPage;
    }
    
    /**
     * Set the value of resultsPerPage.
     * @param v  Value to assign to resultsPerPage.
     */
    public void setResultsPerPage(int  v) 
    {
        this.resultsPerPage = v;
    }


    public NumberKey getALL_TEXT()
    {
        return ALL_TEXT;
    }

    public List getQuickSearchTextAttributeValues()
        throws Exception
    {
        return getTextAttributeValues(true);
    }

    public List getTextAttributeValues()
        throws Exception
    {
        return getTextAttributeValues(false);
    }

    private List getTextAttributeValues(boolean quickSearchOnly)
        throws Exception
    {
        SequencedHashtable searchValues = getModuleAttributeValuesMap();
        List searchAttributes = new ArrayList(searchValues.size());

        for ( int i=0; i<searchValues.size(); i++ ) 
        {
            AttributeValue searchValue = 
                (AttributeValue)searchValues.getValue(i);
            if ( (!quickSearchOnly || searchValue.isQuickSearchAttribute())
                 && searchValue instanceof StringAttribute ) 
            {
                searchAttributes.add(searchValue.getAttribute());
            }
        }

        return searchAttributes;
    }

    /**
     * Returns OptionAttributes which have been marked for Quick search.
     *
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getQuickSearchOptionAttributeValues()
        throws Exception
    {
        return getOptionAttributeValues(true);
    }

    /**
     * Returns OptionAttributes which have been marked for Quick search.
     *
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getOptionAttributeValues()
        throws Exception
    {
        return getOptionAttributeValues(false);
    }


    /**
     * Returns OptionAttributes which have been marked for Quick search.
     *
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    private List getOptionAttributeValues(boolean quickSearchOnly)
        throws Exception
    {
        SequencedHashtable searchValues = getModuleAttributeValuesMap();
        List searchAttributeValues = new ArrayList(searchValues.size());

        for ( int i=0; i<searchValues.size(); i++ ) 
        {
            AttributeValue searchValue = 
                (AttributeValue)searchValues.getValue(i);
            if ( (!quickSearchOnly || searchValue.isQuickSearchAttribute())
                 && searchValue instanceof OptionAttribute ) 
            {
                searchAttributeValues.add(searchValue);
            }
        }

        return searchAttributeValues;
    }


    /**
     * remove unset AttributeValues.
     *
     * @param attValues a <code>List</code> value
     */
    private void removeUnsetValues(List attValues)
    {
        for ( int i=attValues.size()-1; i>=0; i-- ) 
        {
            AttributeValue attVal = (AttributeValue) attValues.get(i);
            if ( attVal.getOptionId() == null && attVal.getValue() == null
                 && attVal.getUserId() == null ) 
            {
                attValues.remove(i);
            }
        }
    }

    private void addMinimumVotes(Criteria crit)
        throws ScarabException
    {
        if ( minVotes > 0 ) 
        {
            crit.addJoin(AttributeValuePeer.ISSUE_ID, IssuePeer.ISSUE_ID)
                .add(AttributeValuePeer.ATTRIBUTE_ID, 
                     AttributePeer.TOTAL_VOTES__PK)
                .add(AttributeValuePeer.NUMERIC_VALUE, minVotes,
                     Criteria.GREATER_EQUAL);
        }
    }

    private void addIssueIdRange(Criteria crit)
        throws ScarabException, Exception
    {
        System.out.println("minId=" + minId + " maxId=" + maxId);
        // check limits to see which ones are present
        // if neither are present, do nothing
        if ( (minId != null && minId.length() != 0)
              || (maxId != null && maxId.length() != 0) ) 
        {
            Issue.FederatedId minFid = null;
            Issue.FederatedId maxFid = null;
            if ( minId == null || minId.length() == 0 ) 
            {
                maxFid = new Issue.FederatedId(maxId);
                //minFid = new Issue.FederatedId(maxFid.getDomainId(),
                //                               maxFid.getPrefix(), 1);
                crit.add(IssuePeer.ID_DOMAIN, maxFid.getDomain());
                crit.add(IssuePeer.ID_PREFIX, maxFid.getPrefix());
                crit.add(IssuePeer.ID_COUNT, maxFid.getCount(), 
                         Criteria.LESS_EQUAL);
            }
            else if ( maxId == null || maxId.length() == 0 ) 
            {
                minFid = new Issue.FederatedId(minId);
                crit.add(IssuePeer.ID_DOMAIN, minFid.getDomain());
                crit.add(IssuePeer.ID_PREFIX, minFid.getPrefix());
                crit.add(IssuePeer.ID_COUNT, minFid.getCount(), 
                         Criteria.GREATER_EQUAL);

            }
            else 
            {
                minFid = new Issue.FederatedId(minId);
                maxFid = new Issue.FederatedId(maxId);
                // give reasonable defaults if module code was not specified
                if ( minFid.getPrefix() == null ) 
                {
                    minFid.setPrefix(getScarabModule().getCode());
                }
                if ( maxFid.getPrefix() == null ) 
                {
                    maxFid.setPrefix(minFid.getPrefix());
                }
                
                // make sure min id is less than max id and that the character
                // parts are equal otherwise skip the query, there are no 
                // matches
                if ( minFid.getCount() <= maxFid.getCount() 
                     && minFid.getPrefix().equals(maxFid.getPrefix())
                     && StringUtils
                     .equals( minFid.getDomain(), maxFid.getDomain() ))
                {
                    Criteria.Criterion c1 = crit.getNewCriterion(
                        IssuePeer.ID_COUNT, new Integer(minFid.getCount()), 
                        Criteria.GREATER_EQUAL);
                    c1.and(crit.getNewCriterion(
                        IssuePeer.ID_COUNT, new Integer(maxFid.getCount()), 
                        Criteria.LESS_EQUAL) );
                    crit.add(c1);
                    crit.add(IssuePeer.ID_DOMAIN, minFid.getDomain());
                    crit.add(IssuePeer.ID_PREFIX, minFid.getPrefix());
                }
                else 
                {
                    throw new ScarabException("Incompatible issue Ids: " +
                                              minId + " and " + maxId);
                }
            }
        }
    }


    private void addDateRange(Criteria crit)
        throws ScarabException
    {
        // DateFormat dateFormatter = DateFormat.getDateInstance();
        DateFormat dateTimeFormatter = new SimpleDateFormat("MM/dd/yy HH:mm");
        DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy");
        Date minUtilDate = null;
        Date maxUtilDate = null;
        if ( minDate != null ) 
        {
            try
            {
                minUtilDate = dateFormatter.parse(minDate);
            }
            catch (Exception e)
            {
                try
                {
                    minUtilDate = dateTimeFormatter.parse(minDate);
                }
                catch (Exception ee)
                {
                    // ignore
                    ee.printStackTrace();
                }
            }
        }

        if ( maxDate != null )
        {         
            try
            {
                maxUtilDate = dateFormatter.parse(maxDate);
                // add 24 hours to max date so it is inclusive
                maxUtilDate.setTime(maxUtilDate.getTime() + 86400000);
            }
            catch (Exception e)
            {
                try
                {
                    maxUtilDate = dateTimeFormatter.parse(maxDate);
                }
                catch (Exception ee)
                {
                    // ignore
                    ee.printStackTrace();
                }
            }
        }

        // check limits to see which ones are present
        // if neither are present, do nothing
        if ( minUtilDate != null || maxUtilDate != null ) 
        {
            if ( minUtilDate == null ) 
            {
                crit.add(IssuePeer.CREATED_DATE, maxUtilDate,
                         Criteria.LESS_THAN);
            }
            else if ( maxUtilDate == null ) 
            {
                crit.add(IssuePeer.CREATED_DATE, minUtilDate,
                         Criteria.GREATER_EQUAL);
            }
            else 
            {
                // make sure min id is less than max id and that the character
                // parts are equal otherwise skip the query, there are no 
                // matches
                if ( minUtilDate.before(maxUtilDate) )
                {
                    Criteria.Criterion c1 = crit.getNewCriterion(
                        IssuePeer.CREATED_DATE, minUtilDate, 
                        Criteria.GREATER_EQUAL);
                    c1.and(crit.getNewCriterion(
                        IssuePeer.CREATED_DATE, maxUtilDate, 
                        Criteria.LESS_EQUAL) );
                    crit.add(c1);
                }
                else 
                {
                    throw new ScarabException("maxDate " + maxDate + 
                        "is before minDate " + minDate);
                }
            }
        }
    }


    /**
     * Returns a List of matching issues.  if no OptionAttributes were
     * found in the input list null is returned.
     *
     * @param attValues a <code>List</code> value
     */
    private void addOptionAttributes(Criteria crit, List attValues)
        throws Exception
    {
        Criteria.Criterion c = null;
        boolean atLeastOne = false;
        for ( int i=0; i<attValues.size(); i++ ) 
        {
            AttributeValue aval = (AttributeValue)attValues.get(i);
            if ( aval instanceof OptionAttribute ) 
            {
                atLeastOne = true;
                Criteria.Criterion c1 = crit.getNewCriterion("av" + i,
                    AV_ISSUE_ID, "av" + i + "." + AV_ISSUE_ID + "=" + 
                    IssuePeer.ISSUE_ID, Criteria.CUSTOM); 
                crit.addAlias("av" + i, AttributeValuePeer.TABLE_NAME);
                List descendants = aval.getAttributeOption().getDescendants();
                if ( descendants.size() == 0 ) 
                {
                    c1.and(crit.getNewCriterion( "av" + i, AV_OPTION_ID,
                        aval.getOptionId(), Criteria.EQUAL));
                }
                else
                { 
                    NumberKey[] ids = new NumberKey[descendants.size()];
                    for ( int j=ids.length-1; j>=0; j-- ) 
                    {
                        ids[j] = ((AttributeOption)descendants.get(j))
                            .getOptionId();
                    }
                    c1.and(crit.getNewCriterion( "av" + i, AV_OPTION_ID,
                        ids, Criteria.IN));
                }                
                if ( c == null ) 
                {
                    c = c1;
                }
                else 
                {
                    c.and(c1);
                }
            }
        }

        if ( atLeastOne ) 
        {
            crit.add(c);            
        }
    }

    private NumberKey[] addTextMatches(Criteria crit, List attValues)
        throws Exception
    {
        NumberKey[] matchingIssueIds = null;
        SearchIndex searchIndex = SearchFactory.getInstance(); 
        if ( getSearchWords() != null && getSearchWords().length() != 0 ) 
        {
            searchIndex.addQuery(getTextScope(), getSearchWords());
            matchingIssueIds = searchIndex.getRelatedIssues();    
            if ( matchingIssueIds.length != 0 )
            { 
                crit.addIn(AttributeValuePeer.ISSUE_ID, matchingIssueIds);
            }
        }
        else 
        {
            boolean atLeastOne = false;
            for ( int i=0; i<attValues.size(); i++ ) 
            {
                AttributeValue aval = (AttributeValue)attValues.get(i);
                if ( aval instanceof StringAttribute 
                     && aval.getValue() != null 
                     && aval.getValue().length() != 0 )
                {
                    atLeastOne = true;
                    NumberKey[] id = {aval.getAttributeId()};
                    searchIndex
                        .addQuery(id, aval.getValue());
                }
            }
            if ( atLeastOne ) 
            {
                matchingIssueIds = searchIndex.getRelatedIssues();    
                if ( matchingIssueIds.length != 0 )
                { 
                    crit.addIn(AttributeValuePeer.ISSUE_ID, matchingIssueIds);
                }       
            }
        }

        return matchingIssueIds;
    }

    /**
     * Get a List of Issues that match the criteria given by this
     * SearchIssue's searchWords and the quick search attribute values.
     *
     * @param limitResults an <code>int</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getMatchingIssues(int limitResults)
        throws Exception
    {
        // List matchingIssues = null;
        Criteria crit = new Criteria();

        addIssueIdRange(crit);
        addDateRange(crit);
        addMinimumVotes(crit);
        // add option values
        Criteria tempCrit = new Criteria(2)
            .add(AttributeValuePeer.DELETED, false);        
        List attValues = getAttributeValues(tempCrit);
        // remove unset AttributeValues before searching
        removeUnsetValues(attValues);        
        addOptionAttributes(crit, attValues);
        // search for issues based on text
        NumberKey[] matchingIssueIds = addTextMatches(crit, attValues);

        System.out.println("Search criteria = " + crit);
        // get matching issues
        List matchingIssues = IssuePeer.doSelect(crit);
        
        // text search can lead to an ordered list according to search engine's
        // ranking mechanism, so sort the results according to this list
        // unless another sorting criteria has been specified.
        if ( matchingIssueIds != null ) 
        {
            matchingIssues = 
                sortByIssueIdList(matchingIssueIds, matchingIssues, 
                                  limitResults);
        }
        // no sorting
        else
        {
            int maxIssues = matchingIssues.size();
            if ( limitResults > 0 && maxIssues > limitResults )
            {
                maxIssues = limitResults;
            }
            matchingIssues = matchingIssues.subList(0, maxIssues);
        }
            
        return matchingIssues;
    }

    /**
     * Takes a List of Issues and an array of IDs and sorts the Issues in
     * the list to the order given in the ID array
     */
    private List sortByIssueIdList(NumberKey[] ids, List issues, 
                                   int limitResults)
    {
        int maxIssues = issues.size();
        ArrayList sortedIssues = new ArrayList(maxIssues);
        
        // Place issues into a map by id for searching
        Map issueIdMap = new HashMap((int)(1.25*maxIssues+1));
        for ( int i=maxIssues-1; i>=0; i-- ) 
        {
            Issue issue = (Issue)issues.get(i);
            issueIdMap.put( issue.getIssueId(), issue );
        }

        for ( int i=0; i<ids.length  
                  && ( limitResults <= 0 || sortedIssues.size() <= limitResults); i++ ) 
        {
            Object issueObj = issueIdMap.get(ids[i]);
            if (issueObj != null) 
            {
                sortedIssues.add(issueObj);
            }
        }
     
        return sortedIssues;
    }
}
