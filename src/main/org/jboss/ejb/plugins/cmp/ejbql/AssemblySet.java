package org.jboss.ejb.plugins.cmp.ejbql;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AssemblySet implements Set {
	private Set set = new HashSet();
	
	public AssemblySet() {
	}

	public AssemblySet(Collection c) {
		addAll(c);
	}

	public boolean add(Object o) {
		checkType(o);
		return set.add(o);
	}

	public boolean addAll(Collection c) {
		boolean changed = false;
		for(Iterator i = c.iterator(); i.hasNext(); ) {
			Object o = i.next();
			checkType(o);
			Assembly copy = new Assembly((Assembly)o);
			changed = set.add(copy) || changed;			
		}
		return changed;
	}

	public void clear() {
		set.clear();
	}

	public boolean contains(Object o) {
		return set.contains(o);
	}
	
	public boolean containsAll(Collection c) {
		return set.containsAll(c);
	}

	public boolean equals(Object o) {
		if(o instanceof AssemblySet) {
			return ((AssemblySet)o).set.equals(set);
		}
		return false;
	}

	public int hashCode() {
		return set.hashCode();
	}

	public boolean isEmpty() {
		return set.isEmpty();
	}

	public Iterator iterator() {
		return set.iterator();
	}

	public boolean remove(Object o) {
		return set.remove(o);
	}

	public boolean removeAll(Collection c) {
		return set.removeAll(c);
	}

	public boolean retainAll(Collection c) {
		return set.retainAll(c);
	}

	public int size() {
		return set.size();
	}

	public Object[] toArray() {
		return set.toArray();
	}

	public Object[] toArray(Object a[]) {
		return set.toArray(a);
	}
	
	public String toString() {
		return set.toString();
	}

	private void checkType(Object o) {
		if(!(o instanceof Assembly)) {
			throw new IllegalArgumentException("Object must be an instance of Assebly");
		}
	}
}
