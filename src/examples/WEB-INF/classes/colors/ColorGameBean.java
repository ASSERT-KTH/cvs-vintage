/*
 * Copyright (c) 1995-1997 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * CopyrightVersion 1.0
 */

package colors;

import javax.servlet.http.*;

public class ColorGameBean {

    private String background = "yellow";
    private String foreground = "red";
    private String color1 = foreground;
    private String color2 = background;
    private String hint = "no";
    private int attempts = 0;
	private int intval = 0;
    private boolean tookHints = false;

    public void processRequest(HttpServletRequest request) {

	// background = "yellow";
	// foreground = "red";

	if (! color1.equals(foreground)) {
	    if (color1.equalsIgnoreCase("black") ||
			color1.equalsIgnoreCase("cyan")) {
			background = color1;
		}
	}

	if (! color2.equals(background)) {
	    if (color2.equalsIgnoreCase("black") ||
			color2.equalsIgnoreCase("cyan")) {
			foreground = color2;
	    }
	}

	attempts++;
    }

    public void setColor2(String x) {
	color2 = x;
    }

    public void setColor1(String x) {
	color1 = x;
    }

    public void setAction(String x) {
	if (!tookHints)
	    tookHints = x.equalsIgnoreCase("Hint");
	hint = x;
    }

    public String getColor2() {
	 return background;
    }

    public String getColor1() {
	 return foreground;
    }

    public int getAttempts() {
	return attempts;
    }

    public boolean getHint() {
	return hint.equalsIgnoreCase("Hint");
    }

    public boolean getSuccess() {
	if (background.equalsIgnoreCase("black") ||
	    background.equalsIgnoreCase("cyan")) {
	
	    if (foreground.equalsIgnoreCase("black") ||
		foreground.equalsIgnoreCase("cyan"))
		return true;
	    else
		return false;
	}

	return false;
    }

    public boolean getHintTaken() {
	return tookHints;
    }

    public void reset() {
	foreground = "red";
	background = "yellow";
    }

    public void setIntval(int value) {
	intval = value;
	}

    public int getIntval() {
	return intval;
	}
}

