/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
     IBM Corporation - initial API and implementation
**********************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of each opcode mnemonic according to the JVM specifications.
 *  
 * @since 2.0
 */
public class OpcodeStringValues implements IOpcodeMnemonics {

	public static final String[] BYTECODE_NAMES = new String[256];
	static {
		BYTECODE_NAMES[NOP] = "nop";
		BYTECODE_NAMES[ACONST_NULL] = "aconst_null";
		BYTECODE_NAMES[ICONST_M1] = "iconst_m1";
		BYTECODE_NAMES[ICONST_0] = "iconst_0";
		BYTECODE_NAMES[ICONST_1] = "iconst_1";
		BYTECODE_NAMES[ICONST_2] = "iconst_2";
		BYTECODE_NAMES[ICONST_3] = "iconst_3";
		BYTECODE_NAMES[ICONST_4] = "iconst_4";
		BYTECODE_NAMES[ICONST_5] = "iconst_5";
		BYTECODE_NAMES[LCONST_0] = "lconst_0";
		BYTECODE_NAMES[LCONST_1] = "lconst_1";
		BYTECODE_NAMES[FCONST_0] = "fconst_0";
		BYTECODE_NAMES[FCONST_1] = "fconst_1";
		BYTECODE_NAMES[FCONST_2] = "fconst_2";
		BYTECODE_NAMES[DCONST_0] = "dconst_0";
		BYTECODE_NAMES[DCONST_1] = "dconst_1";
		BYTECODE_NAMES[BIPUSH] = "bipush";
		BYTECODE_NAMES[SIPUSH] = "sipush";
		BYTECODE_NAMES[LDC] = "ldc";
		BYTECODE_NAMES[LDC_W] = "ldc_w";
		BYTECODE_NAMES[LDC2_W] = "ldc2_w";
		BYTECODE_NAMES[ILOAD] = "iload";
		BYTECODE_NAMES[LLOAD] = "lload";
		BYTECODE_NAMES[FLOAD] = "fload";
		BYTECODE_NAMES[DLOAD] = "dload";
		BYTECODE_NAMES[ALOAD] = "aload";
		BYTECODE_NAMES[ILOAD_0] = "iload_0";
		BYTECODE_NAMES[ILOAD_1] = "iload_1";
		BYTECODE_NAMES[ILOAD_2] = "iload_2";
		BYTECODE_NAMES[ILOAD_3] = "iload_3";
		BYTECODE_NAMES[LLOAD_0] = "lload_0";
		BYTECODE_NAMES[LLOAD_1] = "lload_1";
		BYTECODE_NAMES[LLOAD_2] = "lload_2";
		BYTECODE_NAMES[LLOAD_3] = "lload_3";
		BYTECODE_NAMES[FLOAD_0] = "fload_0";
		BYTECODE_NAMES[FLOAD_1] = "fload_1";
		BYTECODE_NAMES[FLOAD_2] = "fload_2";
		BYTECODE_NAMES[FLOAD_3] = "fload_3";
		BYTECODE_NAMES[DLOAD_0] = "dload_0";
		BYTECODE_NAMES[DLOAD_1] = "dload_1";
		BYTECODE_NAMES[DLOAD_2] = "dload_2";
		BYTECODE_NAMES[DLOAD_3] = "dload_3";
		BYTECODE_NAMES[ALOAD_0] = "aload_0";
		BYTECODE_NAMES[ALOAD_1] = "aload_1";
		BYTECODE_NAMES[ALOAD_2] = "aload_2";
		BYTECODE_NAMES[ALOAD_3] = "aload_3";
		BYTECODE_NAMES[IALOAD] = "iaload";
		BYTECODE_NAMES[LALOAD] = "laload";
		BYTECODE_NAMES[FALOAD] = "faload";
		BYTECODE_NAMES[DALOAD] = "daload";
		BYTECODE_NAMES[AALOAD] = "aaload";
		BYTECODE_NAMES[BALOAD] = "baload";
		BYTECODE_NAMES[CALOAD] = "caload";
		BYTECODE_NAMES[SALOAD] = "saload";
		BYTECODE_NAMES[ISTORE] = "istore";
		BYTECODE_NAMES[LSTORE] = "lstore";
		BYTECODE_NAMES[FSTORE] = "fstore";
		BYTECODE_NAMES[DSTORE] = "dstore";
		BYTECODE_NAMES[ASTORE] = "astore";
		BYTECODE_NAMES[ISTORE_0] = "istore_0";
		BYTECODE_NAMES[ISTORE_1] = "istore_1";
		BYTECODE_NAMES[ISTORE_2] = "istore_2";
		BYTECODE_NAMES[ISTORE_3] = "istore_3";
		BYTECODE_NAMES[LSTORE_0] = "lstore_0";
		BYTECODE_NAMES[LSTORE_1] = "lstore_1";
		BYTECODE_NAMES[LSTORE_2] = "lstore_2";
		BYTECODE_NAMES[LSTORE_3] = "lstore_3";
		BYTECODE_NAMES[FSTORE_0] = "fstore_0";
		BYTECODE_NAMES[FSTORE_1] = "fstore_1";
		BYTECODE_NAMES[FSTORE_2] = "fstore_2";
		BYTECODE_NAMES[FSTORE_3] = "fstore_3";
		BYTECODE_NAMES[DSTORE_0] = "dstore_0";
		BYTECODE_NAMES[DSTORE_1] = "dstore_1";
		BYTECODE_NAMES[DSTORE_2] = "dstore_2";
		BYTECODE_NAMES[DSTORE_3] = "dstore_3";
		BYTECODE_NAMES[ASTORE_0] = "astore_0";
		BYTECODE_NAMES[ASTORE_1] = "astore_1";
		BYTECODE_NAMES[ASTORE_2] = "astore_2";
		BYTECODE_NAMES[ASTORE_3] = "astore_3";
		BYTECODE_NAMES[IASTORE] = "iastore";
		BYTECODE_NAMES[LASTORE] = "lastore";
		BYTECODE_NAMES[FASTORE] = "fastore";
		BYTECODE_NAMES[DASTORE] = "dastore";
		BYTECODE_NAMES[AASTORE] = "aastore";
		BYTECODE_NAMES[BASTORE] = "bastore";
		BYTECODE_NAMES[CASTORE] = "castore";
		BYTECODE_NAMES[SASTORE] = "sastore";
		BYTECODE_NAMES[POP] = "pop";
		BYTECODE_NAMES[POP2] = "pop2";
		BYTECODE_NAMES[DUP] = "dup";
		BYTECODE_NAMES[DUP_X1] = "dup_x1";
		BYTECODE_NAMES[DUP_X2] = "dup_x2";
		BYTECODE_NAMES[DUP2] = "dup2";
		BYTECODE_NAMES[DUP2_X1] = "dup2_x1";
		BYTECODE_NAMES[DUP2_X2] = "dup2_x2";
		BYTECODE_NAMES[SWAP] = "swap";
		BYTECODE_NAMES[IADD] = "iadd";
		BYTECODE_NAMES[LADD] = "ladd";
		BYTECODE_NAMES[FADD] = "fadd";
		BYTECODE_NAMES[DADD] = "dadd";
		BYTECODE_NAMES[ISUB] = "isub";
		BYTECODE_NAMES[LSUB] = "lsub";
		BYTECODE_NAMES[FSUB] = "fsub";
		BYTECODE_NAMES[DSUB] = "dsub";
		BYTECODE_NAMES[IMUL] = "imul";
		BYTECODE_NAMES[LMUL] = "lmul";
		BYTECODE_NAMES[FMUL] = "fmul";
		BYTECODE_NAMES[DMUL] = "dmul";
		BYTECODE_NAMES[IDIV] = "idiv";
		BYTECODE_NAMES[LDIV] = "ldiv";
		BYTECODE_NAMES[FDIV] = "fdiv";
		BYTECODE_NAMES[DDIV] = "ddiv";
		BYTECODE_NAMES[IREM] = "irem";
		BYTECODE_NAMES[LREM] = "lrem";
		BYTECODE_NAMES[FREM] = "frem";
		BYTECODE_NAMES[DREM] = "drem";
		BYTECODE_NAMES[INEG] = "ineg";
		BYTECODE_NAMES[LNEG] = "lneg";
		BYTECODE_NAMES[FNEG] = "fneg";
		BYTECODE_NAMES[DNEG] = "dneg";
		BYTECODE_NAMES[ISHL] = "ishl";
		BYTECODE_NAMES[LSHL] = "lshl";
		BYTECODE_NAMES[ISHR] = "ishr";
		BYTECODE_NAMES[LSHR] = "lshr";
		BYTECODE_NAMES[IUSHR] = "iushr";
		BYTECODE_NAMES[LUSHR] = "lushr";
		BYTECODE_NAMES[IAND] = "iand";
		BYTECODE_NAMES[LAND] = "land";
		BYTECODE_NAMES[IOR] = "ior";
		BYTECODE_NAMES[LOR] = "lor";
		BYTECODE_NAMES[IXOR] = "ixor";
		BYTECODE_NAMES[LXOR] = "lxor";
		BYTECODE_NAMES[IINC] = "iinc";
		BYTECODE_NAMES[I2L] = "i2l";
		BYTECODE_NAMES[I2F] = "i2f";
		BYTECODE_NAMES[I2D] = "i2d";
		BYTECODE_NAMES[L2I] = "l2i";
		BYTECODE_NAMES[L2F] = "l2f";
		BYTECODE_NAMES[L2D] = "l2d";
		BYTECODE_NAMES[F2I] = "f2i";
		BYTECODE_NAMES[F2L] = "f2l";
		BYTECODE_NAMES[F2D] = "f2d";
		BYTECODE_NAMES[D2I] = "d2i";
		BYTECODE_NAMES[D2L] = "d2l";
		BYTECODE_NAMES[D2F] = "d2f";
		BYTECODE_NAMES[I2B] = "i2b";
		BYTECODE_NAMES[I2C] = "i2c";
		BYTECODE_NAMES[I2S] = "i2s";
		BYTECODE_NAMES[LCMP] = "lcmp";
		BYTECODE_NAMES[FCMPL] = "fcmpl";
		BYTECODE_NAMES[FCMPG] = "fcmpg";
		BYTECODE_NAMES[DCMPL] = "dcmpl";
		BYTECODE_NAMES[DCMPG] = "dcmpg";
		BYTECODE_NAMES[IFEQ] = "ifeq";
		BYTECODE_NAMES[IFNE] = "ifne";
		BYTECODE_NAMES[IFLT] = "iflt";
		BYTECODE_NAMES[IFGE] = "ifge";
		BYTECODE_NAMES[IFGT] = "ifgt";
		BYTECODE_NAMES[IFLE] = "ifle";
		BYTECODE_NAMES[IF_ICMPEQ] = "if_icmpeq";
		BYTECODE_NAMES[IF_ICMPNE] = "if_icmpne";
		BYTECODE_NAMES[IF_ICMPLT] = "if_icmplt";
		BYTECODE_NAMES[IF_ICMPGE] = "if_icmpge";
		BYTECODE_NAMES[IF_ICMPGT] = "if_icmpgt";
		BYTECODE_NAMES[IF_ICMPLE] = "if_icmple";
		BYTECODE_NAMES[IF_ACMPEQ] = "if_acmpeq";
		BYTECODE_NAMES[IF_ACMPNE] = "if_acmpne";
		BYTECODE_NAMES[GOTO] = "goto";
		BYTECODE_NAMES[JSR] = "jsr";
		BYTECODE_NAMES[RET] = "ret";
		BYTECODE_NAMES[TABLESWITCH] = "tableswitch";
		BYTECODE_NAMES[LOOKUPSWITCH] = "lookupswitch";
		BYTECODE_NAMES[IRETURN] = "ireturn";
		BYTECODE_NAMES[LRETURN] = "lreturn";
		BYTECODE_NAMES[FRETURN] = "freturn";
		BYTECODE_NAMES[DRETURN] = "dreturn";
		BYTECODE_NAMES[ARETURN] = "areturn";
		BYTECODE_NAMES[RETURN] = "return";
		BYTECODE_NAMES[GETSTATIC] = "getstatic";
		BYTECODE_NAMES[PUTSTATIC] = "putstatic";
		BYTECODE_NAMES[GETFIELD] = "getfield";
		BYTECODE_NAMES[PUTFIELD] = "putfield";
		BYTECODE_NAMES[INVOKEVIRTUAL] = "invokevirtual";
		BYTECODE_NAMES[INVOKESPECIAL] = "invokespecial";
		BYTECODE_NAMES[INVOKESTATIC] = "invokestatic";
		BYTECODE_NAMES[INVOKEINTERFACE] = "invokeinterface";
		BYTECODE_NAMES[NEW] = "new";
		BYTECODE_NAMES[NEWARRAY] = "newarray";
		BYTECODE_NAMES[ANEWARRAY] = "anewarray";
		BYTECODE_NAMES[ARRAYLENGTH] = "arraylength";
		BYTECODE_NAMES[ATHROW] = "athrow";
		BYTECODE_NAMES[CHECKCAST] = "checkcast";
		BYTECODE_NAMES[INSTANCEOF] = "instanceof";
		BYTECODE_NAMES[MONITORENTER] = "monitorenter";
		BYTECODE_NAMES[MONITOREXIT] = "monitorexit";
		BYTECODE_NAMES[WIDE] = "wide";
		BYTECODE_NAMES[MULTIANEWARRAY] = "multianewarray";
		BYTECODE_NAMES[IFNULL] = "ifnull";
		BYTECODE_NAMES[IFNONNULL] = "ifnonnull";
		BYTECODE_NAMES[GOTO_W] = "goto_w";
		BYTECODE_NAMES[JSR_W] = "jsr_w";
		BYTECODE_NAMES[BREAKPOINT] = "breakpoint";
		BYTECODE_NAMES[IMPDEP1] = "impdep1";
		BYTECODE_NAMES[IMPDEP2] = "impdep2";
	}
}
