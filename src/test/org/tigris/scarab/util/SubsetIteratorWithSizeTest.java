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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * used for testing org.tigris.scarab.util.SubsetIterator
 * 
 * @author <a href="mailto:sebastian.dietrich@anecon.com">Sebastian Dietrich</a>
 */
public class SubsetIteratorWithSizeTest extends SubsetIteratorTest
{
    protected static final String FIRST = "first element";
    protected static final String SECOND = "second element";
    protected static final String THIRD = "third element";
    protected static final String FOURTH = "fourth element";
    protected static final String LAST = "last element";

    protected static final int SIZE = 2;
    protected static final int OFFSET = 2;


    public SubsetIteratorWithSizeTest(String testName)
    {
        super(testName);
    }
    /**
     * A Mock-IteratorWithSize to test SubsetIteratorWithSize with
     */
    class MockIteratorWithSize implements IteratorWithSize
    {
        private Collection coll;
        private Iterator it;

        public MockIteratorWithSize(Collection coll)
        {
            this.coll = coll;
            this.it = coll.iterator();
        }

        /* (non-Javadoc)
         * @see org.tigris.scarab.util.IteratorWithSize#size()
         */
        public int size()
        {
            return coll.size();
        }

        /* (non-Javadoc)
        * @see java.util.Iterator#remove()
        */
        public void remove()
        {
            it.remove();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            return it.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next()
        {
            return it.next();
        }

    }

    public void setUp()
    {
        Collection coll = new ArrayList();

        coll.add(FIRST);
        coll.add(SECOND);
        coll.add(THIRD);
        coll.add(FOURTH);
        coll.add(LAST);

        // a SubsetIteratorWithSize from "third element" to "fourth element"
        subsetIterator =
            new SubsetIteratorWithSize(
                new MockIteratorWithSize(coll),
                OFFSET,
                SIZE);

        // a SubsetIterator from "third element" to the last element
        subsetIteratorUntilLast =
            new SubsetIteratorWithSize(new MockIteratorWithSize(coll), OFFSET);
    }

    public void tearDown()
    {
        subsetIterator = null;
    }

    public void testConstructor()
    {
        assertEquals(
            "Could not fetch third element",
            subsetIterator.next(),
            THIRD);
        assertEquals(
            "SubsetIterator has wrong size",
            SIZE,
            ((SubsetIteratorWithSize) subsetIterator).size());

        assertTrue(
            "Filled collection should have next element",
            subsetIterator.hasNext());

        subsetIterator.remove(); // remove "third element"
        assertEquals(
            "Could not fetch fourth element",
            subsetIterator.next(),
            FOURTH);
        assertFalse(
            "SubsetIterator on last position should not have next element",
            subsetIterator.hasNext());
    }

    public void testConstructorWithoutElements()
    {
        assertEquals(
            "Could not fetch third element",
            subsetIteratorUntilLast.next(),
            THIRD);
        assertEquals(
            "SubsetIterator has wrong size",
            3,
            ((SubsetIteratorWithSize) subsetIteratorUntilLast).size());

        assertTrue(
            "Filled collection should have next element",
            subsetIteratorUntilLast.hasNext());

        subsetIteratorUntilLast.remove(); // remove "third element"
        assertEquals(
            "Could not fetch fourth element",
            subsetIteratorUntilLast.next(),
            FOURTH);
        assertEquals(
            "Could not fetch last element",
            subsetIteratorUntilLast.next(),
            LAST);
        assertFalse(
            "SubsetIterator on last position should not have next element",
            subsetIteratorUntilLast.hasNext());
    }

    public void testConstructorWithEmptyIteratorWithSize()
    {
        SubsetIteratorWithSize i =
            new SubsetIteratorWithSize(IteratorWithSize.EMPTY, 0, 0);

        assertFalse(
            "Iterator on empty should not have next element",
            i.hasNext());

        try
        {
            i.next();
            fail("Iterator on empty should raise an exception on next()");
        }
        catch (NoSuchElementException e)
        {
            // that's what we expect
        }
        catch (Exception e)
        {
            fail(
                "Iterator on empty should raise NoSuchElementException on next() and not "
                    + e.getClass().getName());
        }

        try
        {
            i.remove();
            fail("Iterator on empty should raise an exception on remove()");
        }
        catch (IllegalStateException e)
        {
            // that's what we expect
        }
        catch (Exception e)
        {
            fail(
                "Iterator on empty should raise IllegalStateException on remove() and not "
                    + e.getClass().getName());
        }
    }

    public void testConstructorWithBiggerSubsetThanTheOriginal()
    {
        Collection coll = new ArrayList();

        coll.add(FIRST);
        coll.add(SECOND);
        coll.add(THIRD);
        coll.add(FOURTH);
        coll.add(LAST);

        // a SubsetIteratorWithSize from "third element" to "fourth element"
        subsetIterator =
            new SubsetIteratorWithSize(
                new MockIteratorWithSize(coll),
                OFFSET,
                100);
        assertEquals(
            "SubsetIterator has wrong size",
            3,
            ((SubsetIteratorWithSize) subsetIterator).size());

        assertEquals(
            "Could not fetch third element",
            subsetIterator.next(),
            THIRD);

        assertTrue(
            "Filled collection should have next element",
            subsetIterator.hasNext());
        assertEquals(
            "Could not fetch fourth element",
            subsetIterator.next(),
            FOURTH);
        assertEquals(
            "Could not fetch last element",
            subsetIterator.next(),
            LAST);
        assertFalse(
            "SubsetIterator on last position should not have next element",
            subsetIterator.hasNext());
    }

    public void testSize()
    {
        assertEquals(
            "size() didn't work",
            SIZE,
            ((SubsetIteratorWithSize) subsetIterator).size());
    }

}
