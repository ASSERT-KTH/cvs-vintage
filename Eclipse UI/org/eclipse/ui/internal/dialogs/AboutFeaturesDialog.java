package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.Collator;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;

/**
 * Displays information about the product plugins.
 *
 * @private
 *		This class is internal to the workbench and must not be called outside the workbench
 */
public class AboutFeaturesDialog extends ProductInfoDialog {
	
	/**
	 * Table height in dialog units (value 150).
	 */
	private static final int TABLE_HEIGHT = 150;
	private static final int INFO_HEIGHT = 100;

	private final static int MORE_ID = IDialogConstants.CLIENT_ID + 1;
	private final static int PLUGINS_ID = IDialogConstants.CLIENT_ID + 2;
	
	private Table table;
	private Label imageLabel;	
	private StyledText text;
	private Composite infoArea;
	
	private Map cachedImages = new HashMap();

	private String columnTitles[] =
		{ WorkbenchMessages.getString("AboutFeaturesDialog.provider"), //$NON-NLS-1$
		WorkbenchMessages.getString("AboutFeaturesDialog.featureName"), //$NON-NLS-1$
		WorkbenchMessages.getString("AboutFeaturesDialog.version"), //$NON-NLS-1$
	};

	private AboutInfo[] featuresInfo;
	private AboutInfo aboutInfo;
	
	private int lastColumnChosen = 0;	// initially sort by provider
	private boolean reverseSort = false;	// initially sort ascending
	private AboutInfo lastSelection = null;
	private Button moreButton;
	private Button pluginsButton;
	
	private static Map featuresMap;

	/**
	 * Constructor for AboutFeaturesDialog
	 */
	public AboutFeaturesDialog(Shell parentShell) {
		super(parentShell);
		Workbench workbench = (Workbench)PlatformUI.getWorkbench();
		aboutInfo = workbench.getAboutInfo();
		featuresInfo = workbench.getFeaturesInfo();
		sortByProvider();
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case MORE_ID : {
				TableItem[] items = table.getSelection();
				if (items.length > 0) {
					AboutInfo info = (AboutInfo)items[0].getData();
					IFeature feature = getFeatureFor(info);
					if (feature != null) {
						IURLEntry entry = feature.getLicense();
						if (entry != null) {
							openLink(entry.getURL().toString());
							return;
						}
					}
					MessageDialog.openInformation(
						getShell(), 
						WorkbenchMessages.getString("AboutFeaturesDialog.noInfoTitle"), //$NON-NLS-1$
						WorkbenchMessages.getString("AboutFeaturesDialog.noInformation")); //$NON-NLS-1$
				}
				return;
			}
			case PLUGINS_ID : {
				TableItem[] items = table.getSelection();
				if (items.length > 0) {
					AboutInfo info = (AboutInfo)items[0].getData();
					IFeature feature = getFeatureFor(info);
					IPluginDescriptor[] descriptors;
					if (feature == null)
						descriptors = new IPluginDescriptor[0];	
					else
						descriptors = getPluginsFor(feature);
					AboutPluginsDialog d = 
						new AboutPluginsDialog(
							getShell(), 
							descriptors,
							WorkbenchMessages.getString("AboutFeaturesDialog.pluginInfoTitle"), //$NON-NLS-1$
							WorkbenchMessages.format("AboutFeaturesDialog.pluginInfoMessage",	new Object[] {info.getFeatureLabel()})); //$NON-NLS-1$
					d.open();
				}				
				return;
			}
		}
		super.buttonPressed(buttonId);
	}

	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		String title = aboutInfo.getProductName();
		if (title != null) { 
		newShell.setText(
			WorkbenchMessages.format(
				"AboutFeaturesDialog.shellTitle",	//$NON-NLS-1$
				new Object[] {title}));
		}
		WorkbenchHelp.setHelp(
			newShell,
			IHelpContextIds.ABOUT_FEATURES_DIALOG);
	} 
	/**
	 * Add buttons to the dialog's button bar.
	 *
	 * Subclasses should override.
	 *
	 * @param parent the button bar composite
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		moreButton = createButton(parent, MORE_ID, WorkbenchMessages.getString("AboutFeaturesDialog.moreInfo"), false);
		pluginsButton = createButton(parent, PLUGINS_ID, WorkbenchMessages.getString("AboutFeaturesDialog.pluginsInfo"), false);

		Label l = new Label(parent, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = (GridLayout)parent.getLayout();
		layout.numColumns++;
		layout.makeColumnsEqualWidth = false;
	
		Button b = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		b.setFocus();
		
		TableItem[] items = table.getSelection();
		if (items.length > 0) 
			updateButtons((AboutInfo)items[0].getData());
	}

	/**
	 * Create the contents of the dialog (above the button bar).
	 *
	 * Subclasses should overide.
	 *
	 * @param the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected Control createDialogArea(Composite parent) {
		setHandCursor(new Cursor(parent.getDisplay(), SWT.CURSOR_HAND));
		setBusyCursor(new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT));
		getShell().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (getHandCursor() != null)
					getHandCursor().dispose();
				if (getBusyCursor() != null)
					getBusyCursor().dispose();
			}
		});

		Composite outer = (Composite) super.createDialogArea(parent);

		createTable(outer);
		createColumns();
		createInfoArea(outer);

		GridData gridData =
			new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;

		// suggest a height for the table
		gridData.heightHint = convertVerticalDLUsToPixels(TABLE_HEIGHT);
		table.setLayoutData(gridData);

		return outer;
	}
	/** 
	 * Create the info area containing the image and text
	 */
	protected void createInfoArea(Composite parent) {
		infoArea = new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns = 2; 
		infoArea.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = convertVerticalDLUsToPixels(INFO_HEIGHT);
		infoArea.setLayoutData(data);
		
		imageLabel = new Label(infoArea, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.heightHint = 32;
		data.widthHint = 32;
		imageLabel.setLayoutData(data);
		
		// text on the right
		text = new StyledText(infoArea, SWT.MULTI | SWT.READ_ONLY);
		text.setCaret(null);
		text.setFont(parent.getFont());
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = true;
		text.setLayoutData(data);
		text.setCursor(null);
		text.setBackground(infoArea.getBackground());
		addListeners(text);
		
		TableItem[] items = table.getSelection();
		if (items.length > 0) 
			updateInfoArea((AboutInfo)items[0].getData());
	}		
	
	/**
	 * Create the table part of the dialog.
	 *
	 * @param the parent composite to contain the dialog area
	 */
	protected void createTable(Composite parent) {
		table =
			new Table(
				parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AboutInfo info = (AboutInfo)e.item.getData();
				updateInfoArea(info);
				updateButtons(info);
			}
		};
		table.addSelectionListener(listener);
	}
	/**
	 * @see Window#close()
	 */
	public boolean close() {
		Collection values = cachedImages.values();
		for (Iterator iter = values.iterator(); iter.hasNext();) {
			Image image = (Image) iter.next();
			image.dispose();
		}
		return super.close();
	}
	/**
	 * Returns a mapping from feature id to feature
	 */
	private Map getFeaturesMap() {
		if (featuresMap != null) 
			return featuresMap;
			
		featuresMap = new HashMap();

		IPluginRegistry reg = Platform.getPluginRegistry();
		if (reg == null) {
			MessageDialog.openError(
				getShell(), 
				WorkbenchMessages.getString("AboutFeaturesDialog.errorTitle"), //$NON-NLS-1$
				WorkbenchMessages.getString("AboutFeaturesDialog.unableToObtainFeatureInfo")); //$NON-NLS-1$
			return featuresMap;
		}
		
		final ILocalSite[] localSiteArray = new ILocalSite[1];
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				// this may take a few seconds
				try {
					localSiteArray[0] = SiteManager.getLocalSite();
				} catch (CoreException e) {
					MessageDialog.openError(
						getShell(), 
						WorkbenchMessages.getString("AboutFeaturesDialog.errorTitle"), //$NON-NLS-1$
						WorkbenchMessages.getString("AboutFeaturesDialog.unableToObtainFeatureInfo")); //$NON-NLS-1$
				}
			}
		});
		if (localSiteArray[0] == null)
			return featuresMap;
		
		IInstallConfiguration installConfiguration = localSiteArray[0].getCurrentConfiguration(); 
		IConfiguredSite[] configuredSites = installConfiguration.getConfiguredSites();

		for (int i = 0; i < configuredSites.length; i++) {
			IFeatureReference[] featureReferences = configuredSites[i].getConfiguredFeatures();
			for (int j = 0; j < featureReferences.length; j++) {
				IFeature feature;
				try {
					feature = featureReferences[j].getFeature();
				} catch (CoreException e) {
					// just skip it
					break;
				}
				String key = feature.getVersionedIdentifier().toString();
				featuresMap.put(key, feature);
			}
		}
		return featuresMap;
	}

	/**
	 * Return the feature for the given info
	 */
	private IFeature getFeatureFor(AboutInfo info) {
		Map map = getFeaturesMap();
		if (map == null) 
			return null;
		String key = info.getFeatureId() + "_" + info.getVersion();
		return (IFeature)map.get(key);
	}

	
	/**
	 * Return the plugins for the given feature
	 */
	private IPluginDescriptor[] getPluginsFor(IFeature feature) {
		IPluginRegistry reg = Platform.getPluginRegistry();
		if (reg == null)
			return new IPluginDescriptor[0];
		IPluginEntry[] pluginEntries = feature.getPluginEntries();	
		ArrayList plugins = new ArrayList();
		for (int k = 0; k < pluginEntries.length; k++) {
			VersionedIdentifier id = pluginEntries[k].getVersionedIdentifier();

			IPluginDescriptor desc = reg.getPluginDescriptor(id.getIdentifier(), id.getVersion());
			if (desc != null)
				plugins.add(desc);
		}
		return (IPluginDescriptor[])plugins.toArray(new IPluginDescriptor[plugins.size()]);	
	}
		


	/**
	 * Update the button enablement
	 */
	private void updateButtons(AboutInfo info) {
		if (info == null) {
			moreButton.setEnabled(false);
			pluginsButton.setEnabled(false);
			return;
		}
		boolean shouldEnable = true; // by default enable
		// Avoid creating the map just to determine enablement
		if (featuresMap != null) {
			IFeature feature = getFeatureFor(info);
			shouldEnable = feature != null && feature.getLicense() != null;
		}
		moreButton.setEnabled(shouldEnable);		
				
		// Assume there is at least one plugin		
		shouldEnable = true; // by default enable
		if (featuresMap != null) {
			IFeature feature = getFeatureFor(info);
			shouldEnable = feature != null;
		}
		pluginsButton.setEnabled(shouldEnable);
	}

	/**
	 * Update the info area
	 */
	private void updateInfoArea(AboutInfo info) {
		if (info == null) {
			imageLabel.setImage(null);
			text.setText(""); //$NON-NLS-1$
			return;
		}
		ImageDescriptor desc = info.getFeatureImage();
		Image image =  (Image)cachedImages.get(desc);
		if (image == null && desc != null) {
			image = desc.createImage();
			cachedImages.put(desc, image);
		}
		imageLabel.setImage(image);
		String aboutText = info.getAboutText();
		setItem(null);
		if (aboutText != null) {
			// get an about item
			setItem(scan(aboutText));
		}
		if (getItem() == null)
			text.setText(WorkbenchMessages.getString("AboutFeaturesDialog.noInformation"));
		else {
			text.setText(getItem().getText());	
			text.setCursor(null);
			setLinkRanges(text, getItem().getLinkRanges());
		}
	}
	
		/** 
	 * Select the initial selection
	 * 
	 */
	public void setInitialSelection(AboutInfo info) {
		lastSelection = info;
	}	
	
	/**
	 * Populate the table with plugin info obtained from the registry.
	 *
	 * @param the parent composite to contain the dialog area
	 */
	protected void createColumns() {
		/* create table headers */
		int[] columnWidths =
			{
				convertHorizontalDLUsToPixels(125),
				convertHorizontalDLUsToPixels(190),
				convertHorizontalDLUsToPixels(110)};
		for (int i = 0; i < columnTitles.length; i++) {
			TableColumn tableColumn = new TableColumn(table, SWT.NULL);
			tableColumn.setWidth(columnWidths[i]);
			tableColumn.setText(columnTitles[i]);
			final int columnIndex = i;
			tableColumn.addSelectionListener(new SelectionAdapter() {		
				public void widgetSelected(SelectionEvent e) {
					sort(columnIndex);
				}
			});
		
		}

		int initialSelectionIndex = 0;
		/* fill each row of the table with feature info */
		for (int i = 0; i < featuresInfo.length; i++) {
			if (featuresInfo[i] == lastSelection)
				initialSelectionIndex = i;
			String provider = featuresInfo[i].getProviderName();
			String featureName = featuresInfo[i].getFeatureLabel();
			String version = featuresInfo[i].getVersion();
			if (provider == null)
				provider = "";
			if (featureName == null)
				featureName = "";
			if (version == null)
				version = "";
			String[] row = { provider, featureName, version };
			TableItem item = new TableItem(table, SWT.NULL);
			item.setText(row);
			item.setData(featuresInfo[i]);
		}
		
		// set initial selection
		if (featuresInfo.length > 0) {
			table.setSelection(initialSelectionIndex);
		}
	}

	
	/**
	 * Sort the rows of the table based on the selected column.
	 *
	 * @param column index of table column selected as sort criteria
	 */
	private void sort(int column) {
		// Choose new sort algorithm
		if (lastColumnChosen == column){
			reverseSort = !reverseSort;
		}
		else{
			reverseSort = false;
			lastColumnChosen = column;
		}
		
		if(table.getItemCount() <= 1)	return;

		// Remember the last selection
		int idx = table.getSelectionIndex();
		if (idx != -1)
			lastSelection = featuresInfo[idx];
			
		switch (column){
			case 0:
				sortByProvider();
				break;
			case 1:
				sortByName();
				break;
			case 2:
				sortByVersion();
				break;
		}

		refreshTable(column);
	}

	/**
	 * Refresh the rows of the table based on the selected column.
	 * Maintain selection from before sort action request.
	 *
	 * @param items the old state table items 
	 */
	private void refreshTable(int col){
		TableItem[] items = table.getItems();
		int idx = -1;	// the new index of the selection
		// Create new order of table items
		for(int i = 0; i < items.length; i++) {
			String provider = featuresInfo[i].getProviderName();
			String featureName = featuresInfo[i].getFeatureLabel();
			String version = featuresInfo[i].getVersion();
			if (provider == null)
				provider = "";
			if (featureName == null)
				featureName = "";
			if (version == null)
				version = "";
			String[] row = { provider, featureName, version };
			items[i].setText(row);
			items[i].setData(featuresInfo[i]);
		}
		// Maintain the original selection
		if (lastSelection != null){
			for (int k = 0; k < featuresInfo.length; k++){
				if (lastSelection == featuresInfo[k])
					idx = k;
			}	
			table.setSelection(idx);
			table.showSelection();
		}

		updateInfoArea(lastSelection);
	}
	/**
	 * Sort the rows of the table based on the plugin provider.
	 * Secondary criteria is unique plugin id.
	 */
	private void sortByProvider(){
		/* If sorting in reverse, info array is already sorted forward by
		 * key so the info array simply needs to be reversed.
		 */
		if (reverseSort){
			java.util.List infoList = Arrays.asList(featuresInfo);
			Collections.reverse(infoList);
			for (int i=0; i< featuresInfo.length; i++){
				featuresInfo[i] = (AboutInfo)infoList.get(i);
			}
		}
		else {
			// Sort ascending
			Arrays.sort(featuresInfo, new Comparator() {
				Collator coll = Collator.getInstance(Locale.getDefault());
				public int compare(Object a, Object b) {
					AboutInfo i1, i2;
					String provider1, provider2, name1, name2;
					i1 = (AboutInfo)a;
					provider1 = i1.getProviderName();
					name1 = i1.getFeatureLabel();
					if (provider1 == null)
						provider1 = "";
					if (name1 == null)
						name1 = "";
					i2 = (AboutInfo)b;
					provider2 = i2.getProviderName();
					name2 = i2.getFeatureLabel();
					if (provider2 == null)
						provider2 = "";
					if (name2 == null)
						name2 = "";
					if (provider1.equals(provider2))
						return coll.compare(name1, name2);
					else
						return coll.compare(provider1, provider2);
				}
			});
		}
	}
	/**
	 * Sort the rows of the table based on unique plugin id.
	 */	
	private void sortByName(){
		/* If sorting in reverse, info array is already sorted forward by
		 * key so the info array simply needs to be reversed.
		 */
		if (reverseSort){
			java.util.List infoList = Arrays.asList(featuresInfo);
			Collections.reverse(infoList);
			for (int i=0; i< featuresInfo.length; i++){
				featuresInfo[i] = (AboutInfo)infoList.get(i);
			}
		}
		else {
			// Sort ascending
			Arrays.sort(featuresInfo, new Comparator() {
				Collator coll = Collator.getInstance(Locale.getDefault());
				public int compare(Object a, Object b) {
					AboutInfo i1, i2;
					String name1, name2;
					i1 = (AboutInfo)a;
					name1 = i1.getFeatureLabel();
					i2 = (AboutInfo)b;
					name2 = i2.getFeatureLabel();
					if (name1 == null)
						name1 = "";
					if (name2 == null)
						name2 = "";
					return coll.compare(name1, name2);
				}
			});
		}
	
	}
	/**
	 * Sort the rows of the table based on the plugin version.
	 * Secondary criteria is unique plugin id.
	 */
	private void sortByVersion(){
		/* If sorting in reverse, info array is already sorted forward by
		 * key so the info array simply needs to be reversed.
		 */		
		if (reverseSort){
			java.util.List infoList = Arrays.asList(featuresInfo);
			Collections.reverse(infoList);
			for (int i=0; i< featuresInfo.length; i++){
				featuresInfo[i] = (AboutInfo)infoList.get(i);
			}
		}
		else {
			// Sort ascending
			Arrays.sort(featuresInfo, new Comparator() {
				Collator coll = Collator.getInstance(Locale.getDefault());
				public int compare(Object a, Object b) {
					AboutInfo i1, i2;
					String version1, version2, name1, name2;
					i1 = (AboutInfo)a;
					version1 = i1.getVersion();
					name1 = i1.getFeatureLabel();
					if (version1 == null)
						version1 = "";
					if (name1 == null)
						name1 = "";
					i2 = (AboutInfo)b;
					version2 = i2.getVersion();
					name2 = i2.getFeatureLabel();
					if (version2 == null)
						version2 = "";
					if (name2 == null)
						name2 = "";
					if (version1.equals(version2))
						return coll.compare(name1, name2);
					else
						return coll.compare(version1, version2);
				}
			});
		}
	}
	
			}