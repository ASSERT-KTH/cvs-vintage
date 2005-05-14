/**
 * Copyright (c) 2004 Red Hat, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * Component of: Red Hat Application Server
 *
 * Initial Developers: Rafael H. Schloming
 */
package org.objectweb.carol.irmi;

/**
 * The Pair class is essentially an ordered set of two elements. This
 * class is useful for creating {@link Map}s with compound keys,
 * and/or {@link Set}s with compound elements.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

class Pair {

    /**
     * The first element of the pair.
     */
    private Object one;

    /**
     * The second element of the pair.
     */
    private Object two;

    /**
     * Construct a new Pair object with the given first and second element.
     *
     * @param one the first element of the pair
     * @param two the second element of the pair
     */

    Pair(Object one, Object two) {
        this.one = one;
        this.two = two;
    }

    /**
     * Construct a new Pair object with the given first and second element.
     *
     * @param one the first element of the pair
     * @param two the second element of the pair
     */

    Pair(Object one, int two) {
        this(one, new Integer(two));
    }

    /**
     * Construct a new Pair object with the given first and second element.
     *
     * @param one the first element of the pair
     * @param two the second element of the pair
     */

    Pair(int one, Object two) {
        this(new Integer(one), two);
    }

    /**
     * Returns the first element of this Pair.
     *
     * @return the first element of this Pair
     */
    public Object getOne() {
        return one;
    }

    /**
     * Returns the second element of this Pair.
     *
     * @return the second element of this Pair
     */
    public Object getTwo() {
        return two;
    }

    /**
     * Returns the hash code for this Pair instance. The algorithm
     * used to compute the hash code is the same algorithm specified
     * by the {@link List#hashCode()} interface.
     *
     * @return the hash code of this Pair
     */

    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + one.hashCode();
        hash = hash * 31 + two.hashCode();
        return hash;
    }

    /**
     * Compares this Pair to the given Object. Two Pair instances are
     * equal iff their first elements are equal and their second
     * elements are equal.
     *
     * @param o the object to test for equality
     * @return true iff <var>o</var> is a Pair instance whos first and
     * second elements equal this Pairs first and second elements
     */

    public boolean equals(Object o) {
        if (o instanceof Pair) {
            Pair p = (Pair) o;
            return one.equals(p.one) && two.equals(p.two);
        } else {
            return false;
        }
    }

}
