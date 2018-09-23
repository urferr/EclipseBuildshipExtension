package com.profidata.eclipse.buildship.enhancements.aspects;

import java.util.Arrays;

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.profidata.eclipse.buildship.enhancements.Activator;
import com.profidata.eclipse.project.model.ProjectConstants;
import com.profidata.eclipse.project.model.ProjectWrapper;
import com.profidata.eclipse.project.model.fix.FixProjectDefinition;
import com.profidata.eclipse.project.model.fix.IgnoreProjectFolder;
import com.profidata.eclipse.project.model.fix.TestFragmentCreator;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.JavaCore;

public aspect SynchronizeGradleBuildOperationAspect {

	/**
	 * Prevent gradle nature to added to a plugin project (plugin nature)
	 * @param theProject
	 * @param theWorkspaceProject
	 * @param theRefreshNeeded
	 * @param theProgress
	 */
	@SuppressAjWarnings("adviceDidNotMatch")
	void around(EclipseProject theProject, IProject theWorkspaceProject, boolean theRefreshNeeded, SubMonitor theProgress):
               execution(void org.eclipse.buildship.core.internal.workspace.SynchronizeGradleBuildOperation.synchronizeOpenWorkspaceProject(EclipseProject, IProject, boolean, SubMonitor)) && 
               args(theProject,  theWorkspaceProject,  theRefreshNeeded, theProgress) {
		// Ignore the gradle build folder for all projects because Eclipse IDE is not interested in these folders and their content
		IgnoreProjectFolder.run(theWorkspaceProject, "target");
		
		ProjectWrapper aProjectWrapper = ProjectWrapper.of(theWorkspaceProject);
		
		if (aProjectWrapper.hasNature(JavaCore.NATURE_ID)) {
			aProjectWrapper.asJavaProject();
			FixProjectDefinition.run(aProjectWrapper);
		}

		if (!aProjectWrapper.hasNature(ProjectConstants.PLUGIN_NATURE_ID)) {
			proceed(theProject, theWorkspaceProject, theRefreshNeeded, theProgress);
		}

		else {
			// if for any reason the gradle classpath container has already been added it will no be removed again.
			aProjectWrapper.removeClasspathEntry(ProjectConstants.GRADLE_CLASSPATH);
			
			// check if there are folders containing test classes generate corresponding fragment for it.
			if (!theProject.getName().endsWith("-integration")) {
				TestFragmentCreator.run(theWorkspaceProject, Arrays.asList("test", "integration", "manual"));
			}
		}
		
		if (aProjectWrapper.hasProtocol()) {
			Activator.info(aProjectWrapper.getProtocolMessage());
		}
		if (aProjectWrapper.hasError()) {
			Activator.info(aProjectWrapper.getErrorMessage());
		}
	}
}
