package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.*;
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
	implements ILabelDecorator, ILabelProviderListener, IDecoratorManager {

	//Hold onto the list of listeners to be told if a change has occured
	private HashSet listeners = new HashSet();

	private OverlayCache overlayCache = new OverlayCache();

	//The cachedDecorators are a 1-many mapping of type to full decorator.
	private HashMap cachedFullDecorators = new HashMap();

	//The cachedDecorators are a 1-many mapping of type to full decorator.
	private HashMap cachedLightweightDecorators = new HashMap();

	//The full definitions read from the registry
	private FullDecoratorDefinition[] fullDefinitions;

	//The lightweight definitionsread from the registry
	private LightweightDecoratorDefinition[] lightweightDefinitions;

	private static final FullDecoratorDefinition[] EMPTY_FULL_DEF =
		new FullDecoratorDefinition[0];

	private static final LightweightDecoratorDefinition[] EMPTY_LIGHTWEIGHT_DEF =
		new LightweightDecoratorDefinition[0];

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

		ArrayList full = new ArrayList();
		ArrayList lightweight = new ArrayList();
		Iterator allDefinitions = values.iterator();
		while (allDefinitions.hasNext()) {
			DecoratorDefinition nextDefinition =
				(DecoratorDefinition) allDefinitions.next();
			if (nextDefinition.isFull())
				full.add(nextDefinition);
			else
				lightweight.add(nextDefinition);
		}

		fullDefinitions = new FullDecoratorDefinition[full.size()];
		full.toArray(fullDefinitions);

		lightweightDefinitions =
			new LightweightDecoratorDefinition[lightweight.size()];
		lightweight.toArray(lightweightDefinitions);
	}

	/**
	 * Restore the stored values from the preference
	 * store and register the receiver as a listener
	 * for all of the enabled decorators.
	 */

	public void restoreListeners() {
		applyDecoratorsPreference();
		for (int i = 0; i < fullDefinitions.length; i++) {
			//Add a listener if it is an enabled option
			if (fullDefinitions[i].isEnabled())
				fullDefinitions[i].addListener(this);
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
			ILabelProviderListener listener =
				(ILabelProviderListener) iterator.next();
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

		//Get any adaptions to IResource
		Object adapted = getResourceAdapter(element);
		String result = decorateWithText(text, element, adapted);
		FullDecoratorDefinition[] decorators = getFullDecoratorsFor(element);
		for (int i = 0; i < decorators.length; i++) {
			if (decorators[i].getEnablement().isEnabledFor(element)) {
				String newResult = decorators[i].decorateText(result, element);
				if (newResult != null)
					result = newResult;
			}
		}

		if (adapted != null) {
			decorators = getFullDecoratorsFor(adapted);
			for (int i = 0; i < decorators.length; i++) {
				if (decorators[i].isAdaptable()
					&& decorators[i].getEnablement().isEnabledFor(adapted)) {
					String newResult =
						decorators[i].decorateText(result, adapted);
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

		Object adapted = getResourceAdapter(element);
		Image result = decorateWithOverlays(image, element, adapted);
		FullDecoratorDefinition[] decorators = getFullDecoratorsFor(element);

		for (int i = 0; i < decorators.length; i++) {
			if (decorators[i].getEnablement().isEnabledFor(element)) {
				Image newResult = decorators[i].decorateImage(result, element);
				if (newResult != null)
					result = newResult;
			}
		}

		//Get any adaptions to IResource

		if (adapted != null) {
			decorators = getFullDecoratorsFor(adapted);
			for (int i = 0; i < decorators.length; i++) {
				if (decorators[i].isAdaptable()
					&& decorators[i].getEnablement().isEnabledFor(adapted)) {
					Image newResult =
						decorators[i].decorateImage(result, adapted);
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
				resourceAdapter =
					DefaultContributorResourceAdapter.getDefault();

			Object adapted =
				(
					(
						IContributorResourceAdapter) resourceAdapter)
							.getAdaptedResource(
					adaptable);
			if (adapted != element)
				return adapted; //Avoid applying decorator twice
		}
		return null;
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
		boolean fullCheck =
			isLabelProperty(element, property, getFullDecoratorsFor(element));

		if (fullCheck)
			return fullCheck;

		boolean lightweightCheck =
			isLabelProperty(
				element,
				property,
				getLightweightDecoratorsFor(element));

		if (lightweightCheck)
			return true;

		if (checkAdapted) {
			//Get any adaptions to IResource
			Object adapted = getResourceAdapter(element);
			if (adapted == null || adapted == element)
				return false;

			fullCheck =
				isLabelProperty(
					adapted,
					property,
					getFullDecoratorsFor(adapted));
			if (fullCheck)
				return fullCheck;

			return isLabelProperty(
				adapted,
				property,
				getLightweightDecoratorsFor(adapted));
		}
		return false;
	}

	private boolean isLabelProperty(
		Object element,
		String property,
		DecoratorDefinition[] decorators) {
		for (int i = 0; i < decorators.length; i++) {
			if (decorators[i].getEnablement().isEnabledFor(element)
				&& decorators[i].isLabelProperty(element, property))
				return true;
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
	 * Return the enabled full decorator definitions.
	 * @return FullDecoratorDefinition[]
	 */
	private FullDecoratorDefinition[] enabledFullDefinitions() {
		ArrayList result = new ArrayList();
		for (int i = 0; i < fullDefinitions.length; i++) {
			if (fullDefinitions[i].isEnabled())
				result.add(fullDefinitions[i]);
		}
		FullDecoratorDefinition[] returnArray =
			new FullDecoratorDefinition[result.size()];
		result.toArray(returnArray);
		return returnArray;
	}

	/**
	 * Return the enabled lightweight decorator definitions.
	 * @return LightweightDecoratorDefinition[]
	 */
	private LightweightDecoratorDefinition[] enabledLightweightDefinitions() {
		ArrayList result = new ArrayList();
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].isEnabled())
				result.add(lightweightDefinitions[i]);
		}
		LightweightDecoratorDefinition[] returnArray =
			new LightweightDecoratorDefinition[result.size()];
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
		cachedFullDecorators = new HashMap();
		cachedLightweightDecorators = new HashMap();
		fireListeners(new LabelProviderChangedEvent(this));
		writeDecoratorsPreference();
	}

	/**
	 * Get the DecoratorDefinitions defined on the receiver.
	 */
	public DecoratorDefinition[] getAllDecoratorDefinitions() {
		DecoratorDefinition[] returnValue =
			new DecoratorDefinition[fullDefinitions.length
				+ lightweightDefinitions.length];
		System.arraycopy(
			fullDefinitions,
			0,
			returnValue,
			0,
			fullDefinitions.length);
		System.arraycopy(
			lightweightDefinitions,
			0,
			returnValue,
			fullDefinitions.length,
			lightweightDefinitions.length);
		return returnValue;
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
		writeDecoratorsPreference(enabledIds, fullDefinitions);
		writeDecoratorsPreference(enabledIds, lightweightDefinitions);

		WorkbenchPlugin.getDefault().getPreferenceStore().setValue(
			IPreferenceConstants.ENABLED_DECORATORS,
			enabledIds.toString());
	}

	private void writeDecoratorsPreference(
		StringBuffer enabledIds,
		DecoratorDefinition[] definitions) {
		for (int i = 0; i < definitions.length; i++) {
			enabledIds.append(definitions[i].getId());
			enabledIds.append(VALUE_SEPARATOR);
			if (definitions[i].isEnabled())
				enabledIds.append(P_TRUE);
			else
				enabledIds.append(P_FALSE);

			enabledIds.append(PREFERENCE_SEPARATOR);
		}
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
			String id =
				nextValuePair.substring(
					0,
					nextValuePair.indexOf(VALUE_SEPARATOR));
			if (nextValuePair.endsWith(P_TRUE))
				enabledIds.add(id);
			else
				disabledIds.add(id);
		}

		for (int i = 0; i < fullDefinitions.length; i++) {
			String id = fullDefinitions[i].getId();
			if (enabledIds.contains(id))
				fullDefinitions[i].setEnabledWithErrorHandling(true);
			else {
				if (disabledIds.contains(id))
					fullDefinitions[i].setEnabledWithErrorHandling(false);
			}
		}

		for (int i = 0; i < lightweightDefinitions.length; i++) {
			String id = lightweightDefinitions[i].getId();
			if (enabledIds.contains(id))
				lightweightDefinitions[i].setEnabledWithErrorHandling(true);
			else {
				if (disabledIds.contains(id))
					lightweightDefinitions[i].setEnabledWithErrorHandling(
						false);
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
		for (int i = 0; i < fullDefinitions.length; i++) {
			if (fullDefinitions[i].isEnabled())
				fullDefinitions[i].setEnabledWithErrorHandling(false);
		}
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].isEnabled())
				lightweightDefinitions[i].setEnabledWithErrorHandling(false);
		}
		overlayCache.disposeAll();
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
	public void setEnabled(String decoratorId, boolean enabled)
		throws CoreException {
		DecoratorDefinition definition = getDecoratorDefinition(decoratorId);
		if (definition != null)
			definition.setEnabled(enabled);
	}

	/**
	 * @see IDecoratorManager#getLabelDecorator(String)
	 */
	public ILabelDecorator getLabelDecorator(String decoratorId) {
		FullDecoratorDefinition definition =
			getFullDecoratorDefinition(decoratorId);

		//Do not return for a disabled decorator
		if (definition.isEnabled()) {
			try {
				return definition.getDecorator();
			} catch (CoreException exception) {
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
		DecoratorDefinition returnValue =
			getFullDecoratorDefinition(decoratorId);
		if (returnValue == null)
			return getLightweightDecoratorDefinition(decoratorId);
		else
			return returnValue;
	}

	/**
	 * Get the FullDecoratorDefinition with the supplied id
	 * @return FullDecoratorDefinition or <code>null</code> if it is not found
	 * @param decoratorId String
	 */
	private FullDecoratorDefinition getFullDecoratorDefinition(String decoratorId) {
		for (int i = 0; i < fullDefinitions.length; i++) {
			if (fullDefinitions[i].getId().equals(decoratorId))
				return fullDefinitions[i];
		}
		return null;
	}

	/**
	 * Get the LightweightDecoratorDefinition with the supplied id
	 * @return LightweightDecoratorDefinition or <code>null</code> if it is not found
	 * @param decoratorId String
	 */
	private LightweightDecoratorDefinition getLightweightDecoratorDefinition(String decoratorId) {
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].getId().equals(decoratorId))
				return lightweightDefinitions[i];
		}
		return null;
	}

	/**
	 * Get the full decorator definitions registered for elements of this type.
	 */
	private FullDecoratorDefinition[] getFullDecoratorsFor(Object element) {

		if (element == null)
			return EMPTY_FULL_DEF;

		String className = element.getClass().getName();
		FullDecoratorDefinition[] decoratorArray =
			(FullDecoratorDefinition[]) cachedFullDecorators.get(className);
		if (decoratorArray != null) {
			return decoratorArray;
		}

		Collection decorators =
			getDecoratorsFor(element, enabledFullDefinitions());

		if (decorators.size() == 0)
			decoratorArray = EMPTY_FULL_DEF;
		else {
			decoratorArray = new FullDecoratorDefinition[decorators.size()];
			decorators.toArray(decoratorArray);
		}

		cachedFullDecorators.put(className, decoratorArray);
		return decoratorArray;
	}

	/**
	 * Get the lightweight  registered for elements of this type.
	 */
	private LightweightDecoratorDefinition[] getLightweightDecoratorsFor(Object element) {

		if (element == null)
			return EMPTY_LIGHTWEIGHT_DEF;

		String className = element.getClass().getName();
		LightweightDecoratorDefinition[] decoratorArray =
			(LightweightDecoratorDefinition[]) cachedLightweightDecorators.get(
				className);
		if (decoratorArray != null) {
			return decoratorArray;
		}

		Collection decorators =
			getDecoratorsFor(element, enabledLightweightDefinitions());

		if (decorators.size() == 0)
			decoratorArray = EMPTY_LIGHTWEIGHT_DEF;
		else {
			decoratorArray =
				new LightweightDecoratorDefinition[decorators.size()];
			decorators.toArray(decoratorArray);
		}

		cachedLightweightDecorators.put(className, decoratorArray);
		return decoratorArray;
	}

	/**
	 * See if the supplied decorator cache has a value for the
	 * element. If not calculate it from the enabledDefinitions and
	 * update the cache.
	 * @return Collection of DecoratorDefinition.
	 * @param element. The element being tested.
	 * @param cachedDecorators. The cache for decorator lookup.
	 * @param enabledDefinitions. The definitions currently defined for this decorator.
	 */

	private Collection getDecoratorsFor(
		Object element,
		DecoratorDefinition[] enabledDefinitions) {

		String className = element.getClass().getName();

		ArrayList decorators = new ArrayList();

		for (int i = 0; i < enabledDefinitions.length; i++) {
			if (enabledDefinitions[i]
				.getEnablement()
				.isEnabledForExpression(
					element,
					ActionExpression.ATT_OBJECT_CLASS))
				decorators.add(enabledDefinitions[i]);
		}

		return decorators;

	}

	/**
	 * Decorate the Image supplied with the overlays for any of
	 * the enabled lightweight decorators. 
	 */
	private Image decorateWithOverlays(
		Image image,
		Object element,
		Object adapted) {

		LightweightDecoratorDefinition[] decorators =
			getLightweightDecoratorsFor(element);
		LightweightDecoratorDefinition[] adaptedDecorators;
		if (adapted == null)
			return overlayCache.getImageFor(image, element, decorators);
		else {
			adaptedDecorators = getLightweightDecoratorsFor(adapted);
			return overlayCache.getImageFor(
				image,
				element,
				decorators,
				adapted,
				adaptedDecorators);
		}
	}

	/**
	 * Decorate the String supplied with the prefixes and suffixes
	 * for the enabled lightweight decorators.
	 *  
	 */
	private String decorateWithText(
		String text,
		Object element,
		Object adapted) {

		LinkedList appliedDecorators = new LinkedList();
		LinkedList appliedAdaptedDecorators = new LinkedList();
		StringBuffer result = new StringBuffer();

		LightweightDecoratorDefinition[] decorators =
			getLightweightDecoratorsFor(element);

		for (int i = 0; i < decorators.length; i++) {
			if (decorators[i].getEnablement().isEnabledFor(element)) {
				//Add in reverse order for symmetry of suffixes
				appliedDecorators.addFirst(decorators[i]);
				result.append(decorators[i].getPrefix(element));
			}
		}

		if (adapted != null) {
			LightweightDecoratorDefinition[] adaptedDecorators =
				getLightweightDecoratorsFor(adapted);
			for (int i = 0; i < adaptedDecorators.length; i++) {
				if (adaptedDecorators[i]
					.getEnablement()
					.isEnabledFor(adapted)) {
					//Add in reverse order for symmetry of suffixes
					appliedAdaptedDecorators.addFirst(adaptedDecorators[i]);
					result.append(adaptedDecorators[i].getPrefix(adapted));
				}
			}
		}

		//Nothing happened so just return the text
		if(appliedDecorators.isEmpty() && appliedAdaptedDecorators.isEmpty())
			return text;
			
		result.append(text);

		if (adapted != null) {
			Iterator appliedIterator = appliedAdaptedDecorators.iterator();
			while (appliedIterator.hasNext()) {
				result.append(
					(
						(LightweightDecoratorDefinition) appliedIterator
							.next())
							.getSuffix(
						element));
			}
		}

		Iterator appliedIterator = appliedDecorators.iterator();
		while (appliedIterator.hasNext()) {
			result.append(
				(
					(LightweightDecoratorDefinition) appliedIterator
						.next())
						.getSuffix(
					element));
		}
		return result.toString();

	}
}