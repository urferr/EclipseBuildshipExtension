package com.profidata.eclipse.buildship.enhancements.aspects;

import java.util.LinkedHashMap;
import java.util.List;

import com.profidata.eclipse.project.model.ProjectConstants;
import com.profidata.eclipse.project.model.ProjectWrapper;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

public aspect GradleClasspathContainerUpdaterAspect {

	/**
	* Prevent gradle classpath container implementation to be added to plugin project (plugin nature defined)
	* @param theEclipseProject
	* @param theClasspathEntries
	* @param theMonitor
	*/
	@SuppressAjWarnings("adviceDidNotMatch")
	void around(IJavaProject theEclipseProject, List<IClasspathEntry> theClasspathEntries, IProgressMonitor theMonitor):
              execution(private static void org.eclipse.buildship.core.workspace.internal.GradleClasspathContainerUpdater.setClasspathContainer(IJavaProject, List<IClasspathEntry>, IProgressMonitor)) && 
              args(theEclipseProject, theClasspathEntries, theMonitor) {
		ProjectWrapper aProjectWrapper = ProjectWrapper.of(theEclipseProject.getProject());
		
		if (!aProjectWrapper.hasNature(ProjectConstants.PLUGIN_NATURE_ID)) {
			proceed(theEclipseProject, theClasspathEntries, theMonitor);
		}
	}

	/**
	* Prevent gradle classpath container to be added to the classpath  of a plugin project (plugin container defined)
	* 
	* @param theContainersToAdd
	*/
	@SuppressAjWarnings("adviceDidNotMatch")
	void around(LinkedHashMap<IPath, IClasspathEntry> theContainersToAdd):
              execution(private void org.eclipse.buildship.core.workspace.internal.ClasspathContainerUpdater.ensureGradleContainerIsPresent(LinkedHashMap<IPath, IClasspathEntry>)) && 
              args(theContainersToAdd) {
		if (!theContainersToAdd.containsKey(ProjectConstants.PLUGIN_CLASSPATH)) {
			proceed(theContainersToAdd);
		}
	}
}
