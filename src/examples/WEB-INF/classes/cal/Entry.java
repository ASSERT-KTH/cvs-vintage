/*
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
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
 */

package cal;

public class Entry {

  String hour;
  String description;
  String color;

  public Entry (String hour) {
    this.hour = hour;
    this.description = "";

  }

  public String getHour () {
    return this.hour;
  }

  public String getColor () {
    if (description.equals("")) return "lightblue";
    else return "red";
  }

  public String getDescription () {
    if (description.equals("")) return "None";
    else return this.description;
  }

  public void setDescription (String descr) {
    description = descr;
  }
 
}





