<%@ taglib uri="/jsp/libs/taglib.jar" prefix="akv" %>

Radio stations that rock:

<ul>
<akv:foo att1="98.5" att2="92.3" att3="107.7">
<li><%= member %></li>
</akv:foo>
</ul>

<akv:log>
Did you see me on the stderr window?
</akv:log>

<akv:log tobrowser="true">
Did you see me on the browser window as well?
</akv:log>