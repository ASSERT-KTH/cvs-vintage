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

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class WindowIterator 
    implements Iterator
{
    public static final WindowIterator EMPTY = new EmptyWindowIterator();
    private static final Object NOT_INITIALIZED = new Object();

    private final Iterator i;
    private final Object[] window;
    private final int bsize;
    private final int fsize;
    private final int size;
    private Boolean hasNext;
    
    /**
     *
     */
    public WindowIterator(Iterator i, int backSize, int forwardSize)
    {
        this.i = i;
        this.fsize = Math.abs(forwardSize);
        this.bsize = Math.abs(backSize);
        size = fsize + bsize + 1;
        window = new Object[size];

        for (int m = 0; m < size; m++) 
        {
            window[m] = NOT_INITIALIZED;
        }
    }

    public boolean hasNext()
    {
        if (hasNext == null) 
        {
            hasNext = (internalIterate()) 
                ? Boolean.TRUE : Boolean.FALSE;
        }
        return hasNext.booleanValue();
    }

    boolean firstCall = true;
    /*
    int whatsLeft;
    boolean internalIteratorHadNext = true;
    private boolean internalIterate()
    {
        for (int i = 1; i < size; i++) 
        {
            window[i-1] = window[i];
        }
        window[size-1] = NOT_INITIALIZED;

        boolean hasNext;
        if (internalIteratorHadNext) 
        {
            hasNext = i.hasNext();
            internalIteratorHadNext = hasNext; 
            if (hasNext) 
            {
                if (firstCall) 
                {
                    for (m = 0; m <= fsize && internalIteratorHadNext; m++) 
                    {
                        window[bsize + m] = i.next();
                        internalIteratorHadNext = i.hasNext();
                        whatsLeft++;
                    }                    
                    firstCall = false;
                }         
                else 
                {
                    window[size-1] = i.next();
                }
            }
        }
        else 
        {
            hasNext = (whatsLeft > 0);
            whatsLeft--;
        }
        
        return hasNext;
    }
    */

    private boolean internalIterate()
    {
        if (firstCall) 
        {
            for (int m = 0; m <= fsize && i.hasNext(); m++) 
            {
                window[bsize + m] = i.next();
            }                    
            firstCall = false;
        }         
        else 
        {
            for (int i = 1; i < size; i++) 
            {
                window[i-1] = window[i];
            }

            if (i.hasNext()) 
            {
                //System.out.println("WindowIterator: i.hasNext = true");
                window[size-1] = i.next();                
            }
            else
            {
                //System.out.println("WindowIterator: i.hasNext = false");
                window[size-1] = NOT_INITIALIZED;            
            }
        }
        
        return window[bsize] != NOT_INITIALIZED;
    }

    public Object next()
    {
        if (hasNext()) 
        {
            hasNext = null;
            return window[bsize];
        }
        else 
        {
            throw new NoSuchElementException("Iterator is exhausted"); //EXCEPTION
        }
    }


    public void remove()
    {
        throw new UnsupportedOperationException("'remove' is not implemented"); //EXCEPTION
    }

    /**
     * Allows retrieving a given element some distance relative to the element
     * last returned from next(). 
     */
    public Object get(int i)
    {
        if (i < 0 && (-1 * i) > bsize) 
        {
            throw new ArrayIndexOutOfBoundsException("window was only defined "
                + bsize + " in the negative direction. Argument was " + i); //EXCEPTION
        }

        if (i > 0 && i > fsize) 
        {
            throw new ArrayIndexOutOfBoundsException("window was only defined "
                + fsize + " in the positive direction. Argument was " + i); //EXCEPTION
        }
        
        return window[bsize + i];
    }
    
    public boolean hasValue(int i)
    {
        return get(i) != NOT_INITIALIZED; 
    }
}

class EmptyWindowIterator extends WindowIterator
{
    EmptyWindowIterator()
    {
        super(Collections.EMPTY_SET.iterator(), 0, 0);
    }

    public boolean hasNext()
    {
        return false;
    }
    
    public Object next()
    {
        throw new NoSuchElementException("This is an empty list."); //EXCEPTION
    }
    
    public void remove()
    {
        throw new IllegalStateException("next() will throw exception, it is "
                                        + "not possible to call this method."); //EXCEPTION
    }
    
    public Object get(int i)
    {
        throw new NoSuchElementException("This is an empty list."); //EXCEPTION
    }

    public boolean hasValue(int i)
    {
        return false;
    }
}
