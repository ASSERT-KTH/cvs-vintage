package org.apache.fulcrum.template;

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
import org.apache.fulcrum.ServiceException;
import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.template.TemplateContext;

/**
 * This is a simple static accessor to common TemplateService tasks such as
 * getting a Screen that is associated with a screen template.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: TurbineTemplate.java,v 1.1 2004/10/24 22:12:30 dep4b Exp $
 */
public abstract class TurbineTemplate
{
    /**
     * Utility method for accessing the service
     * implementation
     *
     * @return a TemplateService implementation instance
     */
    protected static TemplateService getService()
    {
        return (TemplateService)TurbineServices
            .getInstance().getService(TemplateService.SERVICE_NAME);
    }

    public static final void registerTemplateEngineService(TemplateEngineService service)
    {
        getService().registerTemplateEngineService(service);
    }

    public static final String[] translateTemplatePaths(String[] templatePaths)
    {
        return getService().translateTemplatePaths(templatePaths);
    }

    public static final boolean templateExists(String template, String[] templatePaths)
    {
        return getService().templateExists(template, templatePaths);
    }

    public static final String handleRequest(TemplateContext context, String template)
        throws ServiceException
    {
        return getService().handleRequest(context, template);
    }

    public static final void handleRequest(TemplateContext context,
                                             String template,
                                             OutputStream outputStream)
        throws ServiceException
    {
        getService().handleRequest(context, template, outputStream);
    }

    public static final void handleRequest(TemplateContext context,
                                           String template,
                                           Writer writer)
        throws ServiceException
    {
        getService().handleRequest(context, template, writer);
    }

    public static final TemplateContext getTemplateContext()
    {
        return getService().getTemplateContext();
    }

    public static final boolean templateExists(String template)
    {
        return getService().templateExists(template);
    }
}
