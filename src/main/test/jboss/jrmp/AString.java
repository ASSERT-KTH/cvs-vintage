package test.jboss.jrmp;

import java.io.Serializable;

public class AString implements IString, Serializable
{
    private String theString;

    public AString(String theString)
    {
        this.theString = theString;
    }
    public String toString()
    {
        return theString;
    }
}
