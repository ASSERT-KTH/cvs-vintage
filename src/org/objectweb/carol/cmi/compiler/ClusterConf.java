/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
 */
package org.objectweb.carol.cmi.compiler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;

public class ClusterConf {
    private String uri;

    //  Maps Class names to maps of MethodProto to ClusterMethodInfo
    private HashMap classInfos = new HashMap();

    public ClusterMethodInfo getMethodInfo(MethodContext m, String className) {
        HashMap mthInfos = (HashMap) classInfos.get(className);
        if (mthInfos == null) {
            return null;
        }
        MethodProto mp = new MethodProto(m);
        ClusterMethodInfo cmi = (ClusterMethodInfo) mthInfos.get(mp);
        if (cmi == null) {
            return null;
        }
        return cmi;
    }

    public void loadConfig(String uri) throws Exception {
        this.uri = uri;
        Iterator i = XMLTree.read(uri).childs.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof XMLElement) {
                XMLElement e = (XMLElement) o;
                if (!e.name.equals("cluster-config"))
                    throw new Exception(uri + ": cluster-config node expected");
                checkTop(e.childs);
            } else
                throw new Exception(uri + ": text not expected");
        }
    }

    private void checkTop(LinkedList l) throws Exception {
        Iterator i = l.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof XMLElement) {
                XMLElement e = (XMLElement) o;
                if (e.name.equals("class"))
                    checkClass(e.childs);
                else
                    throw new Exception(uri + ": class expected");
            } else
                throw new Exception(uri + ": text not expected");
        }
    }

    private void checkClass(LinkedList l) throws Exception {
        String clName = null;
        HashMap mthInfos = new HashMap();
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
            throw new Exception(
                uri
                    + ": a class name must be provided first in a <class> element");

        if (classInfos.put(clName, mthInfos) != null)
            throw new Exception(
                uri + ": only one definition expected for class " + clName);

        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof XMLElement) {
                XMLElement e = (XMLElement) o;
                if (e.name.equals("method")) {
                    checkMethod(mthInfos, e.childs);
                } else if (e.name.equals("lookup-choice")) {
                    checkEmpty(e);
                    ClusterClassInfo cci = new ClusterClassInfo();
                    cci.lookupChoice = true;
                } else
                    throw new Exception(uri + ": unexpected element : " + e.name);
            } else
                throw new Exception(uri + ": text not expected");
        }
    }

    private ClusterMethodInfo checkMethod(HashMap mthInfos, LinkedList l)
        throws Exception {
        ClusterMethodInfo methinfo = new ClusterMethodInfo();
        Iterator i = l.iterator();

        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof XMLElement) {
                XMLElement e = (XMLElement) o;
                if (e.name.equals("signature")) {
                    String sign = checkString(e);
                    MethodProto mp = methinfo.setSignature(sign);
                    if (mthInfos.containsKey(mp)) {
                        throw new Exception(uri + ": method already defined: " + sign);
                    }
                    mthInfos.put(mp, methinfo);
                } else if (e.name.equals("one-choice")) {
                    checkEmpty(e);
                    methinfo.setOneChoice();
                } else if (e.name.equals("redo-choice")) {
                    checkEmpty(e);
                    methinfo.setRedoChoice();
                } else if (e.name.equals("redo-choice-retry")) {
                    checkEmpty(e);
                    methinfo.setRedoChoiceRetry();
                } else
                    throw new Exception(uri + ": bad sub-element: " + e.name);
            } else
                throw new Exception(uri + ": text not expected");
        }

        methinfo.verify();
        return methinfo;
    }

    private void checkEmpty(XMLElement e) throws Exception {
        Iterator i = e.childs.iterator();
        if (i.hasNext())
            throw new Exception(
                uri + ": element <" + e.name + "> should be empty");
    }

    private String checkString(XMLElement e) throws Exception {
        Iterator i = e.childs.iterator();
        if (i.hasNext()) {
            Object o = i.next();
            if ((o instanceof String) && (!i.hasNext())) {
                return (String) o;
            }
        }
        throw new Exception(
            uri + ": element <" + e.name + "> must be a string");
    }
}
