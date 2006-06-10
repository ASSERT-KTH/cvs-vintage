<?xml version="1.0" encoding="UTF-8"?>
   <xsl:stylesheet version="1.0"
   	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   	xmlns:ical="http://www.ietf.org/rfc/rfc2445.txt"
   	xmlns:itip="http://www.ietf.org/rfc/rfc2446.txt"
   	xmlns="http://www.ietf.org/rfc/rcf2445.txt">
   <!-- ======================================================================== -->
   <!-- xcal2ics.xsl XSL transformation of xCalendar documents to iCalendar file -->
   <!-- Tim Hare, April 2004                                                     -->
   <!-- Tim Hare, August 2004 -  updated to wrap at 75 chars                     -->
   <!-- This transformation may be freely used, attribution is appreciated       -->
   <!-- Concepts and inspiration due to rdf2ical from Masahide Kanzaki           -->
   <!-- who should also receive attribution                                      -->
   <!-- ======================================================================== -->
   <!-- define output format for iCalendar for use in result-document instr.     -->
<xsl:output name="iCalendar" method="text" media-type="text/calendar" Indent="no" />
   <xsl:template match="icalendar|iCalendar|ical:icalendar|ical:iCalendar">
   	<xsl:apply-templates select="vcalendar|ical:vcalendar" />
   </xsl:template>
   <xsl:template match="vcalendar|ical:vcalendar">
   	<!-- name result document from name= attribute on vcalendar -->
   	<!-- V2.0 <xsl:result-document format="iCalendar" href="testcalx.ics"> -->
   		<xsl:call-template name="emit_text">
   		<!-- parameter "line" is entire result of here to end of with-param -->
   		<xsl:with-param name="line">
   			<xsl:text>BEGIN:VCALENDAR</xsl:text>
   		</xsl:with-param>
   		</xsl:call-template>
   			<xsl:if test="not(@method) and
   		      	        not(@Method) and
   			      	  not(@ical:method) and
   				 	  not(@ical:Method)">
   				<xsl:call-template name="emit_text">
   				<xsl:with-param name="line">
   					<xsl:text>METHOD:PUBLISH</xsl:text>
   				</xsl:with-param>
   				</xsl:call-template>
   			</xsl:if>
   			<xsl:for-each select="@*">
   				<xsl:call-template name="emit_text">
   				<xsl:with-param name="line">
   					<xsl:call-template name="stringcap">
   					<xsl:with-param name="s" select="local-name()" />
   					</xsl:call-template>
   					<xsl:text>:</xsl:text>
   					<xsl:call-template name="stringcap">
   					<xsl:with-param name="s" select="current()" />
   					</xsl:call-template>
   				</xsl:with-param>
   				</xsl:call-template>
   			</xsl:for-each>
   		<xsl:apply-templates select="./*" />
   		<xsl:call-template name="emit_text">
   		<xsl:with-param name="line">
   			<xsl:text>END:VCALENDAR</xsl:text>
   		</xsl:with-param>
   		</xsl:call-template>
   	<!-- </xsl:result-document> -->
   </xsl:template>
   <xsl:template name="eachcomponent" 	match="vevent|vtodo|vjournal|valarm|ical:vevent|ical:vtodo|ical:vjournal|ical:valarm">
   	<xsl:call-template name="emit_text">
   	<xsl:with-param name="line">
   		<xsl:text>BEGIN:</xsl:text>
<xsl:call-template name="stringcap">
   		<xsl:with-param name="s" select="local-name()" />
   		</xsl:call-template>
   	</xsl:with-param>
   	</xsl:call-template>
   		<xsl:for-each select="./*" >
   				<xsl:call-template name="eachproperty" />
   		</xsl:for-each>
   		<xsl:call-template name="emit_text">
   		<xsl:with-param name="line">
   			<xsl:text>END:</xsl:text>
   			<xsl:call-template name="stringcap">
   			<xsl:with-param name="s" select="local-name()" />
   			</xsl:call-template>
   		</xsl:with-param>
   		</xsl:call-template>
   </xsl:template>
   <xsl:template name="eachproperty">
   	<xsl:if test="local-name() != 'categories' and
   			  local-name() != 'CATEGORIES' and
   			  local-name() != 'geo' and
   			  local-name() != 'GEO' and
   			  local-name() != 'resources' and
   			  local-name() != 'RESOURCES'">
   	<!-- this block outputs a property with all of its parameters -->
   		<xsl:call-template name="emit_text">
   		<xsl:with-param name="line">
   			<xsl:call-template name="stringcap">
   			<xsl:with-param name="s" select="local-name()" />
   			</xsl:call-template>
   			<xsl:for-each select="@*">
   				<xsl:call-template name="eachparam">
   				<xsl:with-param name="p" select="local-name()" />
   				</xsl:call-template>
   			</xsl:for-each>
   			<xsl:text>:</xsl:text><xsl:value-of select="." />
   		</xsl:with-param>
   		</xsl:call-template>
   	<!-- end of regular properties ouput block                     -->
   	</xsl:if>
   	<xsl:if test="local-name() = 'categories' or
   			  local-name() = 'CATEGORIES' or
   			  local-name() = 'resources' or
   			  local-name() = 'RESOURCES'">
   	<!-- this block outputs a Categories or Resources property     -->
   		<xsl:call-template name="emit_text">
   		<xsl:with-param name="line">
   			<xsl:call-template name="stringcap">
<xsl:with-param name="s" select="local-name()" />
   			</xsl:call-template>
   			<xsl:for-each select="@*">
   				<xsl:call-template name="eachparam">
   				<xsl:with-param name="p" select="local-name()" />
   				</xsl:call-template>
   			</xsl:for-each>
   			<xsl:text>:</xsl:text>
   			<xsl:for-each select="./item|./ITEM">
   				<xsl:call-template name="stringcap">
   				<xsl:with-param name="s" select="current()" />
   				</xsl:call-template>
   				<xsl:if test="position() != last()">
   					<xsl:text>,</xsl:text>
   				</xsl:if>
   			</xsl:for-each>
   		</xsl:with-param>
   		</xsl:call-template>
   	<!-- end of Categories or Resources property output block       -->
   	</xsl:if>
   	<xsl:if test="local-name() = 'geo' or
   			  local-name() = 'GEO'">
   	<!-- this block outputs a Geo property                          -->
   		<xsl:call-template name="emit_text">
   		<xsl:with-param name="line">
   			<xsl:call-template name="stringcap">
   			<xsl:with-param name="s" select="local-name()" />
   			</xsl:call-template>
   			<xsl:text>:</xsl:text>
   			<xsl:value-of select="lat" />
   			<xsl:text>;</xsl:text>
   			<xsl:value-of select="lon" />
   		</xsl:with-param>
   		</xsl:call-template>
   	<!-- end of Geo property output block                            -->
   	</xsl:if>
   </xsl:template>
   <xsl:template name="eachparam">
   	<xsl:param name="p" />
   	<xsl:text>;</xsl:text>
   	<xsl:call-template name="stringcap">
   		<xsl:with-param name="s" select="string($p)"/>
   	</xsl:call-template>
   	<xsl:text>=</xsl:text>
   	<xsl:value-of select="." />
   </xsl:template>
   <!-- Capitalize and output a string -->
   <xsl:template name="stringcap">

<xsl:param name="s"/>
   <xsl:value-of select="translate($s,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
   </xsl:template>
   <!-- Output the newline string -->
   <xsl:template name="newline">
   <xsl:text>&#13;&#10;</xsl:text>
   </xsl:template>
   <!-- Emit text, 75 character max per line, recursively -->
   <xsl:template name="emit_text">
   	<xsl:param name="limit" select="number(75)"/> <!-- default limit is 75 " -->
   	<xsl:param name="line"/>
   	<xsl:value-of select="substring(normalize-space($line),1,$limit)" />
   	<!-- Output the newline string -->
   	<xsl:text>&#13;&#10;</xsl:text>
   	<xsl:if test="string-length($line) > $limit">
   		<xsl:text> </xsl:text>
   		<xsl:call-template name="emit_text">
   			<xsl:with-param name="limit" select="($limit - 1)" /> <!-- use 74 allow for space -->
   			<xsl:with-param name="line" select="substring($line,($limit + 1))" />
   		</xsl:call-template>
   	</xsl:if>
   </xsl:template>
   </xsl:stylesheet>
