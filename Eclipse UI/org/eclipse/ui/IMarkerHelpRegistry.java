package org.eclipse.ui;

import org.eclipse.core.resources.IMarker;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/** 
 * Registry of F1 help contexts and resolutions for markers.
 * <p>
 * The information contained in the registry is read from the 
 * org.eclipse.ui.markerhelp and org.eclipse.ui.markerresolution
 * extension points.
 * </p>
 * 
 * @since 2.0
 */
public interface IMarkerHelpRegistry {
	/**
	 * Returns a help context id for the given marker or
	 * <code>null</code> if no help has been registered 
	 * for the marker.
	 * 
	 * @param marker the marker for which to obtain help
	 * @since 2.0
	 */
	public String getHelp(IMarker marker);
	/**
	 * Returns <code>false</code> if there are no resolutions for 
	 * the given marker. Returns <code>true</code> if their may
	 * be resolutions. In most cases a <code>true</code> value 
	 * means there are resolutions but due to plugin loading 
	 * issues getResolutions may sometimes return an empty array
	 * after this method returns <code>true</code>. 
	 * 
	 * @param marker the marker for which to determine if there
	 * are resolutions
	 * @since 2.0
	 */
	public boolean hasResolutions(IMarker marker);
	/**
	 * Returns an array of resolutions for the given marker. 
	 * The returned array will be empty if there are no resolutions
	 * for the marker.
	 * 
	 * @param marker the marker for which to obtain resolutions
	 * @since 2.0
	 */
	public IMarkerResolution[] getResolutions(IMarker marker);
}

