package org.apache.fulcrum.intake.validator;

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

import java.math.BigDecimal;
import java.util.Map;

import org.apache.fulcrum.ServiceException;

/**
 * Validates numbers with the following constraints in addition to those
 * listed in DefaultValidator.
 *
 * <table>
 * <tr><th>Name</th><th>Valid Values</th><th>Default Value</th></tr>
 * <tr><td>minValue</td><td>greater than BigDecimal.MIN_VALUE</td>
 * <td>&nbsp;</td></tr>
 * <tr><td>maxValue</td><td>less than BigDecimal.MAX_VALUE</td>
 * <td>&nbsp;</td></tr>
 * <tr><td>notANumberMessage</td><td>Some text</td>
 * <td>Entry was not a valid number</td></tr>
 * </table>
 *
 * @author <a href="mailto:jmcnally@collab.net>John McNally</a>
 * @version $Id: NumberValidator.java,v 1.1 2004/10/24 22:12:30 dep4b Exp $
 */
public class NumberValidator
    extends DefaultValidator
{
    private static String INVALID_NUMBER = "Entry was not a valid number";

    private BigDecimal minValue;
    protected String minValueMessage;
    private BigDecimal maxValue;
    protected String maxValueMessage;
    protected String invalidNumberMessage;

    public NumberValidator(Map paramMap)
        throws ServiceException
    {
        this();
        init(paramMap);
    }

    public NumberValidator()
    {
        invalidNumberMessage = getDefaultInvalidNumberMessage();
    }

    /**
     * Extract the relevant parameters from the constraints listed
     * in <input-param> tags within the intake.xml file.
     *
     * @param inputParameters a <code>Map</code> of <code>InputParam</code>'s
     * containing constraints on the input.
     * @exception ServiceException if an error occurs
     */
    public void init(Map paramMap)
        throws ServiceException
    {
        super.init(paramMap);

        minValueMessage = null;
        maxValueMessage = null;

        doInit(paramMap);

        Constraint constraint = (Constraint)paramMap.get("notANumberMessage");
        if ( constraint != null )
        {
            String param = constraint.getValue();
            if ( param != null && param.length() != 0 )
            {
                invalidNumberMessage = param;
            }
            else if ( constraint.getMessage().length() != 0 )
            {
                invalidNumberMessage = constraint.getMessage();
            }
        }
    }

    protected void doInit(Map paramMap)
    {
        minValue = null;
        maxValue = null;

        Constraint constraint = (Constraint)paramMap.get("minValue");
        if ( constraint != null )
        {
            String param = constraint.getValue();
            minValue = new BigDecimal(param);
            minValueMessage = constraint.getMessage();
        }

        constraint = (Constraint)paramMap.get("maxValue");
        if ( constraint != null )
        {
            String param = constraint.getValue();
            maxValue = new BigDecimal(param);
            maxValueMessage = constraint.getMessage();
        }
    }

    protected String getDefaultInvalidNumberMessage()
    {
        return INVALID_NUMBER;
    }

    /**
     * Determine whether a testValue meets the criteria specified
     * in the constraints defined for this validator
     *
     * @param testValue a <code>String</code> to be tested
     * @exception ValidationException containing an error message if the
     * testValue did not pass the validation tests.
     */
    protected void doAssertValidity(String testValue)
        throws ValidationException
    {
        BigDecimal bd = null;
        try
        {
            bd = new BigDecimal(testValue);
        }
        catch (RuntimeException e)
        {
            message = invalidNumberMessage;
            throw new ValidationException(invalidNumberMessage);
        }

        if ( minValue != null && bd.compareTo(minValue) < 0 )
        {
            message = minValueMessage;
            throw new ValidationException(minValueMessage);
        }
        if ( maxValue != null && bd.compareTo(maxValue) > 0 )
        {
            message = maxValueMessage;
            throw new ValidationException(maxValueMessage);
        }
    }

    // ************************************************************
    // **                Bean accessor methods                   **
    // ************************************************************

    /**
     * Get the value of minValue.
     * @return value of minValue.
     */
    public BigDecimal getMinValueAsBigDecimal()
    {
        return minValue;
    }

    /**
     * Set the value of minValue.
     * @param v  Value to assign to minValue.
     */
    public void setMinValue(BigDecimal  v)
    {
        this.minValue = v;
    }

    /**
     * Get the value of minValueMessage.
     * @return value of minValueMessage.
     */
    public String getMinValueMessage()
    {
        return minValueMessage;
    }

    /**
     * Set the value of minValueMessage.
     * @param v  Value to assign to minValueMessage.
     */
    public void setMinValueMessage(String  v)
    {
        this.minValueMessage = v;
    }

    /**
     * Get the value of maxValue.
     * @return value of maxValue.
     */
    public BigDecimal getMaxValueAsBigDecimal()
    {
        return maxValue;
    }

    /**
     * Set the value of maxValue.
     * @param v  Value to assign to maxValue.
     */
    public void setMaxValue(BigDecimal  v)
    {
        this.maxValue = v;
    }

    /**
     * Get the value of maxValueMessage.
     * @return value of maxValueMessage.
     */
    public String getMaxValueMessage()
    {
        return maxValueMessage;
    }

    /**
     * Set the value of maxValueMessage.
     * @param v  Value to assign to maxValueMessage.
     */
    public void setMaxValueMessage(String  v)
    {
        this.maxValueMessage = v;
    }

    /**
     * Get the value of invalidNumberMessage.
     * @return value of invalidNumberMessage.
     */
    public String getInvalidNumberMessage()
    {
        return invalidNumberMessage;
    }

    /**
     * Set the value of invalidNumberMessage.
     * @param v  Value to assign to invalidNumberMessage.
     */
    public void setInvalidNumberMessage(String  v)
    {
        this.invalidNumberMessage = v;
    }

}
