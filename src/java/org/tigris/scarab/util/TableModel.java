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

// JDK classes
import java.util.List;
import java.util.Date;


/**
 * A model that provides for an application to present a set of tabular data.
 * Can be used along with a velocity macro to create a table.
 * 
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: TableModel.java,v 1.5 2003/02/04 11:26:03 jon Exp $
 */
public abstract class TableModel
    // implements Retrievable
{
    private List headings;
    private List rows;
    
    public static boolean isDate(Object obj)
    {
        return obj instanceof Date;
    }

    public static boolean isHeading(Object obj)
    {
        return obj instanceof Heading;
    }

    public static boolean isColumnHeading(Object obj)
    {
        return obj instanceof ColumnHeading;
    }

    public abstract int getColumnCount();
    public abstract int getRowCount();
    public abstract Object getValueAt(int row, int column)
    throws Exception;


    
    /**
     * Get the value of headings.
     * @return value of headings.
     */
    public List getHeadings() 
    {
        return headings;
    }
    
    /**
     * Set the value of headings.
     * @param v  Value to assign to headings.
     */
    public void setHeadings(List  v) 
    {
        this.headings = v;
    }
    
    
    /**
     * Get the value of rows.
     * @return value of rows.
     */
    public List getRows() 
    {
        return rows;
    }
    
    /**
     * Set the value of rows.
     * @param v  Value to assign to rows.
     */
    public void setRows(List  v) 
    {
        this.rows = v;
    }


    public class ColumnHeading
        extends Heading
    {        
        /**
         * Get the value of colspan.  This method only works with one
         * level of subheadings
         * @return value of colspan.
         */
        public int getColspan() 
        {
            int colspan = 1;
            List subHeadings = getSubHeadings();
            if (subHeadings != null && subHeadings.size() > 0)
            { 
                colspan = subHeadings.size();
            }
        
            return colspan;
        }
    }

    public class RowHeading
        extends Heading
    {        
        /**
         * Get the value of rowspan.  This method only works with one
         * level of subheadings
         * @return value of rowspan.
         */
        public int getRowspan() 
        {
            int rowspan = 1;
            List subHeadings = getSubHeadings();
            if (subHeadings != null && subHeadings.size() > 0)
            { 
                rowspan = subHeadings.size();
            }
        
            /*
            while (subHeadings != null || subHeadings.size() > 0)
            {                
                subHeadings = recurseHeadings
                
                int size = subHeadings.size();
                for (int i=0; i<size; i++) 
                {
                    int max = 1;
                    List recurseHeadings = 
                        ((Heading)subHeadings.get(i)).getSubHeadings();
                    while (recurseHeadings != null 
                            && recurseHeadings.size() > 0) 
                    {
                        int test = recurseHeadings.size();
                        max = (test > max) ? test : max;

                        recurseHeadings
                    }
                    
                }  
            }
            */
            return rowspan;
        }
    }

    public class Heading
    {
        List subHeadings;
        Object label;
        
        /**
         * Get the value of rowspan.
         * @return value of rowspan.
         */
        public int getRowspan() 
        {
            return 1;
        }

        /**
         * Get the value of colspan.
         * @return value of colspan.
         */
        public int getColspan() 
        {
            return 1;
        }
                        
        /**
         * Get the value of subHeadings.
         * @return value of subHeadings.
         */
        public List getSubHeadings() 
        {
            return subHeadings;
        }
        
        /**
         * Set the value of subHeadings.
         * @param v  Value to assign to subHeadings.
         */
        public void setSubHeadings(List  v) 
        {
            this.subHeadings = v;
        }

        
        /**
         * Get the value of label.
         * @return value of label.
         */
        public Object getLabel() 
        {
            return label;
        }
        
        /**
         * Set the value of label.
         * @param v  Value to assign to label.
         */
        public void setLabel(Object  v) 
        {
            this.label = v;
        }        
    }
}
