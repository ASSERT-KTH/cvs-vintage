package org.tigris.scarab.util.word;

// JDK classes
import java.util.*;
import java.sql.Connection;

// Turbine classes
import org.apache.turbine.services.db.om.*;
import org.apache.turbine.services.db.util.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.services.resources.TurbineResources;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.SequencedHashtable;
import org.apache.turbine.services.db.TurbineDB;
import org.apache.turbine.services.db.pool.DBConnection;
import org.apache.turbine.services.db.map.DatabaseMap;

import org.tigris.scarab.om.*;
import org.tigris.scarab.util.ScarabConstants;
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
    private String searchWords;
    private NumberKey[] textScope;
    private static final NumberKey ALL_TEXT = new NumberKey("0");

    private static final String PARENT_ID;
    private static final String CHILD_ID;
    private static final String AV_ISSUE_ID;
    private static final String AV_OPTION_ID;

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
        List textAttributes = getQuickSearchTextAttributes();
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

    public NumberKey getALL_TEXT()
    {
        return ALL_TEXT;
    }

    public List getQuickSearchTextAttributes()
        throws Exception
    {
        SequencedHashtable searchValues = getModuleAttributeValuesMap();
        List searchAttributes = new ArrayList(searchValues.size());

        for ( int i=0; i<searchValues.size(); i++ ) 
        {
            AttributeValue searchValue = 
                (AttributeValue)searchValues.getValue(i);
            if ( searchValue.isQuickSearchAttribute() &&                  
                 searchValue instanceof StringAttribute ) 
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
        SequencedHashtable searchValues = getModuleAttributeValuesMap();
        List searchAttributeValues = new ArrayList(searchValues.size());

        for ( int i=0; i<searchValues.size(); i++ ) 
        {
            AttributeValue searchValue = 
                (AttributeValue)searchValues.getValue(i);
            if ( searchValue.isQuickSearchAttribute() && 
                 searchValue instanceof OptionAttribute ) 
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
            if ( attVal.getOptionId() == null && attVal.getValue() == null ) 
            {
                attValues.remove(i);
            }
        }
    }


    /**
     * Returns a List of matching issues.  if no OptionAttributes were
     * found in the input list null is returned.
     *
     * @param attValues a <code>List</code> value
     */
    private List searchOnOptionAttributes(List attValues)
        throws Exception
    {
        Criteria crit = new Criteria();
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
            return IssuePeer.doSelect(crit);
        }
        else 
        {
            return null;
        }
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
        List matchingIssues = null;

        Criteria crit = new Criteria(2)
            .add(AttributeValuePeer.DELETED, false);        
        List attValues = getAttributeValues(crit);
        // remove unset AttributeValues before searching
        removeUnsetValues(attValues);
        
        // get matching issues according to option values
        List optionMatchingIssues = searchOnOptionAttributes(attValues);

        // search for duplicate issues based on text
        NumberKey[] matchingIssueIds = null;      
        if ( getSearchWords() != null && getSearchWords().length() != 0 ) 
        {
            SearchIndex searchIndex = SearchFactory.getInstance();
            searchIndex.addQuery(getSearchWords());
            searchIndex.setAttributeIds(getTextScope());
            matchingIssueIds = searchIndex.getRelatedIssues();      
        }
        
        // only have text search
        if ( optionMatchingIssues == null && matchingIssueIds != null ) 
        {
            int maxIssues = matchingIssueIds.length;
            if ( maxIssues > limitResults )
            {
                maxIssues = limitResults;
            }
            matchingIssues = new ArrayList(maxIssues);

            // fetch them one by one to keep the order
            for (int i=0; i<maxIssues; i++)
            {
                matchingIssues
                    .add(IssuePeer.retrieveByPK(matchingIssueIds[i]));
            }            
        }
        // options only
        else if ( optionMatchingIssues != null && matchingIssueIds == null )
        {
            int maxIssues = optionMatchingIssues.size();
            if ( maxIssues > limitResults )
            {
                maxIssues = limitResults;
            }
            matchingIssues = new ArrayList(maxIssues);
        
            for (int i=0; i<maxIssues; i++)
            {
                matchingIssues.add(optionMatchingIssues.get(i));
            }
        }
        // text and options
        else if ( optionMatchingIssues != null && matchingIssueIds != null )
        {            
            int maxIssues = optionMatchingIssues.size();
            matchingIssues = new ArrayList(maxIssues);

            // Place id's into a map for searching
            Map issueIdMap = new HashMap((int)(1.25*maxIssues+1));
            for ( int i=maxIssues-1; i>=0; i-- ) 
            {
                Issue issue = (Issue)optionMatchingIssues.get(i);
                issueIdMap.put( issue.getIssueId(), issue );
            }

            for ( int i=0; i<matchingIssueIds.length 
                           || matchingIssues.size() == limitResults; i++ ) 
            {
                Object issueObj = issueIdMap.get(matchingIssueIds[i]);
                if (issueObj != null) 
                {
                    matchingIssues.add(issueObj);
                }
            }
        }
            
        return matchingIssues;
    }
}
