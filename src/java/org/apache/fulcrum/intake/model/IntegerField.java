package org.apache.fulcrum.intake.model;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.apache.fulcrum.intake.xmlmodel.XmlField;
import org.apache.log4j.Category;

/**
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id: IntegerField.java,v 1.1 2004/10/24 22:12:29 dep4b Exp $
 */
public class IntegerField 
    extends Field
{
    /** Log4j category */
    Category category = Category.getInstance(getClass().getName());

    public IntegerField(XmlField field, Group group)
        throws Exception
    {
        super(field, group);
    }

    /**
     * Sets the default value for an Integer Field
     *
     * @param prop Parameter for the default values
     */
    protected void setDefaultValue(String prop)
    {
        defaultValue = null;

        if (prop == null)
        {
            return;
        }

        try
        {
            defaultValue = new Integer(prop);
        }
        catch (Exception e)
        {
            category.error("Could not convert "
                           + prop + " into an Integer. ("
                           + name + ")");
        }
    }

    /**
     * A suitable validator.
     *
     * @return "IntegerValidator"
     */
    protected String getDefaultValidator()
    {
        return "org.apache.fulcrum.intake.validator.IntegerValidator";
    }

    /**
     * converts the parameter to the correct Object.
     */
    protected void doSetValue()
    {
        if ( isMultiValued  )
        {
            String[] ss = pp.getStrings(getKey());
            try 
            {            
                Integer[] values = new Integer[ss.length];
                for (int i=0; i<ss.length; i++)
                {
                    if (ss[i] != null && ss[i].length() > 0) 
                    {
                        values[i] = new Integer(ss[i]);
                    }
                }
                setTestValue(values);
            }
            catch (ClassCastException e)
            {
                int[] ival = new int[ss.length];
                for (int i=0; i<ss.length; i++)
                {
                    if (ss[i] != null && ss[i].length() > 0) 
                    {
                        ival[i] = Integer.parseInt(ss[i]);
                    }
                }
                setTestValue(ival);
            }
        }
        else
        {
            String s = pp.getString(getKey());
            if (s != null && s.length() > 0) 
            {
                setTestValue(new Integer(s));
            }
            else 
            {
                set_flag = false;
            }
        }
    }
}
