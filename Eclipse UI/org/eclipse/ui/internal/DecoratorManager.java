package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.IDecoratorManager;

/**
 * The DecoratorManager is the class that handles all of the
 * decorators defined in the image.
 * 
 * @since 2.0
 */
public class DecoratorManager
	implements
		ILabelDecorator,
		ILabelProviderListener,
		IDecoratorManager {

	//Hold onto the list of listeners to be told if a change has occured
	private HashSet listeners = new HashSet();

	//The cachedDecorators are a 1-many mapping of type to decorator.
	private HashMap cachedDecorators = new HashMap();

	//The definitions are definitions read from the registry
	private DecoratorDefinition[] definitions;

	private static final DecoratorDefinition[] EMPTY_DEF =
		new DecoratorDefinition[0];

	private final String PREFERENCE_SEPARATOR = ","; //$NON-NLS-1$
	private final String VALUE_SEPARATOR = ":"; //$NON-NLS-1$
	private final String P_TRUE = "true"; //$NON-NLS-1$
	private final String P_FALSE = "false"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver and load the
	 * settings from the installed plug-ins.
	 */
	public DecoratorManager() {
		DecoratorRegistryReader reader = new DecoratorRegistryReader();
		Collection values = reader.readRegistry(Platform.getPluginRegistry());
		definitions = new DecoratorDefinition[values.size()];
		values.toArray(definitions);
	}

	/**
	 * Restore the stored values from the preference
	 * store and register the receiver as a listener
	 * for all of the enabled decorators.
	 */

	public void restoreListeners() {
		applyDecoratorsPreference();
		for (int i = 0; i < definitions.length; i++) {
			//Add a listener if it is an enabled option
			if (definitions[i].isEnabled())
				definitions[i].addListener(this);
		}
	}

	/**
	 * Add the listener to the list of listeners.
	 */
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove the listener from the list.
	 */
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Inform all of the listeners that require an update
	 */
	private void fireListeners(LabelProviderChangedEvent event) {
		Iterator iterator = listeners.iterator();
		while (iterator.hasNext()) {
			ILabelProviderListener listener = (ILabelProviderListener) iterator.next();
			listener.labelProviderChanged(event);
		}
	}

	/**
	 * Decorate the image provided for the element type.
	 * Then look for an IResource that adapts to it an apply
	 * all of the adaptable decorators.
	 * @return String or null if there are none defined for this type.
	 * @param Image
	 * @param Object
	 */
	public String decorateText(String text, Object element) {
		
		DecoratorDefinition[] decorators = getDecoratorsFor(element);
		String result = text;
		for (int i = 0; i < decorators.length; i++) {
			String newResult = decorators[i].decorateText(result, element);
			if (newResult != null)
				result = newResult;
		}

		//Get any adaptions to IResource
		Object adapted = getResourceAdapter(element);
		if (adapted != null){
			DecoratorDefinition[] adaptedDecorators = getDecoratorsFor(adapted);
			for (int i = 0; i < adaptedDecorators.length; i++) {
				if(adaptedDecorators[i].isAdaptable()){
					String newResult = adaptedDecorators[i].decorateText(result, adapted);
					if (newResult != null)
						result = newResult;
				}
			}
		}
		
		return result;
	}

	/**
	 * Decorate the image provided for the element type.
	 * Then look for an IResource that adapts to it an apply
	 * all of the adaptable decorators.
	 * @return Image or null if there are none defined for this type.
	 * @param Image
	 * @param Object
	 */
	public Image decorateImage(Image image, Object element) {
		
		DecoratorDefinition[] decorators = getDecoratorsFor(element);
		Image result = image;
		for (int i = 0; i < decorators.length; i++) {
			Image newResult = decorators[i].decorateImage(result, element);
			if (newResult != null)
				result = newResult;
		}

		//Get any adaptions to IResource
		Object adapted = getResourceAdapter(element);
		if (adapted != null){
			DecoratorDefinition[] adaptedDecorators = getDecoratorsFor(adapted);
			for (int i = 0; i < adaptedDecorators.length; i++) {
				if(adaptedDecorators[i].isAdaptable()){
					Image newResult = adaptedDecorators[i].decorateImage(result, adapted);
					if (newResult != null)
						result = newResult;
				}
			}
		}
		
		return result;
	}

	/**
	 * Get the resource adapted object for the supplied
	 * element. Return null if there isn't one.
	 */
	private Object getResourceAdapter(Object element) {

		//Get any adaptions to IResource
		if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			Object resourceAdapter =
				adaptable.getAdapter(IContributorResourceAdapter.class);
			if (resourceAdapter == null)
				resourceAdapter = DefaultContributorResourceAdapter.getDefault();

			Object adapted =
				((IContributorResourceAdapter) resourceAdapter).getAdaptedResource(adaptable);
			if (adapted != element)
				return adapted; //Avoid applying decorator twice
		}
		return null;
	}

	/**
	 * Get the decoratordefinitionss registered for elements of this type.
	 * If there is one return it. If not search for one first
	 * via superclasses and then via interfaces.
	 * If still nothing is found then add in a decorator that
	 * does nothing.
	 */
	private DecoratorDefinition[] getDecoratorsFor(Object element) {

		if (element == null)
			return EMPTY_DEF;

		Class elementClass = element.getClass();
		String className = elementClass.getName();
		DecoratorDefinition[] decoratorArray =
			(DecoratorDefinition[]) cachedDecorators.get(className);
		if (decoratorArray != null) {
			return decoratorArray;
		}

		List allClasses = computeClassOrder(elementClass);
		ArrayList decorators = new ArrayList();
		DecoratorDefinition[] enabledDefinitions = enabledDefinitions();

		findDecorators(allClasses, enabledDefinitions, decorators);

		findDecorators(
			computeInterfaceOrder(allClasses),
			enabledDefinitions,
			decorators);

		decoratorArray = new DecoratorDefinition[decorators.size()];
		decorators.toArray(decoratorArray);
		cachedDecorators.put(element.getClass().getName(), decoratorArray);
		return decoratorArray;
	}

	/** 
	 * Find a defined decorators that have a type that is the same
	 * as one of the classes. 
	 */
	private void findDecorators(
		Collection classList,
		DecoratorDefinition[] enabledDefinitions,
		ArrayList result) {

		Iterator classes = classList.iterator();
		while (classes.hasNext()) {
			String className = ((Class) classes.next()).getName();
			for (int i = 0; i < enabledDefinitions.length; i++) {
				if (className.equals(enabledDefinitions[i].getObjectClass()))
					result.add(enabledDefinitions[i]);
			}
		}

	}

	/**
	* Return whether or not the decorator registered for element
	* has a label property called property name.
	*/
	public boolean isLabelProperty(Object element, String property) {
		return isLabelProperty(element, property, true);
	}

	/**
	* Return whether or not the decorator registered for element
	* has a label property called property name.
	* Check for an adapted resource if checkAdapted is true.
	*/
	public boolean isLabelProperty(
		Object element,
		String property,
		boolean checkAdapted) {
		DecoratorDefinition[] decorators = getDecoratorsFor(element);
		for (int i = 0; i < decorators.length; i++) {
			if (decorators[i].isLabelProperty(element, property))
				return true;
		}

		if (checkAdapted) {
			//Get any adaptions to IResource
			Object adapted = getResourceAdapter(element);
			if (adapted != null && adapted != element) {
				if (isLabelProperty(adapted, property, false))
					return true;
			}
		}

		return false;
	}

	/**
	* Returns the class search order starting with <code>extensibleClass</code>.
	* The search order is defined in this class' comment.
	*/
	private Vector computeClassOrder(Class extensibleClass) {
		Vector result = new Vector(4);
		Class clazz = extensibleClass;
		while (clazz != null) {
			result.addElement(clazz);
			clazz = clazz.getSuperclass();
		}
		return result;
	}
	/**
	 * Returns the interface search order for the class hierarchy described
	 * by <code>classList</code>.
	 * The search order is defined in this class' comment.
	 */
	private List computeInterfaceOrder(List classList) {
		List result = new ArrayList(4);
		Map seen = new HashMap(4);
		for (Iterator list = classList.iterator(); list.hasNext();) {
			Class[] interfaces = ((Class) list.next()).getInterfaces();
			internalComputeInterfaceOrder(interfaces, result, seen);
		}
		return result;
	}

	/**
	 * Add interface Class objects to the result list based
	 * on the class hierarchy. Interfaces will be searched
	 * based on their position in the result list.
	 */
	private void internalComputeInterfaceOrder(
		Class[] interfaces,
		List result,
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
			internalComputeInterfaceOrder(
				((Class) newList.next()).getInterfaces(),
				result,
				seen);
	}

	/**
	 * Return the enabled decorator definitions
	 */
	private DecoratorDefinition[] enabledDefinitions() {
		ArrayList result = new ArrayList();
		for (int i = 0; i < definitions.length; i++) {
			if (definitions[i].isEnabled())
				result.add(definitions[i]);
		}
		DecoratorDefinition[] returnArray = new DecoratorDefinition[result.size()];
		result.toArray(returnArray);
		return returnArray;
	}
	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		//Do nothing as this is not viewer dependant
	}

	/*
	 * @see IDecoratorManager.reset()
	 */
	public void reset() {
		cachedDecorators = new HashMap();
		fireListeners(new LabelProviderChangedEvent(this));
		writeDecoratorsPreference();
	}

	/**
	 * Get the DecoratorDefinitions defined on the receiver.
	 */
	public DecoratorDefinition[] getDecoratorDefinitions() {
		return definitions;
	}

	/*
	 * @see ILabelProviderListener#labelProviderChanged(LabelProviderChangedEvent)
	 */
	public void labelProviderChanged(LabelProviderChangedEvent event) {
		fireListeners(event);
	}

	/**
	 * Store the currently enabled decorators in
	 * preference store.
	 */
	private void writeDecoratorsPreference() {
		StringBuffer enabledIds = new StringBuffer();
		for (int i = 0; i < definitions.length; i++) {
			enabledIds.append(definitions[i].getId());
			enabledIds.append(VALUE_SEPARATOR);
			if (definitions[i].isEnabled())
				enabledIds.append(P_TRUE);
			else
				enabledIds.append(P_FALSE);

			enabledIds.append(PREFERENCE_SEPARATOR);
		}

		WorkbenchPlugin.getDefault().getPreferenceStore().setValue(
			IPreferenceConstants.ENABLED_DECORATORS,
			enabledIds.toString());
	}

	/**
	 * Get the currently enabled decorators in
	 * preference store and set the state of the
	 * current definitions accordingly.
	 */
	private void applyDecoratorsPreference() {

		String preferenceValue =
			WorkbenchPlugin.getDefault().getPreferenceStore().getString(
				IPreferenceConstants.ENABLED_DECORATORS);

		StringTokenizer tokenizer =
			new StringTokenizer(preferenceValue, PREFERENCE_SEPARATOR);
		Set enabledIds = new HashSet();
		Set disabledIds = new HashSet();
		while (tokenizer.hasMoreTokens()) {
			String nextValuePair = tokenizer.nextToken();

			//Strip out the true or false to get the id
			String id = nextValuePair.substring(0, nextValuePair.indexOf(VALUE_SEPARATOR));
			if (nextValuePair.endsWith(P_TRUE))
				enabledIds.add(id);
			else
				disabledIds.add(id);
		}

		for (int i = 0; i < definitions.length; i++) {
			String id = definitions[i].getId();
			if (enabledIds.contains(id))
				definitions[i].setEnabledWithErrorHandling(true);
			else {
				if (disabledIds.contains(id))
					definitions[i].setEnabledWithErrorHandling(false);
			}
		}

	}

	/**
	 * Shutdown the decorator manager by disabling all
	 * of the decorators so that dispose() will be called
	 * on them.
	 */
	public void shutdown() {
		//Disable all fo the enabled decorators 
		//so as to force a dispose of thier decorators
		for (int i = 0; i < definitions.length; i++) {
			if (definitions[i].isEnabled())
				definitions[i].setEnabledWithErrorHandling(false);
		}
	}
	/**
	 * @see IDecoratorManager#getEnabled(String)
	 */
	public boolean getEnabled(String decoratorId) {
		DecoratorDefinition definition = getDecoratorDefinition(decoratorId);
		if (definition == null)
			return false;
		else
			return definition.isEnabled();
	}

	/**
	 * @see IDecoratorManager#getLabelDecorator()
	 */
	public ILabelDecorator getLabelDecorator() {
		return this;
	}

	/**
	 * @see IDecoratorManager#setEnabled(String, boolean)
	 */
	public void setEnabled(String decoratorId, boolean enabled) throws CoreException{
		DecoratorDefinition definition = getDecoratorDefinition(decoratorId);
		if (definition != null)
			definition.setEnabled(enabled);
	}
	
	/**
	 * @see IDecoratorManager#getLabelDecorator(String)
	 */
	public ILabelDecorator getLabelDecorator(String decoratorId) {
		DecoratorDefinition definition = 
			getDecoratorDefinition(decoratorId);
			
		//Do not return for a disabled decorator
		if(definition.isEnabled()){
			try{
				return definition.getDecorator();
			}
			catch (CoreException exception){
				//Cannot be thrown - remove when API is updated
			}
		}
		return null;
	}

	/**
	 * Get the DecoratorDefinition with the supplied id
	 * @return DecoratorDefinition or <code>null</code> if it is not found
	 * @param decoratorId String
	 */
	private DecoratorDefinition getDecoratorDefinition(String decoratorId) {
		for (int i = 0; i < definitions.length; i++) {
			if (definitions[i].getId().equals(decoratorId))
				return definitions[i];
		}
		return null;
	}
}