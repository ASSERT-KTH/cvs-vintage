package org.apache.struts.tiles.beans;

import java.io.Serializable;


  /**
   * A bean MenuItem implementation.
   * Used to read menu item in definitions.
   */
public class SimpleMenuItem implements MenuItem, Serializable
{

  private String value;
  private String link;
  private String icon;
  private String tooltip;

    /**
     * Constructor.
     */
  public SimpleMenuItem()
  {
  }
    /**
     * Set value property
     */
  public void setValue(String value)
  {
    this.value = value;
  }

    /**
     * Get value property
     */
  public String getValue()
  {
    return value;
  }

    /**
     * Set link property
     */
  public void setLink(String link)
  {
    this.link = link;
  }

    /**
     * Get link property
     */
  public String getLink()
  {
    return link;
  }

    /**
     * Get icon property
     */
  public void setIcon(String icon)
  {
    this.icon = icon;
  }

    /**
     * Get icon property
     */
  public String getIcon()
  {
    return icon;
  }

    /**
     * Get tooltip property
     */
  public void setTooltip(String tooltip)
  {
    this.tooltip = tooltip;
  }

    /**
     * Get tooltip property
     */
  public String getTooltip()
  {
    return tooltip;
  }

    /**
     * toString method
     */
  public String toString()
  {
  StringBuffer buff = new StringBuffer();

  buff.append( "SimpleMenuItem[");
  if( getValue() != null )
    buff.append( "value=" ).append(getValue()).append( ", " );
  if( getLink() != null )
    buff.append( "link=" ).append(getLink()).append( ", " );
  if( getTooltip() != null )
    buff.append( "tooltip=" ).append(getTooltip()).append( ", " );
  if( getIcon() != null )
    buff.append( "icon=" ).append(getIcon()).append( ", " );
  buff.append( "]");
  return buff.toString();
  }
}
