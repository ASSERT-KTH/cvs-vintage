/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
 */
package org.objectweb.carol.cmi.compiler;

import org.xml.sax.Attributes;
import java.util.LinkedList;

class XMLElement {
    public XMLElement parent;
    public String name;
    public Attributes attrs;
    public LinkedList childs;

    public XMLElement(XMLElement parent, String name, Attributes attrs) {
        this.parent = parent;
        this.name = name;
        this.attrs = attrs;
        childs = new LinkedList();
    }

    public void add(XMLElement e) {
        childs.add(e);
    }
    public void add(String s) {
        childs.add(s);
    }
}
