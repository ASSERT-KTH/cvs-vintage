package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.core.resources.IMarker;
/**
 * Clients should implement this interface when creating an
 * extension to define images for marker dynamically.
 * <p>
 * The name of the class should be specified in the extension contributed 
 * to the workbench's maker image provider extension point 
 * (named <code>"org.eclipse.ui.makerImageProvider"</code>).
 * For example, the plug-in's XML markup might contain:
 * <pre>
 * &LT;extension point="org.eclipse.ui.makerImageProvider"&GT;
 *      &LT;imageprovider 
 *		   id="com.example.myplugin.myprofiderID"
 *         makertype="com.example.myMarkerType"
 *         icon="icons/basic/view16/myGIF.gif"/&GT;
 * &LT;/extension&GT;
 * </pre>
 * It can also define the image provider using the tag <code>class</code>
 * instead of icon.
 * </p>
 * Either the image path specified by the tag <code>icon</code> or
 * the path returned from <code>getImagePath</code> will be used
 * to create the image when the following code is executed:
 * <p><code>myMarker.getAdapter(IWorkbenchAdapter).getImageDescriptor(myMarker);</code></p>
 */
public interface IMarkerImageProvider {
/**
 * Returns the relative path for the image
 * to be used for displaying an marker in the workbench.
 * This path is relative to the plugin location
 *
 * Returns <code>null</code> if there is no appropriate image.
 *
 * @param marker The marker to get an image path for.
 *
 * @see org.eclipse.jface.resource.FileImageDescriptor
 */
public String getImagePath(IMarker marker);
}
