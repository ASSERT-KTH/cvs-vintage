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

import org.apache.commons.fileupload.FileItem;
import org.apache.fulcrum.ServiceException;
import org.apache.fulcrum.intake.validator.FileValidator;
import org.apache.fulcrum.intake.validator.ValidationException;
import org.apache.fulcrum.intake.xmlmodel.XmlField;
import org.apache.fulcrum.util.parser.ParameterParser;
import org.apache.fulcrum.util.parser.ValueParser;

/**
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id: FileItemField.java,v 1.1 2004/10/24 22:12:29 dep4b Exp $
 */
public class FileItemField 
    extends Field
{
    public FileItemField(XmlField field, Group group)
        throws Exception
    {
        super(field, group);
    }

    /**
     * Sets the default value for an FileItemField
     */

    protected void setDefaultValue(String prop)
    {
        defaultValue = prop;
    }

    /**
     * A suitable validator.
     *
     * @return "FileValidator"
     */
    protected String getDefaultValidator()
    {
        return "org.apache.fulcrum.intake.validator.FileValidator";
    }

    /**
     * Method called when this field (the group it belongs to) is
     * pulled from the pool.  The request data is searched to determine
     * if a value has been supplied for this field.  if so, the value
     * is validated.
     *
     * @param pp a <code>ParameterParser</code> value
     * @return a <code>Field</code> value
     * @exception ServiceException if an error occurs
     */
    public Field init(ValueParser vp)
        throws ServiceException
    {
        try
        {
            super.pp = (ParameterParser) vp;
        }
        catch (ClassCastException e)
        {
            throw new ServiceException(
                "FileItemFields can only be used with ParameterParser");
        }

        valid_flag = true;

        if ( pp.containsKey(getKey()) )
        {
            set_flag = true;
            validate(pp);
        }

        initialized = true;
        return this;
    }

    /**
     * Compares request data with constraints and sets the valid flag.
     */
    protected boolean validate()
    //    throws ServiceException
    {
        ParameterParser pp = (ParameterParser) super.pp;
        if ( isMultiValued  )
        {
            FileItem[] ss = pp.getFileItems(getKey());
            // this definition of not set might need refined.  But
            // not sure the situation will arise.
            if ( ss.length == 0 )
            {
                set_flag = false;
            }

            if ( validator != null )
            {
                for (int i=0; i<ss.length; i++)
                {
                    try
                    {
                        ((FileValidator)validator).assertValidity(ss[i]);
                    }
                    catch (ValidationException ve)
                    {
                        setMessage(ve.getMessage());
                    }
                }
            }

            if ( set_flag && valid_flag )
            {
                doSetValue(pp);
            }

        }
        else
        {
            FileItem s = pp.getFileItem(getKey());
            if ( s == null || s.getSize() == 0 )
            {
                set_flag = false;
            }

            if ( validator != null )
            {
                try
                {
                    ((FileValidator)validator).assertValidity(s);

                    if ( set_flag )
                    {
                        doSetValue(pp);
                    }
                }
                catch (ValidationException ve)
                {
                    setMessage(ve.getMessage());
                }
            }
            else if ( set_flag )
            {
                doSetValue(pp);
            }
        }

        return valid_flag;
    }

    /**
     * converts the parameter to the correct Object.
     */
    protected void doSetValue()
    {
        ParameterParser pp = (ParameterParser) super.pp;
        if ( isMultiValued  )
        {
            setTestValue(pp.getFileItems(getKey()));
        }
        else
        {
            setTestValue(pp.getFileItem(getKey()));
        }
    }
}
