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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ClassConf {
    private HashMap mthInfos = new HashMap();
    private boolean lookupChoice = false;
    private ArrayList rrList = new ArrayList();
    private ArrayList randList = new ArrayList();
    private int balancerNo = 0;
    private String defaultBalancer = null;
    private Class cl;

    public ClassConf(Class cl) {
        this.cl = cl;
    }

    public String getClassName() {
        return MethodProto.getName(cl);
    }

    public boolean containsMethod(MethodProto mp) {
        return mthInfos.containsKey(mp);
    }

    public void putMethod(MethodProto mp, MethodConf cmc) {
        mthInfos.put(mp, cmc);
    }

    public Iterator getMethodConfs() {
        return mthInfos.values().iterator();
    }

    public void setLookupChoice() {
        lookupChoice = true;
    }

    public MethodConf getMethodConf(MethodProto mp)
        throws CompilerException {
        MethodConf cmc = (MethodConf) mthInfos.get(mp);
        if (cmc == null) {
            throw new CompilerException(
                "No configuration found for method "
                    + mp
                    + " in class "
                    + getClassName());
        }
        return cmc;
    }

    public String getRemoteItfString() {
        Iterator it = Compiler.getRemoteItfs(cl).iterator();
        String s = "";
        while (it.hasNext()) {
            Class itf = (Class) it.next();
            if (s.equals("")) {
                s = MethodProto.getName(itf);
            } else {
                s += ", " + MethodProto.getName(itf);
            }
        }
        return s;
    }

    public String addRR() {
        String s = "rr" + balancerNo;
        balancerNo++;
        rrList.add(s);
        return s;  
    }

    public String addRandom() {
        String s = "rand" + balancerNo;
        balancerNo++;
        randList.add(s);
        return s;  
    }

    public ArrayList getRR() {
        return rrList;
    }

    public ArrayList getRandom() {
        return randList;
    }

    public String getBalancer() {
        if (defaultBalancer == null) {
            defaultBalancer = "balancer";
        }
        randList.add(defaultBalancer);
        return defaultBalancer;
    }

    public void validate() throws CompilerException {
        Method[] m = Compiler.getRemoteMethods(cl);
        if (m.length == 0) {
            throw new CompilerException("class " + cl + " does not implement remote methods");
        }
        for (int i=0; i<m.length; i++) {
            MethodProto mp = new MethodProto(m[i]);
            MethodConf mc = (MethodConf) mthInfos.get(mp);
            if (mc == null) {
                throw new CompilerException("no configuration given for method " + mp);
            }
            mc.setMethod(m[i]);
        }
        Iterator it = getMethodConfs();
        while (it.hasNext()) {
            MethodConf mc = (MethodConf) it.next();
            if (mc.getMethod() == null) {
                throw new CompilerException("class " + mc.getClassConf().getClassName() + " has no method " + mc.getMethodProto());
            }
        }
    }
}
