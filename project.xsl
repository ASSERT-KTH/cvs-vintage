<?xml version="1.0" encoding="ISO-8859-1" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output indent="yes" method="text"/>

<xsl:variable name="cr" select="'&#xa;'"/>
<xsl:variable name="separator">######################################################################</xsl:variable>
<xsl:variable name="gsep">==============================================================</xsl:variable>
<xsl:variable name="psep">--------------------------------------------------------------</xsl:variable>

<xsl:template name="create-comment">
  <xsl:param name="text"/>
  <xsl:param name="indent-correction">-1</xsl:param>
  <xsl:variable name="current-line">
    <xsl:choose>
      <xsl:when test="contains($text,$cr)"><xsl:value-of select="substring-before($text,$cr)"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$text"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="current-indent-correction">
    <xsl:choose>
      <xsl:when test="$indent-correction='-1' and string-length($current-line) &gt; 0">
        <xsl:variable name="start-character" select="substring(concat(normalize-space($current-line),'.'),1,1)"/>
        <xsl:value-of select="string-length(substring-before(concat($current-line,'.'),$start-character))"/>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$indent-correction"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="display-line">
    <xsl:choose>
      <xsl:when test="$current-indent-correction='-1'"># <xsl:value-of select="concat($current-line,$cr)"/></xsl:when>
      <xsl:otherwise># <xsl:value-of select="concat(substring($current-line,$current-indent-correction),$cr)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
<xsl:value-of select="$display-line"/>
  <xsl:if test="contains($text,$cr)">
    <xsl:call-template name="create-comment">
      <xsl:with-param name="text" select="substring-after($text,$cr)"/>
      <xsl:with-param name="indent-correction" select="$current-indent-correction"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>


<xsl:template match="/properties"><xsl:value-of select="$separator"/>
  <xsl:call-template name="create-comment">
    <xsl:with-param name="text" select="details/."/>
  </xsl:call-template>#<xsl:value-of select="$separator"/>
  <xsl:apply-templates />
</xsl:template>


<xsl:template match="section">


#<xsl:value-of select="$separator"/>
# Section: <xsl:value-of select="@name"/>
#<xsl:value-of select="$separator"/>
  <xsl:choose>
    <xsl:when test="boolean(details)">
      <xsl:call-template name="create-comment">
        <xsl:with-param name="text" select="details/."/>
      </xsl:call-template>#<xsl:value-of select="$separator"/>
    </xsl:when>
    <xsl:otherwise>#<xsl:value-of select="$cr"/>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:apply-templates />
</xsl:template>




<xsl:template match="group">


# <xsl:value-of select="substring($gsep,1,7 + string-length(@name))"/>
# Group: <xsl:value-of select="@name"/>
# <xsl:value-of select="substring($gsep,1,7 + string-length(@name))"/>
#
<xsl:for-each select="property">#   <xsl:value-of select="concat(name,$cr)"/></xsl:for-each>
<xsl:call-template name="create-comment">
  <xsl:with-param name="text" select="details/."/>
</xsl:call-template>
<xsl:apply-templates/>

# <xsl:value-of select="substring($gsep,1,13 + string-length(@name))"/>
# End of Group <xsl:value-of select="@name"/>
# <xsl:value-of select="substring($gsep,1,13 + string-length(@name))"/>
</xsl:template>



<xsl:template match="property">
<xsl:if test="boolean(details)">

# <xsl:value-of select="substring($psep,1,string-length(name/.))"/>
# <xsl:value-of select="name" />
# <xsl:value-of select="substring($psep,1,string-length(name/.))"/>
#
<xsl:call-template name="create-comment">
  <xsl:with-param name="text" select="details/."/>
</xsl:call-template>
</xsl:if>
<xsl:value-of select="$cr"/><xsl:value-of select="name"/>=<xsl:value-of select="default"/>
</xsl:template>


<!-- By default, discard anything not matched yet -->
<xsl:template match="@*|node()" priority="-1"/>

</xsl:stylesheet>
