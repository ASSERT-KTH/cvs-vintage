/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
 */
package org.objectweb.carol.cmi.compiler;


public class ClusterMethodInfo {
    public static final int ONE_CHOICE = 1;
    public static final int REDO_CHOICE = 2;
    public static final int REDO_CHOICE_RETRY = 3;

    private MethodProto methodProto = null;
    private int type = 0;

    public ClusterMethodInfo() {
    }

    public MethodProto setSignature(String sign) throws Exception {
        if (methodProto != null) {
            throw new Exception("Only one signature per method allowed.");
        }
        methodProto = new MethodProto(sign);
        return methodProto;
    }

    private void assertType() throws Exception {
        if (type != 0)
            throw new Exception("Only one type per method allowed.");
    }

    public void setOneChoice() throws Exception {
        assertType();
        type = ONE_CHOICE;
    }

    public void setRedoChoice() throws Exception {
        assertType();
        type = REDO_CHOICE;
    }

    public void setRedoChoiceRetry() throws Exception {
        assertType();
        type = REDO_CHOICE_RETRY;
    }

    public void verify() throws Exception {
        if (methodProto == null)
            throw new Exception("A signature has to be be provided for each method.");
        if (type == 0)
            throw new Exception(
                "Clustering type not provided for : " + methodProto.toString());
    }

    public int getType() {
        return type;
    }

    public String methodFieldType() {
        switch (type) {
            case REDO_CHOICE_RETRY :
                return "org.objectweb.StubListRandomChooser";
            case ONE_CHOICE :
                return null;
            case REDO_CHOICE :
                return null;
            default :
                return null;
        }
    }

    public MethodProto getMethodProto() {
        return methodProto;
    }
}
