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
// Turbine classes
import org.apache.torque.om.Retrievable;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
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

    private String name;    
    private String description;
    private int type;
    private String[] filters;
    private List dates;
    private int axis1Category;
    private int axis2Category;
    private String[] axis1AttributesAndOptions;
    private String[] axis2AttributesAndOptions;

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
     */
    public String[] getFilterAttributesAndOptions() 
    {
        return this.filters;
    }
    
    /**
     */
    public void setFilterAttributesAndOptions(String[] v) 
    {
        if ( v != null && (v.length == 0 || v[0].length() == 0) ) 
        {
            this.filters = null;
        }
        else 
        {
            this.filters = v;            
        }
    }

    public List getSelectedFilterOptions(ModuleEntity module)
    {
        List options = null;
        String[] filters = getFilterAttributesAndOptions();
        if ( filters == null ) 
        {
            options = new ArrayList(0);
        }
        else 
        {
            // fill out list of RModuleOptions based on selected attributes
            // and options
            
        }
        return options;
    }

    public List getAllFilterOptions(ModuleEntity module)
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
                //Criteria crit = new Criteria()
                //    .add(RModuleOptionPeer.ACTIVE, true);
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

    public List getDates()
    {
        return dates;
    }

    /**
     * Returns the last setNewDate or now, if no dates have been set.
     * @return value of newDate.
     */
    public Date getNewDate() 
    {
        Date date = null;
        if ( dates != null && dates.size() != 0 ) 
        {
            date = (Date)dates.get(dates.size()-1);
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

    public List getOptionsMinusFilter(ModuleEntity module)
        throws Exception
    {
        List rmas = module.getRModuleAttributes(true);
        List options = new ArrayList(7*rmas.size());
        for ( int i=0; i<rmas.size(); i++ ) 
        {
            RModuleAttribute rma = (RModuleAttribute)rmas.get(i);

            if ( !isFilterAttribute(rma) && 
                 rma.getAttribute().isOptionAttribute()) 
            {            
                options.add( new AttributeOrOptionSelectOption(rma) );
                List rmos = module.getLeafRModuleOptions(rma.getAttribute());

                for ( int j=0; j<rmos.size(); j++ ) 
                {
                    RModuleOption rmo = (RModuleOption)rmos.get(j);
                    if ( !isFilterOption(rmo)) 
                    {
                        options.add( new AttributeOrOptionSelectOption(rmo));
                    }   
                }               
            }
        }
        return options;
    }

    private boolean isFilterAttribute(RModuleAttribute rma)
        throws Exception
    {
        String test = getKey(rma);
        return isFilterAttributeOrOption(test);
    }

    private boolean isFilterOption(RModuleOption rmo)
        throws Exception
    {
        String test = getKey(rmo);
        boolean isFilterOption = isFilterAttributeOrOption(test);
        if ( !isFilterOption ) 
        {
            // check that the whole attribute is not picked
            test = getKey(rmo.getAttributeOption().getAttribute());
            isFilterOption = isFilterAttributeOrOption(test);
        }
        
        return isFilterOption;
    }

    private boolean isFilterAttributeOrOption(String test)
    {
        boolean isFilter = false;
        String[] filterAttributeAndOptions = getFilterAttributesAndOptions();
        if ( filterAttributeAndOptions != null ) 
        {
            for (int i=0; i<filterAttributeAndOptions.length; i++)
            {
                if ( test.equals(filterAttributeAndOptions[i]) )
                {
                    isFilter = true;
                    break;
                }
            }
        }
        return isFilter;
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


 
