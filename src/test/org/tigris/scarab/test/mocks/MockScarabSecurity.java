/*
 * Created on Nov 20, 2004
 *
 */
package org.tigris.scarab.test.mocks;

import org.tigris.scarab.services.security.ScarabSecurity;

/**
 * @author Eric Pugh
 *
 */
public class MockScarabSecurity extends ScarabSecurity {

    public MockScarabSecurity() {
        super();

    }
    protected String getPermissionImpl(String permConstant)
    {
        //return props.getString(MAP_PREFIX + permConstant,null);
        return permConstant;
    }
}
