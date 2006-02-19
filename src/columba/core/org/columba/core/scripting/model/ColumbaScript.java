/*
  The contents of this file are subject to the Mozilla Public License Version 1.1
  (the "License"); you may not use this file except in compliance with the 
  License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
  
  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
  for the specific language governing rights and
  limitations under the License.

  The Original Code is "The Columba Project"
  
  The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
  Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
  
  All Rights Reserved.
*/
package org.columba.core.scripting.model;

import java.io.File;

/**
    @author Celso Pinto (cpinto@yimports.com)
 */
public class ColumbaScript
{

    private final File scriptFile;

    private String
        name = "",
        author = "",
        description = "",
        extension = "";

    public ColumbaScript(File file)
    {
        scriptFile = file;
        extension = extractExtensionFromFilename();
    }

    private String extractExtensionFromFilename()
    {

        String name = scriptFile.getName();
        int pos = name.lastIndexOf('.');
        if (pos == -1 || pos + 1 == name.length()) return null;

        return name.substring(pos + 1);

    }

    public String getExtension()
    {
        return extension;
    }

    public void setMetadata(String name, String author, String desc)
    {
        this.name = name;
        this.author = author;
        this.description = desc;
    }

    public String getName()
    {
        if (name.equals("")) return scriptFile.getName();
        else return name;

    }

    public String getAuthor()
    {
        return author;
    }

    public String getDescription()
    {
        return description;
    }

    public long getLastModified()
    {
        return scriptFile.lastModified();
    }

    public String getPath()
    {
        return scriptFile.getPath();
    }

    public boolean exists()
    {
        return scriptFile.exists();
    }

    public boolean deleteFromDisk()
    {
        return scriptFile.delete();
    }
}
