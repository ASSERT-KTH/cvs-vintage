package examples;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

public class FooTagExtraInfo extends TagExtraInfo {
    public VariableInfo[] getVariableInfo(TagData data) {
        return new VariableInfo[] 
            {
                new VariableInfo("member",
                                 "String",
                                 true,
                                 VariableInfo.NESTED)
            };
    }
}

        
