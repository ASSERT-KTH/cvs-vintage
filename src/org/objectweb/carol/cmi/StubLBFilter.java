/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
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
 */
package org.objectweb.carol.cmi;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author nieuviar
 *
 */
public class StubLBFilter extends HashSet {
    /**
     * 
     */
    public StubLBFilter() {
        super();
    }

    /**
     * @param c
     */
    public StubLBFilter(Collection c) {
        super(c);
    }

    /**
     * @param initialCapacity
     * @param loadFactor
     */
    public StubLBFilter(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * @param initialCapacity
     */
    public StubLBFilter(int initialCapacity) {
        super(initialCapacity);
    }
}
