/**********************************************************************
 * Created on Apr 7, 2003
 *
 * Copyright (c) 2002,2003 IBM/Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Ltd. - Rational Software - Initial API and implementation
************************************************************************/
package org.eclipse.cdt.internal.core.dom;

/**
 * @author jcamelon
 *
 */
public class Inclusion extends PreprocessorStatement {

	public Inclusion( String name, int nameOffset, int startingOffset, int totalLength )
	{
		super( name, nameOffset, startingOffset, totalLength );
	}
}
