//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//Portions created by Celso Pinto are Copyright (C) 2004.
//All Rights Reserved.
package org.columba.mail.gui.message.util;

import java.net.URL;


/**
 * This class is a substitute for java.net.URL. It allows one to add specific
 * properties depending on the URL's protocol.<br>
 * At the moment only handles mailto: protocol.<br>
 * I.E.: URL might be mailto:cpinto@yimports.com and sender is Celso Pinto, 
 * resulting on getEmailAddress()==cpinto@yimports.com and getSender() on Celso Pinto.
 * 
 * @author Celso Pinto &lt;cpinto@yimports.com&gt;
 */
public class ColumbaURL
{

  private URL iRealURL = null;
  private String iSender = "";
  
  public ColumbaURL(URL aRealURL)
  {
    iRealURL = aRealURL;
  }
  
  public String getEmailAddress()
  {
    if (iRealURL == null)
      return "";
    
    return iRealURL.getFile();
  }
  public void setRealURL(URL aRealURL)
  {
    iRealURL = aRealURL;
  }
  public URL getRealURL()
  {
  	return iRealURL;  
  }
  
  public String getSender()
  {
    return iSender;
  }
  public void setSender(String aSender)
  {
    iSender = aSender;
  }
  
  public boolean isMailTo()
  {
    return iRealURL.getProtocol().equalsIgnoreCase("mailto");
  }
  
}
