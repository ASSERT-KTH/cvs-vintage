/*
 * $Header: /tmp/cvs-vintage/struts/src/tiles-documentation/org/apache/struts/webapp/tiles/invoice/Invoice.java,v 1.1 2002/07/11 15:35:21 cedric Exp $
 * $Revision: 1.1 $
 * $Date: 2002/07/11 15:35:21 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.struts.webapp.tiles.invoice;

import org.apache.struts.action.ActionForm;

/**
 * An invoice.
 */
public class Invoice extends ActionForm
{

  /**
   * Shipping address
   */
  private Address shippingAddress;

  /**
   * Bill address
   */
  private Address billAddress;

  /**
   * Invoice total amount
   */
  private double amount;

  /**
   * Customer firstname
   */
  private String firstname;

  /**
   * Customer last name
   */
  private String lastname;

  public Invoice()
    {
    shippingAddress = new Address();
    billAddress = new Address();
    }

  /**
* Access method for the shippingAddress property.
*
* @return   the current value of the shippingAddress property
   */
  public Address getShippingAddress() {
    return shippingAddress;}

  /**
   * @return void
* Sets the value of the shippingAddress property.
*
* @param aShippingAddress the new value of the shippingAddress property
   */
  public void setShippingAddress(Address aShippingAddress) {
    shippingAddress = aShippingAddress;}

  /**
* Access method for the billAddress property.
*
* @return   the current value of the billAddress property
   */
  public Address getBillAddress() {
    return billAddress;}

  /**
   * @return void
* Sets the value of the billAddress property.
*
* @param aBillAddress the new value of the billAddress property
   */
  public void setBillAddress(Address aBillAddress) {
    billAddress = aBillAddress;}

  /**
* Access method for the amount property.
*
* @return   the current value of the amount property
   */
  public double getAmount() {
    return amount;}

  /**
   * @return void
* Sets the value of the amount property.
*
* @param aAmount the new value of the amount property
   */
  public void setAmount(double aAmount) {
    amount = aAmount;}

  /**
* Access method for the firstname property.
*
* @return   the current value of the firstname property
   */
  public String getFirstname() {
    return firstname;}

  /**
   * @return void
* Sets the value of the firstname property.
*
* @param aFirstname the new value of the firstname property
   */
  public void setFirstname(String aFirstname) {
    firstname = aFirstname;}

  /**
* Access method for the lastname property.
*
* @return   the current value of the lastname property
   */
  public String getLastname() {
    return lastname;}

  /**
   * @return void
* Sets the value of the lastname property.
*
* @param aLastname the new value of the lastname property
   */
  public void setLastname(String aLastname) {
    lastname = aLastname;}
}
