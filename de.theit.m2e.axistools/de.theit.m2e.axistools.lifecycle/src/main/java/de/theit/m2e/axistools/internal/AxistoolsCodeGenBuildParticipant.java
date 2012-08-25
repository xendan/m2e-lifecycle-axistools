/*
 * @(#)AxistoolsCodeGenBuildParticipant.java
 * Copyright (C)2011 Thorsten Heit
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.theit.m2e.axistools.internal;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.util.Scanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Project configurator class for the Axistools code generator plugin.
 * 
 * @author <a href="mailto:theit@gmx.de">Thorsten Heit (theit@gmx.de)</a>
 * @since 28.06.2011 17:17:19
 * @version $Id$
 */
public class AxistoolsCodeGenBuildParticipant extends
		MojoExecutionBuildParticipant {
	private static final long URL_RELOAD_TIMEOUT = 24 * 60 * 60 * 1000;
	
	private long lastWsdlReloadTime = System.currentTimeMillis();

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param execution
	 *            Mojo execution.
	 */
	public AxistoolsCodeGenBuildParticipant(MojoExecution execution) {
		super(execution, true);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant#build(int,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Set<IProject> build(int kind, IProgressMonitor monitor)
			throws Exception {
		IMaven maven = MavenPlugin.getMaven();
		BuildContext buildContext = getBuildContext();

		if (skipRebuild(kind, maven, buildContext)) {
			return null;
		}
		// execute mojo
		Set<IProject> result = super.build(kind, monitor);

		// tell m2e builder to refresh generated files
		File outputDir = maven.getMojoParameterValue(getSession(),
				getMojoExecution(), "outputDirectory", File.class);
		File generated = AxistoolsResolveOutputUtil.getOutputFolder(outputDir,
				getMojoExecution());
		if (null != generated) {
			buildContext.refresh(generated);
		}

		return result;
	}

	private boolean skipRebuild(int kind, IMaven maven,
			BuildContext buildContext) throws CoreException {
		String wsdlFileName = maven.getMojoParameterValue(getSession(),
				getMojoExecution(), "wsdlFile", String.class);
		
		File wsdlFile = new File(wsdlFileName);
		
		// is url
		if (!wsdlFile.exists() && wsdlFileName.startsWith("http")) {
			long now = System.currentTimeMillis();
			long delta = now - lastWsdlReloadTime;
			if (delta > URL_RELOAD_TIMEOUT) {
				lastWsdlReloadTime = now;
				return false;
			}
			return true;
		}
		return !filesModified(buildContext, wsdlFile);
	}

	private boolean filesModified(BuildContext buildContext, File source) {
		if (buildContext == null || source == null || !source.exists()) {
			return false;
		}

		Scanner ds = buildContext.newScanner(source);
		ds.scan();
		String[] included = ds.getIncludedFiles();
		return included != null && included.length > 0 ;
	}
}
