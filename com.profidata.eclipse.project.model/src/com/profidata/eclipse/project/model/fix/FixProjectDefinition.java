package com.profidata.eclipse.project.model.fix;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.JavaCore;

import com.profidata.eclipse.project.model.Activator;
import com.profidata.eclipse.project.model.ProjectWrapper;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurations.AccessRule;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurations.ProjectConfiguration;

public class FixProjectDefinition {
	private final ProjectWrapper projectWrapper;
	private final IProject project;
	private final ProjectConfiguration additionalConfiguration;

	public static void run(ProjectWrapper theProject) {
		run(theProject, false);
	}

	public static void run(ProjectWrapper theProject, boolean theAddJunitLibraryPath) {
		new FixProjectDefinition(theProject).execute(theAddJunitLibraryPath);
	}

	private FixProjectDefinition(ProjectWrapper theProjectWrapper) {
		projectWrapper = theProjectWrapper;
		project = theProjectWrapper.getProject();
		additionalConfiguration = AdditionalProjectConfigurationDefinitionProvider.getInstance().find(project.getName());
	}

	private void execute(boolean theAddJunitLibraryPath) {
		setDefaultCharset();
		enhanceClasspath();

		if (theAddJunitLibraryPath) {
			addJUnitLibraryPath();
		}

		projectWrapper.sortClasspath();
	}

	private void setDefaultCharset() {
		// Some of the Xentis projects have now set the encoding UTF-8 which is not the default.
		if (additionalConfiguration.encoding != null) {
			try {
				String aDefaultCharset = projectWrapper.getProject().getDefaultCharset();

				if (!additionalConfiguration.encoding.equals(aDefaultCharset)) {
					project.setDefaultCharset(additionalConfiguration.encoding, null);
				}
			}
			catch (CoreException theCause) {
				Activator.error("Access to default charset of project '" + project.getName() + "' failed:\n-> " + projectWrapper.getErrorMessage());
			}
		}
	}

	private void enhanceClasspath() {
		final IClasspathAttribute[] NO_EXTRA_ATTRIBUTES = {};

		additionalConfiguration.additionalClasspathEntries.forEach(theClasspathEntry -> {
			final IAccessRule[] someAccessRules = getAccessRules(theClasspathEntry.accessRules);

			switch (theClasspathEntry.type) {
				case Library:
					IPath aLibraryPath = project.getLocation().append(theClasspathEntry.path);
					projectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aLibraryPath, null, null, someAccessRules, NO_EXTRA_ATTRIBUTES, theClasspathEntry.exported));
					break;

				case Project:
					IPath aProjectPath = Path.fromPortableString("/" + theClasspathEntry.path);
					projectWrapper.addClasspathEntry(theProject -> JavaCore.newProjectEntry(aProjectPath, someAccessRules, false, NO_EXTRA_ATTRIBUTES, theClasspathEntry.exported));
					break;

				case Container:
					IPath aContainerPath = Path.fromPortableString(theClasspathEntry.path);
					projectWrapper.addClasspathEntry(theProject -> JavaCore.newContainerEntry(aContainerPath, someAccessRules, null, theClasspathEntry.exported));
					break;

				default:
					break;
			}
		});
	}

	private IAccessRule[] getAccessRules(Set<AccessRule> theAccessRules) {
		final IAccessRule[] NO_ACCESS_RULES = {};

		if (!theAccessRules.isEmpty()) {
			return theAccessRules.stream()
					.map(theAccessRule -> JavaCore.newAccessRule(new Path(theAccessRule.pattern), theAccessRule.kind))
					.toArray(IAccessRule[]::new);
		}

		return NO_ACCESS_RULES;
	}

	private void addJUnitLibraryPath() {
		final IAccessRule[] NO_ACCESS_RULES = {};

		String aJUnitLibraryPath = AdditionalProjectConfigurationDefinitionProvider.getInstance().findJUnitLibraryPath(project.getName());

		if (aJUnitLibraryPath != null) {
			IPath aJunitContainerPath = Path.fromPortableString(aJUnitLibraryPath);
			projectWrapper.addClasspathEntry(theProject -> JavaCore.newContainerEntry(aJunitContainerPath, NO_ACCESS_RULES, null, false));
			IPath aHamcrestContainerPath = Path.fromPortableString("com.profidata.eclipse.HAMCREST_CONTAINER");
			projectWrapper.addClasspathEntry(theProject -> JavaCore.newContainerEntry(aHamcrestContainerPath, NO_ACCESS_RULES, null, false));
		}
	}

}
