package org.tigris.scarab.services.email;

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

import java.io.OutputStream;
import java.io.Writer;
import org.apache.fulcrum.Service;
import org.apache.fulcrum.ServiceException;
import org.apache.velocity.context.Context;

/**
 * The Turbine service interface to
 * <a href="http://jakarta.apache.org/velocity/">Velocity</a>.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: EmailService.java,v 1.2 2003/05/03 22:37:24 jon Exp $
 */
public interface EmailService extends Service
{
    static final String SERVICE_NAME = "EmailService";

    /**
     * Process the request and fill in the template using the values
     * set in <code>context</code>.
     *
     * @param context A context to use when evaluating the specified
     * template.
     * @param filename The file name of the template.
     * @return The processed template.
     * @exception Exception, a generic exception.
     */
    String handleRequest(Context context, String filename)
        throws Exception;

    /**
     * Process the request and fill in the template using the values
     * set in <code>context</code>.
     *
     * @param context A context to use when evaluating the specified
     * template.
     * @param filename The file name of the template.
     * @param charset The character set to use when writing the result.
     * @param encoding The encoding to use when merging context and
     * template.
     * @return The processed template.
     * @exception Exception, a generic exception.
     */
    String handleRequest(Context context, String template,
                                String charset, String encoding)
        throws Exception;

    /**
     * Process the request and fill in the template using the values
     * set in <code>context</code>.
     *
     * @param context A context to use when evaluating the specified
     * template.
     * @param filename The file name of the template.
     * @param out The stream to which we will write the processed
     * template as a String.
     * @throws ServiceException Any exception trown while processing will be
     *         wrapped into a ServiceException and rethrown.
     */
    void handleRequest(Context context, String filename,
                              OutputStream out)
        throws ServiceException;

    /**
     * Process the request and fill in the template using the values
     * set in <code>context</code>.
     *
     * @param context A context to use when evaluating the specified
     * template.
     * @param filename The file name of the template.
     * @param out The stream to which we will write the processed
     * template as a String.
     * @param charset The character set to use when writing the result.
     * @param encoding The encoding to use when merging context and
     * template.
     * @throws ServiceException Any exception trown while processing will be
     *         wrapped into a ServiceException and rethrown.
     */
    void handleRequest(Context context, String filename,
                              OutputStream out, String charset,
                              String encoding)
        throws ServiceException;

    /**
     * Process the request and fill in the template using the values
     * set in <code>context</code>.
     *
     * @param context A context to use when evaluating the specified
     * template.
     * @param filename The file name of the template.
     * @param writer The writer to which we will write the processed template.
     * @throws ServiceException Any exception trown while processing will be
     *         wrapped into a ServiceException and rethrown.
     */
    void handleRequest(Context context, String filename,
                              Writer writer)
        throws ServiceException;

    /**
     * Process the request and fill in the template using the values
     * set in <code>context</code>.
     *
     * @param context A context to use when evaluating the specified
     * template.
     * @param filename The file name of the template.
     * @param writer The writer to which we will write the processed template.
     * @param encoding The encoding to use when merging context and
     * template.
     * @throws ServiceException Any exception trown while processing will be
     *         wrapped into a ServiceException and rethrown.
     */
    void handleRequest(Context context, String filename,
                              Writer writer, String encoding)
        throws ServiceException;
}
