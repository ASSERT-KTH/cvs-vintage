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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * used for testing org.tigris.scarab.util.SubsetIterator
 * 
 * @author <a href="mailto:sebastian.dietrich@anecon.com">Sebastian Dietrich</a>
 */
public class SubsetIteratorTest extends TestCase
{
 protected SubsetIterator subsetIterator;
 protected SubsetIterator subsetIteratorUntilLast;
 
 protected static final String FIRST = "first element";
 protected static final String SECOND = "second element";
 protected static final String THIRD = "third element";
 protected static final String FOURTH = "fourth element";
 protected static final String LAST = "last element";
 
 public SubsetIteratorTest(String testName)
 {
	 super(testName);
 }

 public void setUp() 
 {
   Collection coll = new ArrayList();
   
   coll.add(FIRST);
   coll.add(SECOND);
   coll.add(THIRD);
   coll.add(FOURTH);
   coll.add(LAST);
   
   // a SubsetIterator from "third element" to "fourth element"
   subsetIterator = new SubsetIterator(coll.iterator(), 2, 2);
   
   // a SubsetIterator from "third element" to the last element
   subsetIteratorUntilLast = new SubsetIterator(coll.iterator(), 2);
 }
 
 public void tearDown()
 {
   subsetIterator = null;
 }

 public void testConstructor()
 {
   assertEquals("Could not fetch third element", subsetIterator.next(), THIRD);
   
   assertTrue("Filled collection should have next element", subsetIterator.hasNext());
   
   subsetIterator.remove();  // remove "third element"
   assertEquals("Could not fetch fourth element", subsetIterator.next(), FOURTH);
   assertFalse("SubsetIterator on last position should not have next element", subsetIterator.hasNext());
 }

 public void testConstructorWithoutElements()
 {
   assertEquals("Could not fetch third element", subsetIteratorUntilLast.next(), THIRD);
   
   assertTrue("Filled collection should have next element", subsetIteratorUntilLast.hasNext());
   
   subsetIteratorUntilLast.remove(); // remove "third element"
   assertEquals("Could not fetch fourth element", subsetIteratorUntilLast.next(), FOURTH);
   assertEquals("Could not fetch last element", subsetIteratorUntilLast.next(), LAST);
   assertFalse("SubsetIterator on last position should not have next element", subsetIteratorUntilLast.hasNext());
 }
 
 public void testConstructorWithEmptyCollection()
 {
   Collection coll = new ArrayList();
   SubsetIterator i = new SubsetIterator(coll.iterator(), 0, 0);
   
   assertFalse("Empty collection should not have next element", i.hasNext());
   
   try {
     i.next();
     fail("Empty collection should raise an exception on next()");
   } catch (NoSuchElementException e) {
     // that's what we expect
   } catch (Exception e) {
     fail("Empty collection should raise NoSuchElementException on next() and not " + e.getClass().getName());
   }
   
   try {
     i.remove();
     fail("Empty collection should raise an exception on remove()");
   } catch (IllegalStateException e) {
     // that's what we expect
   } catch (Exception e) {
     fail("Empty collection should raise IllegalStateException on remove() and not " + e.getClass().getName());
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
   
   // a SubsetIterator from "third element" to the end
   subsetIterator = new SubsetIterator(coll.iterator(), 2, 100);
   
   assertEquals("Could not fetch third element", subsetIterator.next(), THIRD);
   
   assertTrue("Filled collection should have next element", subsetIterator.hasNext());
   assertEquals("Could not fetch fourth element", subsetIterator.next(), FOURTH);
   assertEquals("Could not fetch last element", subsetIterator.next(), LAST);
   assertFalse("SubsetIterator on last position should not have next element", subsetIterator.hasNext());  
 }
 
 public void testHasNext() 
 {
   assertTrue("Before first element hasNext() should be true", subsetIterator.hasNext());
   subsetIterator.next();
   assertTrue("On first element hasNext() should be true", subsetIterator.hasNext());
   subsetIterator.next();
   assertFalse("On last element hasNext() should be false", subsetIterator.hasNext());
 }

 public void testNext() 
 {
   assertEquals("Could not fetch third element", subsetIterator.next(), THIRD);
   assertEquals("Could not fetch fourth element", subsetIterator.next(), FOURTH);
   try
   {
     subsetIterator.next();
     fail("next() on last element should raise an exception");
   } catch (NoSuchElementException e) {
     // that's what we expect
   } catch (Exception e) {
     fail("next() on last element should raise NoSuchElementException and not " + 
         e.getClass().getName());
   }
 }

 public void testRemove() 
 {
   try 
   {
     subsetIterator.remove();
     fail("remove() before first element should raise an exception");
   } catch (IllegalStateException e) {
     // that's what we expect
   } catch (Exception e) {
     fail("remove() before first element should raise IllegalStateException and not " + 
         e.getClass().getName());
   }
   
   subsetIterator.next();
   subsetIterator.remove();
   assertTrue("Before first element hasNext() should be true", subsetIterator.hasNext());
   subsetIterator.next();
   assertFalse("On last element hasNext() should be false", subsetIterator.hasNext());
   subsetIterator.remove();
 }

}
