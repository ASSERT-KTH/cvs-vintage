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

package org.eclipse.ui.internal.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.util.ImageSupport;

final class ImageFactory {

	static {
		put("blank", "icons/full/commands/blank.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("change", "icons/full/commands/change.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("clear", "icons/full/commands/clear.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("exclamation", "icons/full/commands/exclamation.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("minus", "icons/full/commands/minus.gif"); //$NON-NLS-1$//$NON-NLS-2$
		put("plus", "icons/full/commands/plus.gif"); //$NON-NLS-1$//$NON-NLS-2$
	}

	private static ImageRegistry imageRegistry = new ImageRegistry();
	private static Map map = new HashMap();

	static Image getImage(String key) {
		Image image = (Image) imageRegistry.get(key);

		if (image == null) {
			ImageDescriptor imageDescriptor = getImageDescriptor(key);

			if (imageDescriptor != null) {
				image = imageDescriptor.createImage(false);

				if (image == null)
					System.err.println(ImageFactory.class +": error creating image for " + key); //$NON-NLS-1$

				imageRegistry.put(key, image);
			}
		}

		return image;
	}

	static ImageDescriptor getImageDescriptor(String key) {
		ImageDescriptor imageDescriptor = (ImageDescriptor) map.get(key);

		if (imageDescriptor == null)
			System.err.println(ImageFactory.class +": no image descriptor for " + key); //$NON-NLS-1$

		return imageDescriptor;
	}

	private static void put(String key, String value) {
		map.put(key, ImageSupport.getImageDescriptor(value));
	}
}
