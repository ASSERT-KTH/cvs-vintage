<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" encoding="ISO-8859-1" omit-xml-declaration="no" doctype-system="scarab.dtd" indent="yes"/>

<xsl:template match="bugzilla">
<scarab>
    <module id="6" parent="3">
        <name>Source</name>
        <code>TBNS</code>
        <xsl:for-each select="bug">
        <issue id="{bug_id}">
            <artifact-type>defect</artifact-type>
            
            <transaction id="1">
                <type>Create Issue</type>
                <committed-by><xsl:value-of select="reporter"/></committed-by>
                <xsl:if test="bug_status">
                <activity>
                    <attribute>
                        <name>Status</name>
                        <value><xsl:value-of select="bug_status"/></value>
                        <type>combo-box</type>
                    </attribute>
                    <description>Status set to <xsl:value-of select="bug_status"/></description>
                </activity>
                </xsl:if>
                <xsl:if test="bug_severity">
                <activity>
                    <attribute>
                        <name>Severity</name>
                        <value><xsl:value-of select="bug_severity"/></value>
                        <type>combo-box</type>
                    </attribute>
                    <description>Severity set to <xsl:value-of select="bug_severity"/></description>
                </activity>
                </xsl:if>
                <xsl:if test="short_desc">
                <activity>
                    <attribute>
                        <name>Summary</name>
                        <value><xsl:value-of select="short_desc"/></value>
                        <type>long-string</type>
                    </attribute>
                    <description>Summary set to <xsl:value-of select="short_desc"/></description>
                </activity>
                <activity>
                    <attribute>
                        <name>Description</name>
                        <value><xsl:value-of select="short_desc"/></value>
                        <type>long-string</type>
                    </attribute>
                    <description>Description set to <xsl:value-of select="short_desc"/></description>
                </activity>
                </xsl:if>
                <xsl:if test="rep_platform">
                <activity>
                    <attribute>
                        <name>Platform</name>
                        <value><xsl:value-of select="rep_platform"/></value>
                        <type>combo-box</type>
                    </attribute>
                    <description>Platform set to <xsl:value-of select="rep_platform"/></description>
                </activity>
                </xsl:if>
                <xsl:if test="op_sys">
                <activity>
                    <attribute>
                        <name>Operating System</name>
                        <value><xsl:value-of select="op_sys"/></value>
                        <type>combo-box</type>
                    </attribute>
                    <description>Operating System set to <xsl:value-of select="op_sys"/></description>
                </activity>
                </xsl:if>
                <xsl:if test="assigned_to">
                <activity>
                    <attribute>
                        <name>Assigned To</name>
                        <value><xsl:value-of select="assigned_to"/></value>
                        <type>user</type>
                    </attribute>
                    <description>Assigned To set to <xsl:value-of select="assigned_to"/></description>
                </activity>
                </xsl:if>
                <xsl:if test="resolution">
                <activity>
                    <attribute>
                        <name>Resolution</name>
                        <value><xsl:value-of select="resolution"/></value>
                        <type>combo-box</type>
                    </attribute>
                    <description>Resolution set to <xsl:value-of select="resolution"/></description>
                </activity>
                </xsl:if>
            </transaction>
            
            <xsl:for-each select="dependson">
            <dependency>
                <type>blocking</type>
                <child>TBNS<xsl:value-of select="."/></child>
            </dependency>
            </xsl:for-each>
                
            <xsl:for-each select="blocks">
            <dependency>
                <type>blocking</type>
                <parent>TBNS<xsl:value-of select="."/></parent>
            </dependency>
            </xsl:for-each>
            
            <xsl:for-each select="attachment">
            <attachment>
                <name>Bugzilla Attachment</name>
                <type>ATTACHMENT</type>
                <path>file://server/path/to/file.txt</path>
                <data></data>
                <mimetype>text/plain</mimetype>
                <created-date format="MM/dd/yy HH:mm"><xsl:value-of select="date"/></created-date>
                <modified-date format="MM/dd/yy HH:mm"><xsl:value-of select="date"/></modified-date>
                <created-by>jon@latchkey.com</created-by>
                <modified-by>jon@latchkey.com</modified-by>
            </attachment>
            </xsl:for-each>
            
            <xsl:for-each select="long_desc">
            <attachment>
                <name>Bugzilla Comment</name>
                <type>COMMENT</type>
                <data><xsl:value-of select="thetext"/></data>
                <mimetype>text/plain</mimetype>
                <created-date format="yyyy-MM-dd HH:mm:ss"><xsl:value-of select="bug_when"/></created-date>
                <modified-date format="yyyy-MM-dd HH:mm:ss"><xsl:value-of select="bug_when"/></modified-date>
                <created-by><xsl:value-of select="who"/></created-by>
                <modified-by><xsl:value-of select="who"/></modified-by>
            </attachment>
            </xsl:for-each>
        </issue>
        </xsl:for-each>
    </module>
</scarab>
</xsl:template>

</xsl:stylesheet>
