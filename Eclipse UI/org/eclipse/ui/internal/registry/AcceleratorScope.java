package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import java.util.HashMap;

import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.internal.*;

/**
 * An accelerator scope is a range in which a given accelerator (a mapping
 * between an accelerator key and an action id) is available.
 * A scope may represent a view, editor, a page of a multi-page editor, etc.
 * An accelerator is available when the part represented by its scope is active.
 */
public class AcceleratorScope {
	private String id;
	private String name;
	private String description;
	private String parentScopeString;
	private AcceleratorScope parentScope;
	private AcceleratorConfiguration configuration;
	
	private HashMap defIdToAccelerator = new HashMap();
	private HashMap defaultActionToAccelerator = new HashMap();
	private HashMap defaultAcceleratorToAction = new HashMap();
	
	private static AcceleratorMode currentMode;
	private static AcceleratorMode defaultMode;
	private static KeyBindingService currentService;
	/**
	 * Create an instance of AcceleratorScope and initializes 
	 * it with its id, name, description and parent scope.
	 */			
	public AcceleratorScope(String id, String name, String description, String parentScope) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.parentScopeString = parentScope;
		if(parentScope==null)
			this.parentScopeString = IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID;	
	}
	/**
	 * Return this scope's id
	 */
	public String getId() {
		return id;	
	}
	/**
	 * Return this scope's name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Return this scope's description
	 */
	public String getDescription() {
		return description;	
	}
	/**
	 * Register and action with its accelerator. This action is
	 * used only if there is no other action using this accelerator
	 * defined in XML.
	 */
	public void registerAction(int accelerator,String actionDefId) {
		if(accelerator == 0)
			return;
		defaultAcceleratorToAction.put(new Integer(accelerator),new AcceleratorAction(actionDefId));
		defaultActionToAccelerator.put(actionDefId,new Accelerator(actionDefId,accelerator));
	}
	/**
	 * Return the accelerator for this scope.
	 */
	public Accelerator getAccelerator(String defId) {
		Accelerator result = (Accelerator)defIdToAccelerator.get(defId);
		if(result == null)
			result = (Accelerator)defaultActionToAccelerator.get(defId);
		return result;
	}
	/**
	 * Returns the defition id of the action registered for the
	 * specified accelerator;
	 */
	public String getDefinitionId(int accelerator[]) {
		if(accelerator == null)
			return null;
		AcceleratorMode mode = defaultMode;
		for (int i = 0; i < accelerator.length; i++) {
			AcceleratorAction action = mode.getAction(accelerator[i]);
			if(action == null)
				return null;
			if(action.isMode())
				mode = (AcceleratorMode)action;
			else
				return action.getId();
		}
		return null;
	}
	/**
	 * Returns the parent scope of the current scope. For example, if the current
	 * scope is that of a page of a multi-page editor, the parent scope would be
	 * the scope of the editor.
	 */
	public AcceleratorScope getParentScope() {
		if(id.equals(IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID))
			return null;
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		if(parentScope ==  null) {
			parentScope = registry.getScope(parentScopeString);
			if(parentScope ==  null) 
				parentScope = registry.getScope(IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID);
		}
		return parentScope;
	}
	/**
	 * Reset the current mode
	 */
	public static void resetMode(KeyBindingService service) {
		/*
		 * Avoid clearing the status line when in the default mode.
		 * This is a temporary fix to avoid clearing any status message
		 * that might be from the action that was run.
		 * 
		 * This fix does not cover the case when we are in a different
		 * mode and running an action. Currently we do not have API to
		 * tell if the status line message has been modified by the action
		 * that was run. 
		 */
		if (currentMode != defaultMode) {
			currentMode = defaultMode;
			if(getStatusLineManager(service)!=null)
				getStatusLineManager(service).setMessage("");	 //$NON-NLS-1$
			service.updateAccelerators(defaultMode == currentMode);				
		}

	}
	/**
	 * Set the current mode and service
	 */
	private static void setCurrentMode(final KeyBindingService service,AcceleratorMode mode) {
		if(currentMode != mode) {
			currentMode = mode;
			service.updateAccelerators(defaultMode == currentMode);
		}
	}
	/**
	 * Verify if the current mode was set with this service. Reset the mode
	 * if not.
	 */
	private static void verifyService(KeyBindingService service) {
		if(service == currentService)
			return;
		currentService = service;
		resetMode(service);
	}
	/**
	 * Initialize this scope with all accelerators defined in the default 
	 * configuration and then override with the ones defined in the active
	 * configuration. Fists initialize with the accelerators defined in the
	 * parent scope and then override with the ones defined in this scope.
	 */
	public void initializeAccelerators(AcceleratorConfiguration configuration) {
		this.configuration = configuration;
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		defaultMode = new AcceleratorMode();
		currentMode = defaultMode;
		initializeAccelerators(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID,defaultMode,registry,defIdToAccelerator);
		if(!IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID.equals(configuration.getId()))
			initializeAccelerators(configuration.getId(),defaultMode,registry,defIdToAccelerator);
	}
	/**
	 * Recurcive initialize this scope. First initialize its parents
	 * and then override with the accelerators defined in this scope.
	 */	
	private void initializeAccelerators(String configId,AcceleratorMode mode,AcceleratorRegistry registry,HashMap defIdToAcc) {
		AcceleratorScope parent = getParentScope();
		if(parent != null)
			parent.initializeAccelerators(configId,mode,registry,defIdToAcc);
		Accelerator accelerators[] = registry.getAccelerators(configId,getId());
		for (int i = 0; i < accelerators.length; i++) {
			int accKeys[][] = accelerators[i].getAccelerators();
			defIdToAcc.put(accelerators[i].getId(),accelerators[i]);
			for (int j = 0; j < accKeys.length; j++) {
				AcceleratorMode childMode = mode;
				for (int k = 0; k < accKeys[j].length - 1; k++) {
					AcceleratorAction a = childMode.getAction(accKeys[j][k]);
					if ((a == null) || (!a.isMode())) {
						AcceleratorMode newMode = new AcceleratorMode();
						childMode.addAction(accKeys[j][k], newMode);
						childMode = newMode;
					} else {
						childMode = (AcceleratorMode) a;
					}
				}
				childMode.addAction(accKeys[j][accKeys[j].length - 1], new AcceleratorAction(accelerators[i].getId()));
			}
		}
	}
	/*
	 * Returns true if the event has only modifier otherwise return
	 * false
	 */
	private boolean isModifierOnly(KeyEvent event) {
    	if (event.character != 0)
    		return false;
    		
    	switch (event.keyCode) {
			case SWT.CONTROL:
			case SWT.ALT:
			case SWT.SHIFT:
 				return true;
    	}
    	return false;
    }
    /**
     * Finds the action of mode for the accelerator and returns its id.
     */
	public String getActionDefinitionId(int acc) {
		AcceleratorAction a = currentMode.getAction(acc);
		if(a == null)
			a = (AcceleratorAction)defaultAcceleratorToAction.get(new Integer(acc));
		if(a == null)
			return null;
		return a.getId();
	}
    
    /**
     * Finds the action of mode for the accelerator and runs it.
     */
	public boolean processKey(KeyBindingService service, Event e, int acc) {
		verifyService(service);
		AcceleratorAction a = currentMode.getAction(acc);
		if(a == null) {
			a = (AcceleratorAction)defaultAcceleratorToAction.get(new Integer(acc));
			resetMode(service);	
		}
		if(a == null) {
			if(currentMode == defaultMode)
				return service.processEditorAction(e,acc);
			resetMode(service);
			return true;
		}
		a.run(service,e,acc);
		return true;
	}
	/**
	 * Returns all accelerators for the current mode.
	 */
	public int[] getAccelerators() {
		Integer modeAccs[] = currentMode.getAccelerators();
		int result[] = new int[modeAccs.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = modeAccs[i].intValue();
		}
		return result;
	}
	/*
	 * Returns the status line manager for the current window.
	 */
	private static IStatusLineManager getStatusLineManager(KeyBindingService service) {
		WorkbenchWindow window = (WorkbenchWindow)service.getWindow();
		if (window != null)
			return window.getActionBars().getStatusLineManager();
		return null;
	}
	/**
	 * Adapter for an IAction.
	 */
	public static class AcceleratorAction {
		String id;
		AcceleratorAction(String defId) {
			id = defId;
		}
		public String getId() {
			return id;
		}
		public boolean isMode() {
			return false;
		}
		public void run(KeyBindingService service,Event e,int acc) {
			IAction a = service.getAction(id);
			if((a != null) && (a.isEnabled())) {
				a.runWithEvent(e);
			}
			resetMode(service);
		}
	}

	/**
	 * Adapter for a Mode.
	 * 
	 * A mode represents a particular state of use in which the user has already
 	 * pressed a sequence (possibly of length one) of accelerator keys which matches
	 * the beginning of a valid sequence of accelerator keys which will cause an
	 * action to occur.
	 * <p>
	 * When a mode is active, the next accelerator key to be pressed is processed
	 * in the context of the active mode. If it matches the next key in any valid
	 * sequence, a new mode will become the active mode. If the next key presses
	 * is invalid, the active mode becomes inactive and the user is returned to a 
	 * modeless (defaultMode) state.
	 * </p>
	 */
	public static class AcceleratorMode extends AcceleratorAction {	
		private static String previousMessage = ""; //$NON-NLS-1$
			
		private HashMap acceleratorToAction = new HashMap();
		
		AcceleratorMode() {
			super(null);
		}
		public boolean isMode() {
			return true;
		}	
		public void run(KeyBindingService service,Event e,int acc) {
			if(e != null)
				setStatusLineMessage(service, acc);
			setCurrentMode(service,this);
		}
		/*
		 * Displays the appropriate message for the current mode on the status
		 * line.
		 */
		private void setStatusLineMessage(KeyBindingService service, int acc) {
			String keyString = Action.convertAccelerator(acc);
			if(currentMode==defaultMode) {
				getStatusLineManager(service).setMessage(keyString);
				previousMessage = keyString;
			} else {
				getStatusLineManager(service).setMessage(previousMessage+" "+keyString); //$NON-NLS-1$
				previousMessage = previousMessage+" "+keyString; //$NON-NLS-1$
			}		
		}
		public AcceleratorAction getAction(int keyCode) {
			return (AcceleratorAction)acceleratorToAction.get(new Integer(keyCode));	
		}
		public void addAction(int keyCode,AcceleratorAction acc) {
			acceleratorToAction.put(new Integer(keyCode),acc);
		}
		public Integer[] getAccelerators() {
			Set keys = acceleratorToAction.keySet();
			Integer result[] = new Integer[keys.size()];
			return (Integer[])keys.toArray(result);
		}
	}
}
