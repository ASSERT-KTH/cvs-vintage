/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;

/**
 * The LightweightDecoratorManager is a decorator manager
 * that encapsulates the behavior for the lightweight decorators.
 */
class LightweightDecoratorManager {

	/**
	 * The runnable is the object used to run the decorations
	 * so that an error in someones decorator will not kill the thread.
	 * It is implemented here to prevent aborting of decoration
	 * i.e. successful decorations will still be applied.
	 */

	private class LightweightRunnable implements ISafeRunnable {
		private Object element;
		private DecorationBuilder decoration;
		private LightweightDecoratorDefinition decorator;

		void setValues(
			Object object,
			DecorationBuilder builder,
			LightweightDecoratorDefinition definition) {
			element = object;
			decoration = builder;
			decorator = definition;

		}
		/*
		 * @see ISafeRunnable.handleException(Throwable).
		 */
		public void handleException(Throwable exception) {
			IStatus status = StatusUtil.newStatus(IStatus.ERROR,exception.getMessage(),exception);
			WorkbenchPlugin.log("Exception in Decorator",status); //$NON-NLS-1$
		}
		/*
		 * @see ISafeRunnable.run
		 */
		public void run() throws Exception {
			decorator.decorate(element, decoration);
		}
	}

	private LightweightRunnable runnable = new LightweightRunnable();

	//The cachedDecorators are a 1-many mapping of type to full decorator.
	private HashMap cachedLightweightDecorators = new HashMap();

	//The lightweight definitionsread from the registry
	private LightweightDecoratorDefinition[] lightweightDefinitions;

	private static final LightweightDecoratorDefinition[] EMPTY_LIGHTWEIGHT_DEF =
		new LightweightDecoratorDefinition[0];

	private OverlayCache overlayCache = new OverlayCache();

	LightweightDecoratorManager(LightweightDecoratorDefinition[] definitions) {
		super();
		lightweightDefinitions = definitions;
	}

	/**
	 * Get the lightweight definitions for the receiver.
	 * @return LightweightDecoratorDefinition[]
	 */
	LightweightDecoratorDefinition[] getDefinitions() {
		return lightweightDefinitions;
	}

	/**
	 * Return the enabled lightweight decorator definitions.
	 * @return LightweightDecoratorDefinition[]
	 */
	LightweightDecoratorDefinition[] enabledDefinitions() {
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

	/**
	 * Reset any cached values.
	 */
	void reset() {
		cachedLightweightDecorators = new HashMap();
	}

	/**
	* Shutdown the decorator manager by disabling all
	* of the decorators so that dispose() will be called
	* on them.
	*/
	void shutdown() {
		//Disable all fo the enabled decorators 
		//so as to force a dispose of thier decorators
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].isEnabled())
				lightweightDefinitions[i].setEnabled(false);
		}
		overlayCache.disposeAll();
	}

	/**
	 * Get the LightweightDecoratorDefinition with the supplied id
	 * @return LightweightDecoratorDefinition or <code>null</code> if it is not found
	 * @param decoratorId String
	 */
	LightweightDecoratorDefinition getDecoratorDefinition(String decoratorId) {
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].getId().equals(decoratorId))
				return lightweightDefinitions[i];
		}
		return null;
	}

	/**
	* Get the lightweight  registered for elements of this type.
	*/
	LightweightDecoratorDefinition[] getDecoratorsFor(Object element) {

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
			DecoratorManager.getDecoratorsFor(element, enabledDefinitions());

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
	* Fill the decoration with all of the results of the 
	* decorators.
	* 
	* @param element The source element
	* @param decoration. The DecorationResult we are working on.
	* @param adaptableDecoration. If it is true only apply the decorators
	*  where adaptable is true.
	*/

	void getDecorations(
		Object element,
		DecorationBuilder decoration,
		boolean adaptableDecoration) {

		LightweightDecoratorDefinition[] decorators = getDecoratorsFor(element);

		for (int i = 0; i < decorators.length; i++) {
			//If we are doing the adaptable one make sure we are
			//only applying the adaptable decorations
			if(adaptableDecoration && !decorators[i].isAdaptable())
				continue;
			if (decorators[i].getEnablement().isEnabledFor(element)) {
				decoration.setCurrentDefinition(decorators[i]);
				decorate(element, decoration, decorators[i]);
			}
		}
	}

	/**
	 * Decorate the element receiver in a SafeRunnable.
	 * @param element. The Object to be decorated
	 * @param decoration. The object building decorations.
	 * @param decorator. The decorator being applied.
	 */
	private void decorate(
		Object element,
		DecorationBuilder decoration,
		LightweightDecoratorDefinition decorator) {

		runnable.setValues(element, decoration, decorator);
		Platform.run(runnable);

	}
	/**
	 * Returns the overlayCache.
	 * @return OverlayCache
	 */
	OverlayCache getOverlayCache() {
		return overlayCache;
	}

}
