/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 */
package org.objectweb.carol.cmi.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;

public class Conf {
    private String uri;

    //  Maps Class names to ClassConf
    private HashMap classInfos = new HashMap();
    private ArrayList classes;
    private ClassLoader classLoader;

    public Conf(ClassLoader cl, ArrayList classes) {
        this.classes = classes;
        classLoader = cl;
    }

    public void loadConfig(String uri) throws CompilerException {
        this.uri = uri;
        Iterator i;
        try {
            i = XMLTree.read(uri).childs.iterator();
        } catch (Exception e1) {
            throw new CompilerException("Cluster config loading failed", e1);
        }
        while (i.hasNext()) {
            Object o = i.next();
            if (!(o instanceof XMLElement)) {
                throw new CompilerException("Text not expected at top level");
            }
            XMLElement e = (XMLElement) o;
            if (!e.name.equals("cluster-config")) {
                throw new CompilerException(
                    "Element " + e.name + " not expected at top level");
            }
            checkTop(e.childs);
        }
    }

    private void checkTop(LinkedList l) throws CompilerException {
        Iterator i = l.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (!(o instanceof XMLElement)) {
                throw new CompilerException("Text not expected at top level");
            }
            XMLElement e = (XMLElement) o;
            if (e.name.equals("class")) {
                checkClass(e.childs);
            } else {
                throw new CompilerException(
                    "Element " + e.name + " not expected here");
            }
        }
    }

    private void checkClass(LinkedList l) throws CompilerException {
        String clName = null;
        ClassConf ccc;
        Iterator i = l.iterator();

        if (i.hasNext()) {
            Object o = i.next();
            if (o instanceof XMLElement) {
                XMLElement e = (XMLElement) o;
                if (e.name.equals("name"))
                    clName = checkString(e);
            }
        }

        if (clName == null)
            throw new CompilerException(
                uri
                    + ": a class name must be provided first in a <class> element");

        if (!classes.contains(clName)) {
            return;
        }

        Class cl;
        try {
            cl = classLoader.loadClass(clName);
        } catch (ClassNotFoundException e1) {
            throw new CompilerException("class not found " + clName, e1);
        }

        ccc = new ClassConf(cl);
        if (classInfos.put(clName, ccc) != null)
            throw new CompilerException(
                uri + ": only one definition expected for class " + clName);

        while (i.hasNext()) {
            Object o = i.next();
            if (!(o instanceof XMLElement)) {
                throw new CompilerException(uri + ": text not expected");
            }
            XMLElement e = (XMLElement) o;
            if (e.name.equals("method")) {
                checkMethod(ccc, e.childs, null);
            } else if (e.name.equals("lookup")) {
                checkEmpty(e);
                ccc.setLookupChoice();
            } else if (e.name.equals("rr")) {
                checkChooser(ccc, e.childs, ccc.addRR());
            } else if (e.name.equals("random")) {
                checkChooser(ccc, e.childs, ccc.addRandom());
            } else
                throw new CompilerException(
                    uri + ": unexpected element : " + e.name);
        }
        ccc.validate();
    }

    private void checkChooser(ClassConf ccc, LinkedList l, String chooser)
        throws CompilerException {
        Iterator i = l.iterator();

        while (i.hasNext()) {
            Object o = i.next();
            if (!(o instanceof XMLElement)) {
                throw new CompilerException(uri + ": text not expected");
            }
            XMLElement e = (XMLElement) o;
            if (e.name.equals("method")) {
                checkMethod(ccc, e.childs, chooser);
            }
        }
    }

    private MethodConf checkMethod(ClassConf ccc, LinkedList l)
        throws CompilerException {
        return checkMethod(ccc, l, ccc.getBalancer());
    }

    private MethodConf checkMethod(
        ClassConf ccc,
        LinkedList l,
        String balancer)
        throws CompilerException {
        MethodProto mp = null;
        MethodConf mc;
        Iterator i = l.iterator();

        if (i.hasNext()) {
            Object o = i.next();
            if (o instanceof XMLElement) {
                XMLElement e = (XMLElement) o;
                if (e.name.equals("signature")) {
                    String sign = checkString(e);
                    mp = new MethodProto(sign);
                }
            }
        }

        if (mp == null)
            throw new CompilerException(
                uri
                    + ": a signature must be provided first in a <method> element");

        if (ccc.containsMethod(mp)) {
            throw new CompilerException(
                uri + ": method already defined: " + mp.toString());
        }

        mc = new MethodConf(ccc, mp, balancer);
        ccc.putMethod(mp, mc);

        while (i.hasNext()) {
            Object o = i.next();
            if (!(o instanceof XMLElement)) {
                throw new CompilerException(uri + ": text not expected");
            }
            XMLElement e = (XMLElement) o;
            throw new CompilerException(uri + ": bad sub-element: " + e.name);
        }

        return mc;
    }

    private void checkEmpty(XMLElement e) throws CompilerException {
        Iterator i = e.childs.iterator();
        if (i.hasNext())
            throw new CompilerException(
                uri + ": element <" + e.name + "> should be empty");
    }

    private String checkOptString(XMLElement e) throws CompilerException {
        Iterator i = e.childs.iterator();
        if (!i.hasNext()) {
            return null;
        }
        Object o = i.next();
        if ((o instanceof String) && (!i.hasNext())) {
            return (String) o;
        }
        throw new CompilerException(
            uri + ": element <" + e.name + "> may be only a string");
    }

    private String checkString(XMLElement e) throws CompilerException {
        Iterator i = e.childs.iterator();
        if (i.hasNext()) {
            Object o = i.next();
            if ((o instanceof String) && (!i.hasNext())) {
                return (String) o;
            }
        }
        throw new CompilerException(
            uri + ": element <" + e.name + "> must be a string");
    }

    public ClassConf getClassConf(String className) throws CompilerException {
        ClassConf ccc = (ClassConf) classInfos.get(className);
        if (ccc == null) {
            throw new CompilerException(
                "No configuration found for class " + className);
        }
        return ccc;
    }
}
