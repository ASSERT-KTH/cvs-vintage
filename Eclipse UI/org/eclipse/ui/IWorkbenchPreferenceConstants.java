/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;

/**
 * Preference ids exposed by the Eclipse Platform User Interface.
 * These preference settings can be obtained from the UI plug-in's
 * preference store.
 * 
 * @see PlatformUI#PLUGIN_ID
 * @see PlatformUI#getPreferenceStore()
 */
public interface IWorkbenchPreferenceConstants {
    
    /**
     * A named preference for whether to show an editor when its
     * input file is selected in the Navigator (and vice versa).
     * <p>
     * Value is of type <code>boolean</code>.
     * </p>
     */
    public static final String LINK_NAVIGATOR_TO_EDITOR = "LINK_NAVIGATOR_TO_EDITOR"; //$NON-NLS-1$

    /**
     * A named preference for how a new perspective is opened.
     * <p>
     * Value is of type <code>String</code>.  The possible values are defined 
     * by <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE and 
     * OPEN_PERSPECTIVE_REPLACE</code>.
     * </p>
     * 
     * @see #OPEN_PERSPECTIVE_WINDOW
     * @see #OPEN_PERSPECTIVE_PAGE
     * @see #OPEN_PERSPECTIVE_REPLACE
     * @see #NO_NEW_PERSPECTIVE
     */
    public static final String OPEN_NEW_PERSPECTIVE = "OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$

    /**
     * A named preference for how a new perspective is opened
     * when the alternate key modifiers are pressed.  The alternate key modifiers
     * are platform dependent.
     * <p>
     * Value is of type <code>String</code>.  The possible values are defined 
     * by <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE and 
     * OPEN_PERSPECTIVE_REPLACE</code>.
     * </p>
     * @deprecated Workbench no longer supports alternate key modifier to open
     * 		a new perspective. Callers should use IWorkbench.showPerspective methods.
     */
    public static final String ALTERNATE_OPEN_NEW_PERSPECTIVE = "ALTERNATE_OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$

    /**
     * A named preference for how a new perspective is opened
     * when the shift key modifier is pressed.  
     * <p>
     * Value is of type <code>String</code>.  The possible values are defined 
     * by <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE and 
     * OPEN_PERSPECTIVE_REPLACE</code>.
     * </p>
     * 
     * @deprecated Workbench no longer supports shift key modifier to open
     * 		a new perspective. Callers should use IWorkbench.showPerspective methods.
     */
    public static final String SHIFT_OPEN_NEW_PERSPECTIVE = "SHIFT_OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$

    /**
     * A named preference for how a new perspective should be opened
     * when a new project is created.
     * <p>
     * Value is of type <code>String</code>.  The possible values are defined 
     * by the constants <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE, 
     * OPEN_PERSPECTIVE_REPLACE, and NO_NEW_PERSPECTIVE</code>.
     * </p>
     * 
     * @see #OPEN_PERSPECTIVE_WINDOW
     * @see #OPEN_PERSPECTIVE_PAGE
     * @see #OPEN_PERSPECTIVE_REPLACE
     * @see #NO_NEW_PERSPECTIVE
     * @deprecated in 3.0. This preference is IDE-specific, and is therefore found
     * only in IDE configurations. IDE-specific tools should use 
     * <code>org.eclipse.ui.ide.IDE.Preferences.PROJECT_OPEN_NEW_PERSPECTIVE</code>
     * instead.
     */
    public static final String PROJECT_OPEN_NEW_PERSPECTIVE = "PROJECT_OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$

    /**
     * A preference value indicating that an action should open a new 
     * perspective in a new window.
     * 
     * @see #PROJECT_OPEN_NEW_PERSPECTIVE
     */
    public static final String OPEN_PERSPECTIVE_WINDOW = "OPEN_PERSPECTIVE_WINDOW"; //$NON-NLS-1$

    /**
     * A preference value indicating that an action should open a new 
     * perspective in a new page.
     * 
     * @see #PROJECT_OPEN_NEW_PERSPECTIVE
     * @deprecated Opening a Perspective in a new page is no longer
     * supported functionality as of 2.0.
     */
    public static final String OPEN_PERSPECTIVE_PAGE = "OPEN_PERSPECTIVE_PAGE"; //$NON-NLS-1$

    /**
     * A preference value indicating that an action should open a new 
     * perspective by replacing the current perspective.
     * 
     * @see #PROJECT_OPEN_NEW_PERSPECTIVE
     */
    public static final String OPEN_PERSPECTIVE_REPLACE = "OPEN_PERSPECTIVE_REPLACE"; //$NON-NLS-1$

    /**
     * A preference value indicating that an action should not open a 
     * new perspective.
     * 
     * @see #PROJECT_OPEN_NEW_PERSPECTIVE
     */
    public static final String NO_NEW_PERSPECTIVE = "NO_NEW_PERSPECTIVE"; //$NON-NLS-1$

    /**
     * A named preference indicating the default workbench perspective.
     */
    public static final String DEFAULT_PERSPECTIVE_ID = "defaultPerspectiveId"; //$NON-NLS-1$

    /**
     * A named preference indicating the presentation factory
     * to use for the workbench look and feel.
     * 
     * @since 3.0
     */
    public static final String PRESENTATION_FACTORY_ID = "presentationFactoryId"; //$NON-NLS-1$

    /**
     * A named preference indicating where the perspective bar should be docked.
     * The default value (when this preference is not set) is <code>TOP_RIGHT</code>.
     * <p>
     * This preference may be one of the following values:
     * {@link #TOP_RIGHT}, {@link #TOP_LEFT}, or {@link #LEFT}.
     * </p>
     *
     * @since 3.0
     */
    public static String DOCK_PERSPECTIVE_BAR = "DOCK_PERSPECTIVE_BAR"; //$NON-NLS-1$	

    /**
     * A named preference indicating where the fast view bar should be docked in a
     * fresh workspace.  This preference is meaningless after a workspace has been
     * setup, since the fast view bar state is then persisted in the workbench.  This
     * preference is intended for applications that want the initial docking location
     * to be somewhere specific.  The default value (when this preference is not set)
     * is the bottom.
     * 
     * @see #LEFT
     * @see #BOTTOM
     * @see #RIGHT
     * @since 3.0
     */
    public static final String INITIAL_FAST_VIEW_BAR_LOCATION = "initialFastViewBarLocation"; //$NON-NLS-1$

    /**
     * Constant to be used when referring to the top right of the workbench window. 
     *
     * @see #DOCK_PERSPECTIVE_BAR
     * @since 3.0
     */
    public static final String TOP_RIGHT = "topRight"; //$NON-NLS-1$

    /**
     * Constant to be used when referring to the top left of the workbench window. 
     *
     * @see #DOCK_PERSPECTIVE_BAR
     * @since 3.0
     */
    public static final String TOP_LEFT = "topLeft"; //$NON-NLS-1$

    /**
     * Constant to be used when referring to the left side of the workbench window. 
     *
     * @see #DOCK_PERSPECTIVE_BAR
     * @see #INITIAL_FAST_VIEW_BAR_LOCATION
     * @since 3.0
     */
    public static final String LEFT = "left"; //$NON-NLS-1$

    /**
     * Constant to be used when referring to the bottom of the workbench window. 
     *
     * @see #INITIAL_FAST_VIEW_BAR_LOCATION
     * @since 3.0
     */
    public static final String BOTTOM = "bottom"; //$NON-NLS-1$

    /**
     * Constant to be used when referring to the right side of the workbench window. 
     *
     * @see #INITIAL_FAST_VIEW_BAR_LOCATION
     * @since 3.0
     */
    public static final String RIGHT = "right"; //$NON-NLS-1$

    /**
     * A named preference indicating whether the workbench should show the 
     * introduction component (if available) on startup.
     * 
     * <p>
     * The default value for this preference is: <code>true</code> (show intro)
     * </p> 
     * 
     * @see org.eclipse.ui.application.WorkbenchAdvisor#openIntro(IWorkbenchWindowConfigurer)
     * @since 3.0
     */
    public static final String SHOW_INTRO = "showIntro"; //$NON-NLS-1$

    /**
     * A named preference for whether the workbench should show traditional style tabs in
     * editors and views.
     * 
     * Boolean-valued: <code>true</code> if editors and views should use a traditional style of tab and
     * <code>false</code> if editors should show new style tab (3.0 style)
     * <p>
     * The default value for this preference is: <code>true</code>
     * </p>
     * 
     * @since 3.0
     */
    public static String SHOW_TRADITIONAL_STYLE_TABS = "SHOW_TRADITIONAL_STYLE_TABS"; //$NON-NLS-1$

    /**
     * A named preference for whether the workbench should show text 
     * on the perspective bar. 
     * 
     * Boolean-valued: <code>true</code>, if editors should show text on the perspective bar,
     * <code>false</code> otherwise.
     * <p>
     * The default value for this preference is: <code>true</code> (show text on the perspective bar)
     * </p>
     * 
     * @since 3.0
     */
    public static String SHOW_TEXT_ON_PERSPECTIVE_BAR = "SHOW_TEXT_ON_PERSPECTIVE_BAR"; //$NON-NLS-1$

    /**
     * A named preference for the text of the Help Contents action. 
     * 
     * String-valued.  If not specified, <code>"&Help Contents"</code> is used.
     * <p>
     * The default value for this preference is: <code>null</code>
     * </p>
     * 
     * @since 3.0
     */
    public static String HELP_CONTENTS_ACTION_TEXT = "helpContentsActionText"; //$NON-NLS-1$
    
    /**
     * A named preference for enabling animations when a layout transition occurs
     * <p>
     * The default value for this preference is: <code>true</code> (show animations when a transition occurs)
     * </p>
     * 
     * @since 3.1
     */
    public static final String ENABLE_ANIMATIONS = "ENABLE_ANIMATIONS"; //$NON-NLS-1$
    
    /**
     * <p>
     * Workbench preference id for the key configuration identifier to be
     * treated as the default.
     * </p>
     * <p>
     * The default value for this preference is
     * <code>"org.eclipse.ui.defaultAcceleratorConfiguration"</code>.
     * <p>
     * 
     * @since 3.1
     */
    public static final String KEY_CONFIGURATION_ID = "KEY_CONFIGURATION_ID"; //$NON-NLS-1$
    
    /**
     * <p>
     * Workbench preference identifier for the minimum width of editor tabs. By
     * default, Eclipse does not define this value and allows SWT to determine
     * this constant. We use <code>-1</code> internally to signify "use
     * default".
     * </p>
     * <p>
     * The default value for this preference is <code>-1</code>.
     * </p>
     * 
     * @since 3.1
     */
    public static final String EDITOR_MINIMUM_CHARACTERS = "EDITOR_MINIMUM_CHARACTERS"; //$NON-NLS-1$

    /**
	 * <p>
	 * Specifies whether or not the workspace selection dialog should be shown on startup.
	 * </p>
	 * <p>
	 * The default value for this preference is <code>true</code>.
	 * </p>
	 */
	public static final String SHOW_WORKSPACE_SELECTION_DIALOG = "SHOW_WORKSPACE_SELECTION_DIALOG"; //$NON-NLS-1$

	/**
	 * <p>
	 * Stores the maximum number of workspaces that should be displayed in the
	 * ChooseWorkspaceDialog.
	 * </p>
	 */
	public static final String MAX_RECENT_WORKSPACES = "MAX_RECENT_WORKSPACES"; //$NON-NLS-1$

	/**
	 * <p>
	 * Stores a comma separated list of the recently used workspace paths.
	 * </p>
	 */
	public static final String RECENT_WORKSPACES = "RECENT_WORKSPACES"; //$NON-NLS-1$

	/**
	 * <p>
	 * Stores the version of the protocol used to decode/encode the list of recent workspaces.
	 * </p>
	 */
	public static final String RECENT_WORKSPACES_PROTOCOL = "RECENT_WORKSPACES_PROTOCOL"; //$NON-NLS-1$
	
	/**
	 * <p> Stores whether or not system jobs are being
	 * shown</p>.
	 */
	public static final String SHOW_SYSTEM_JOBS = "SHOW_SYSTEM_JOBS";//$NON-NLS-1$
}
