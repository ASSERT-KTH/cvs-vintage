<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="bugzilla">
<scarab>
    <module>
        <name></name>
        <code></code>
        <xsl:for-each select="bug">
        <issue id="{bug_id}">
            <artifact-type>defect</artifact-type>
            <xsl:if test="bug_status">
            <issue-attribute>
                <name>Status</name>
                <value><xsl:value-of select="bug_status"/></value>
                <type>combo-box</type>
            </issue-attribute>
            </xsl:if>
            <xsl:if test="bug_severity">
            <issue-attribute>
                <name>Severity</name>
                <value><xsl:value-of select="bug_severity"/></value>
                <type>combo-box</type>
            </issue-attribute>
            </xsl:if>
            <xsl:if test="short_desc">
            <issue-attribute>
                <name>Summary</name>
                <value><xsl:value-of select="short_desc"/></value>
                <type>long-string</type>
            </issue-attribute>
            </xsl:if>
            <xsl:if test="op_sys">
            <issue-attribute>
                <name>Operating System</name>
                <value><xsl:value-of select="op_sys"/></value>
                <type>combo-box</type>
            </issue-attribute>
            </xsl:if>
            <xsl:if test="assigned_to">
            <issue-attribute>
                <name>Assigned To</name>
                <value><xsl:value-of select="assigned_to"/></value>
                <type>user</type>
            </issue-attribute>
            </xsl:if>
            
            <xsl:for-each select="attachment">
            <attachment>
                <name>Bugzilla Attachment</name>
                <type>ATTACHMENT</type>
                <mimetype></mimetype>
                <modified-date><xsl:value-of select="date"/></modified-date>
                <created-by></created-by>
                <modified-by></modified-by>
            </attachment>
            </xsl:for-each>
            
            <xsl:for-each select="long_desc">
            <attachment>
                <name>Bugzilla Comment</name>
                <type>COMMENT</type>
                <mimetype></mimetype>
                <modified-date><xsl:value-of select="bug_when"/></modified-date>
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
