/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.XVALUE;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of an expression.
 */
public class EvalFixed implements ICPPEvaluation {
	public static final ICPPEvaluation INCOMPLETE =
			new EvalFixed(ProblemType.UNKNOWN_FOR_EXPRESSION, PRVALUE, Value.UNKNOWN);

	private final IType fType;
	private final IValue fValue;
	private final ValueCategory fValueCategory;
	private boolean fIsTypeDependent;
	private boolean fCheckedIsTypeDependent;
	private boolean fIsValueDependent;
	private boolean fCheckedIsValueDependent;

	public EvalFixed(IType type, ValueCategory cat, IValue value) {
		fType= type;
		fValueCategory= cat;
		fValue= value;
	}

	public IType getType() {
		return fType;
	}

	public IValue getValue() {
		return fValue;
	}

	public ValueCategory getValueCategory() {
		return fValueCategory;
	}

	@Override
	public boolean isInitializerList() {
		return false;
	}

	@Override
	public boolean isFunctionSet() {
		return false;
	}

	@Override
	public boolean isTypeDependent() {
		if (!fCheckedIsTypeDependent) {
			fCheckedIsTypeDependent= true;
			fIsTypeDependent= CPPTemplates.isDependentType(fType);
		}
		return fIsTypeDependent;
	}

	@Override
	public boolean isValueDependent() {
		if (!fCheckedIsValueDependent) {
			fCheckedIsValueDependent= true;
			fIsValueDependent= Value.isDependentValue(fValue);
		}
		return fIsValueDependent;
	}

	@Override
	public IType getTypeOrFunctionSet(IASTNode point) {
		return fType;
	}

	@Override
	public IValue getValue(IASTNode point) {
		return fValue;
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return fValueCategory;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		includeValue= includeValue && fValue != Value.UNKNOWN;
		int firstByte = ITypeMarshalBuffer.EVAL_FIXED;
		if (includeValue)
			firstByte |= ITypeMarshalBuffer.FLAG1;
		switch (fValueCategory) {
		case LVALUE:
			firstByte |= ITypeMarshalBuffer.FLAG2;
			break;
		case PRVALUE:
			firstByte |= ITypeMarshalBuffer.FLAG3;
			break;
		default:
			break;
		}

		buffer.putByte((byte) firstByte);
		buffer.marshalType(fType);
		if (includeValue) {
			buffer.marshalValue(fValue);
		}
	}

	public static ISerializableEvaluation unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		final boolean readValue= (firstByte & ITypeMarshalBuffer.FLAG1) != 0;
		IValue value;
		ValueCategory cat;
		switch (firstByte & (ITypeMarshalBuffer.FLAG2 | ITypeMarshalBuffer.FLAG3)) {
		case ITypeMarshalBuffer.FLAG2:
			cat= LVALUE;
			break;
		case ITypeMarshalBuffer.FLAG3:
			cat= PRVALUE;
			break;
		default:
			cat= XVALUE;
			break;
		}

		IType type= buffer.unmarshalType();
		value= readValue ? buffer.unmarshalValue() : Value.UNKNOWN;
		return new EvalFixed(type, cat, value);
	}
}