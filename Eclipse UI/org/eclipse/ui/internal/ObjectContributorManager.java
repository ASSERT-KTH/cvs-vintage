/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

/**
 * This class is a default implementation of <code>IObjectContributorManager</code>.
 * It provides fast merging of contributions with the following semantics:
 * <ul>
 * <li> All of the matching contributors will be invoked per property lookup
 * <li> The search order from a class with the definition<br>
 *			<code>class X extends Y implements A, B</code><br>
 *		is as follows:
 * 		<il>
 *			<li>the target's class: X
 *			<li>X's superclasses in order to <code>Object</code>
 *			<li>a depth-first traversal of the target class's interaces in the order 
 *				returned by <code>getInterfaces()</code> (in the example, A and 
 *				its superinterfaces then B and its superinterfaces)
 *		</il>
 * </ul>
 *
 * @see IObjectContributor
 */
public abstract class ObjectContributorManager implements IExtensionChangeHandler {
	
	/** 
	 * @since 3.1
	 */
	private class ContributorRecord {
		/**
		 * @param contributor
		 * @param targetType
		 */
		public ContributorRecord(IObjectContributor contributor, String targetType) {
			this.contributor = contributor;
			this.objectClassName = targetType;
		}
		
		String objectClassName;
		IObjectContributor contributor;
	}

    /** Table of contributors. */
    protected Map contributors;

    /** Cache of object class contributor search paths; <code>null</code> if none. */
    protected Map objectLookup;

    /** Cache of resource adapter class contributor search paths; <code>null</code> if none. */
    protected Map resourceAdapterLookup;
    
    /** Cache of adaptable class contributor search paths; <code>null</code> if none. */
    protected Map adaptableLookup;
    
    protected Set contributorRecordSet;

    /** 
     * Constructs a new contributor manager.
     */
    public ObjectContributorManager() {
        contributors = new Hashtable(5);
        contributorRecordSet = new HashSet(5);
        objectLookup = null;
        resourceAdapterLookup = null;
        adaptableLookup = null;
        IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
        tracker.registerHandler(this, null);
    }

    /**
     * Adds contributors for the given types to the result list.
     */
    private void addContributorsFor(List types, List result) {
        for (Iterator classes = types.iterator(); classes.hasNext();) {
            Class clazz = (Class) classes.next();
            List contributorList = (List) contributors.get(clazz.getName());
            if (contributorList != null)
                result.addAll(contributorList);
        }
    }

    /**
     * Returns the class search order starting with <code>extensibleClass</code>.
     * The search order is defined in this class' comment.
     */
    protected final List computeClassOrder(Class extensibleClass) {
        ArrayList result = new ArrayList(4);
        Class clazz = extensibleClass;
        while (clazz != null) {
            result.add(clazz);
            clazz = clazz.getSuperclass();
        }
        return result;
    }

    /**
     * Returns the interface search order for the class hierarchy described
     * by <code>classList</code>.
     * The search order is defined in this class' comment.
     */
    protected final List computeInterfaceOrder(List classList) {
        ArrayList result = new ArrayList(4);
        Map seen = new HashMap(4);
        for (Iterator list = classList.iterator(); list.hasNext();) {
            Class[] interfaces = ((Class) list.next()).getInterfaces();
            internalComputeInterfaceOrder(interfaces, result, seen);
        }
        return result;
    }

    /**
     * Flushes the cache of contributor search paths.  This is generally required
     * whenever a contributor is added or removed.  
     * <p>
     * It is likely easier to just toss the whole cache rather than trying to be
     * smart and remove only those entries affected.
     */
    public void flushLookup() {
        objectLookup = null;
        resourceAdapterLookup = null;
        adaptableLookup = null;
    }

    /**
     * Cache the real adapter class contributor search path.
     */
    private void cacheResourceAdapterLookup(Class adapterClass, List results) {
        if (resourceAdapterLookup == null)
            resourceAdapterLookup = new HashMap();
        resourceAdapterLookup.put(adapterClass, results);
    }
    
    /**
     * Cache the real adapter class contributor search path.
     */
    private void cacheAdaptableLookup(String adapterClass, List results) {
        if (adaptableLookup == null)
        	adaptableLookup = new HashMap();
        adaptableLookup.put(adapterClass, results);
    }

    /**
     * Cache the object class contributor search path.
     */
    private void cacheObjectLookup(Class objectClass, List results) {
        if (objectLookup == null)
            objectLookup = new HashMap();
        objectLookup.put(objectClass, results);
    }

    /**
     * Get the contributions registered to this manager.
     * 
     * @return an unmodifiable <code>Collection</code> containing all registered
     * contributions.  The objects in this <code>Collection</code> will be 
     * <code>List</code>s containing the actual contributions.
     * @since 3.0
     */
    public Collection getContributors() {
        return Collections.unmodifiableCollection(contributors.values());
    }

    /**
     * Return the list of contributors for the supplied class.
     */
    protected List addContributorsFor(Class objectClass) {

        List classList = computeClassOrder(objectClass);
        List result = new ArrayList();
        addContributorsFor(classList, result);
        classList = computeInterfaceOrder(classList); // interfaces
        addContributorsFor(classList, result);
        return result;
    }

    /**
     * Returns true if contributors exist in the manager for
     * this object and any of it's super classes, interfaces, or
     * adapters.
     * 
     * @param object the object to test
     * @return whether the object has contributors
     */
    public boolean hasContributorsFor(Object object) {

        List contributors = getContributors(object);
        return contributors.size() > 0;
    }

    /**
     * Add interface Class objects to the result list based
     * on the class hierarchy. Interfaces will be searched
     * based on their position in the result list.
     */
    private void internalComputeInterfaceOrder(Class[] interfaces, List result,
            Map seen) {
        List newInterfaces = new ArrayList(seen.size());
        for (int i = 0; i < interfaces.length; i++) {
            Class interfac = interfaces[i];
            if (seen.get(interfac) == null) {
                result.add(interfac);
                seen.put(interfac, interfac);
                newInterfaces.add(interfac);
            }
        }
        for (Iterator newList = newInterfaces.iterator(); newList.hasNext();)
            internalComputeInterfaceOrder(((Class) newList.next())
                    .getInterfaces(), result, seen);
    }

    /**
	 * Return whether the given contributor is applicable to all elements in the
	 * selection.
	 * 
	 * @param selection
	 *            the selection
	 * @param contributor
	 *            the contributor
	 * @return whether it is applicable
	 */
    public boolean isApplicableTo(IStructuredSelection selection,
            IObjectContributor contributor) {
        Iterator elements = selection.iterator();
        while (elements.hasNext()) {
            if (contributor.isApplicableTo(elements.next()) == false)
                return false;
        }
        return true;
    }

    /**
	 * Return whether the given contributor is applicable to all elements in the
	 * list.
	 * 
	 * @param list
	 *            the selection
	 * @param contributor
	 *            the contributor
	 * @return whether it is applicable
	 */

    public boolean isApplicableTo(List list, IObjectContributor contributor) {
        Iterator elements = list.iterator();
        while (elements.hasNext()) {
            if (contributor.isApplicableTo(elements.next()) == false)
                return false;
        }
        return true;
    }

    /**
     * Register a contributor.
     * 
     * @param contributor the contributor
     * @param targetType the target type
     */
    public void registerContributor(IObjectContributor contributor,
            String targetType) {
        List contributorList = (List) contributors.get(targetType);
        if (contributorList == null) {
            contributorList = new ArrayList(5);
            contributors.put(targetType, contributorList);
        }
        contributorList.add(contributor);
        flushLookup();
        
        //hook the object listener
        if (contributor instanceof IAdaptable) {
        	IConfigurationElement element = (IConfigurationElement) ((IAdaptable) contributor)
					.getAdapter(IConfigurationElement.class);
			if (element == null)
				return;
			ContributorRecord contributorRecord = new ContributorRecord(
					contributor, targetType);
			contributorRecordSet.add(contributorRecord);
			PlatformUI.getWorkbench().getExtensionTracker().registerObject(
					element.getDeclaringExtension(), contributorRecord,
					IExtensionTracker.REF_WEAK);
        }
    }

    /**
     * Unregister all contributors.
     */
    public void unregisterAllContributors() {
        contributors = new Hashtable(5);
        flushLookup();
    }

    /**
     * Unregister a contributor from the target type.
     * 
     * @param contributor the contributor
     * @param targetType the target type
     */
    public void unregisterContributor(IObjectContributor contributor,
            String targetType) {    	
        List contributorList = (List) contributors.get(targetType);
        if (contributorList == null)
            return;
        contributorList.remove(contributor);
        if (contributorList.isEmpty()) // if we've removed the last one clean out the list
        	contributors.remove(targetType);
        flushLookup();
    }


    /**
     * Unregister all contributors for the target type.
     * 
     * @param targetType the target type
     */
    public void unregisterContributors(String targetType) {
        contributors.remove(targetType);
        flushLookup();
    }
    
    protected List getContributors(Object object) {
    	// Determine is the object is a resource
    	Object resource  = LegacyResourceSupport.getAdaptedContributorResource(object);	
    	
    	// Fetch the unique adapters
    	List adapters = new ArrayList(Arrays.asList(Platform.getAdapterManager().computeAdapterTypes(object.getClass())));
    	removeCommonAdapters(adapters, Arrays.asList(new Class[] {object.getClass()}));
    	
    	// Calculate the contributors for this object class
        List contributors = getObjectContributors(object.getClass());
        // Calculate the contributors for resource classes
        if(resource != null)
        	contributors.addAll(getResourceContributors(resource.getClass()));
        // Calculate the contributors for each adapter type
    	if(adapters != null) {
    		for (Iterator it = adapters.iterator(); it.hasNext();) {
				String adapter = (String) it.next();				
				contributors.addAll(getAdaptableContributors(adapter));
			}
    	}    		
        return contributors;
    }
    
    /**
     * Returns the contributions for the given class. This considers
     * contributors on any super classes and interfaces.
     * 
     * @param objectClass the class to search for contributions.
     * @return the contributions for the given class. This considers
     * contributors on any super classes and interfaces.
     * 
     * @since 3.1
     */
    protected List getObjectContributors(Class objectClass) {
		List objectList = null;
		// Lookup the results in the cache first.
		if (objectLookup != null) {
			objectList = (List) objectLookup.get(objectClass);
		}
		if (objectList == null) {
			objectList = addContributorsFor(objectClass);
			if (objectList.size() == 0)
				objectList = Collections.EMPTY_LIST;
			cacheObjectLookup(objectClass, objectList);
		}
		// return a shallow copy of the contributors, ensure that the caller
		// cannot modify the cache directly.
		return new ArrayList(objectList);
	}

    /**
     * Returns the contributions for the given <code>IResource</code>class. 
     * This considers contributors on any super classes and interfaces. This
     * will only return contributions that are adaptable.
     * 
     * @param resourceClass the class to search for contributions.
     * @return the contributions for the given class. This considers
     * adaptable contributors on any super classes and interfaces.
     * 
     * @since 3.1
     */
	protected List getResourceContributors(Class resourceClass) {
		List resourceList = null;
		if (resourceAdapterLookup != null) {
			resourceList = (List) resourceAdapterLookup.get(resourceClass);
		}
		if (resourceList == null) {
			resourceList = addContributorsFor(resourceClass);
			if (resourceList.size() == 0)
				resourceList = Collections.EMPTY_LIST;
			else
				resourceList = filterOnlyAdaptableContributors(resourceList);
			cacheResourceAdapterLookup(resourceClass, resourceList);
		}
		// return a shallow copy of the contributors, ensure that the caller
		// cannot modify the cache directly.
		return new ArrayList(resourceList);
	}

    /**
     * Returns the contributions for the given type name. 
     * 
     * @param adapterType the class to search for contributions.
     * @return the contributions for the given class. This considers
     * contributors to this specific type.
     * 
     * @since 3.1
     */
	protected List getAdaptableContributors(String adapterType) {
		List adaptableList = null;
		// Lookup the results in the cache first, there are two caches
		// one that stores non-adapter contributions and the other
		// contains adapter contributions.
		if (adaptableLookup != null) {
			adaptableList = (List) adaptableLookup.get(adapterType);
		}
		if (adaptableList == null) {
			adaptableList = new ArrayList(contributors.size());
			// ignore resource adapters because these must be adapted via the
			// IContributorResourceAdapter.
			if (LegacyResourceSupport.isResourceType(adapterType) || LegacyResourceSupport.isResourceMappingType(adapterType))
				return Collections.EMPTY_LIST;
			adaptableList = (List) contributors.get(adapterType);
			if (adaptableList == null || adaptableList.size() == 0)
				adaptableList = Collections.EMPTY_LIST;
			else
				adaptableList = filterOnlyAdaptableContributors(adaptableList);
			cacheAdaptableLookup(adapterType, adaptableList);
		}
		// return a shallow copy of the contributors, ensure that the caller
		// cannot modify the cache directly.
		return new ArrayList(adaptableList);
	}
	
	/**
	 * Prunes from the list of adapters type names that are in the class
	 * search order of every class in <code>results</code>.  
	 * @param adapters
	 * @param results
	 * @since 3.1
	 */
	protected void removeCommonAdapters(List adapters, List results) {
    	for (Iterator it = results.iterator(); it.hasNext();) {
			Class clazz = ((Class) it.next());
			List commonTypes = computeCombinedOrder(clazz);
			for (Iterator it2 = commonTypes.iterator(); it2.hasNext();) {
				Class type = (Class) it2.next();
				adapters.remove(type.getName());	
			}				
		}
    }
	
	/**
     * Returns the class search order starting with <code>extensibleClass</code>.
     * The search order is defined in this class' comment.
     */
    protected List computeCombinedOrder(Class inputClass) {
        List result = new ArrayList(4);
        Class clazz = inputClass;
        while (clazz != null) {
            // add the class
            result.add(clazz);
            // add all the interfaces it implements
            Class[] interfaces = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                result.add(interfaces[i]);
            }
            // get the superclass
            clazz = clazz.getSuperclass();
        }
        return result;
    }

	private List filterOnlyAdaptableContributors(List contributors) {
		List adaptableContributors = null;
		for (Iterator it = contributors.iterator(); it.hasNext();) {
			IObjectContributor c = (IObjectContributor) it.next();
			if(c.canAdapt()) {
				if(adaptableContributors == null) {
					adaptableContributors = new ArrayList();
				}
				adaptableContributors.add(c);
			}
		}
		return adaptableContributors == null ? Collections.EMPTY_LIST : adaptableContributors;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
     */
    public void removeExtension(IExtension source, Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof ContributorRecord) {
                ContributorRecord contributorRecord = (ContributorRecord) objects[i];
                unregisterContributor((contributorRecord).contributor, (contributorRecord).objectClassName);
                contributorRecordSet.remove(contributorRecord);
            }
        }
    }

    /**
     * Remove listeners and dispose of this manager.
     * 
     * @since 3.1
     */
    public void dispose() {
    	PlatformUI.getWorkbench().getExtensionTracker().unregisterHandler(this);
    }	
}
