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
package org.eclipse.ui.part;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ReferenceCounter;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Abstract base implementation of all workbench parts.
 * <p>
 * This class is not intended to be subclassed by clients outside this
 * package; clients should instead subclass <code>ViewPart</code> or
 * <code>EditorPart</code>.
 * </p>
 * 
 * @see ViewPart
 * @see EditorPart
 */
public abstract class WorkbenchPart implements IWorkbenchPart2, IExecutableExtension {
	private String title = ""; //$NON-NLS-1$
	private ImageDescriptor imageDescriptor;
	private Image titleImage;
	private String toolTip = ""; //$NON-NLS-1$
	private IConfigurationElement configElement;
	private IWorkbenchPartSite partSite;
	private ListenerList propChangeListeners = new ListenerList(2);
    
    private String partName = ""; //$NON-NLS-1$
    private String contentDescription = ""; //$NON-NLS-1$
    
/**
 * Creates a new workbench part.
 */
protected WorkbenchPart() {
	super();
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public void addPropertyListener(IPropertyListener l) {
	propChangeListeners.add(l);
}
/* (non-Javadoc)
 * Creates the SWT controls for this workbench part.
 * <p>
 * Subclasses must implement this method.  For a detailed description of the
 * requirements see <code>IWorkbenchPart</code>
 * </p>
 *
 * @param parent the parent control
 * @see IWorkbenchPart
 */
public abstract void createPartControl(Composite parent);
/**
 * The <code>WorkbenchPart</code> implementation of this 
 * <code>IWorkbenchPart</code> method disposes the title image
 * loaded by <code>setInitializationData</code>. Subclasses may extend.
 */
public void dispose() {
	ReferenceCounter imageCache = WorkbenchImages.getImageCache();
	Image image = (Image)imageCache.get(imageDescriptor);
	if (image != null) {
		int count = imageCache.removeRef(imageDescriptor);
		if(count <= 0)
			image.dispose();
	}
	
	// Clear out the property change listeners as we
	// should not be notifying anyone after the part
	// has been disposed.
	if (!propChangeListeners.isEmpty()) {
		propChangeListeners = new ListenerList(1);
	}
}
/**
 * Fires a property changed event.
 *
 * @param propertyId the id of the property that changed
 */
protected void firePropertyChange(final int propertyId) {
	Object [] array = propChangeListeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPropertyListener l = (IPropertyListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.propertyChanged(WorkbenchPart.this, propertyId);
			}
		});
	}
}
/**
 * This implementation of the method declared by <code>IAdaptable</code>
 * passes the request along to the platform's adapter manager; roughly
 * <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
 * Subclasses may override this method (however, if they do so, they
 * should invoke the method on their superclass to ensure that the
 * Platform's adapter manager is consulted).
 */
public Object getAdapter(Class adapter) {
	return Platform.getAdapterManager().getAdapter(this, adapter);
}
/**
 * Returns the configuration element for this part. The configuration element
 * comes from the plug-in registry entry for the extension defining this part.
 *
 * @return the configuration element for this part
 */
protected IConfigurationElement getConfigurationElement() {
	return configElement;
}
/**
 * Returns the default title image.
 *
 * @return the default image
 */
protected Image getDefaultImage() {
	return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEF_VIEW);
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public IWorkbenchPartSite getSite() {
	return partSite;
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public String getTitle() {
	return title;
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public Image getTitleImage() {
	if (titleImage != null) {
		return titleImage;
	}
	return getDefaultImage();
}
/* (non-Javadoc)
 * Gets the title tool tip text of this part.
 *
 * @return the tool tip text
 */
public String getTitleToolTip() {
	return toolTip;
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public void removePropertyListener(IPropertyListener l) {
	propChangeListeners.remove(l);
}
/* (non-Javadoc)
 * Asks this part to take focus within the workbench.
 * <p>
 * Subclasses must implement this method.  For a detailed description of the
 * requirements see <code>IWorkbenchPart</code>
 * </p>
 *
 * @see IWorkbenchPart
 */
public abstract void setFocus();
/**
 * The <code>WorkbenchPart</code> implementation of this
 * <code>IExecutableExtension</code> records the configuration element in
 * and internal state variable (accessible via <code>getConfigElement</code>).
 * It also loads the title image, if one is specified in the configuration element.
 * Subclasses may extend.
 * 
 * Should not be called by clients. It is called by the core plugin when creating
 * this executable extension.
 */
public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {

	// Save config element.
	configElement = cfig;

	// Part name and title.  
	title = Util.safeString(cfig.getAttribute("name"));//$NON-NLS-1$;
	setPartName(title);

	// Icon.
	String strIcon = cfig.getAttribute("icon");//$NON-NLS-1$
	if (strIcon == null)
		return;

	imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				configElement.getDeclaringExtension().getNamespace(),
				strIcon);
	
	if (imageDescriptor == null)
		return; 

	/* remember the image in a separatly from titleImage,
	 * since it must be disposed even if the titleImage is changed
	 * to something else*/
 	ReferenceCounter imageCache = WorkbenchImages.getImageCache();
	Image image = (Image)imageCache.get(imageDescriptor);
	if(image != null) {
		imageCache.addRef(imageDescriptor);
	} else {
		image = imageDescriptor.createImage();
		imageCache.put(imageDescriptor,image);
	}
	titleImage = image;
}
/**
 * Sets the part site.
 * <p>
 * Subclasses must invoke this method from <code>IEditorPart.init</code>
 * and <code>IViewPart.init</code>.
 *
 * @param site the workbench part site
 */
protected void setSite(IWorkbenchPartSite site) {
	this.partSite = site;
}
/**
 * Sets or clears the title of this part. Setting this to null or the empty string (default)
 * will cause the title to be automatically generated based on the part name and
 * content description. Setting this to a non-empty string may overwrite a value that
 * was set previously using setContentDescription or setPartName.
 *
 * @deprecated new code should use setPartName and setContentDescription
 *
 * @param title the title, or <code>null</code> to clear
 */
protected void setTitle(String title) {
	title = Util.safeString(title);
	
	if (title.equals("")) { //$NON-NLS-1$
		setDefaultTitle();
	}
	
	internalSetTitle(title);
}

/**
 * Sets or clears the title image of this part.
 *
 * @param titleImage the title image, or <code>null</code> to clear
 */
protected void setTitleImage(Image titleImage) {
    Assert.isTrue(titleImage == null || !titleImage.isDisposed());
	//Do not send changes if they are the same
    if (this.titleImage == titleImage)
        return;
	this.titleImage = titleImage;
	firePropertyChange(IWorkbenchPart.PROP_TITLE);
}
/**
 * Sets or clears the title tool tip text of this part.
 *
 * @param toolTip the new tool tip text, or <code>null</code> to clear
 */
protected void setTitleToolTip(String toolTip) {
    toolTip = Util.safeString(toolTip);
	//Do not send changes if they are the same
	if(Util.equals(this.toolTip, toolTip))
		return;
	this.toolTip = toolTip;
	firePropertyChange(IWorkbenchPart.PROP_TITLE);
}
/**
 * Show that this part is busy due to a Job running that it 
 * is listening to.
 * @param busy boolean to indicate that the busy state has started
 *  	or ended.
 * @see IWorkbenchPartProgressService.schedule(Job,int,boolean).
 * @since 3.0
 */
public void showBusy(boolean busy){
	//By default do nothing
}

/* (non-Javadoc)
 * Method declared on IWorkbenchPart2.
 * 
 * @since 3.0
 */
public String getPartName() {
    return partName;
}

/**
 * Sets the name of this part. The name will be shown in the tab area for 
 * the part. 
 * 
 * <p>
 * This may overwrite a value that was set previously in setTitle. New code
 * should use setPartName and setContentDescription but not setTitle.  
 * </p>
 *
 * @param partName the part name, as it should be displayed in tabs.
 * 
 * @since 3.0
 */
protected void setPartName(String partName) {
	internalSetPartName(partName);
	
	setDefaultTitle();
}

/* (non-Javadoc)
 * Method declared on IWorkbenchPart2.
 * 
 * @since 3.0
 */
public String getContentDescription() {
    return contentDescription;
}

/**
 * Sets the content description for this part. The content description is typically
 * a short string describing the current contents of the part. 
 *
 * <p>
 * This may overwrite a value that was previously set in setTitle
 * </p>
 * <ul>
 * <li>The default value for editors is the empty string</li>
 * <li>The default value for views is the empty string if no title has been set, or </li>  
 * </ul>
 * 
 * @param description the content description
 * 
 * @since 3.0
 */
protected void setContentDescription(String description) {
	internalSetContentDescription(description);
	
	setDefaultTitle();
}

private void setDefaultTitle() {
	String description = getContentDescription();
	String name = getPartName();
	String newTitle = name;
	
	if (!Util.equals(description, "")) { //$NON-NLS-1$
		newTitle = MessageFormat.format(WorkbenchMessages.getString("WorkbenchPart.AutoTitleFormat"), new String[] {name, description}); //$NON-NLS-1$
	}

	internalSetTitle(newTitle);
}

/* package */ void internalSetContentDescription(String description) {
	Assert.isNotNull(description);
	
	//Do not send changes if they are the same
	if(Util.equals(contentDescription, description))
		return;
	this.contentDescription = description;
	
	firePropertyChange(IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION);

}

/* package */ void internalSetTitle(String title) {
    
	//Do not send changes if they are the same
	if(Util.equals(this.title, title))
		return;
	this.title = title;
	firePropertyChange(IWorkbenchPart.PROP_TITLE);	
}

/* package */ void internalSetPartName(String partName) {
	Assert.isNotNull(partName);

	//Do not send changes if they are the same
	if(Util.equals(this.partName, partName))
		return;
	this.partName = partName;
	firePropertyChange(IWorkbenchPartConstants.PROP_PART_NAME);
}

}
