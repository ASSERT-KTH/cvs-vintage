package org.tigris.scarab.util;

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
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.util.ObjectUtils;
import com.workingdogs.village.Record;

// Turbine classes
import org.apache.torque.om.Retrievable;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.pool.DBConnection;

import org.apache.fulcrum.cache.TurbineGlobalCacheService;
import org.apache.fulcrum.cache.GlobalCacheService;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.fulcrum.cache.CachedObject;

import org.apache.fulcrum.TurbineServices;

import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.user.UserManager;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ActivityPeer;
import org.tigris.scarab.om.TransactionPeer;

/** 
 * generates reports
 */
public class ReportGenerator
    implements Retrievable
{
    private static final String[] REPORT_TYPES = 
        {"comparative analysis (single date/time)", 
         "rate of change (multiple date/time)"};

    private static final String[] AXIS_CATEGORIES = 
        {"attributes/options", "users"};

    private static List reportTypes;
    private static List axisCategories;

    private ModuleEntity module;
    private String name;    
    private String description;
    private int type;
    private ScarabUser generatedBy;
    private Date generatedDate;
    private String[] toBeGrouped;
    private List dates;
    private int axis1Category;
    private int axis2Category;
    private String[] axis1AttributesAndOptions;
    private String[] axis2AttributesAndOptions;
    private List optionGroups;

    /** used to store query key as part of Retrievable interface */ 
    private String queryKey;

    static
    {
        reportTypes = new ArrayList();
        for ( int i=0; i<REPORT_TYPES.length; i++ ) 
        {
            reportTypes.add( new SimpleSelectOption(i, REPORT_TYPES[i]) );
        }

        axisCategories = new ArrayList();
        for ( int i=0; i<AXIS_CATEGORIES.length; i++ ) 
        {
            axisCategories.add(new SimpleSelectOption(i, AXIS_CATEGORIES[i]));
        }
    }

    public List getReportTypes()
    {
        return reportTypes;
    }

    public List getAxisCategories()
    {
        return axisCategories;
    }
    
    /**
     * Get the value of module.
     * @return value of module.
     */
    public ModuleEntity getModule() 
    {
        return module;
    }
    
    /**
     * Set the value of module.
     * @param v  Value to assign to module.
     */
    public void setModule(ModuleEntity  v) 
    {
        this.module = v;
    }
    
    /**
     * Get the value of name.
     * @return value of name.
     */
    public String getName() 
    {
        return name;
    }
    
    /**
     * Set the value of name.
     * @param v  Value to assign to name.
     */
    public void setName(String  v) 
    {
        this.name = v;
    }
    
    /**
     * Get the value of description.
     * @return value of description.
     */
    public String getDescription() 
    {
        return description;
    }
    
    /**
     * Set the value of description.
     * @param v  Value to assign to description.
     */
    public void setDescription(String  v) 
    {
        this.description = v;
    }
    
    
    /**
     * Get the value of type.
     * @return value of type.
     */
    public int getType() 
    {
        return type;
    }
    
    /**
     * Set the value of type.
     * @param v  Value to assign to type.
     */
    public void setType(int  v) 
    {
        this.type = v;
    }

    
    /**
     * Get the value of generatedBy.
     * @return value of generatedBy.
     */
    public ScarabUser getGeneratedBy() 
    {
        return generatedBy;
    }
    
    /**
     * Set the value of generatedBy.
     * @param v  Value to assign to generatedBy.
     */
    public void setGeneratedBy(ScarabUser  v) 
    {
        this.generatedBy = v;
    }
    
    
    /**
     * Get the value of generatedDate.
     * @return value of generatedDate.
     */
    public Date getGeneratedDate() 
    {
        return generatedDate;
    }
    
    /**
     * Set the value of generatedDate.
     * @param v  Value to assign to generatedDate.
     */
    public void setGeneratedDate(Date  v) 
    {
        this.generatedDate = v;
    }
    
    /**
     */
    public String[] getAttributesAndOptionsForGrouping() 
    {
        return this.toBeGrouped;
    }
    
    /**
     */
    public void setAttributesAndOptionsForGrouping(String[] v) 
    {
        if ( v != null && (v.length == 0 || v[0].length() == 0) ) 
        {
            this.toBeGrouped = null;
        }
        else 
        {
            this.toBeGrouped = v;            
        }
    }

    public List getOptionGroups()
    {
        System.out.println("returning og's: " + optionGroups );
        return optionGroups;
    }

    public OptionGroup getNewOptionGroup()
    {
        System.out.println("getting new option group");
        return new OptionGroup();
    }

    public void setOptionGroups(List groups)
    {
        this.optionGroups = groups;
    }

    public String[] getGroupNames()
    {
        String[] names = null;
        if ( optionGroups != null ) 
        {
            names = new String[optionGroups.size()];
            for ( int i=0; i<names.length; i++ ) 
            {
                names[i] = ((OptionGroup)optionGroups.get(i)).getDisplayValue();
            }
        }
        return names;
    }

    public void setGroupNames(String[] names)
    {
        if ( names == null ) 
        {
            optionGroups = null;
        }
        else 
        {
            optionGroups = new ArrayList(names.length);
            for ( int i=0; i<names.length; i++ ) 
            {
                optionGroups.add(new OptionGroup(names[i]));
            }
        }
    }

    public List getSelectedOptionsForGrouping()
        throws Exception
    {
        List options = null;
        String[] toBeGrouped = getAttributesAndOptionsForGrouping();
        if ( toBeGrouped == null ) 
        {
            options = new ArrayList(0);
        }
        else 
        {
            options = getSelectedOptions(toBeGrouped);
        }
        return options;
    }

    private String[] remove(String[] array, int index)
    {
        String[] newArray = new String[array.length-1];
        for ( int i=0; i<array.length; i++ ) 
        {
            if ( i != index ) 
            {
                newArray[i] = array[i];
            }
        }
        return newArray;
    }

    private void moveToFront(String[] array, int index)
    {
        String tmp = array[index];
        for ( int i=index-1; i>=0; i-- ) 
        {
            array[i+1] = array[i];
        }
        array[0] = tmp;
    }

    /**
     * fill out list of RModuleOptions based on selected attributes
     * and options
     */
    private List getSelectedOptions(String[] keys)
        throws Exception
    {
        List rmas = module.getRModuleAttributes(true);
        List options = new ArrayList(7*rmas.size());
        int start = 0;
        for ( int i=0; i<rmas.size() && keys.length != start; i++ ) 
        {
            RModuleAttribute rma = (RModuleAttribute)rmas.get(i);
            System.out.println("Attribute: " + rma.getAttribute().getName() );
            if ( rma.getAttribute().isOptionAttribute()) 
            {            
                String rmaId = getKey(rma);
                boolean isRMASelected = false;
                for ( int j=start; j<keys.length; j++ ) 
                {
                    if ( rmaId.equals(keys[j]) ) 
                    {
            System.out.println("matched ");
                        isRMASelected = true;
                        //removing the key, as it is already matched
                        moveToFront(keys, j);
                        start++;
                        break;
                    }                    
                }
                // if selected add all the attributes otherwise we still need
                // to check for a partial list
                List rmos = module
                    .getLeafRModuleOptions(rma.getAttribute());
                if ( isRMASelected ) 
                {
            System.out.println("adding all options " );
                    for ( int j=0; j<rmos.size(); j++ ) 
                    {
System.out.println("adding Option: " + ((RModuleOption)rmos.get(j)).getDisplayValue() );
                        options.add( rmos.get(j) );
                    }               
                }
                else 
                {
            System.out.println("searching options: " );

                    for ( int j=0; j<rmos.size(); j++ ) 
                    {
System.out.println("?? Option: " + ((RModuleOption)rmos.get(j)).getDisplayValue() );
                        String rmoId = getKey((RModuleOption)rmos.get(j));
                        boolean isRMOSelected = false;
                        for ( int k=start; k<keys.length; k++ ) 
                        {
                            if ( rmoId.equals(keys[k]) ) 
                            {
System.out.println("match: " + ((RModuleOption)rmos.get(j)).getDisplayValue() );
                                isRMOSelected = true;
                                //removing the key, as it is already matched
                                moveToFront(keys, k);
                                start++;
                                break;
                            }                    
                        }
                        if ( isRMOSelected ) 
                        {
                            options.add( rmos.get(j) );
                        }                                   
                    }
System.out.println("done searching options: " );
                }
            }
        }
        return options;
    }

    public List getSelectedAxis1Options()
        throws Exception
    {
        List options = null;
        String[] axis1AOs = getAxis1AttributesAndOptions();
        if ( axis1AOs == null ) 
        {
            options = new ArrayList(0);
        }
        else 
        {
            options = getSelectedOptions(axis1AOs);
        }
        return options;
    }

    public List getSelectedAxis2Options()
        throws Exception
    {
        List options = null;
        String[] axis2AOs = getAxis2AttributesAndOptions();
        if ( axis2AOs == null ) 
        {
            options = new ArrayList(0);
        }
        else 
        {
            options = getSelectedOptions(axis2AOs);
        }
        return options;
    }

    public List getAllOptionsForGrouping()
        throws Exception
    {
        List rmas = module.getRModuleAttributes(true);
        List allOptions = new ArrayList(7*rmas.size());
        for ( int i=0; i<rmas.size(); i++ ) 
        {
            RModuleAttribute rma = (RModuleAttribute)rmas.get(i);
            if ( rma.getAttribute().isOptionAttribute()) 
            {            
                allOptions.add( new AttributeOrOptionSelectOption(rma) );
                List rmos = module.getLeafRModuleOptions(rma.getAttribute());

                for ( int j=0; j<rmos.size(); j++ ) 
                {
                    allOptions.add( new AttributeOrOptionSelectOption(
                        (RModuleOption)rmos.get(j) ));
                }               
            }
        }
        return allOptions;
    }

    public Date[] getDates()
    {
        Date[] d = null;
        if ( dates != null ) 
        {
            int max = dates.size();
            d = new Date[max];
            for ( int i=0; i<max; i++ ) 
            {
                d[i] = (Date)dates.get(i);
            }
        }
        
        return d;
    }

    public void setDates(List v)
    {
        this.dates = v;
    }

    public void setDates(Date[] v)
    {
        if ( v == null ) 
        {
            dates = null;
        }
        else
        {
            int max = v.length;
            dates = new ArrayList(max);
            for ( int i=0; i<max; i++ ) 
            {
                dates.add(v[i]);
            }
        }
    }

    /**
     * Returns the last setNewDate or null, if no dates have been set.
     * @return value of newDate.
     */
    public Date getNewDate() 
    {
        Date date = null;
        if ( dates != null && dates.size() != 0 ) 
        {
            date = (Date)dates.get(dates.size()-1);
        }
        else 
        {
            System.out.println("NO DATES");
        }
        

        return date;
    }

    /**
     * Set the value of newDate.
     * @param v  Value to assign to newDate.
     */
    public void setNewDate(Date  date) 
    {
        if ( date != null ) 
        {
            if ( dates == null ) 
            {
                dates = new ArrayList();
            }
            dates.add(date);
        }
    }

    public List getOptionsMinusGroupedOptions()
        throws Exception
    {
        List rmas = module.getRModuleAttributes(true);
        List options = new ArrayList(7*rmas.size());
        for ( int i=0; i<rmas.size(); i++ ) 
        {
            RModuleAttribute rma = (RModuleAttribute)rmas.get(i);

            if ( !isGroupedAttribute(rma) && 
                 rma.getAttribute().isOptionAttribute()) 
            {            
                options.add( new AttributeOrOptionSelectOption(rma) );
                List rmos = module.getLeafRModuleOptions(rma.getAttribute());

                for ( int j=0; j<rmos.size(); j++ ) 
                {
                    RModuleOption rmo = (RModuleOption)rmos.get(j);
                    if ( !isGroupedOption(rmo)) 
                    {
                        options.add( new AttributeOrOptionSelectOption(rmo));
                    }   
                }               
            }
        }
        return options;
    }

    private boolean isGroupedAttribute(RModuleAttribute rma)
        throws Exception
    {
        String test = getKey(rma);
        return isGroupedAttributeOrOption(test);
    }

    private boolean isGroupedOption(RModuleOption rmo)
        throws Exception
    {
        String test = getKey(rmo);
        boolean isGroupedOption = isGroupedAttributeOrOption(test);
        if ( !isGroupedOption ) 
        {
            // check that the whole attribute is not picked
            test = getKey(rmo.getAttributeOption().getAttribute());
            isGroupedOption = isGroupedAttributeOrOption(test);
        }
        
        return isGroupedOption;
    }

    private boolean isGroupedAttributeOrOption(String test)
    {
        boolean isGrouped = false;
        String[] attributeAndOptions = getAttributesAndOptionsForGrouping();
        if ( attributeAndOptions != null ) 
        {
            for (int i=0; i<attributeAndOptions.length; i++)
            {
                if ( test.equals(attributeAndOptions[i]) )
                {
                    isGrouped = true;
                    break;
                }
            }
        }
        return isGrouped;
    }

    /**
     * Get the value of axis1Category.
     * @return value of axis1Category.
     */
    public int getAxis1Category() 
    {
        return axis1Category;
    }
    
    /**
     * Set the value of axis1Category.
     * @param v  Value to assign to axis1Category.
     */
    public void setAxis1Category(int  v) 
    {
        this.axis1Category = v;
    }
    
    
    /**
     * Get the value of axis2Category.
     * @return value of axis2Category.
     */
    public int getAxis2Category() 
    {
        return axis2Category;
    }
    
    /**
     * Set the value of axis2Category.
     * @param v  Value to assign to axis2Category.
     */
    public void setAxis2Category(int  v) 
    {
        this.axis2Category = v;
    }
    
    /**
     */
    public String[] getAxis1AttributesAndOptions() 
    {
        return this.axis1AttributesAndOptions;
    }
    
    /**
     */
    public void setAxis1AttributesAndOptions(String[] v) 
    {
        if ( v != null && (v.length == 0 || v[0].length() == 0) ) 
        {
            this.axis1AttributesAndOptions = null;
        }
        else 
        {
            this.axis1AttributesAndOptions = v;
        }
    }

    /**
     */
    public String[] getAxis2AttributesAndOptions() 
    {
        return this.axis2AttributesAndOptions;
    }
    
    /**
     */
    public void setAxis2AttributesAndOptions(String[] v) 
    {
        if ( v != null && (v.length == 0 || v[0].length() == 0) ) 
        {
            this.axis2AttributesAndOptions = null;
        }
        else 
        {
            this.axis2AttributesAndOptions = v;
        }
    }

    public void generateReport()
    {
    }

    public int getIssueCount(AttributeOption o1, AttributeOption o2,
                             OptionGroup group, Date date)
        throws Exception
    {
        return runQuery(o1, o2, group, date);
    }

    public int getIssueCount(AttributeOption o1, AttributeOption o2,
                             RModuleOption rmo, Date date)
        throws Exception
    {
        return runQuery(o1, o2, rmo, date);
    }


    private int runQuery(AttributeOption o1, AttributeOption o2,
                         Object ogOrRmo, Date date)
        throws Exception
    {
        Criteria crit = new Criteria();
        // select count(issue_id) from activity a1 a2 a3, transaction t1 t2 t3
        crit.addSelectColumn("count(a1.ISSUE_ID)");
        crit.addAlias("a1", ActivityPeer.TABLE_NAME);
        crit.addAlias("a2", ActivityPeer.TABLE_NAME);
        crit.addAlias("a3", ActivityPeer.TABLE_NAME);
        crit.addAlias("t1", TransactionPeer.TABLE_NAME);
        crit.addAlias("t2", TransactionPeer.TABLE_NAME);
        crit.addAlias("t3", TransactionPeer.TABLE_NAME);
        System.out.println("1:  " + crit);
        // where a1.new_option_id=axis1option 
        // and a2.new_option_id=axis2option 
        // and a3.new_option_id in (grouped_options)
        String A1_NEW_OPTION_ID = "a1.NEW_OPTION_ID";
        String A2_NEW_OPTION_ID = "a2.NEW_OPTION_ID";
        String A3_NEW_OPTION_ID = "a3.NEW_OPTION_ID";
        crit.add(A1_NEW_OPTION_ID, o1.getOptionId());
        crit.add(A2_NEW_OPTION_ID, o2.getOptionId());
        addOptionOrOptionGroup(ogOrRmo, crit);
        System.out.println("2:  " + crit);
        // and a1.issue_id=a2.issue_id
        // and a1.issue_id=a3.issue_id
        // and t1.transaction_id=a1.transaction_id
        // and t2.transaction_id=a2.transaction_id
        // and t3.transaction_id=a3.transaction_id
        String A1_ISSUE_ID = "a1.ISSUE_ID";
        String A2_ISSUE_ID = "a2.ISSUE_ID";
        String A3_ISSUE_ID = "a3.ISSUE_ID";
        String A1_TRANSACTION_ID = "a1.TRANSACTION_ID";
        String A2_TRANSACTION_ID = "a2.TRANSACTION_ID";
        String A3_TRANSACTION_ID = "a3.TRANSACTION_ID";
        String T1_TRANSACTION_ID = "t1.TRANSACTION_ID";
        String T2_TRANSACTION_ID = "t2.TRANSACTION_ID";
        String T3_TRANSACTION_ID = "t3.TRANSACTION_ID";
        crit.addJoin(A1_ISSUE_ID, A2_ISSUE_ID);
        crit.addJoin(A1_ISSUE_ID, A3_ISSUE_ID);
        crit.addJoin(T1_TRANSACTION_ID, A1_TRANSACTION_ID);
        crit.addJoin(T2_TRANSACTION_ID, A2_TRANSACTION_ID);
        crit.addJoin(T3_TRANSACTION_ID, A3_TRANSACTION_ID);
        System.out.println("3:  " + crit);
        // and t1.created_date<date
        // and t2.created_date<date
        // and t3.created_date<date
        String T1_CREATED_DATE = "t1.CREATED_DATE";
        String T2_CREATED_DATE = "t2.CREATED_DATE";
        String T3_CREATED_DATE = "t3.CREATED_DATE";
        crit.add(T1_CREATED_DATE, date, Criteria.LESS_THAN);
        crit.add(T2_CREATED_DATE, date, Criteria.LESS_THAN);
        crit.add(T3_CREATED_DATE, date, Criteria.LESS_THAN);
        System.out.println("4:  " + date);
        System.out.println("4:  " + crit);
        // and a1.end_date>date
        // and a2.end_date>date
        // and a3.end_date>date
        String A1_END_DATE = "a1.END_DATE";
        String A2_END_DATE = "a2.END_DATE";
        String A3_END_DATE = "a3.END_DATE";
        crit.add(A1_END_DATE, date, Criteria.GREATER_THAN);
        crit.add(A2_END_DATE, date, Criteria.GREATER_THAN);
        crit.add(A3_END_DATE, date, Criteria.GREATER_THAN);
        // need to add in module criteria
        System.out.println("5:  " + crit);
        List records = ActivityPeer.doSelectVillageRecords(crit);
        return ((Record)records.get(0)).getValue(1).asInt();
    }

    private void addOptionOrOptionGroup(Object obj, Criteria crit)
    {
        if ( obj instanceof OptionGroup ) 
        {
            List options = ((OptionGroup)obj).getOptions();
            NumberKey[] nks = new NumberKey[options.size()];
            for ( int i=0; i<nks.length; i++) 
            {
                nks[i] = ((RModuleOption)options.get(i)).getOptionId();
            }
            
            crit.addIn("a3.OPTION_ID", nks);
        }
        else 
        {
            crit.add("a3.OPTION_ID", ((RModuleOption)obj).getOptionId());
        }
    }


    // *********************************************************
    // Retrievable implementation
    // *********************************************************
    
    /**
     * Get the value of queryKey.
     * @return value of queryKey.
     */    
    public String getQueryKey() 
    {
        if ( queryKey == null ) 
        {
            return "";
        }
        return queryKey;
    }
    
    /**
     * Set the value of queryKey.
     * @param v  Value to assign to queryKey.
     */
    public void setQueryKey(String  v) 
    {
        this.queryKey = v;
    }



    // *********************************************************

    public static class OptionGroup
        implements Retrievable
    {
        private String displayValue;
        private boolean selected;
        private String queryKey;
        private List options;
        
        public OptionGroup()
        {
        }

        public OptionGroup(String name)
        {
            displayValue = name;
        }

        /**
         * Get the value of displayValue.
         * @return value of displayValue.
         */
        public String getDisplayValue() 
        {
            return displayValue;
        }
        
        /**
         * Set the value of displayValue.
         * @param v  Value to assign to displayValue.
         */
        public void setDisplayValue(String  v) 
        {
            this.displayValue = v;
        }
        
        
        /**
         * Get the value of selected.
         * @return value of selected.
         */
        public boolean isSelected() 
        {
            return selected;
        }
        
        /**
         * Set the value of selected.
         * @param v  Value to assign to selected.
         */
        public void setSelected(boolean  v) 
        {
            this.selected = v;
        }
        

        public void addOption(RModuleOption rmo)
        {
            if ( options == null ) 
            {
                options = new ArrayList();
            }
            options.add(rmo);
        }

        public List getOptions()
        {
            if ( options == null ) 
            {
                options = new ArrayList();
            }
            return options;
        }

        // *********************************************************
        // Retrievable implementation
        // *********************************************************
        
        /**
         * Get the value of queryKey.
         * @return value of queryKey.
         */    
        public String getQueryKey() 
        {
            /*
            List groups = getOptionGroups();
            int index = -1;
            for ( int i=0; i<groups.size(); i++ ) 
            {
                if ( ((OptionGroup)groups.get(i)).getDisplayValue()
                     .equals(displayValue)) 
                {
                    index = i;
                    break;
                }
            }
            
            return String.valueOf(index);
            */
            if ( queryKey == null ) 
            {
                return "";
            }
            return queryKey;
        }

        
        /**
         * Set the value of queryKey.
         * @param v  Value to assign to queryKey.
         */
        public void setQueryKey(String  v) 
        {
            this.queryKey = v;
        }
    }

    
    // *********************************************************

    public static class SimpleSelectOption
    {
        protected String name;
        protected String value;
        protected boolean selected;

        public SimpleSelectOption()
        {
        }

        public SimpleSelectOption(String value, String name)
        {
            this(value, name, false);
        }

        public SimpleSelectOption(int value, String name)
        {
            this(String.valueOf(value), name);
        }

        public SimpleSelectOption(int value, String name, boolean selected)
        {
            this(String.valueOf(value), name, selected);
        }

        public SimpleSelectOption(String value, String name, boolean selected)
        {
            this.name = name;
            this.value = value;
            this.selected = selected;
        }

        /**
         * Get the value of name.
         * @return value of name.
         */
        public String getName() 
        {
            return name;
        }
        
        /**
         * Set the value of name.
         * @param v  Value to assign to name.
         */
        public void setName(String  v) 
        {
            this.name = v;
        }
        
        
        /**
         * Get the value of value.
         * @return value of value.
         */
        public String getValue() 
        {
            return value;
        }
        
        /**
         * Set the value of value.
         * @param v  Value to assign to value.
         */
        public void setValue(String  v) 
        {
            this.value = v;
        }
        
        
        /**
         * Get the value of selected.
         * @return value of selected.
         */
        public boolean isSelected() 
        {
            return selected;
        }

        /**
         * returns a form string appropriate for the option.
         * @return selected="selected" or "".
         */
        public String getSelected() 
        {
            String s = null;
            if ( isSelected() ) 
            {
                s = " selected=\"selected\"";
            }
            else 
            {
                s = "";
            }
            
            return s;
        }
        
        /**
         * Set the value of selected.
         * @param v  Value to assign to selected.
         */
        public void setSelected(boolean  v) 
        {
            this.selected = v;
        }
        
    }


    // *********************************************************

    public static class AttributeOrOptionSelectOption
        extends SimpleSelectOption
    {
        private boolean isAttribute;

        AttributeOrOptionSelectOption(RModuleAttribute rma)
            throws Exception
        {
            setIsAttribute(true);
            super.setName(rma.getDisplayValue());
            setValue( getKey(rma) );
        }

        AttributeOrOptionSelectOption(RModuleOption rmo)
            throws Exception
        {
            setIsAttribute(false);
            super.setName(rmo.getDisplayValue());
            setValue( getKey(rmo) );
        }

        /**
         * Get the value of isAttribute.
         * @return value of isAttribute.
         */
        public boolean isAttribute() 
        {
            return isAttribute;
        }
        
        /**
         * Set the value of isAttribute.
         * @param v  Value to assign to isAttribute.
         */
        public void setIsAttribute(boolean  v) 
        {
            this.isAttribute = v;
        }   
    }    

        private static String getKey(RModuleAttribute rma)
            throws Exception
        {
            return getKey(rma.getAttribute());
        }

        private static String getKey(Attribute a)
        {
            return "a" + a.getQueryKey();
        }

        private static String getKey(RModuleOption rmo)
            throws Exception
        {
            return getKey(rmo.getAttributeOption());
        }

        private static String getKey(AttributeOption o)
        {
            return o.getQueryKey();
        }
        

}


 
