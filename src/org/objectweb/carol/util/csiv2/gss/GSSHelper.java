/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2004 Bull S.A.
 * Contact: jonas-team@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: GSSHelper.java,v 1.1 2004/12/13 16:24:13 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.util.csiv2.gss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.omg.GSSUP.GSSUPMechOID;

import org.objectweb.carol.util.configuration.TraceCarol;



/**
 * Handle RFC 2743 encoding
 * @author Florent Benoit
 */
public class GSSHelper {

    /**
     * TOKEN ID used when creating exported name
     */
    private static final byte[] EXPORTED_NAME_TOK_ID = new byte[]{0x04, 0x01};

    /**
     * Constant for bit operations
     */
    private static final int[] TWO_BYTES = {0xFF00, 0xFF};

    /**
     * Constant for bit operations
     */
    private static final int[] FOUR_BYTES = {0xFF000000, 0xFF0000, 0xFF00, 0xFF};

    /**
     * Constant for bit operations
     */
    private static final int BYTES = 0xFF;

    /**
     * Constant used when decoding a token
     */
    private static final int SEQUENCE = 0x60;

    /**
     * Constant used when decoding a token
     */
    private static final int OBJECT_IDENTIFIER = 0x06;


    /**
     * Utility class, no public constructor
     */
    private GSSHelper() {

    }


    /**
     * An encoding of a GSS Mechanism-Independent Exported Name Object as
     * defined in [IETF RFC 2743] Section 3.2, "GSS Mechanism-Independent
     * Exported Name Object Format," p. 84.
     * http://www.ietf.org/rfc/rfc2743.txt
     *
     * 3.2: Mechanism-Independent Exported Name Object Format
     *
     * This section specifies a mechanism-independent level of encapsulating
     * representation for names exported via the GSS_Export_name() call,
     * including an object identifier representing the exporting mechanism.
     * The format of names encapsulated via this representation shall be
     * defined within individual mechanism drafts.  The Object Identifier
     * value to indicate names of this type is defined in Section 4.7 of
     * this document.
     *
     * No name type OID is included in this mechanism-independent level of
     * format definition, since (depending on individual mechanism
     * specifications) the enclosed name may be implicitly typed or may be
     * explicitly typed using a means other than OID encoding.
     *
     * The bytes within MECH_OID_LEN and NAME_LEN elements are represented
     * most significant byte first (equivalently, in IP network byte order).
     *
     * Length    Name          Description
     *
     * 2               TOK_ID          Token Identifier<br>
     * For exported name objects, this<br>
     * must be hex 04 01.<br>
     * 2               MECH_OID_LEN    Length of the Mechanism OID<br>
     * MECH_OID_LEN    MECH_OID        Mechanism OID, in DER<br>
     * 4               NAME_LEN        Length of name<br>
     * NAME_LEN        NAME            Exported name; format defined in<br>
     * applicable mechanism draft.<br>
     *
     * A concrete example of the contents of an exported name object,
     * derived from the Kerberos Version 5 mechanism, is as follows:
     *
     * 04 01 00 0B 06 09 2A 86 48 86 F7 12 01 02 02 hx xx xx xl pp qq ... zz<br>
     *
     * 04 01        mandatory token identifier<br>
     *
     * 00 0B        2-byte length of the immediately following DER-encoded<br>
     * ASN.1 value of type OID, most significant octet first<br>
     *
     * 06 09 2A 86 48 86 F7 12 01 02 02    DER-encoded ASN.1 value<br>
     * of type OID; Kerberos V5<br>
     * mechanism OID indicates<br>
     * Kerberos V5 exported name<br>
     *
     * in Detail:      06                  Identifier octet (6=OID)<br>
     * 09                           Length octet(s)<br>
     * 2A 86 48 86 F7 12 01 02 02   Content octet(s)<br>
     *
     * hx xx xx xl   4-byte length of the immediately following exported<br>
     * name blob, most significant octet first<br>
     *
     * pp qq ... zz  exported name blob of specified length,<br>
     * bits and bytes specified in the<br>
     * (Kerberos 5) GSS-API v2 mechanism spec<br>
     */

    /**
     * Encode the given string into an array of byte
     * By following RFC 2743
     * @param name the given name to encode
     * @return encoded string following RFC 2743 3.2 section
     */
    public static byte[] encodeExported(String name) {

        byte[] mechOidDer = GSSHelper.getMechOidDer();
        byte[] nameBytes = null;
        try {
            // Use UTF-8 for bytes
            nameBytes = name.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException("Cannot get utf-8 encoding" + uee.getMessage());
        }
        int nameLength = name.length();

        // Token identifier for exported name is 04 01 (EXPORTED_NAME_TOK_ID)
        //TOK_ID (2 bytes)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(EXPORTED_NAME_TOK_ID[0]);
        bos.write(EXPORTED_NAME_TOK_ID[1]);

        // MECH_OID_LEN (2 bytes)
        int mechOidLength = mechOidDer.length;
        bos.write(mechOidLength & TWO_BYTES[0]);
        bos.write(mechOidLength & TWO_BYTES[1]);

        // MECH_OID (in DER format)
        bos.write(mechOidDer, 0, mechOidDer.length);

        // NAME_LEN (4 bytes)
        bos.write(nameLength & FOUR_BYTES[0]);
        bos.write(nameLength & FOUR_BYTES[1]);
        bos.write(nameLength & FOUR_BYTES[2]);
        bos.write(nameLength & FOUR_BYTES[3]);

        // NAME
        bos.write(nameBytes, 0, nameBytes.length);
        return bos.toByteArray();

    }

    /**
     * @param toDecode byte to decode
     * @return a string corresponding to the decoding of the given array of bytes
     * @throws Exception if the decoding failed
     */
    public static String decodeExported(byte[] toDecode) throws Exception {

        ByteArrayInputStream bis = new ByteArrayInputStream(toDecode);

        // TOK_ID (2 bytes)
        if (bis.read() != EXPORTED_NAME_TOK_ID[0] || bis.read() != EXPORTED_NAME_TOK_ID[1]) {
            throw new IllegalArgumentException("Invalid header, this is not an exported name");
        }

        // MECH_OID_LEN (2 bytes)
        int mechOidLength = bis.read() * 8 + bis.read();

        // MECH_OID (in DER format)
        byte[] mechOidDerTemplate = GSSHelper.getMechOidDer();
        byte[] mechOidDer = new byte[mechOidLength];
        int success = bis.read(mechOidDer);
        if (success == -1 || success != mechOidDerTemplate.length) {
            throw new IllegalArgumentException("Not able to decode name, length is incorrect");
        } else {
            // validate it
            for (int b = 0; b < mechOidDerTemplate.length; b++) {
                if (mechOidDer[b] != mechOidDerTemplate[b]) {
                    throw new IllegalArgumentException("Not a valid MechoID");
                }
            }
        }

        // NAME_LEN
        int nameLength = bis.read() * 24 + bis.read() * 16 + bis.read() * 8 + bis.read();

        byte[] name = new byte[nameLength];
        success = bis.read(name);
        if (success == -1 || success != nameLength) {
            throw new IllegalArgumentException("Not able to decode name, length is incorrect");
        }
        return new String(name);
    }

    /**
     * Gets the MechOID for the type GSSUP
     * @return MechOID for the type GSSUP
     */
    private static String getMechOID() {
        return GSSUPMechOID.value.substring(4);
    }


    /**
     * 3.1: Mechanism-Independent Token Format
     * This section specifies a mechanism-independent level of encapsulating
     * representation for the initial token of a GSS-API context
     * establishment sequence, incorporating an identifier of the mechanism
     * type to be used on that context and enabling tokens to be interpreted
     * unambiguously at GSS-API peers. Use of this format is required for
     * initial context establishment tokens of Internet standards-track
     * GSS-API mechanisms; use in non-initial tokens is optional.
     *
     * The encoding format for the token tag is derived from ASN.1 and DER
     * (per illustrative ASN.1 syntax included later within this
     * subsection), but its concrete representation is defined directly in
     * terms of octets rather than at the ASN.1 level in order to facilitate
     * interoperable implementation without use of general ASN.1 processing
     * code.  The token tag consists of the following elements, in order:
     *
     * 1. 0x60 -- Tag for [APPLICATION 0] SEQUENCE; indicates that
     * -- constructed form, definite length encoding follows.
     *
     * 2. Token length octets, specifying length of subsequent data
     * (i.e., the summed lengths of elements 3-5 in this list, and of the
     * mechanism-defined token object following the tag).  This element
     * comprises a variable number of octets:

     * 2a. If the indicated value is less than 128, it shall be
     * represented in a single octet with bit 8 (high order) set to
     * "0" and the remaining bits representing the value.
     *
     * 2b. If the indicated value is 128 or more, it shall be
     * represented in two or more octets, with bit 8 of the first
     * octet set to "1" and the remaining bits of the first octet
     * specifying the number of additional octets.  The subsequent
     * octets carry the value, 8 bits per octet, most significant
     * digit first.  The minimum number of octets shall be used to
     * encode the length (i.e., no octets representing leading zeros
     * shall be included within the length encoding).
     *
     * 3. 0x06 -- Tag for OBJECT IDENTIFIER
     *
     * 4. Object identifier length -- length (number of octets) of
     * -- the encoded object identifier contained in element 5,
     * -- encoded per rules as described in 2a. and 2b. above.
     *
     * 5. Object identifier octets -- variable number of octets,
     * -- encoded per ASN.1 BER rules:
     *
     * 5a. The first octet contains the sum of two values: (1) the
     * top-level object identifier component, multiplied by 40
     * (decimal), and (2) the second-level object identifier
     * component.  This special case is the only point within an
     * object identifier encoding where a single octet represents
     * contents of more than one component.
     *
     * 5b. Subsequent octets, if required, encode successively-lower
     * components in the represented object identifier.  A component's
     * encoding may span multiple octets, encoding 7 bits per octet
     * (most significant bits first) and with bit 8 set to "1" on all
     * but the final octet in the component's encoding.  The minimum
     * number of octets shall be used to encode each component (i.e.,
     * no octets representing leading zeros shall be included within a
     * component's encoding).

     *       (Note: In many implementations, elements 3-5 may be stored and
     * referenced as a contiguous string constant.)

     * The token tag is immediately followed by a mechanism-defined token
     * object.  Note that no independent size specifier intervenes following
     * the object identifier value to indicate the size of the mechanism-
     * defined token object.  While ASN.1 usage within mechanism-defined
     * tokens is permitted, there is no requirement that the mechanism-
     * specific innerContextToken, innerMsgToken, and sealedUserData data
     * elements must employ ASN.1 BER/DER encoding conventions.

     * The following ASN.1 syntax is included for descriptive purposes only,
     * to illustrate structural relationships among token and tag objects.
     * For interoperability purposes, token and tag encoding shall be
     * performed using the concrete encoding procedures described earlier in
     * this subsection.
     * @param toExtract the array of byte to decode
     * @return the extracted token in bytes form.
     */
    public static byte[] decodeToken(byte[] toExtract) {
        int b = 0;

        // 1. 0x60 -- Tag for [APPLICATION 0] SEQUENCE
        if (toExtract[b++] != SEQUENCE) {
            throw new IllegalArgumentException("Invalid token");
        }

        // 2. Token length octets
        // Two cases : <128 or >128
        int tokenLegnth = 0;
        int lengthTmp = toExtract[b++];

        // case 2.b : with bit 8 of the first octet set to "1" and
        // the remaining bits of the first octet specifying the
        // number of additional octets.
        if ((lengthTmp & 128) == 128) { // first bit is set to 1
            int additionalOctets = lengthTmp & 0x7f; // others are the number of additional octets

            for (int i = 0; i < additionalOctets; i++) {
                tokenLegnth = (tokenLegnth << 8) + (toExtract[b++] & BYTES);
            }
        } else {
            // case 2.a (keep value as it is < 128)
            tokenLegnth = lengthTmp;
        }


        // 3. 0x06 -- Tag for OBJECT IDENTIFIER
        if (toExtract[b] != OBJECT_IDENTIFIER) { // no increment as we check after the Identifier
            throw new IllegalArgumentException("Invalid object identifier");
        }

        // 4. Object identifier length (MechOID)
        byte[] mechOidDerTemplate = GSSHelper.getMechOidDer();
        // validate it
        for (int i = 0; i < mechOidDerTemplate.length; i++) {
            if (toExtract[b++] != mechOidDerTemplate[i]) {
                throw new IllegalArgumentException("Not a valid MechoID");
            }
        }

        // 5. In many implementations, elements 3-5 may be stored and
        // referenced as a contiguous string constant
        int objLength = toExtract.length - b;
        byte[] objId = new byte[objLength];
        System.arraycopy(toExtract, b, objId, 0, objLength);

        return objId;

    }

    /**
     * Encode a given array of bytes by following RFC2743 3.1
     * @param contextData array to encode
     * @return the decoded array of bytes
     * @throws IOException if byte array stream cannot be built
     */
    public static byte[] encodeToken(byte[] contextData) throws IOException {

        byte[] mechOidDer = GSSHelper.getMechOidDer();
        int mechOidLength = mechOidDer.length;
        int contextDataLength = contextData.length;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // 1. 0x60 -- Tag for [APPLICATION 0] SEQUENCE
        bos.write(SEQUENCE);


        // 2. Token length octets (case 2a as we always use MechOid
        bos.write(mechOidLength + contextDataLength);

        // 3-4. Object identifier length (MechOID)
        bos.write(mechOidDer);

        // 5 token
        bos.write(contextData);

        return bos.toByteArray();
    }


    /**
     * @return GSSUP mechOid in DER format
     */
    public static byte[] getMechOidDer() {
        Oid oid = null;
        byte[] gssupDerEncoding = null;
        try {
            oid = new Oid(getMechOID());
            gssupDerEncoding = oid.getDER();
        } catch (GSSException gsse) {
            TraceCarol.error("Error while getting MechOID");
            return null;
        }
        return gssupDerEncoding;
    }

}
