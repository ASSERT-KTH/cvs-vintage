package org.tigris.scarab.util.xmlissues;

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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.IteratorUtils;

/**
 * A lazily constructed <code>Collection</code> implementation which
 * records the list of any errors which occur during an import.
 *
 * @since Scarab 0.16.29
 */
class ImportErrors extends AbstractCollection
{
    /**
     * Internal storage for the list of errors.
     */
    private Collection errors;

    /**
     * Contextual information recorded while parsing.
     */
    private Object parseContext = null;

    public ImportErrors()
    {
    }

    public Iterator iterator()
    {
        return (errors == null ?
                IteratorUtils.EMPTY_ITERATOR : errors.iterator());
    }

    public int size()
    {
        return (errors == null ? 0 : errors.size());
    }

    /**
     * Adds <code>error</code>, possibly annotated using any current
     * contextual information.
     *
     * @param error An error which will <code>toString()</code>
     * nicely.
     * @see #setParseContext(Object)
     */
    public boolean add(Object error)
    {
        if (errors == null)
        {
            errors = new ArrayList();
        }
        if (parseContext != null)
        {
            // Format error as an object which toString()'s nicely
            // using any applicable contextual state.
            error = '[' + parseContext.toString() + "] " + error;
        }
        return errors.add(error);
    }

    /**
     * Pushes contextual information onto {@link #parseContext} to
     * help identify exactly where errors occur, empowering users to
     * resolve any data formatting problems.
     *
     * @param parseContext The current parse context.  A value of
     * <code>null</code> indicates that any current state should be
     * cleared.
     */
    public void setParseContext(Object parseContext)
    {
        this.parseContext = parseContext;
    }
}
