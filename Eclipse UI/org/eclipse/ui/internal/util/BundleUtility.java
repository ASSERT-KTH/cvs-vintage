/*
 * Created on Mar 16, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.internal.util;

import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

// TODO: needs a better name
public class BundleUtility {
	public static boolean isActivated(Bundle bundle) {
		if (bundle == null)
			return false;

		switch (bundle.getState()) {
			case Bundle.STARTING :
			case Bundle.ACTIVE :
			case Bundle.STOPPING :
				return true;
			default :
				return false;
		}
	}

	// TODO: needs a better name
	public static boolean isReady(Bundle bundle) {
		if (bundle == null)
			return false;

		switch (bundle.getState()) {
			case Bundle.RESOLVED :
			case Bundle.STARTING :
			case Bundle.ACTIVE :
			case Bundle.STOPPING :
				return true;
			default :
				return false;
		}
	}

	public static boolean isActivated(String bundleId) {
		return isActivated(Platform.getBundle(bundleId));
	}

	public static boolean isReady(String bundleId) {
		return isReady(Platform.getBundle(bundleId));
	}

	public static URL find(Bundle bundle, String path) {
	    if(bundle == null)
	        return null;
		return Platform.find(bundle, new Path(path));
	}

	public static URL find(String bundleId, String path) {
	    return find(Platform.getBundle(bundleId), path);
	}
}
