/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.Gradient;
import org.eclipse.jface.resource.GradientData;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPresentationPreview;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;

/**
 * Preference page for management of system colors, gradients and fonts.
 * 
 * @since 3.0
 */
public final class ColorsAndFontsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    
    private class PresentationLabelProvider extends LabelProvider implements IFontProvider {
        
        private HashMap fonts = new HashMap();

        private HashMap images = new HashMap();
        
        private int imageSize = -1;
        private int usableImageSize = -1;
        
        private IPropertyChangeListener listener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                fireLabelProviderChanged(new LabelProviderChangedEvent(PresentationLabelProvider.this));                
            }            
        };

        private Image emptyImage;        
        
        /**
         * 
         */
        public PresentationLabelProvider() {
            colorRegistry.addListener(listener);        
            fontRegistry.addListener(listener);
            gradientRegistry.addListener(listener);
        }
    	
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
         */
        public void dispose() {
            super.dispose();
            colorRegistry.removeListener(listener);
            fontRegistry.removeListener(listener);
            gradientRegistry.removeListener(listener);
            for (Iterator i = images.values().iterator(); i.hasNext();) {
                ((Image) i.next()).dispose();
            }
            images.clear();
            
            for (Iterator i = fonts.values().iterator(); i.hasNext();) {
                ((Font) i.next()).dispose();
            }
            fonts.clear();
                        
            if (emptyImage != null) {
            	emptyImage.dispose();
            	emptyImage = null;
            }
        }
        
    	
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
         */
        public Font getFont(Object element) {
            if (element instanceof FontDefinition) {
                int parentHeight = presentationList.getControl().getFont().getFontData()[0].getHeight();
                                
	    		Display display = presentationList.getControl().getDisplay();
	    		
    	        Font baseFont = fontRegistry.get(((FontDefinition)element).getId());
    	        Font font = (Font) fonts.get(baseFont);
    	        if (font == null) {
    	            FontData [] data = baseFont.getFontData();
    	            for (int i = 0; i < data.length; i++) {
                        data[i].setHeight(parentHeight);
                    }
    	            font = new Font(display, data);
	
		    		fonts.put(baseFont, font);
    	        }
    	        return font;
    	    }
            return null;
        }
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
    	 */
    	public Image getImage(Object element) {
    	    if (element instanceof ColorDefinition) {	    			    		
    	        Color c = colorRegistry.get(((ColorDefinition)element).getId());
    	        Image image = (Image) images.get(c);
    	        if (image == null) {
    	        	Display display = presentationList.getControl().getDisplay();
    	            ensureImageSize(display);
		    		//int size = presentationList.getControl().getFont().getFontData()[0].getHeight();
	    	        image = new Image(display, imageSize, imageSize);
	    	        
		    		GC gc = new GC(image);
		    		gc.setBackground(presentationList.getControl().getBackground());
		    		gc.setForeground(presentationList.getControl().getBackground());
		    		gc.drawRectangle(0, 0, imageSize - 1, imageSize - 1);
		    		
		    		gc.setForeground(presentationList.getControl().getForeground());
		    		gc.setBackground(c);
		    		
		    		int offset = (imageSize - usableImageSize) / 2;
		    		gc.drawRectangle(offset, offset, usableImageSize - offset, usableImageSize - offset);
		    		gc.fillRectangle(offset + 1, offset + 1, usableImageSize - offset - 1, usableImageSize - offset - 1);		    			    	
		    		gc.dispose();
		    		
		    		images.put(c, image);
    	        }
	    		return image;
    	        
    	    }
    	    else if (element instanceof GradientDefinition) {
    	    	// currently only draws a gradient from the first to the last gradient element.
    	    	// gradients with more than 2 colors are not visualized correctly.	    		
    	        Gradient g = gradientRegistry.get(((GradientDefinition)element).getId());
    	        Image image = (Image) images.get(g);
    	        if (image == null) {
    	        	Display display = presentationList.getControl().getDisplay();
    	            ensureImageSize(display);
	    	        image = new Image(display, imageSize, imageSize);
	    	        
		    		GC gc = new GC(image);
		    		gc.setBackground(presentationList.getControl().getBackground());		    		
		    		gc.setForeground(presentationList.getControl().getBackground());
		    		gc.drawRectangle(0, 0, imageSize - 1, imageSize - 1);
		    		
		    		gc.setForeground(presentationList.getControl().getForeground());
					int offset = (imageSize - usableImageSize) / 2;
		    		gc.drawRectangle(offset, offset, usableImageSize - offset, usableImageSize - offset);
		    		gc.fillRectangle(offset + 1, offset + 1, usableImageSize - offset - 1, usableImageSize - offset - 1);		    		
		    		
		    		Color[] colors = g.getColors();
                    gc.setForeground(colors[0]);
		    		gc.setBackground(colors[colors.length -1]);
		    		gc.fillGradientRectangle(offset + 1, offset + 1, usableImageSize - offset - 1, usableImageSize - offset - 1, g.getDirection() == SWT.VERTICAL);		    			    	
		    		gc.dispose();
		    		
		    		images.put(g, image);
    	        }
	    		return image;
    	    	
    	    }
    	    else {
    	    	if (emptyImage == null) {
    	    		Display display = presentationList.getControl().getDisplay();
    	    		ensureImageSize(display);
	    	        emptyImage = new Image(display, imageSize, imageSize);
	    	        
		    		GC gc = new GC(emptyImage);
		    		gc.setBackground(presentationList.getControl().getBackground());		    		
		    		gc.setForeground(presentationList.getControl().getBackground());
		    		gc.fillRectangle(0, 0, imageSize - 1 , imageSize - 1);
		    		gc.dispose();
    	    	}
    	    	return emptyImage;
    	    }
    	}

        /**
         * @param display
         * @return
         */
        private void ensureImageSize(Display display) {
            if (imageSize == -1) {    	        
	    		imageSize = presentationList.getTable().getItemHeight();    
	    		usableImageSize = Math.max(1, imageSize - 4);
            }
        }

        /* (non-Javadoc)
    	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
    	 */
    	public String getText(Object element) {
    		if (element instanceof IThemeElementDefinition)
    			return ((IThemeElementDefinition) element).getLabel();
    		return ""; //$NON-NLS-1$
    	}
    }
    
	/**
	 * The translation bundle in which to look up internationalized text.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(ColorsAndFontsPreferencePage.class.getName());

    private Font appliedDialogFont;
	
	private Combo categoryCombo;
	
	/**
	 * Map to precalculate category color lists.
	 */
	private Map categoryMap = new HashMap(7);
	
	/**
	 * The composite containing all color-specific controls. 
	 */
    private Composite colorControls;

	/**
	 * Map of defintion id->RGB objects that map to changes expressed in this
	 * UI session.  These changes should be made in preferences and the 
	 * registry.
	 */
	private Map colorPreferencesToSet = new HashMap(7);
	
	private CascadingColorRegistry colorRegistry;
	private Button colorResetButton;

	private ColorSelector colorSelector;

	/**
	 * Map of defintion id->RGB objects that map to changes expressed in this
	 * UI session.  These changes should be made in the registry.
	 */
	private Map colorValuesToSet = new HashMap(7);	

    private TableColumn column;
	private Text commentText;
    
	/**
	 * The composite that contains the font or color controls (or none).
	 */
    private Composite controlArea;
    
    /**
     * The layout for the controlArea.
     */
    private StackLayout controlAreaLayout;
    
    /**
     * The composite to use when no preview is available. 
     */
    private Composite defaultPreviewControl;
	private Text descriptionText;
    private List dialogFontWidgets = new ArrayList();    
    private Button fontChangeButton;
    
    /**
     * The composite containing all font-specific controls. 
     */
    private Composite fontControls;
	private Map fontPreferencesToSet = new HashMap(7);
	private CascadingFontRegistry fontRegistry;
	private CascadingGradientRegistry gradientRegistry;
	private Button fontResetButton;
    private Button fontSystemButton;
	
	/**
	 * Map of defintion id->FontData[] objects that map to changes expressed in 
	 * this UI session.  These changes should be made in preferences and the 
	 * registry.
	 */    
	private Map fontValuesToSet = new HashMap(7);
	
	private Map gradientValuesToSet = new HashMap(7);
	
	private Map gradientPreferencesToSet = new HashMap(7);
    
    private Composite gradientControls;
	
	/**
	 * The list of fonts and colors.
	 */
	private TableViewer presentationList;
	
	/**
	 * The composite that is parent to all previews.
	 */
    private Composite previewComposite;
    
    /**
     * A mapping from PresentationCategory->Composite for the created previews.
     */
	private Map previewMap = new HashMap(7);
	
	/**
	 * Set containing all IPresentationPreviews created. 
	 */
	private Set previewSet = new HashSet(7);
	
	/**
	 * The layout for the previewComposite.
	 */
    private StackLayout stackLayout;
    
    /**
	 * The last category viewed.
 	 */ 
    private static String lastCategory;

    private final IThemeRegistry themeRegistry;

	/**
	 * Create a new instance of the receiver. 
	 */
	public ColorsAndFontsPreferencePage() {
        themeRegistry = WorkbenchPlugin.getDefault().getThemeRegistry();
		//no-op
	}
    
	/**
	 * Create a button for the preference page.
	 * @param parent
	 * @param label
	 */
	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH | SWT.CENTER);
		button.setText(label);
		myApplyDialogFont(button);
		setButtonLayoutData(button);
		button.setEnabled(false);
		return button;
	}  

    /**
     * @param mainColumn
     */
    private ThemeElementCategory createCategoryControl(Composite mainColumn) {
    	Label label = new Label(mainColumn, SWT.LEFT);
    	label.setText(RESOURCE_BUNDLE.getString("category")); //$NON-NLS-1$
    	myApplyDialogFont(label);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;		
		label.setLayoutData(data);    	
        categoryCombo = new Combo(mainColumn, SWT.NONE | SWT.READ_ONLY);
        myApplyDialogFont(categoryCombo);
        
        ThemeElementCategory [] categories = themeRegistry.getCategories();
        for (int i = 0; i < categories.length; i++) {
            categoryCombo.add(categories[i].getLabel());
        }
        categoryCombo.add(RESOURCE_BUNDLE.getString("uncategorized")); //$NON-NLS-1$
        
        data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;		
		categoryCombo.setLayoutData(data);
         
        if (categories.length == 0) {
        	categoryCombo.select(0);
        	return null;
        }
        	
        int idx = 0;
        if (lastCategory != null) {
        	idx = Arrays.binarySearch(categories, lastCategory, IThemeRegistry.ID_COMPARATOR);
        	if (idx < 0) {
        		categoryCombo.select(categories.length);
        		return null; // unknown category.   default to uncategorized
        	}    	
        }
        
        categoryCombo.select(idx);
        return categories[idx];
    }

	/**
	 * Create the color selection control. 
	 */
	private void createColorControl() {	    
		Composite composite = new Composite(colorControls, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

//		Label label = new Label(composite, SWT.LEFT);
//		label.setText(RESOURCE_BUNDLE.getString("value")); //$NON-NLS-1$
//		myApplyDialogFont(label);
//		Dialog.applyDialogFont(label);
//		GridData data = new GridData(GridData.FILL_HORIZONTAL);
//		data.horizontalSpan = 2;
//		label.setLayoutData(data);

		colorSelector = new ColorSelector(composite);
		colorSelector.getButton().setLayoutData(new GridData());
		myApplyDialogFont(colorSelector.getButton());
		colorSelector.setEnabled(false);

		colorResetButton = createButton(composite, RESOURCE_BUNDLE.getString("reset")); //$NON-NLS-1$		
	}

	/**
	 * Create the text box that will contain the current colors comment text 
	 * (if any).  This includes whether the color is set to its default value or
	 * and what that might be in the case of a mapping.
	 * 
	 * @param parent the parent <code>Composite</code>.
	 */
	private void createCommentControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.LEFT);
		label.setText(RESOURCE_BUNDLE.getString("comment")); //$NON-NLS-1$
		myApplyDialogFont(label);

		commentText = new Text(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
		commentText.setLayoutData(new GridData(GridData.FILL_BOTH));
		commentText.setText("\n\n"); //$NON-NLS-1$
		myApplyDialogFont(commentText);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (appliedDialogFont != null)
					appliedDialogFont.dispose();
			}
		});		
		Composite mainColumn = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		mainColumn.setFont(parent.getFont());
		mainColumn.setLayout(layout);
		
		ThemeElementCategory category = createCategoryControl(mainColumn);

		createList(mainColumn);
		Composite controlColumn = new Composite(mainColumn, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		controlColumn.setLayoutData(data);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		controlColumn.setLayout(layout);

		controlArea = new Composite(controlColumn, SWT.NONE);
		controlAreaLayout = new StackLayout();
		controlArea.setLayout(controlAreaLayout);
		
		colorControls = new Composite(controlArea, SWT.NONE);
		colorControls.setLayout(new FillLayout());
		createColorControl();
		
		fontControls = new Composite(controlArea, SWT.NONE);
		fontControls.setLayout(new FillLayout());
		createFontControl();
		
		gradientControls = new Composite(controlArea, SWT.NONE);
		fontControls.setLayout(new FillLayout());
		createGradientControls();
		
		createCommentControl(controlColumn);

		createDescriptionControl(mainColumn);

		createPreviewControl(mainColumn);
		
		updateCategorySelection(category == null ? null : category.getId());
		
		hookListeners();
		
		return mainColumn;
	}

    /**
	 * Create the text box that will contain the current colors description 
	 * text (if any).
	 * 
	 * @param parent the parent <code>Composite</code>.
	 */
	private void createDescriptionControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		data.horizontalSpan = 3;
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.LEFT);
		label.setText(RESOURCE_BUNDLE.getString("description")); //$NON-NLS-1$
		myApplyDialogFont(label);

		descriptionText = new Text(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
		data = new GridData(GridData.FILL_BOTH);
		descriptionText.setText("\n\n"); //$NON-NLS-1$
		descriptionText.setLayoutData(data);
		myApplyDialogFont(descriptionText);
	}

	/**
     * @param fontControls2
     */
    private void createFontControl() {
		Composite composite = new Composite(fontControls, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

//		Label label = new Label(composite, SWT.LEFT);
//		label.setText(RESOURCE_BUNDLE.getString("value")); //$NON-NLS-1$
//		myApplyDialogFont(label);
//		GridData data = new GridData(GridData.FILL_HORIZONTAL);
//		data.horizontalSpan = 2;
//		label.setLayoutData(data);
		
		fontSystemButton = createButton(composite, WorkbenchMessages.getString("FontsPreference.useSystemFont")); //$NON-NLS-1$

		fontChangeButton = createButton(composite, JFaceResources.getString("openChange")); //$NON-NLS-1$

		fontResetButton = createButton(composite, RESOURCE_BUNDLE.getString("reset")); //$NON-NLS-1$        
    }

    /**
     * 
     */
    private void createGradientControls() {
        // TODO Auto-generated method stub
        
    }

	/**
	 * Create the <code>ListViewer</code> that will contain all color 
	 * definitions as defined in the extension point.
	 * 
	 * @param parent the parent <code>Composite</code>.
	 */
	private void createList(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.LEFT);
		label.setText(RESOURCE_BUNDLE.getString("colorsAndFonts")); //$NON-NLS-1$
		myApplyDialogFont(label);

		presentationList =
			new TableViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		presentationList.setContentProvider(new ArrayContentProvider());
		PresentationLabelProvider provider = new PresentationLabelProvider();
		presentationList.setLabelProvider(provider);
		presentationList.setSorter(new ViewerSorter());
		
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH);		
		presentationList.getControl().setLayoutData(data);
		column = new TableColumn(presentationList.getTable(), SWT.LEFT);
        myApplyDialogFont(presentationList.getControl());
	}

    /**
     * @param mainColumn
     */
    private void createPreviewControl(Composite mainColumn) {
        Composite composite = new Composite(mainColumn, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        data.widthHint = 400;
        data.heightHint = 175;
        composite.setLayoutData(data);
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        Label label = new Label(composite, SWT.LEFT);
        label.setText(RESOURCE_BUNDLE.getString("preview")); //$NON-NLS-1$
        myApplyDialogFont(label);
        previewComposite = new Composite(composite, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        previewComposite.setLayoutData(data);
        stackLayout = new StackLayout();
        previewComposite.setLayout(stackLayout);
    }
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
     */
    public void dispose() {        
        super.dispose();
        
        for (Iterator i = previewSet.iterator(); i.hasNext();) {
            IPresentationPreview preview = (IPresentationPreview) i.next();
            try {
                preview.dispose();
            }
            catch (RuntimeException e) {
                WorkbenchPlugin.log(RESOURCE_BUNDLE.getString("errorDisposePreviewLog"), StatusUtil.newStatus(IStatus.ERROR, e.getMessage(), e)); //$NON-NLS-1$
            }
        }
        
        colorRegistry.dispose();
        fontRegistry.dispose();
        gradientRegistry.dispose();
    }

	/**
	 * Get the ancestor of the given color, if any.
	 * 
	 * @param definition the descendant <code>ColorDefinition</code>.
	 * @return the ancestror <code>ColorDefinition</code>, or <code>null</code> 
	 * 		if none.
	 */
	private ColorDefinition getColorAncestor(ColorDefinition definition) {
		String defaultsTo = definition.getDefaultsTo();
		if (defaultsTo == null)
			return null;

		return themeRegistry.findColor(defaultsTo);
	}

	/**
	 * Get the RGB value of the given colors ancestor, if any.
	 * 
	 * @param definition the descendant <code>ColorDefinition</code>.
	 * @return the ancestror <code>RGB</code>, or <code>null</code> if none.
	 */
	private RGB getColorAncestorValue(ColorDefinition definition) {
		ColorDefinition ancestor = getColorAncestor(definition);
		if (ancestor == null)
			return null;

		return getColorValue(ancestor);
	}

	/**
	 * Get the RGB value for the specified definition.  Cascades through 
	 * preferenceToSet, valuesToSet and finally the registry.
	 * 
	 * @param definition the <code>ColorDefinition</code>.
	 * @return the <code>RGB</code> value.
	 */
	private RGB getColorValue(ColorDefinition definition) {
		String id = definition.getId();
		RGB updatedRGB = (RGB) colorPreferencesToSet.get(id);
		if (updatedRGB == null) {
			updatedRGB = (RGB) colorValuesToSet.get(id);
			if (updatedRGB == null)
				updatedRGB = JFaceResources.getColorRegistry().getRGB(id);
		}
		return updatedRGB;
	}

	/**
     * @return Return the default "No preview available." preview.
     */
    private Composite getDefaultPreviewControl() { 
        if (defaultPreviewControl == null) {
	        defaultPreviewControl = new Composite(previewComposite, SWT.NONE);
	        defaultPreviewControl.setLayout(new FillLayout());
	        Label l = new Label(defaultPreviewControl, SWT.LEFT);
	        l.setText(RESOURCE_BUNDLE.getString("noPreviewAvailable")); //$NON-NLS-1$
	        myApplyDialogFont(l);
        }
        return defaultPreviewControl;
    }

	/**
	 * Get colors that descend from the provided color.
	 * 
	 * @param definition the ancestor <code>ColorDefinition</code>.
	 * @return the ColorDefinitions that have the provided definition as their 
	 * 		defaultsTo attribute.
	 */
	private ColorDefinition[] getDescendantColors(ColorDefinition definition) {
		List list = new ArrayList(5);
		String id = definition.getId();

		ColorDefinition[] colors = themeRegistry.getColors();
        ColorDefinition[] sorted = new ColorDefinition[colors.length];
		System.arraycopy(colors, 0, sorted, 0, sorted.length);

		Arrays.sort(sorted, new IThemeRegistry.HierarchyComparator(colors));

		for (int i = 0; i < sorted.length; i++) {
			if (id.equals(sorted[i].getDefaultsTo()))
				list.add(sorted[i]);
		}

		return (ColorDefinition[]) list.toArray(new ColorDefinition[list.size()]);
	}

    /**
     * @param definition
     * @return
     */
    private FontDefinition[] getDescendantFonts(FontDefinition definition) {
		List list = new ArrayList(5);
		String id = definition.getId();
		
		FontDefinition[] fonts = themeRegistry.getFonts();
        FontDefinition[] sorted = new FontDefinition[fonts.length];
		System.arraycopy(fonts, 0, sorted, 0, sorted.length);

		Arrays.sort(sorted, new IThemeRegistry.HierarchyComparator(fonts));

		for (int i = 0; i < sorted.length; i++) {
			if (id.equals(sorted[i].getDefaultsTo()))
				list.add(sorted[i]);
		}

		return (FontDefinition[]) list.toArray(new FontDefinition[list.size()]);
    }

    /**
     * @param definition
     * @return
     */
    private FontDefinition getFontAncestor(FontDefinition definition) {
		String defaultsTo = definition.getDefaultsTo();
		if (defaultsTo == null)
			return null;

		return themeRegistry.findFont(defaultsTo);
    }

    /**
     * @param definition
     * @return
     */
    private FontData[] getFontAncestorValue(FontDefinition definition) {
		FontDefinition ancestor = getFontAncestor(definition);
		if (ancestor == null)
			return PreferenceConverter.getDefaultFontDataArray(getPreferenceStore(), definition.getId());

		return getFontValue(ancestor);
    }

    /**
     * @param definition
     * @return
     */
    protected FontData[] getFontValue(FontDefinition definition) {
		String id = definition.getId();
		FontData [] updatedFD = (FontData []) fontPreferencesToSet.get(id);
		if (updatedFD == null) {
			updatedFD = (FontData []) fontValuesToSet.get(id);
			if (updatedFD == null)
				updatedFD = JFaceResources.getFontRegistry().getFontData(id);
		}
		return updatedFD;
    }

    /**
     * @return
     */
    protected ColorDefinition getSelectedColorDefinition() {
        Object o = ((IStructuredSelection)presentationList.getSelection()).getFirstElement();
        if (o instanceof ColorDefinition)
            return (ColorDefinition) o;
        return null;        
    }    
    
    /**
     * @return
     */
    protected FontDefinition getSelectedFontDefinition() {
        Object o = ((IStructuredSelection)presentationList.getSelection()).getFirstElement();
        if (o instanceof FontDefinition)
            return (FontDefinition) o;
        return null;        
    }

	/**
	 * Hook all control listeners.
	 */
	private void hookListeners() {
        categoryCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                int index = categoryCombo.getSelectionIndex();
                if (index == categoryCombo.getItemCount() - 1)
                    updateCategorySelection(null);
                else 
                    updateCategorySelection(themeRegistry.getCategories()[index].getId()); 
                
                updateColorControls(null);
            }
        });        
	    
		colorSelector.addListener(new IPropertyChangeListener() {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				ColorDefinition definition = getSelectedColorDefinition();

				RGB newRGB = (RGB) event.getNewValue();
				if (definition != null && newRGB != null && !newRGB.equals(event.getOldValue())) {
					setColorPreferenceValue(definition, newRGB);
					setRegistryValue(definition, newRGB);
				}

				updateColorControls(definition);
			}
		});

		presentationList.addSelectionChangedListener(new ISelectionChangedListener() {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty()) {
					swapNoControls();
					updateColorControls(null);
				} else {
					Object element = ((IStructuredSelection) event.getSelection())
							.getFirstElement();
					if (element instanceof ColorDefinition) {
						swapColorControls();
						updateColorControls((ColorDefinition) element);
					}
					else if (element instanceof FontDefinition) {
						swapFontControls();
						updateFontControls((FontDefinition) element);
					}
					else if (element instanceof GradientDefinition) {
						swapGradientControls();
						updateGradientControls((GradientDefinition) element);
					}					
				}
			}
		});

		colorResetButton.addSelectionListener(new SelectionAdapter() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				ColorDefinition definition = getSelectedColorDefinition();
				if (resetColor(definition))
					updateColorControls(definition);
			}
		});
		
		fontResetButton.addSelectionListener(new SelectionAdapter() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				FontDefinition definition = getSelectedFontDefinition();
				if (resetFont(definition))
					updateFontControls(definition);
			}
		});
		
		
		fontChangeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				FontDefinition definition = getSelectedFontDefinition();
				if (definition != null) {
					FontDialog fontDialog =
						new FontDialog(fontChangeButton.getShell());
					FontData[] currentData = getFontValue(definition);
					fontDialog.setFontList(currentData);
					if (fontDialog.open() != null) {
						setFontPreferenceValue(definition, fontDialog.getFontList());
						setRegistryValue(definition, fontDialog.getFontList());
					}
					
					updateFontControls(definition);					
				}
			}
		});
		
		fontSystemButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				FontDefinition definition = getSelectedFontDefinition();
				if (definition != null) {
					FontData[] defaultFontData =
						JFaceResources.getDefaultFont().getFontData();
					setFontPreferenceValue(definition, defaultFontData);
					setRegistryValue(definition, defaultFontData);
					
					updateFontControls(definition);
				}
			}
		});

		presentationList.getControl().addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                column.setWidth(presentationList.getControl().getSize().x - 4);
                // TODO: why is this 4?  Why is 4 magical?
            }});
		
	}

    /* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		setPreferenceStore(workbench.getPreferenceStore());
	    colorRegistry = new CascadingColorRegistry(
	    		JFaceResources.getColorRegistry());
	    fontRegistry = new CascadingFontRegistry(
	            JFaceResources.getFontRegistry());
	    gradientRegistry = new CascadingGradientRegistry(
	            JFaceResources.getGradientRegistry());	            
	}

	/**
	 * Answers whether the definition is currently set to the default value.
	 * 
	 * @param definition the <code>ColorDefinition</code> to check.
	 * @return Return whether the definition is currently mapped to the default 
	 * 		value, either in the preference store or in the local change record 
	 * 		of this preference page.
	 */
	private boolean isDefault(ColorDefinition definition) {
		String id = definition.getId();

		if (colorPreferencesToSet.containsKey(id)) {
			if (definition.getValue() != null) { // value-based color
				if (colorPreferencesToSet
					.get(id)
					.equals(StringConverter.asRGB(getPreferenceStore().getDefaultString(id), null)))
					return true;
			} else {
				if (colorPreferencesToSet.get(id).equals(getColorAncestorValue(definition)))
					return true;
			}
		} else {
			if (definition.getValue() != null) { // value-based color
				if (getPreferenceStore().isDefault(id))
					return true;
			} else {
				// a descendant is default if it's the same value as its ancestor
				if (getColorValue(definition).equals(getColorAncestorValue(definition)))
					return true;
			}
		}
		return false;
	}

    /**
     * @param definition
     * @return
     */
    private boolean isDefault(FontDefinition definition) {
		String id = definition.getId();

		if (fontPreferencesToSet.containsKey(id)) {
			if (definition.getValue() != null) { // value-based font
				if (Arrays.equals((FontData[]) fontPreferencesToSet.get(id), 
				        new FontData [] {
				        	StringConverter.asFontData(getPreferenceStore().getDefaultString(id), null)
				        }))
					return true;
			} else {
			    FontData [] ancestor = getFontAncestorValue(definition);
			    if (Arrays.equals((FontData[]) fontPreferencesToSet.get(id), ancestor))
					return true;
			}
		} else {
			if (definition.getValue() != null) { // value-based font
				if (getPreferenceStore().isDefault(id))
					return true;
			} else {
			    FontData [] ancestor = getFontAncestorValue(definition);
			    if (ancestor == null)
			        return true;
			    
				// a descendant is default if it's the same value as its ancestor
				if (Arrays.equals(getFontValue(definition), ancestor))
					return true;
			}
		}
		return false;
    }
	
	/**
	 * Apply the dialog font to the control and store 
	 * it for later so that it can be used for a later
	 * update.
	 * @param control
	 */
	private void myApplyDialogFont(Control control) {
		control.setFont(JFaceResources.getDialogFont());
		dialogFontWidgets.add(control);
	}	
    
	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	protected void performApply() {
		super.performApply();

		//Apply the default font to the dialog.
		Font oldFont = appliedDialogFont;
		
		FontDefinition fontDefinition = themeRegistry.findFont(JFaceResources.DIALOG_FONT);
		if (fontDefinition == null)
			return;
			
        FontData[] newData =
			getFontValue(fontDefinition);

		appliedDialogFont = new Font(getControl().getDisplay(), newData);

		updateForDialogFontChange(appliedDialogFont);
		getApplyButton().setFont(appliedDialogFont);
		getDefaultsButton().setFont(appliedDialogFont);

		if (oldFont != null)
			oldFont.dispose();
	}

    /**
     * 
     */
    private void performColorDefaults() {
        ColorDefinition[] definitions = themeRegistry.getColors();

		// apply defaults in depth-order.
		ColorDefinition[] definitionsCopy = new ColorDefinition[definitions.length];
		System.arraycopy(definitions, 0, definitionsCopy, 0, definitions.length);

		Arrays.sort(definitionsCopy, new IThemeRegistry.HierarchyComparator(definitions));

		for (int i = 0; i < definitionsCopy.length; i++)
			resetColor(definitionsCopy[i]);

		updateColorControls(getSelectedColorDefinition());
    }

    /**
     * @return
     */
    private boolean performColorOk() {
        for (Iterator i = colorPreferencesToSet.keySet().iterator(); i.hasNext();) {
			String id = (String) i.next();
			RGB rgb = (RGB) colorPreferencesToSet.get(id);
			String rgbString = StringConverter.asString(rgb);
			String storeString = getPreferenceStore().getString(id);

			if (!rgbString.equals(storeString)) {
				JFaceResources.getColorRegistry().put(id, rgb);
				colorValuesToSet.remove(id); // already taken care of.
				getPreferenceStore().setValue(id, rgbString);
			}
		}

		colorPreferencesToSet.clear();

		for (Iterator i = colorValuesToSet.keySet().iterator(); i.hasNext();) {
			String id = (String) i.next();
			RGB rgb = (RGB) colorValuesToSet.get(id);

			JFaceResources.getColorRegistry().put(id, rgb);
		}

		colorValuesToSet.clear();

		return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		performColorDefaults();		
		performFontDefaults();
	}

	/**
     * 
     */
    private void performFontDefaults() {
		FontDefinition[] definitions = themeRegistry.getFonts();

		// apply defaults in depth-order.
		FontDefinition[] definitionsCopy = new FontDefinition[definitions.length];
		System.arraycopy(definitions, 0, definitionsCopy, 0, definitions.length);

		Arrays.sort(definitionsCopy, new IThemeRegistry.HierarchyComparator(definitions));

		for (int i = 0; i < definitionsCopy.length; i++)
			resetFont(definitionsCopy[i]);

		updateFontControls(getSelectedFontDefinition());		
    }

	/**
     * @return
     */
    private boolean performFontOk() {
        for (Iterator i = fontPreferencesToSet.keySet().iterator(); i.hasNext();) {
			String id = (String) i.next();
			FontData [] fd = (FontData []) fontPreferencesToSet.get(id);
			JFaceResources.getFontRegistry().put(id, fd);
			fontValuesToSet.remove(id); // remove from the value list because it's already been set.
			getPreferenceStore().setValue(id, PreferenceConverter.getStoredRepresentation(fd));
		}

		fontPreferencesToSet.clear();

		for (Iterator i = fontValuesToSet.keySet().iterator(); i.hasNext();) {
			String id = (String) i.next();
			FontData [] fd = (FontData []) fontValuesToSet.get(id);

			JFaceResources.getFontRegistry().put(id, fd);
		}

		fontValuesToSet.clear();
		
		return true;
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		return performColorOk() && performFontOk();
	}

    /**
	 * Resets the supplied definition to its default value.
	 * 
	 * @param definition the <code>ColorDefinition</code> to reset.
	 * @return whether any change was made.
	 */
	private boolean resetColor(ColorDefinition definition) {
		if (!isDefault(definition)) {

			RGB newRGB;
			if (definition.getValue() != null) {
				newRGB =
					StringConverter.asRGB(
						getPreferenceStore().getDefaultString(definition.getId()),
						null);
			} else {
				newRGB = getColorAncestorValue(definition);
			}

			if (newRGB != null) {
				setColorPreferenceValue(definition, newRGB);
				setRegistryValue(definition, newRGB);
				return true;
			}
		}
		return false;
	}

	/**
     * @param definition
     * @return
     */
    protected boolean resetFont(FontDefinition definition) {
		if (!isDefault(definition)) {

			FontData [] newFD;
			if (definition.getDefaultsTo() != null) {
				newFD = getFontAncestorValue(definition);
			} 
			else {
				newFD = PreferenceConverter.getDefaultFontDataArray(
						getPreferenceStore(),
						definition.getId());
			}

			if (newFD != null) {
				setFontPreferenceValue(definition, newFD);
				setRegistryValue(definition, newFD);
				return true;
			}
		}
		return false;
    }

    /**
	 * Set the value (in preferences) for the given color.  
	 * 
	 * @param definition the <code>ColorDefinition</code> to set.
	 * @param newRGB the new <code>RGB</code> value for the definitions 
	 * 		identifier.
	 */
	protected void setColorPreferenceValue(ColorDefinition definition, RGB newRGB) {
		setDescendantRegistryValues(definition, newRGB);
		colorPreferencesToSet.put(definition.getId(), newRGB);
	}

	/**
	 * Set the value (in registry) for the given colors children.  
	 * 
	 * @param definition the <code>ColorDefinition</code> whos children should 
	 * 		be set.
	 * @param newRGB the new <code>RGB</code> value for the definitions 
	 * 		identifier.
	 */
	private void setDescendantRegistryValues(ColorDefinition definition, RGB newRGB) {
		ColorDefinition[] children = getDescendantColors(definition);

		for (int i = 0; i < children.length; i++) {
			if (isDefault(children[i])) {
			    setDescendantRegistryValues(children[i], newRGB);
			    setRegistryValue(children[i], newRGB);
				colorValuesToSet.put(children[i].getId(), newRGB);				
			}
		}
	}

    /**
     * @param definition
     * @param datas
     */
    private void setDescendantRegistryValues(FontDefinition definition, FontData[] datas) {
		FontDefinition[] children = getDescendantFonts(definition);

		for (int i = 0; i < children.length; i++) {
			if (isDefault(children[i])) {
			    setDescendantRegistryValues(children[i], datas);
			    setRegistryValue(children[i], datas);
				fontValuesToSet.put(children[i].getId(), datas);				
			}
		}        
    }

    /**
     * @param definition
     * @param datas
     */
    protected void setFontPreferenceValue(FontDefinition definition, FontData[] datas) {
		setDescendantRegistryValues(definition, datas);
		fontPreferencesToSet.put(definition.getId(), datas);
    }
	
	/**
	 * Updates the working registry.
	 * @param definition
	 * @param newRGB
	 */
    protected void setRegistryValue(ColorDefinition definition, RGB newRGB) {
        colorRegistry.put(definition.getId(), newRGB);
    }	

    /**
     * @param definition
     * @param datas
     */
    protected void setRegistryValue(FontDefinition definition, FontData[] datas) {
		fontRegistry.put(definition.getId(), datas);        
    }

    /**
     * Swap in the color selection controls.
     */
    protected void swapColorControls() {    
        controlAreaLayout.topControl = colorControls;
        controlArea.layout();
    }

    /**
     * Swap in the font selection controls.
     */
    protected void swapFontControls() {
        controlAreaLayout.topControl = fontControls;
        controlArea.layout();        
    }

    /**
     * 
     */
    protected void swapGradientControls() {
        controlAreaLayout.topControl = gradientControls;
        controlArea.layout();        
    }

    /**
     * Swap in no controls (empty the control area)
     */    
    protected void swapNoControls() {
        controlAreaLayout.topControl = null;
        controlArea.layout();        
    }

	/**
	 * Set the color list.
	 * @param category the category to use.
	 */
	private void updateCategorySelection(String categoryId) {
		Object [] defintions;
		String key = categoryId;
		ThemeElementCategory category = null;
		
		if (categoryId == null) {
		    key = "uncategorized"; //$NON-NLS-1$
		}
		else {		
			int idx = Arrays.binarySearch(themeRegistry.getCategories(), categoryId, IThemeRegistry.ID_COMPARATOR);
			if (idx == -1)
				categoryId = null;	
			else 
				category = themeRegistry.getCategories()[idx];	
		}		
		
		lastCategory = key;
		
		defintions = (Object []) categoryMap.get(key);
		if (defintions == null) {	
		    ArrayList list = new ArrayList();
		    			
			ColorDefinition[] colorDefinitions = themeRegistry.getColors();
            for (int i = 0; i < colorDefinitions.length; i++) {
			    String catId = colorDefinitions[i].getCategoryId();
			    if ((catId == null && categoryId == null) || (catId != null && categoryId != null && categoryId.equals(catId))) {
			        list.add(colorDefinitions[i]);
			    }
			}
			
			FontDefinition[] fontDefinitions = themeRegistry.getFonts();
            for (int i = 0; i < fontDefinitions.length; i++) {
			    String catId = fontDefinitions[i].getCategoryId();
			    if ((catId == null && categoryId == null) || (catId != null && categoryId != null && categoryId.equals(catId))) {
			        list.add(fontDefinitions[i]);
			    }
			}

			GradientDefinition[] gradientDefinitions = themeRegistry.getGradients();
            for (int i = 0; i < gradientDefinitions.length; i++) {
			    String catId = gradientDefinitions[i].getCategoryId();
			    if ((catId == null && categoryId == null) || (catId != null && categoryId != null && categoryId.equals(catId))) {
			        list.add(gradientDefinitions[i]);
			    }
			}
			
			defintions = new Object[list.size()];
			list.toArray(defintions);
			categoryMap.put(key, defintions);
		}
	
		presentationList.setInput(defintions);		
		
		Composite previewControl = (Composite) previewMap.get(key);
		if (previewControl == null) {
		    if (category != null) {
		        try {
                    IPresentationPreview preview = category.createPreview();
                    if (preview != null) {
	                    previewControl = new Composite(previewComposite, SWT.NONE);
	                    previewControl.setLayout(new FillLayout());
	                    myApplyDialogFont(previewControl);
	                    preview.createControl(previewControl, colorRegistry, fontRegistry, gradientRegistry);
	                    previewSet.add(preview);
                    }
                } catch (CoreException e) {
                    previewControl = new Composite(previewComposite, SWT.NONE);
                    previewControl.setLayout(new FillLayout());
                    myApplyDialogFont(previewControl);
                    Text error = new Text(previewControl, SWT.WRAP | SWT.READ_ONLY);
                    error.setText(RESOURCE_BUNDLE.getString("errorCreatingPreview")); //$NON-NLS-1$
                    WorkbenchPlugin.log(RESOURCE_BUNDLE.getString("errorCreatePreviewLog"), StatusUtil.newStatus(IStatus.ERROR, e.getMessage(), e)); //$NON-NLS-1$
                }		                        
		    }
		}
		if (previewControl == null) {
		    previewControl = getDefaultPreviewControl();
		}
		previewMap.put(key, previewControl);
		stackLayout.topControl = previewControl;
		previewComposite.layout();
	}

	/**
	 * Update the color controls based on the supplied definition.
	 * 
	 * @param definition The currently selected <code>ColorDefinition</code>.
	 */
	protected void updateColorControls(ColorDefinition definition) {
		if (definition != null)
			colorSelector.setColorValue(getColorValue(definition));

		if (definition != null) {
			colorResetButton.setEnabled(true);
			colorSelector.setEnabled(true);
			if (isDefault(definition)) {
				if (definition.getDefaultsTo() != null) {
					int idx =
						Arrays.binarySearch(themeRegistry.getColors(),
							definition.getDefaultsTo(),
							IThemeRegistry.ID_COMPARATOR);

					if (idx >= 0) {
						commentText.setText(MessageFormat.format(RESOURCE_BUNDLE.getString("Colors.currentlyMappedTo"), //$NON-NLS-1$
						new Object[] { themeRegistry.getColors()[idx].getLabel()}));
					} else
						commentText.setText(""); //$NON-NLS-1$
				} else
					commentText.setText(RESOURCE_BUNDLE.getString("Colors.currentlyDefault")); //$NON-NLS-1$
			} else
				commentText.setText(RESOURCE_BUNDLE.getString("Colors.customValue")); //$NON-NLS-1$

			String description = definition.getDescription();
			descriptionText.setText(description == null ? "" : description); //$NON-NLS-1$
		} else {
			colorResetButton.setEnabled(false);
			colorSelector.setEnabled(false);
			commentText.setText(""); //$NON-NLS-1$
			descriptionText.setText(""); //$NON-NLS-1$
		}
	}

    protected void updateFontControls(FontDefinition definition) {
		if (definition != null) {
		    fontSystemButton.setEnabled(true);
			fontResetButton.setEnabled(true);
			fontChangeButton.setEnabled(true);
			String valueString = StringConverter.asString(getFontValue(definition)[0]);
			if (isDefault(definition)) {
				if (definition.getDefaultsTo() != null) {
					int idx =
						Arrays.binarySearch(
							themeRegistry.getFonts(),
							definition.getDefaultsTo(),
							IThemeRegistry.ID_COMPARATOR);

					if (idx >= 0) {
						commentText.setText(MessageFormat.format(RESOURCE_BUNDLE.getString("Fonts.currentlyMappedTo"), //$NON-NLS-1$
						        new Object[] { themeRegistry.getFonts()[idx].getLabel(), valueString}));
					} else
						commentText.setText(""); //$NON-NLS-1$
				} else
					commentText.setText(MessageFormat.format(RESOURCE_BUNDLE.getString("Fonts.currentlyDefault"), //$NON-NLS-1$
					        	new Object[] {valueString})); 
			} else
				commentText.setText(MessageFormat.format(RESOURCE_BUNDLE.getString("Fonts.customValue"),  //$NON-NLS-1$
						new Object [] {valueString}));

			String description = definition.getDescription();
			descriptionText.setText(description == null ? "" : description); //$NON-NLS-1$
		} else {
		    fontSystemButton.setEnabled(false);
			fontResetButton.setEnabled(false);
			fontChangeButton.setEnabled(false);
			commentText.setText(""); //$NON-NLS-1$
			descriptionText.setText(""); //$NON-NLS-1$
		}
    }
    
	/**
	 * Update for a change in the dialog font.
	 * @param newFont
	 */
	private void updateForDialogFontChange(Font newFont) {
		Iterator iterator = dialogFontWidgets.iterator();
		while (iterator.hasNext()) {
			((Control) iterator.next()).setFont(newFont);
		}
	}    

    /**
     * @param definition
     */
    protected void updateGradientControls(GradientDefinition definition) {
		if (definition != null) {
			// set controls enabled
			String valueString = StringConverter.asString(getGradientValue(definition));
			if (isDefault(definition)) {
				commentText.setText(MessageFormat.format(RESOURCE_BUNDLE.getString("Gradients.currentlyDefault"), //$NON-NLS-1$
					        	new Object[] {valueString})); 
			} else
				commentText.setText(MessageFormat.format(RESOURCE_BUNDLE.getString("Gradients.customValue"),  //$NON-NLS-1$
						new Object [] {valueString}));

			String description = definition.getDescription();
			descriptionText.setText(description == null ? "" : description); //$NON-NLS-1$
		} else {
			// set controls enabled		
		}
        
    }

    /**
     * @param definition
     * @return
     */
    private GradientData getGradientValue(GradientDefinition definition) {
		String id = definition.getId();
		GradientData updatedGD = (GradientData) gradientPreferencesToSet.get(id);
		if (updatedGD == null) {
			updatedGD = (GradientData) gradientValuesToSet.get(id);
			if (updatedGD == null)
				updatedGD = JFaceResources.getGradientRegistry().getGradientData(id);
		}
		return updatedGD;
    }

    /**
     * @param definition
     * @return
     */
    private boolean isDefault(GradientDefinition definition) {
		String id = definition.getId();

        if (gradientPreferencesToSet.containsKey(id)) {			
		    GradientData ourFontValue = (GradientData) gradientPreferencesToSet.get(id);
		    if (ourFontValue.equals(PreferenceConverter.getDefaultGradient(getPreferenceStore(), id)))
		    	return true;
		} else {			
		    GradientData ourFontValue = getGradientValue(definition);
		    if (ourFontValue.equals(PreferenceConverter.getDefaultGradient(getPreferenceStore(), id))) 
		        return true;
		}
		return false;
    }
}
