<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="xml" indent="yes"/>

  <!--top level template converts to top level "server" tag-->
  <xsl:template match="ejb-jar/enterprise-beans">

    <server>

      <xsl:apply-templates select="message-driven"/>

    </server>

  </xsl:template>

<!--deploy ejb 2.0 style jms message driven beans -->
  <xsl:template match="message-driven[message-driven-destination]">

    <xsl:variable name="as-xmbean-dd-uri">jboss-di://JMS Adapter!!XMBEANS_DD#ra-xmbean-dds/activation-specs/activationspec/mbean[class='org.jboss.resource.adapter.jms.JmsActivationSpec']</xsl:variable>

    <xsl:variable name="jboss-dd-uri">jboss-di://!!JBOSS_EJB_DD#jboss/enterprise-beans/message-driven[ejb-name='<xsl:value-of select="ejb-name"/>']</xsl:variable>

    <mbean code="{document($as-xmbean-dd-uri)/mbean/class}" name="jboss.jca:service=ActivationSpec,name={ejb-name}" xmbean-version="-//JBoss//DTD JBOSS XMBEAN 1.0//EN" display-name="ActivationSpec {ejb-name}">

      <xsl:apply-templates select="document($as-xmbean-dd-uri)" mode="copy">
      </xsl:apply-templates>


      <depends optional-attribute-name="ResourceAdapterName">jboss.jca:service=ResourceAdapter,name=jmsra</depends>
      <depends optional-attribute-name="MessageDrivenContainerName">jboss.j2ee:service=EJB,jndiName=local/<xsl:value-of select="ejb-name"/></depends>

      <!--this is not currently in the jboss dd anywhere I can find-->
      <attribute name="ConnectionFactory">java:/JmsXA</attribute>

      <attribute name="Destination"><xsl:value-of select="document($jboss-dd-uri)/message-driven/destination-jndi-name"/></attribute>

      <attribute name="DestinationType"><xsl:value-of select="message-driven-destination/destination-type"/></attribute>

      <xsl:if test="message-selector">
        <attribute name="MessageSelector"><xsl:value-of select="message-selector"/></attribute>
      </xsl:if>

      <xsl:if test="acknowledge-mode">
        <attribute name="AcknowledgeMode"><xsl:value-of select="acknowledge-mode"/></attribute>
      </xsl:if>

      <xsl:if test="message-driven-destination/subscription-durability">
        <attribute name="SubscriptionDurability"><xsl:value-of select="message-driven-destination/subscription-durability"/></attribute>
      </xsl:if>

      <xsl:if test="document($jboss-dd-uri)/message-driven/mdb-client-id">
        <attribute name="ClientId"><xsl:value-of select="document($jboss-dd-uri)/message-driven/mdb-client-id"/></attribute>
      </xsl:if>

      <xsl:if test="document($jboss-dd-uri)/message-driven/mdb-subscription-id">
        <attribute name="SubscriptionName"><xsl:value-of select="document($jboss-dd-uri)/message-driven/mdb-subscription-id"/></attribute>
      </xsl:if>
    </mbean>
  </xsl:template>


<!-- deploy ejb 2.1 arbitrary interface mdbs.  This requires validation to be off, since
 the ejb 2.0 dtd we use doesn't allow activaction-config-->
  <xsl:template match="message-driven[activation-config]">

    <xsl:variable name="jboss-dd-uri">jboss-di://!!JBOSS_EJB_DD#jboss/enterprise-beans/message-driven[ejb-name='<xsl:value-of select="ejb-name"/>']</xsl:variable>


    <xsl:variable name="as-xmbean-dd-uri">jboss-di://JMS Adapter!!XMBEANS_DD#ra-xmbean-dds/activation-specs/activationspec/mbean[class='<xsl:value-of select="$jboss-dd-uri/activation-config/activation-spec-class"/>']</xsl:variable>

    <mbean code="{document($as-xmbean-dd-uri)/mbean/class}" name="jboss.jca:service=ActivationSpec,name={ejb-name}" xmbean-version="-//JBoss//DTD JBOSS XMBEAN 1.0//EN" display-name="ActivationSpec {ejb-name}">

      <xsl:apply-templates select="document($as-xmbean-dd-uri)" mode="copy">
      </xsl:apply-templates>


      <depends optional-attribute-name="ResourceAdapterName">jboss.jca:service=ResourceAdapter,name=<xsl:value-of select="$jboss-dd-uri/activation-config/resource-adapter-name"/></depends>
      <depends optional-attribute-name="MessageDrivenContainerName">jboss.j2ee:service=EJB,jndiName=local/<xsl:value-of select="ejb-name"/></depends>

      <!--apply values from ejb-jar.xml-->
      <xsl:apply-templates select="activation-config-property"/>

      <!--apply values from jboss.xml, some of which may reset the values from ejb-jar.xml-->
      <xsl:apply-templates select="$jboss-dd-uri/activation-config/activation-config-property"/>
 
    </mbean>
  </xsl:template>

  <xsl:template match="activation-config-property">
    <attribute name="{activation-config-property-name}"><xsl:value-of select="activation-config-property-value"/></attribute>
  </xsl:template>

<!-- allow copying xmbean descriptor-->
  <xsl:template  mode="copy" match="*|@*|text()">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|text()" mode="copy"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>