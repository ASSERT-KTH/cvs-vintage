/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
activated and used by other components.
*/

package org.eclipse.ui.internal.actions.keybindings;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.actions.Action;
import org.eclipse.ui.internal.actions.Util;

final class DialogCustomize extends Dialog {

	private final static String ACTION_CONFLICT = Messages.getString("DialogCustomize.ActionConflict"); //$NON-NLS-1$
	private final static String ACTION_UNDEFINED = Messages.getString("DialogCustomize.ActionUndefined"); //$NON-NLS-1$
	private final static int DIFFERENCE_ADD = 0;	
	private final static int DIFFERENCE_CHANGE = 1;	
	private final static int DIFFERENCE_MINUS = 2;	
	private final static int DIFFERENCE_NONE = 3;	
	private final static Image IMAGE_CHANGE = ImageFactory.getImage("change"); //$NON-NLS-1$
	private final static Image IMAGE_MINUS = ImageFactory.getImage("minus"); //$NON-NLS-1$
	private final static Image IMAGE_PLUS = ImageFactory.getImage("plus"); //$NON-NLS-1$
	private final static RGB RGB_CONFLICT = new RGB(255, 0, 0);
	private final static RGB RGB_CONFLICT_MINUS = new RGB(255, 192, 192);
	private final static RGB RGB_MINUS =	new RGB(192, 192, 192);
	private final static int SPACE = 8;	
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	private final class ActionRecord {

		String actionId;
		KeySequence keySequence;
		String scopeId;
		String configurationId;
		Set customSet;
		Set defaultSet;

		boolean customConflict = false;
		String customActionId = null;
		boolean defaultConflict = false;
		String defaultActionId = null;	

		void calculate() {
			if (customSet.size() > 1)
				customConflict = true;
			else if (!customSet.isEmpty())				
				customActionId = (String) customSet.iterator().next();
	
			if (defaultSet.size() > 1)
				defaultConflict = true;
			else if (!defaultSet.isEmpty())				
				defaultActionId = (String) defaultSet.iterator().next();
		}
	}

	private final class KeySequenceRecord {

		String scopeId;
		String configurationId;
		Set customSet;
		Set defaultSet;

		boolean customConflict = false;
		String customActionId = null;
		boolean defaultConflict = false;
		String defaultActionId = null;	

		void calculate() {
			if (customSet.size() > 1)
				customConflict = true;
			else if (!customSet.isEmpty())				
				customActionId = (String) customSet.iterator().next();
	
			if (defaultSet.size() > 1)
				defaultConflict = true;
			else if (!defaultSet.isEmpty())				
				defaultActionId = (String) defaultSet.iterator().next();
		}
	}

	private String defaultConfigurationId;
	private String defaultScopeId;
	private SortedSet preferenceBindingSet;	
	
	private KeyManager keyManager;
	private KeyMachine keyMachine;
	private SortedMap registryActionMap;
	private SortedSet registryBindingSet;
	private SortedMap registryConfigurationMap;
	private SortedMap registryScopeMap;
	
	private List actions;
	private List configurations;
	private List scopes;

	private String[] actionNames;
	private String[] configurationNames;	
	private String[] scopeNames;

	private Label labelAction;
	private Combo comboAction;
	private Table tableAction;
	//private Button buttonDetails;	
	private Label labelKeySequence;
	private Combo comboKeySequence;
	private Table tableKeySequence;
	//private Button buttonBrowseSelectedAction;
	private Group groupState;
	private Label labelScope; 
	private Combo comboScope;
	private Label labelConfiguration; 
	private Combo comboConfiguration;
	private Group groupAction;
	private Button buttonDefault;
	private Text textDefault;
	private Button buttonCustom; 
	private Combo comboCustom;

	private SortedMap tree;
	private Map nameToKeySequenceMap;
	private List actionRecords = new ArrayList();	
	private List keySequenceRecords = new ArrayList();

	public DialogCustomize(Shell parentShell, String defaultConfigurationId, String defaultScopeId, SortedSet preferenceBindingSet)
		throws IllegalArgumentException {
		super(parentShell);
		
		if (defaultConfigurationId == null || defaultScopeId == null || preferenceBindingSet == null)
			throw new IllegalArgumentException();
			
		this.defaultConfigurationId = defaultConfigurationId;
		this.defaultScopeId = defaultScopeId;
		preferenceBindingSet = new TreeSet(preferenceBindingSet);
		Iterator iterator = preferenceBindingSet.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof Binding))
				throw new IllegalArgumentException();
	
		this.preferenceBindingSet = preferenceBindingSet;

		keyManager = KeyManager.getInstance();
		keyMachine = keyManager.getKeyMachine();

		registryActionMap = org.eclipse.ui.internal.actions.Registry.getInstance().getActionMap();
		actions = new ArrayList();
		actions.addAll(registryActionMap.values());
		Collections.sort(actions, Action.nameComparator());				
	
		registryBindingSet = keyManager.getRegistryBindingSet();
		
		registryConfigurationMap = keyManager.getRegistryConfigurationMap();
		configurations = new ArrayList();
		configurations.addAll(registryConfigurationMap.values());	
		Collections.sort(configurations, Configuration.nameComparator());				
		
		registryScopeMap = keyManager.getRegistryScopeMap();	
		scopes = new ArrayList();
		scopes.addAll(registryScopeMap.values());	
		Collections.sort(scopes, Scope.nameComparator());				

		actionNames = new String[1 + actions.size()];
		actionNames[0] = ACTION_UNDEFINED;
		
		for (int i = 0; i < actions.size(); i++)
			actionNames[i + 1] = ((Action) actions.get(i)).getLabel().getName();

		configurationNames = new String[configurations.size()];
		
		for (int i = 0; i < configurations.size(); i++)
			configurationNames[i] = ((Configuration) configurations.get(i)).getLabel().getName();

		scopeNames = new String[scopes.size()];
		
		for (int i = 0; i < scopes.size(); i++)
			scopeNames[i] = ((Scope) scopes.get(i)).getLabel().getName();
		
		tree = new TreeMap();
		SortedSet bindingSet = new TreeSet();
		bindingSet.addAll(preferenceBindingSet);
		bindingSet.addAll(registryBindingSet);
		iterator = bindingSet.iterator();
		
		while (iterator.hasNext()) {
			Binding binding = (Binding) iterator.next();				
			set(tree, binding, false);			
		}

		nameToKeySequenceMap = new HashMap();	
		Collection keySequences = tree.keySet();
		iterator = keySequences.iterator();

		while (iterator.hasNext()) {
			KeySequence keySequence = (KeySequence) iterator.next();
			String name = keyManager.getTextForKeySequence(keySequence);
			
			if (!nameToKeySequenceMap.containsKey(name))
				nameToKeySequenceMap.put(name, keySequence);
		}

		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	public SortedSet getPreferenceBindingSet() {
		return Collections.unmodifiableSortedSet(preferenceBindingSet);	
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.getString("DialogCustomize.Title")); //$NON-NLS-1$
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		createUI(composite);
		return composite;		
	}	

	protected void okPressed() {
		preferenceBindingSet = solve(tree);
		super.okPressed();
	}

	private void buildActionRecords(SortedMap tree, String actionId, List actionRecords) {
		if (actionRecords != null) {
			actionRecords.clear();
				
			if (tree != null) {
				Iterator iterator = tree.entrySet().iterator();
					
				while (iterator.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator.next();
					KeySequence keySequence = (KeySequence) entry.getKey();					
					Map scopeMap = (Map) entry.getValue();						
		
					if (scopeMap != null) {
						Iterator iterator2 = scopeMap.entrySet().iterator();
						
						while (iterator2.hasNext()) {
							Map.Entry entry2 = (Map.Entry) iterator2.next();
							String scopeId = (String) entry2.getKey();										
							Map configurationMap = (Map) entry2.getValue();						
							Iterator iterator3 = configurationMap.entrySet().iterator();
										
							while (iterator3.hasNext()) {
								Map.Entry entry3 = (Map.Entry) iterator3.next();
								String configurationId = (String) entry3.getKey();					
								Map pluginMap = (Map) entry3.getValue();													
								Set customSet = new HashSet();
								Set defaultSet = new HashSet();						
								buildPluginSets(pluginMap, customSet, defaultSet);

								if (customSet.contains(actionId) || defaultSet.contains(actionId)) {
									ActionRecord actionRecord = new ActionRecord();
									actionRecord.actionId = actionId;
									actionRecord.keySequence = keySequence;
									actionRecord.scopeId = scopeId;
									actionRecord.configurationId = configurationId;
									actionRecord.customSet = customSet;
									actionRecord.defaultSet = defaultSet;
									actionRecord.calculate();	
									actionRecords.add(actionRecord);									
								}
							}
						}
					}
				}												
			}	
		}
	}
	
	private void buildKeySequenceRecords(SortedMap tree, KeySequence keySequence, List keySequenceRecords) {
		if (keySequenceRecords != null) {
			keySequenceRecords.clear();
			
			if (tree != null && keySequence != null) {
				Map scopeMap = (Map) tree.get(keySequence);
			
				if (scopeMap != null) {
					Iterator iterator = scopeMap.entrySet().iterator();
			
					while (iterator.hasNext()) {
						Map.Entry entry = (Map.Entry) iterator.next();
						String scopeId2 = (String) entry.getKey();					
						Map configurationMap = (Map) entry.getValue();						
						Iterator iterator2 = configurationMap.entrySet().iterator();
							
						while (iterator2.hasNext()) {
							Map.Entry entry2 = (Map.Entry) iterator2.next();
							String configurationId2 = (String) entry2.getKey();					
							Map pluginMap = (Map) entry2.getValue();			
							KeySequenceRecord keySequenceRecord = new KeySequenceRecord();
							keySequenceRecord.scopeId = scopeId2;
							keySequenceRecord.configurationId = configurationId2;							
							keySequenceRecord.customSet = new HashSet();
							keySequenceRecord.defaultSet = new HashSet();						
							buildPluginSets(pluginMap, keySequenceRecord.customSet, keySequenceRecord.defaultSet);			
							keySequenceRecord.calculate();
							keySequenceRecords.add(keySequenceRecord);
						}												
					}	
				}								
			}			
		}
	}

	private void buildPluginSets(Map pluginMap, Set customSet, Set defaultSet) {
		Iterator iterator = pluginMap.entrySet().iterator(); 

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String pluginId = (String) entry.getKey();
			Map actionMap = (Map) entry.getValue();
			Iterator iterator2 = actionMap.keySet().iterator();
	
			while (iterator2.hasNext()) {
				String actionId = (String) iterator2.next();
		
				if (pluginId == null)
					customSet.add(actionId);
				else 
					defaultSet.add(actionId);									
			}
		}
	}

	private void buildTableAction() {
		tableAction.removeAll();

		for (int i = 0; i < actionRecords.size(); i++) {
			ActionRecord actionRecord = (ActionRecord) actionRecords.get(i);
			Set customSet = actionRecord.customSet;
			Set defaultSet = actionRecord.defaultSet;
			int difference = DIFFERENCE_NONE;
			String actionId = null;
			boolean actionConflict = false;
			String alternateActionId = null;
			boolean alternateActionConflict = false;
	
			if (customSet.isEmpty()) {
				if (defaultSet.contains(actionRecord.actionId)) {												
					actionId = actionRecord.actionId;
					actionConflict = actionRecord.defaultConflict;					
				}
			} else {
				if (defaultSet.isEmpty()) {									
					if (customSet.contains(actionRecord.actionId)) {													
						difference = DIFFERENCE_ADD;
						actionId = actionRecord.actionId;
						actionConflict = actionRecord.customConflict;
					}
				} else {
					if (customSet.contains(actionRecord.actionId)) {
						difference = DIFFERENCE_CHANGE;
						actionId = actionRecord.actionId;
						actionConflict = actionRecord.customConflict;		
						alternateActionId = actionRecord.defaultActionId;
						alternateActionConflict = actionRecord.defaultConflict;
					} else {
						if (defaultSet.contains(actionRecord.actionId)) {	
							difference = DIFFERENCE_MINUS;
							actionId = actionRecord.actionId;
							actionConflict = actionRecord.defaultConflict;		
							alternateActionId = actionRecord.customActionId;
							alternateActionConflict = actionRecord.customConflict;
						}
					}
				}								
			}

			TableItem tableItem = new TableItem(tableAction, SWT.NULL);					

			switch (difference) {
				case DIFFERENCE_ADD:
					tableItem.setImage(0, IMAGE_PLUS);
					break;

				case DIFFERENCE_CHANGE:
					tableItem.setImage(0, IMAGE_CHANGE);
					break;

				case DIFFERENCE_MINUS:
					tableItem.setImage(0, IMAGE_MINUS);
					break;

				case DIFFERENCE_NONE:
					break;				
			}

			boolean conflict = actionConflict || alternateActionConflict;
			StringBuffer stringBuffer = new StringBuffer();

			if (actionRecord.keySequence != null)
				stringBuffer.append(keyManager.getTextForKeySequence(actionRecord.keySequence));

			if (actionConflict)
				stringBuffer.append(" " + ACTION_CONFLICT);

			if (difference == DIFFERENCE_CHANGE) {
				stringBuffer.append(" (was: ");
				String alternateActionName = null;
				
				if (alternateActionId == null) 
					alternateActionName = ACTION_UNDEFINED;
				else {
					Action action = (Action) registryActionMap.get(alternateActionId);
					
					if (action != null)
						alternateActionName = action.getLabel().getName();
					else
						alternateActionName = "[" + alternateActionId + "]";
				}
								
				stringBuffer.append(alternateActionName);

				if (alternateActionConflict)
					stringBuffer.append(" " + ACTION_CONFLICT);

				stringBuffer.append(')');
			} else if (difference == DIFFERENCE_MINUS) {
				stringBuffer.append(" (now: ");
				
				String alternateActionName = null;
				
				if (alternateActionId == null) 
					alternateActionName = ACTION_UNDEFINED;
				else {
					Action action = (Action) registryActionMap.get(alternateActionId);
					
					if (action != null)
						alternateActionName = action.getLabel().getName();
					else
						alternateActionName = "[" + alternateActionId + "]";
				}
								
				stringBuffer.append(alternateActionName);
				
				if (alternateActionConflict)
					stringBuffer.append(" " + ACTION_CONFLICT);

				stringBuffer.append(')');
			}

			tableItem.setText(1, stringBuffer.toString());				
			Scope scope = (Scope) registryScopeMap.get(actionRecord.scopeId);
			tableItem.setText(2, scope != null ? scope.getLabel().getName() : "[" + actionRecord.scopeId + "]");
			Configuration configuration = (Configuration) registryConfigurationMap.get(actionRecord.configurationId);			
			tableItem.setText(3, configuration != null ? configuration.getLabel().getName() : "[" + actionRecord.configurationId + "]");

			if (difference == DIFFERENCE_MINUS) {
				if (conflict)
					tableItem.setForeground(new Color(getShell().getDisplay(), RGB_CONFLICT_MINUS));	
				else 
					tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS));	
			} else if (conflict)
				tableItem.setForeground(new Color(getShell().getDisplay(), RGB_CONFLICT));	
		}			
	}
	
	private void buildTableKeySequence() {
		String scopeId = getScopeId();
		String configurationId = getConfigurationId();
		tableKeySequence.removeAll();
	
		for (int i = 0; i < keySequenceRecords.size(); i++) {
			KeySequenceRecord keySequenceRecord = (KeySequenceRecord) keySequenceRecords.get(i);
			int difference = DIFFERENCE_NONE;
			String actionId = null;
			boolean actionConflict = false;
			String alternateActionId = null;
			boolean alternateActionConflict = false;

			if (keySequenceRecord.customSet.isEmpty()) {
				actionId = keySequenceRecord.defaultActionId;															
				actionConflict = keySequenceRecord.defaultConflict;
			} else {
				actionId = keySequenceRecord.customActionId;															
				actionConflict = keySequenceRecord.customConflict;						

				if (keySequenceRecord.defaultSet.isEmpty())
					difference = DIFFERENCE_ADD;
				else {
					difference = DIFFERENCE_CHANGE;									
					alternateActionId = keySequenceRecord.defaultActionId;
					alternateActionConflict = keySequenceRecord.defaultConflict;																		
				}
			}

			TableItem tableItem = new TableItem(tableKeySequence, SWT.NULL);					

			switch (difference) {
				case DIFFERENCE_ADD:
					tableItem.setImage(0, IMAGE_PLUS);
					break;
	
				case DIFFERENCE_CHANGE:
					tableItem.setImage(0, IMAGE_CHANGE);
					break;
	
				case DIFFERENCE_MINUS:
					tableItem.setImage(0, IMAGE_MINUS);
					break;
	
				case DIFFERENCE_NONE:
					break;				
			}

			boolean conflict = actionConflict || alternateActionConflict;
			StringBuffer stringBuffer = new StringBuffer();
			String actionName = null;
					
			if (actionId == null) 
				actionName = ACTION_UNDEFINED;
			else {
				Action action = (Action) registryActionMap.get(actionId);
						
				if (action != null)
					actionName = action.getLabel().getName();
				else
					actionName = "[" + actionId + "]";
			}
			
			stringBuffer.append(actionName);

			if (actionConflict)
				stringBuffer.append(" " + ACTION_CONFLICT);

			if (difference == DIFFERENCE_CHANGE) {
				stringBuffer.append(" (was: ");
				String alternateActionName = null;
					
				if (alternateActionId == null) 
					alternateActionName = ACTION_UNDEFINED;
				else {
					Action action = (Action) registryActionMap.get(alternateActionId);
						
					if (action != null)
						alternateActionName = action.getLabel().getName();
					else
						alternateActionName = "[" + alternateActionId + "]";
				}
									
				stringBuffer.append(alternateActionName);
	
				if (alternateActionConflict)
					stringBuffer.append(" " + ACTION_CONFLICT);
	
				stringBuffer.append(')');
			}
	
			tableItem.setText(1, stringBuffer.toString());
			Scope scope = (Scope) registryScopeMap.get(keySequenceRecord.scopeId);
			tableItem.setText(2, scope != null ? scope.getLabel().getName() : "[" + keySequenceRecord.scopeId + "]");
			Configuration configuration = (Configuration) registryConfigurationMap.get(keySequenceRecord.configurationId);			
			tableItem.setText(3, configuration != null ? configuration.getLabel().getName() : "[" + keySequenceRecord.configurationId + "]");

			if (difference == DIFFERENCE_MINUS) {
				if (conflict)
					tableItem.setForeground(new Color(getShell().getDisplay(), RGB_CONFLICT_MINUS));	
				else 
					tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS));	
			} else if (conflict)
				tableItem.setForeground(new Color(getShell().getDisplay(), RGB_CONFLICT));	
		}
	}

	private GridLayout createGridLayout() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = SPACE;
		gridLayout.marginHeight = SPACE;
		gridLayout.marginWidth = SPACE;
		gridLayout.verticalSpacing = SPACE;
		return gridLayout;
	}		
		
	private void createUI(Composite composite) {
		Font font = composite.getFont();
		GridLayout gridLayout = createGridLayout();
		composite.setLayout(gridLayout);

		Group groupBrowseAction = new Group(composite, SWT.NULL);	
		groupBrowseAction.setFont(font);
		gridLayout = createGridLayout();
		gridLayout.numColumns = 3;		
		groupBrowseAction.setLayout(gridLayout);
		groupBrowseAction.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupBrowseAction.setText(Messages.getString("DialogCustomize.GroupBrowseAction")); //$NON-NLS-1$	

		labelAction = new Label(groupBrowseAction, SWT.LEFT);
		labelAction.setFont(font);
		labelAction.setText(Messages.getString("DialogCustomize.LabelAction")); //$NON-NLS-1$

		comboAction = new Combo(groupBrowseAction, SWT.READ_ONLY);
		comboAction.setFont(font);
		GridData gridData = new GridData();
		gridData.widthHint = 250;
		comboAction.setLayoutData(gridData);
		
		Label spacer = new Label(groupBrowseAction, SWT.NULL);
		spacer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));				

		tableAction = new Table(groupBrowseAction, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableAction.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 75;		
		gridData.horizontalSpan = 3;		
		tableAction.setLayoutData(gridData);
		tableAction.setFont(font);

		TableColumn tableColumn = new TableColumn(tableAction, SWT.NULL, 0);
		tableColumn.setResizable(false);
		tableColumn.setText(ZERO_LENGTH_STRING);
		tableColumn.setWidth(20);

		tableColumn = new TableColumn(tableAction, SWT.NULL, 1);
		tableColumn.setResizable(true);
		tableColumn.setText(Messages.getString("DialogCustomize.HeaderKeySequence")); //$NON-NLS-1$
		tableColumn.setWidth(350);	

		tableColumn = new TableColumn(tableAction, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Messages.getString("DialogCustomize.HeaderScope")); //$NON-NLS-1$
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(tableAction, SWT.NULL, 3);
		tableColumn.setResizable(true);
		tableColumn.setText(Messages.getString("DialogCustomize.HeaderConfiguration")); //$NON-NLS-1$
		tableColumn.setWidth(100);

		/*
		buttonDetails = new Button(groupBrowseAction, SWT.CENTER | SWT.PUSH);
		buttonDetails(font);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		gridData.horizontalSpan = 3;				
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonDetails.setText(Messages.getString("DialogCustomize.ButtonDetails")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonDetails.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + SPACE;
		buttonDetails.setLayoutData(gridData);		
		*/

		Group groupBrowseKeySequence = new Group(composite, SWT.NULL);	
		groupBrowseKeySequence.setFont(font);
		gridLayout = createGridLayout();
		gridLayout.numColumns = 3;		
		groupBrowseKeySequence.setLayout(gridLayout);
		groupBrowseKeySequence.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupBrowseKeySequence.setText(Messages.getString("DialogCustomize.GroupBrowseKeySequence")); //$NON-NLS-1$	

		labelKeySequence = new Label(groupBrowseKeySequence, SWT.LEFT);
		labelKeySequence.setFont(font);
		labelKeySequence.setText(Messages.getString("DialogCustomize.LabelKeySequence")); //$NON-NLS-1$

		comboKeySequence = new Combo(groupBrowseKeySequence, SWT.NULL);
		comboKeySequence.setFont(font);
		gridData = new GridData();
		gridData.widthHint = 250;
		comboKeySequence.setLayoutData(gridData);
		
		spacer = new Label(groupBrowseKeySequence, SWT.NULL);
		spacer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));				

		tableKeySequence = new Table(groupBrowseKeySequence, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableKeySequence.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 75;		
		gridData.horizontalSpan = 3;		
		tableKeySequence.setLayoutData(gridData);
		tableKeySequence.setFont(font);

		tableColumn = new TableColumn(tableKeySequence, SWT.NULL, 0);
		tableColumn.setResizable(false);
		tableColumn.setText(ZERO_LENGTH_STRING);
		tableColumn.setWidth(20);

		tableColumn = new TableColumn(tableKeySequence, SWT.NULL, 1);
		tableColumn.setResizable(true);
		tableColumn.setText(Messages.getString("DialogCustomize.HeaderAction")); //$NON-NLS-1$
		tableColumn.setWidth(350);	
		
		tableColumn = new TableColumn(tableKeySequence, SWT.NULL, 2);
		tableColumn.setResizable(true);
		tableColumn.setText(Messages.getString("DialogCustomize.HeaderScope")); //$NON-NLS-1$
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(tableKeySequence, SWT.NULL, 3);
		tableColumn.setResizable(true);
		tableColumn.setText(Messages.getString("DialogCustomize.HeaderConfiguration")); //$NON-NLS-1$
		tableColumn.setWidth(100);

		/*
		buttonBrowseSelectedAction = new Button(groupBrowseKeySequence, SWT.CENTER | SWT.PUSH);
		buttonBrowseSelectedAction.setFont(font);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		gridData.horizontalSpan = 3;				
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonBrowseSelectedAction.setText(Messages.getString("DialogCustomize.ButtonBrowseSelectedAction")); //$NON-NLS-1$
		gridData.widthHint = Math.max(widthHint, buttonBrowseSelectedAction.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + SPACE;
		buttonBrowseSelectedAction.setLayoutData(gridData);		
		*/
		
		Composite compositeStateAndAction = new Composite(groupBrowseKeySequence, SWT.NULL);
		gridLayout = createGridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;		
		gridLayout.numColumns = 2;
		compositeStateAndAction.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		compositeStateAndAction.setLayoutData(gridData);

		groupState = new Group(compositeStateAndAction, SWT.NULL);	
		groupState.setFont(font);
		gridLayout = createGridLayout();
		gridLayout.numColumns = 2;		
		groupState.setLayout(gridLayout);
		groupState.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupState.setText(Messages.getString("DialogCustomize.GroupState")); //$NON-NLS-1$

		labelScope = new Label(groupState, SWT.LEFT);
		labelScope.setFont(font);
		labelScope.setText(Messages.getString("DialogCustomize.LabelScope")); //$NON-NLS-1$

		comboScope = new Combo(groupState, SWT.READ_ONLY);
		comboScope.setFont(font);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 100;
		comboScope.setLayoutData(gridData);

		labelConfiguration = new Label(groupState, SWT.LEFT);
		labelConfiguration.setFont(font);
		labelConfiguration.setText(Messages.getString("DialogCustomize.LabelConfiguration")); //$NON-NLS-1$

		comboConfiguration = new Combo(groupState, SWT.READ_ONLY);
		comboConfiguration.setFont(font);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 100;
		comboConfiguration.setLayoutData(gridData);

		groupAction = new Group(compositeStateAndAction, SWT.NULL);	
		groupAction.setFont(font);
		gridLayout = createGridLayout();
		gridLayout.numColumns = 2;		
		groupAction.setLayout(gridLayout);
		groupAction.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupAction.setText(Messages.getString("DialogCustomize.GroupAction")); //$NON-NLS-1$

		buttonDefault = new Button(groupAction, SWT.LEFT | SWT.RADIO);
		buttonDefault.setFont(font);
		buttonDefault.setText(Messages.getString("DialogCustomize.ButtonDefault")); //$NON-NLS-1$

		textDefault = new Text(groupAction, SWT.BORDER | SWT.READ_ONLY);
		textDefault.setFont(font);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 250;
		textDefault.setLayoutData(gridData);

		buttonCustom = new Button(groupAction, SWT.LEFT | SWT.RADIO);
		buttonCustom.setFont(font);
		buttonCustom.setText(Messages.getString("DialogCustomize.ButtonCustom")); //$NON-NLS-1$

		comboCustom = new Combo(groupAction, SWT.READ_ONLY);
		comboCustom.setFont(font);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 250;
		comboCustom.setLayoutData(gridData);

		comboAction.setItems(actionNames);
		comboKeySequence.setItems(getKeySequences());
		comboScope.setItems(scopeNames);
		comboConfiguration.setItems(configurationNames);
		comboCustom.setItems(actionNames);

		setConfigurationId(defaultConfigurationId);
		setScopeId(defaultScopeId);
		setAction(Collections.EMPTY_SET, Collections.EMPTY_SET);

		comboAction.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedComboAction();
			}	
		});

		tableAction.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				selectedButtonDetails();	
			}			
		});		

		tableAction.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedTableAction();
			}	
		});

		/*
		buttonDetails.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedButtonDetails();
			}	
		});
		*/		

		comboKeySequence.addModifyListener(new ModifyListener() {			
			public void modifyText(ModifyEvent e) {
				modifiedComboKeySequence();
			}	
		});

		comboKeySequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedComboKeySequence();
			}	
		});

		tableKeySequence.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				selectedButtonBrowseSelectedAction();	
			}			
		});		

		tableKeySequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {			
				selectedTableKeySequence();
			}	
		});

		/*
		buttonBrowseSelectedAction.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedButtonBrowseSelectedAction();
			}	
		});
		*/

		comboScope.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedComboScope();
			}	
		});

		comboConfiguration.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedComboConfiguration();
			}	
		});

		buttonDefault.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedButtonDefault();
			}	
		});

		buttonCustom.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedButtonCustom();
			}	
		});
		
		comboCustom.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectedComboCustom();
			}	
		});
	}

	private ActionRecord getSelectedActionRecord() {		
		int selection = tableAction.getSelectionIndex();
		
		if (selection >= 0 && selection < actionRecords.size() && tableAction.getSelectionCount() == 1)
			return (ActionRecord) actionRecords.get(selection);
		else
			return null;
	}

	private KeySequenceRecord getSelectedKeySequenceRecord() {		
		int selection = tableKeySequence.getSelectionIndex();
		
		if (selection >= 0 && selection < keySequenceRecords.size() && tableKeySequence.getSelectionCount() == 1)
			return (KeySequenceRecord) keySequenceRecords.get(selection);
		else
			return null;
	}

	private void selectTableAction(String scopeId, String configurationId, KeySequence keySequence) {	
		int selection = -1;
		
		for (int i = 0; i < actionRecords.size(); i++) {
			ActionRecord actionRecord = (ActionRecord) actionRecords.get(i);			
			
			if (Util.equals(scopeId, actionRecord.scopeId) && Util.equals(configurationId, actionRecord.configurationId) && 
				Util.equals(keySequence, actionRecord.keySequence)) {
				selection = i;
				break;			
			}			
		}

		if (tableAction.getSelectionCount() > 1)
			tableAction.deselectAll();

		if (selection != tableAction.getSelectionIndex()) {
			if (selection == -1 || selection >= tableAction.getItemCount())
				tableAction.deselectAll();
			else
				tableAction.select(selection);
		}
	}

	private void selectTableKeySequence(String scopeId, String configurationId) {		
		int selection = -1;
		
		for (int i = 0; i < keySequenceRecords.size(); i++) {
			KeySequenceRecord keySequenceRecord = (KeySequenceRecord) keySequenceRecords.get(i);			
			
			if (Util.equals(scopeId, keySequenceRecord.scopeId) && Util.equals(configurationId, keySequenceRecord.configurationId)) {
				selection = i;
				break;			
			}			
		}

		if (tableKeySequence.getSelectionCount() > 1)
			tableKeySequence.deselectAll();

		if (selection != tableKeySequence.getSelectionIndex()) {
			if (selection == -1 || selection >= tableKeySequence.getItemCount())
				tableKeySequence.deselectAll();
			else
				tableKeySequence.select(selection);
		}
	}

	private void clear(SortedMap tree, KeySequence keySequence, String scope, String configuration) {			
		Map scopeMap = (Map) tree.get(keySequence);
		
		if (scopeMap != null) {
			Map configurationMap = (Map) scopeMap.get(scope);
		
			if (configurationMap != null) {
				Map pluginMap = (Map) configurationMap.get(configuration);
	
				if (pluginMap != null) {
					pluginMap.remove(null);
					
					if (pluginMap.isEmpty()) {
						configurationMap.remove(configuration);
						
						if (configurationMap.isEmpty()) {
							scopeMap.remove(scope);	

							if (scopeMap.isEmpty()) {
								tree.remove(keySequence);	
							}							
						}	
					}	
				}	
			}
		}
	}

	private void set(SortedMap tree, Binding binding, boolean consolidate) {			
		Map scopeMap = (Map) tree.get(binding.getKeySequence());
		
		if (scopeMap == null) {
			scopeMap = new TreeMap();	
			tree.put(binding.getKeySequence(), scopeMap);
		}

		Map configurationMap = (Map) scopeMap.get(binding.getScope());
		
		if (configurationMap == null) {
			configurationMap = new TreeMap();	
			scopeMap.put(binding.getScope(), configurationMap);
		}
		
		Map pluginMap = (Map) configurationMap.get(binding.getConfiguration());
		
		if (pluginMap == null) {
			pluginMap = new HashMap();	
			configurationMap.put(binding.getConfiguration(), pluginMap);
		}

		Map actionMap = consolidate ? null : (Map) pluginMap.get(binding.getPlugin());
		
		if (actionMap == null) {
			actionMap = new HashMap();	
			pluginMap.put(binding.getPlugin(), actionMap);
		}

		Set bindingSet = (Set) actionMap.get(binding.getAction());
		
		if (bindingSet == null) {
			bindingSet = new TreeSet();
			actionMap.put(binding.getAction(), bindingSet);	
		}

		if (consolidate)
			bindingSet.clear();
		
		bindingSet.add(binding);
	}

	private SortedSet solve(SortedMap tree) {
		SortedSet bindingSet = new TreeSet();
		Iterator iterator = tree.values().iterator();
		
		while (iterator.hasNext()) {
			Map scopeMap = (Map) iterator.next();
			Iterator iterator2 = scopeMap.values().iterator();
			
			while (iterator2.hasNext()) {
				Map configurationMap = (Map) iterator2.next();
				Iterator iterator3 = configurationMap.values().iterator();
				
				while (iterator3.hasNext()) {
					Map pluginMap = (Map) iterator3.next();
					Map actionMap = (Map) pluginMap.get(null);
					
					if (actionMap != null) {
						Iterator iterator4 = actionMap.values().iterator();
						
						while (iterator4.hasNext())
							bindingSet.addAll((Set) iterator4.next());
					}
				}
			}		
		}
		
		return bindingSet;
	}

	private String getScopeId() {
		int selection = comboScope.getSelectionIndex();
		
		if (selection >= 0 && selection < scopes.size()) {
			Scope scope = (Scope) scopes.get(selection);
			return scope.getLabel().getId();				
		}
		
		return null;
	}

	private void setScopeId(String scopeId) {				
		comboScope.clearSelection();
		comboScope.deselectAll();
		
		if (scopeId != null)	
			for (int i = 0; i < scopes.size(); i++) {
				Scope scope = (Scope) scopes.get(i);		
				
				if (scope.getLabel().getId().equals(scopeId)) {
					comboScope.select(i);
					break;		
				}
			}
	}

	private String getConfigurationId() {
		int selection = comboConfiguration.getSelectionIndex();
		
		if (selection >= 0 && selection < configurations.size()) {
			Configuration configuration = (Configuration) configurations.get(selection);
			return configuration.getLabel().getId();				
		}
		
		return null;
	}

	private void setConfigurationId(String configurationId) {				
		comboConfiguration.clearSelection();
		comboConfiguration.deselectAll();
		
		if (configurationId != null)	
			for (int i = 0; i < configurations.size(); i++) {
				Configuration configuration = (Configuration) configurations.get(i);		
				
				if (configuration.getLabel().getId().equals(configurationId)) {
					comboConfiguration.select(i);
					break;		
				}
			}
	}	
	
	private void setAction(Set customSet, Set defaultSet) {	
		boolean customConflict = false;
		String customActionId = null;
		boolean defaultConflict = false;
		String defaultActionId = null;	

		if (customSet.size() > 1)
			customConflict = true;
		else if (!customSet.isEmpty())				
			customActionId = (String) customSet.iterator().next();
	
		if (defaultSet.size() > 1)
			defaultConflict = true;
		else if (!defaultSet.isEmpty())				
			defaultActionId = (String) defaultSet.iterator().next();

		buttonDefault.setSelection(customSet.isEmpty());
		textDefault.setText(defaultActionId != null ? defaultActionId : ZERO_LENGTH_STRING);

		if (defaultConflict)
			textDefault.setText(ACTION_CONFLICT);
		else {
			if (defaultActionId == null)
				textDefault.setText(ACTION_UNDEFINED);
			else {
				for (int j = 0; j < actions.size(); j++) {
					Action action = (Action) actions.get(j);		
								
					if (action.getLabel().getId().equals(defaultActionId)) {
						textDefault.setText(action.getLabel().getName());
						break;		
					}
				}
			}
		}	

		buttonCustom.setSelection(!customSet.isEmpty());
		comboCustom.deselectAll();
		comboCustom.setText(customActionId != null ? customActionId : ZERO_LENGTH_STRING);
		
		if (!customSet.isEmpty()) {
			if (customConflict)
				comboCustom.setText(ACTION_CONFLICT);
			else {			
				if (customActionId == null)
					comboCustom.select(0);
				else
					for (int i = 0; i < actions.size(); i++) {
						Action action = (Action) actions.get(i);		
								
						if (action.getLabel().getId().equals(customActionId)) {
							comboCustom.select(i + 1);
							break;		
						}
					}			
			}
		}
	}

	private String[] getKeySequences() {
		String[] items = (String[]) nameToKeySequenceMap.keySet().toArray(new String[nameToKeySequenceMap.size()]);
		Arrays.sort(items, Collator.getInstance());
		return items;
	}

	private void selectedComboAction() {
		actionRecords.clear();
		int selection = comboAction.getSelectionIndex();

		if (selection >= 0 && selection <= actions.size() && tree != null) {		
			String actionId = null;				
			
			if (selection > 0) {
				Action action = (Action) actions.get(selection - 1);
				actionId = action.getLabel().getId();
			}

			buildActionRecords(tree, actionId, actionRecords);
		} 

		buildTableAction();
	}

	private void selectedTableAction() {
		int i = tableAction.getSelectionIndex();

		if (i >= 0) {
			ActionRecord actionRecord = (ActionRecord) actionRecords.get(i);						
					
			if (actionRecord != null) {
				comboKeySequence.clearSelection();
				comboKeySequence.deselectAll();
		
				if (actionRecord.keySequence != null) {
					String name = keyManager.getTextForKeySequence(actionRecord.keySequence);
			
					if (name != null)
						comboKeySequence.setText(name);
				}	

				keySequenceRecords.clear();
				buildKeySequenceRecords(tree, actionRecord.keySequence, keySequenceRecords);
				buildTableKeySequence();
				selectTableKeySequence(actionRecord.scopeId, actionRecord.configurationId);				
				setScopeId(actionRecord.scopeId);
				setConfigurationId(actionRecord.configurationId);
				setAction(actionRecord.customSet, actionRecord.defaultSet);			
			}
		}
	}
	
	private void selectedButtonDetails() {
		// TBD add dialog to display the plugin map for selected row in tableAction
	}

	private void modifiedComboKeySequence() {
		selectedComboKeySequence();
	}
	
	private void selectedComboKeySequence() {			
		KeySequence keySequence = null;
		String name = comboKeySequence.getText();		
		keySequence = (KeySequence) nameToKeySequenceMap.get(name);
			
		if (keySequence == null)
			// TBD review. still not strict enough. convertAccelerator says 'Ctrl+Ax' is valid.				
			keySequence = KeyManager.parseKeySequenceStrict(name);

		keySequenceRecords.clear();
		buildKeySequenceRecords(tree, keySequence, keySequenceRecords);
		buildTableKeySequence();
		String scopeId = getScopeId();
		String configurationId = getConfigurationId();
		selectTableKeySequence(scopeId, configurationId);		
		KeySequenceRecord keySequenceRecord = (KeySequenceRecord) getSelectedKeySequenceRecord();
		
		if (keySequenceRecord != null)
			setAction(keySequenceRecord.customSet, keySequenceRecord.defaultSet);
		else
			setAction(Collections.EMPTY_SET, Collections.EMPTY_SET);	
	}	
	
	private void selectedTableKeySequence() {
		KeySequenceRecord keySequenceRecord = (KeySequenceRecord) getSelectedKeySequenceRecord();
		
		if (keySequenceRecord != null) {
			setScopeId(keySequenceRecord.scopeId);
			setConfigurationId(keySequenceRecord.configurationId);				
			setAction(keySequenceRecord.customSet, keySequenceRecord.defaultSet);
		} else
			setAction(Collections.EMPTY_SET, Collections.EMPTY_SET);	
	}
	
	private void selectedButtonBrowseSelectedAction() {
		/*
		KeySequenceRecord keySequenceRecord = getSelectedKeySequenceRecord();
		
		if (keySequenceRecord != null) {

			if (!actionConflict) {
				comboAction.deselectAll();

				for (int i = 0; )				

				browseAction(actionId);
				return;
			}		
		} 
		
		unbrowseAction();
		*/	
	}

	private void selectedComboScope() {
		selectedComboState();	
	}

	private void selectedComboConfiguration() {	
		selectedComboState();
	}

	private void selectedComboState() {
		selectTableKeySequence(getScopeId(), getConfigurationId());
		KeySequenceRecord keySequenceRecord = (KeySequenceRecord) getSelectedKeySequenceRecord();
		
		if (keySequenceRecord != null) {		
			setAction(keySequenceRecord.customSet, keySequenceRecord.defaultSet);
		} else
			setAction(Collections.EMPTY_SET, Collections.EMPTY_SET);
	}

	private void selectedButtonDefault() {
		change(false);
	}

	private void selectedButtonCustom() {		
		change(true);
	}

	private void selectedComboCustom() {
		change(true);
	}

	private void change(boolean custom) {
		int selection = comboCustom.getSelectionIndex();
				
		if (selection < 0)
			comboCustom.select(comboAction.getSelectionIndex());

		KeySequence keySequence = null;
		String name = comboKeySequence.getText();
		
		if (name != null || name.length() > 0) {
			keySequence = (KeySequence) nameToKeySequenceMap.get(name);
			
			if (keySequence == null)
				keySequence = KeyManager.parseKeySequenceStrict(name);
		}				

		String scopeId = getScopeId();
		String configurationId = getConfigurationId();

		if (keySequence != null) {
			if (!custom)
				clear(tree, keySequence, scopeId, configurationId);						
			else { 
				String actionId = null;				
				selection = comboCustom.getSelectionIndex();
				
				if (selection < 0)
					selection = comboAction.getSelectionIndex();
		
				selection--;
			
				if (selection >= 0 && selection < actions.size()) {
					Action action = (Action) actions.get(selection);
					actionId = action.getLabel().getId();
				}				

				set(tree, Binding.create(actionId, configurationId, keySequence, null, 0, scopeId), true);				
				/*
				name = keyManager.getTextForKeySequence(keySequence);			
				
				if (!nameToKeySequenceMap.containsKey(name))
					nameToKeySequenceMap.put(name, keySequence);
	
				comboKeySequence.setItems(getKeySequences());
				*/					
			}
		}

		ActionRecord actionRecord = getSelectedActionRecord();
		actionRecords.clear();
		
		if (actionRecord != null)		
			buildActionRecords(tree, actionRecord.actionId, actionRecords);
		
		buildTableAction();
		
		if (actionRecord != null)	
			selectTableAction(actionRecord.scopeId, actionRecord.configurationId, actionRecord.keySequence);

		keySequenceRecords.clear();
		
		if (keySequence != null)	
			buildKeySequenceRecords(tree, keySequence, keySequenceRecords);
		
		buildTableKeySequence();
		selectTableKeySequence(scopeId, configurationId);
	}

	/*
	private void update() {
		boolean bValidKeySequence = keySequence != null && keySequence.getKeyStrokes().size() >= 1;
		tableKeySequence.setEnabled(bValidKeySequence);
		buttonBrowseSelectedKeySequence.setEnabled(bValidKeySequence); //TBD + table has selection
		groupState.setEnabled(bValidKeySequence);
		labelScope.setEnabled(bValidKeySequence);
		comboScope.setEnabled(bValidKeySequence);
		labelConfiguration.setEnabled(bValidKeySequence);
		comboConfiguration.setEnabled(bValidKeySequence);
		groupAction.setEnabled(bValidKeySequence);
		buttonDefault.setEnabled(bValidKeySequence);
		textDefault.setEnabled(bValidKeySequence);
		buttonCustom.setEnabled(bValidKeySequence);
		comboCustom.setEnabled(bValidKeySequence);
	}
	*/
}
