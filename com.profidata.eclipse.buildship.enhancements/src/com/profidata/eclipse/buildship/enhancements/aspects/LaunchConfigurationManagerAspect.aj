package com.profidata.eclipse.buildship.enhancements.aspects;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.eclipse.core.resources.IProject;

import com.profidata.eclipse.project.model.ProjectConstants;
import com.profidata.eclipse.project.model.ProjectWrapper;

public aspect LaunchConfigurationManagerAspect {
	/**
	 * Prevent gradle nature to be added to plugin project (plugin nature defined)
	 * 
	 * @param theNatureIds
	 */
	@SuppressAjWarnings("adviceDidNotMatch")
	void around(IProject theProject):
          execution(public void org.eclipse.buildship.core.launch.internal.DefaultExternalLaunchConfigurationManager.updateClasspathProviders(IProject)) &&
          args(theProject) {
		ProjectWrapper aProjectWrapper = ProjectWrapper.of(theProject);

		if (!aProjectWrapper.hasNature(ProjectConstants.PLUGIN_NATURE_ID))
			proceed(theProject);
	}
}
