/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.osgi.framework.Version;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildObject {
	// Schema element names
	public static final String ID = "id"; //$NON-NLS-1$
	public static final String NAME = "name"; //$NON-NLS-1$

	public String getId();

	public String getName();

	/**
	 * @return id of the object this class was based on during creation or id of the object itself.
	 */
	public String getBaseId();

	/** @since 8.0 */
	public Version getVersion();

	/** @since 8.0 */
	public void setVersion(Version version);

	public String getManagedBuildRevision();
}
