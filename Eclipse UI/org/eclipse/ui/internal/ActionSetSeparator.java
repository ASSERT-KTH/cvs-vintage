package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.action.*;

/**
 * This class represents a pseudo-group defined by an action set.
 * It is not a real group ( aka GroupMarker ) because that would interfere with
 * the pre-existing groups in a menu or toolbar.
 */
public class ActionSetSeparator extends ContributionItem 
	implements IActionSetContributionItem 
{
	private String actionSetId;
/**
 * Constructs a new group marker.
 */
public ActionSetSeparator(String groupName, String newActionSetId) {
	super(groupName);
	actionSetId = newActionSetId;
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 * Fills the given menu with a SWT separator MenuItem.
 */
public void fill(Menu menu, int index) {
	if (index >= 0)
		new MenuItem(menu, SWT.SEPARATOR, index);
	else
		new MenuItem(menu, SWT.SEPARATOR);
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 * Fills the given tool bar with a SWT separator ToolItem.
 */
public void fill(ToolBar toolbar, int index) {
	if (index >= 0)
		new ToolItem(toolbar, SWT.SEPARATOR, index);
	else
		new ToolItem(toolbar, SWT.SEPARATOR);
}
/**
 * Returns the action set id.
 */
public String getActionSetId() {
	return actionSetId;
}
/** 
 * The <code>Separator</code> implementation of this <code>IContributionItem</code> 
 * method returns <code>true</code>
 */
public boolean isSeparator() {
	return true;
}
/**
 * Sets the action set id.
 */
public void setActionSetId(String newActionSetId) {
	actionSetId = newActionSetId;	
}
}
