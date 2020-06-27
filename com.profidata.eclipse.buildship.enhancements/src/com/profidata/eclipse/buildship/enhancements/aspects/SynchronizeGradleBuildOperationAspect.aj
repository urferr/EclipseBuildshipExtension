package com.profidata.eclipse.buildship.enhancements.aspects;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;

import com.google.common.collect.ImmutableList;
import com.profidata.eclipse.buildship.enhancements.Activator;
import com.profidata.eclipse.project.model.ProjectConstants;
import com.profidata.eclipse.project.model.ProjectWrapper;
import com.profidata.eclipse.project.model.fix.FixProjectDefinition;
import com.profidata.eclipse.project.model.fix.IgnoreProjectFolder;
import com.profidata.eclipse.project.model.fix.TestFragmentCreator;

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

			// The java plugin project was new and therefore has not yet been fixed
			if (aProjectWrapper.hasNature(JavaCore.NATURE_ID) && aProjectWrapper.hasNature(ProjectConstants.PLUGIN_NATURE_ID)) {
				aProjectWrapper.asJavaProject();
				FixProjectDefinition.run(aProjectWrapper);
			}
			else {
				FixProjectDefinition.run(aProjectWrapper, true);
			}
		}
		else if (aProjectWrapper.hasNature(JavaCore.NATURE_ID)) {
			try {
				SourceFolderUpdater_update(aProjectWrapper.getJavaProject(), ImmutableList.copyOf(theProject.getSourceDirectories()), theProgress.newChild(1));
			}
			catch (CoreException theCause) {
				Activator.error("Failed to update source folders of  plugin project " + aProjectWrapper.getProject().getName() + ": " + theCause.getLocalizedMessage());
			}
		}

		// When a new project is imported, the plugin nature has not been set initially and therefore the proceed method has been executed
		if (aProjectWrapper.hasNature(ProjectConstants.PLUGIN_NATURE_ID)) {
			// if for any reason the gradle classpath container has already been added it will now be removed again.
			aProjectWrapper.removeClasspathEntry(ProjectConstants.GRADLE_CLASSPATH);

			// check if there are folders containing test classes generate corresponding fragment for it.
			if (!theProject.getName().endsWith("-integration")) {
					TestFragmentCreator.run(theWorkspaceProject, Arrays.asList("test", "integration", "manual"), onlyTestClasspathEntries(theWorkspaceProject, ImmutableList.copyOf(theProject.getSourceDirectories())));
			}
		}

		if (aProjectWrapper.hasProtocol()) {
			Activator.info(aProjectWrapper.getProtocolMessage());
		}
		if (aProjectWrapper.hasError()) {
			Activator.error(aProjectWrapper.getErrorMessage());
		}
	}

	private void SourceFolderUpdater_update(IJavaProject theProject, List<EclipseSourceDirectory> theSourceDirectories, IProgressMonitor theMonitor) throws JavaModelException {
		try {
			Class<?> aSourceFolderUpdateClass = getClass().getClassLoader().loadClass("org.eclipse.buildship.core.internal.workspace.SourceFolderUpdater");

			Method aUpdateMethod = aSourceFolderUpdateClass.getMethod("update", IJavaProject.class, List.class, IProgressMonitor.class);

			aUpdateMethod.setAccessible(true);
			aUpdateMethod.invoke(null, theProject, ImmutableList.copyOf(withoutTestSourceDirectories(theSourceDirectories)), theMonitor);
		}
		catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException theCause) {
			Activator.error(theCause.getLocalizedMessage());
		}
	}

	private List<EclipseSourceDirectory> withoutTestSourceDirectories(List<EclipseSourceDirectory> theSourceDirectories) {
		return theSourceDirectories.stream()
				.filter(theSourceDirectory -> isSourceDirectory(theSourceDirectory) )
				.collect(Collectors.toList());
	}

	private List<IClasspathEntry> onlyTestClasspathEntries(IProject theWorkspaceProject, List<EclipseSourceDirectory> theSourceDirectories) {
		return theSourceDirectories.stream()
				.filter(theSourceDirectory -> !isSourceDirectory(theSourceDirectory))
				.map(theTestSourceDirectory -> theWorkspaceProject.getFullPath().append(theTestSourceDirectory.getPath()))
				.map(thePath -> JavaCore.newSourceEntry(thePath))
				.collect(Collectors.toList());
	}
	
	private boolean isSourceDirectory(EclipseSourceDirectory theSourceDirectory) {
		return theSourceDirectory.getPath().equals("src") || theSourceDirectory.getPath().equals("resources") || theSourceDirectory.getPath().startsWith("src/main") || theSourceDirectory.getPath().startsWith("src/generated");
	}

}
