<xsl:stylesheet 
    xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0' >

<xsl:output method="xml" indent="yes" />  

<xsl:template match="/">
<modules>
    <xsl:comment>
This is automatically generated from the tomcat sources, using Alexandria's 
XMLDoclet. It is ( will be/can be ) used by configuration tools. One use is 
in ServerXmlReader to use a simpler syntax.
    </xsl:comment>    

    <xsl:for-each select="/javadoc/package/class[ 'org.apache.tomcat.core.BaseInterceptor' = extends_class/classref/@name]">
      <xsl:sort select="/javadoc/package" />
        <module>
          <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
          <xsl:attribute name="javaClass"><xsl:value-of select="../@name"/>.<xsl:value-of select="@name"/></xsl:attribute>
          <category>
               <xsl:attribute name="name">
               <xsl:call-template name="lastComponent"> 
                  <xsl:with-param name="full" select="../@name" />
               </xsl:call-template>
               </xsl:attribute>
          </category>
          <!-- <xsl:copy-of select="doc" /> -->
          <doc>Not yet</doc>
          <xsl:for-each select="method[substring(@name, 1,3) = 'set']">
             <attribute>
               <xsl:attribute name="name">
                  <xsl:call-template name="lowerCaseFirst">
                     <xsl:with-param name="n" select="substring( @name, 4)" />
                  </xsl:call-template>
                </xsl:attribute>
                <!-- <xsl:copy-of select="doc" />  -->
                <doc>Not yet</doc>
             </attribute>
          </xsl:for-each>
          <!-- request processing hooks - should be done using special tags -->
          <xsl:if test="method[@name = 'addContext']" ><addContext/></xsl:if>
        </module> 
      <xsl:text>


</xsl:text>
      </xsl:for-each>
      
</modules>
</xsl:template>

<xsl:template name="lowerCaseFirst">
     <xsl:param name="n"/>
     <xsl:value-of select="concat( translate( substring( $n ,1,1),
		                   'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
                                   'abcdefghijklmnopqrstuvwxyz'), 
                                   substring($n,2) )"/>
</xsl:template>


<xsl:template name="lastComponent">
    <xsl:param name="full" />
    <xsl:variable name="rest" select="substring-after($full, '.')"/>
    <xsl:if test="not($rest)" ><xsl:value-of select="$full" /></xsl:if>
    <xsl:if test="$rest" >
       <xsl:call-template name="lastComponent"> 
         <xsl:with-param name="full" select="$rest" />
       </xsl:call-template>
    </xsl:if>
</xsl:template>
  
</xsl:stylesheet>