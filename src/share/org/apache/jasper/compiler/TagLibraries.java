/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language 
 */

package org.apache.jasper.compiler;

import java.util.Hashtable;

import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;


/**
 * A container for all tag libraries that have been imported using
 * the taglib directive. 
 *
 * @author Anil K. Vijendran
 * @author Mandar Raje
 */
public class TagLibraries {
    
    public TagLibraries(ClassLoader cl) {
        this.tagLibInfos = new Hashtable();
	this.tagCaches = new Hashtable();
        this.cl = cl;
    }
    
    public void addTagLibrary(String prefix, TagLibraryInfo tli) {
        tagLibInfos.put(prefix, tli);
    }
    
    public boolean isUserDefinedTag(String prefix, String shortTagName) 
        throws JasperException
    {
        TagLibraryInfo tli = (TagLibraryInfo) tagLibInfos.get(prefix);
        if (tli == null)
            return false;
        else if (tli.getTag(shortTagName) != null)
            return true;
        throw new JasperException(Constants.getString("jsp.error.bad_tag",
                                                      new Object[] {
                                                          shortTagName,
                                                          prefix
                                                      }
                                                      ));
    }
    
    public TagLibraryInfo getTagLibInfo(String prefix) {
        return (TagLibraryInfo) tagLibInfos.get(prefix);
    }

    public TagCache getTagCache(String prefix, String shortTagName) {
	return (TagCache) tagCaches.get(new TagID(prefix, shortTagName));
    }

    public void putTagCache(String prefix, String shortTagName, TagCache tc) {
	tagCaches.put(new TagID(prefix, shortTagName), tc);
    }

    private Hashtable tagLibInfos;
    private Hashtable tagCaches;
    private ClassLoader cl;

    private static class TagID {

	private String prefix;
	private String shortTagName;

	public TagID(String prefix, String shortTagName) {
	    this.prefix = prefix;
	    this.shortTagName = shortTagName;
	}

	public boolean equals(Object obj) {
	    return (prefix.equals(((TagID)obj).prefix)) &&
		(shortTagName.equals(((TagID)obj).shortTagName));
	}

	public int hashCode() {
	    return prefix.hashCode() + shortTagName.hashCode();
	}
    }
}

