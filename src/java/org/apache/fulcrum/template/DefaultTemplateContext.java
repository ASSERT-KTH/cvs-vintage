package org.apache.fulcrum.template;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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

import java.util.HashMap;

/**
 *  General purpose implemention of the application Context
 *  interface for general application use.  This class should
 *  be used in place of the original Context class.
 *
 *  This implementation uses a HashMap  (@see java.util.HashMap )
 *  for data storage.
 *
 *  This context implementation cannot be shared between threads
 *  without those threads synchronizing access between them, as
 *  the HashMap is not synchronized, nor are some of the fundamentals
 *  of AbstractContext.  If you need to share a Context between
 *  threads with simultaneous access for some reason, please create
 *  your own and extend the interface Context
 *
 *  @see org.apache.velocity.context.Context
 *
 *  @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 *  @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 *  @author <a href="mailto:fedor.karpelevitch@home.com">Fedor Karpelevitch</a>
 *  @version $Id: DefaultTemplateContext.java,v 1.1 2004/10/24 22:12:30 dep4b Exp $
 */
public class DefaultTemplateContext
    implements TemplateContext
{
    TemplateContext innerContext;

    /**
     *  storage for key/value pairs
     */
    private HashMap context = new HashMap();

    /**
     * default contructor, does nothing
     * interesting
     */
    public DefaultTemplateContext()
    {
    }

    /**
     * Allow chained contexts.
     */
    public DefaultTemplateContext(TemplateContext context)
    {
        super();

        //!! I realize this is not the most efficient
        // way to do this, but I'm not sure if chained
        // contexts can work with templating solutions
        // other than velocity. I don't see why not right
        // of the bat, but this will work for now.

        Object[] keys = context.getKeys();

        for (int i = 0; i < keys.length; i++)
        {
            put((String) keys[i], context.get((String)keys[i]));
        }
    }

    public void put(String key, Object value)
    {
        context.put(key, value);
    }

    public Object get(String key)
    {
        return context.get(key);
    }

    public Object remove(Object key)
    {
        return context.remove(key);
    }

    public boolean containsKey(Object key)
    {
        return context.containsKey(key);
    }

    public Object[] getKeys()
    {
        return context.keySet().toArray();
    }
}

