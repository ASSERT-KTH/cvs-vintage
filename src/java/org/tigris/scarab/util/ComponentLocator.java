/*
 * Created on Dec 2, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigris.scarab.util;

import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.mimetype.MimeTypeService;
import org.apache.turbine.services.yaaficomponent.YaafiComponentService;
import org.tigris.scarab.tools.localization.L10NKeySet;

/**
 * @author Eric Pugh
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ComponentLocator {
	private static MimeTypeService mimeTypeService;

	/**
	 * @return Returns the mimeTypeService.
	 */
	public static MimeTypeService getMimeTypeService() {
		if (mimeTypeService == null) {
			mimeTypeService = (MimeTypeService) lookup(MimeTypeService.class);
		}
		return mimeTypeService;
	}

	/**
	 * @param mimeTypeService
	 *            The mimeTypeService to set.
	 */
	public static void setMimeTypeService(MimeTypeService mimeTypeService) {
		ComponentLocator.mimeTypeService = mimeTypeService;
	}

	/**
	 * @param class1
	 * @return
	 */
	private static Object lookup(Class clazz) {
		YaafiComponentService yaafi = (YaafiComponentService) TurbineServices
				.getInstance().getService(YaafiComponentService.SERVICE_NAME);
		try {
			return yaafi.lookup(clazz.getName());
		} catch (Exception e) {
			throw new ScarabRuntimeException(
			        L10NKeySet.ExceptionComponentLocator,
				    clazz.getName(),
				    e);
		}
	}
}