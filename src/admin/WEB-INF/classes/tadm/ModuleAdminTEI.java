package tadm;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

public class ModuleAdminTEI extends TagExtraInfo {

    public VariableInfo[] getVariableInfo(TagData data) {
	return (new VariableInfo[] {
	    new VariableInfo(data.getAttributeString( "var" ),
			     data.getAttributeString( "type" ),
			     true,
			     VariableInfo.AT_BEGIN ), 
	});

    }


}
