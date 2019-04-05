package com.profidata.eclipse.project.model.fix;

import java.util.Set;

import com.profidata.eclipse.project.model.Activator;
import com.profidata.eclipse.project.model.ProjectWrapper;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurations.AccessRule;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurations.ProjectConfiguration;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.JavaCore;

public class FixProjectDefinition {
	private final ProjectWrapper projectWrapper;
	private final IProject project;
	private final ProjectConfiguration additionalConfiguration;

	public static void run(ProjectWrapper theProject) {
		new FixProjectDefinition(theProject).execute();
	}

	private FixProjectDefinition(ProjectWrapper theProjectWrapper) {
		this.projectWrapper = theProjectWrapper;
		this.project = theProjectWrapper.getProject();
		this.additionalConfiguration = AdditionalProjectConfigurationDefinitionProvider.getInstance().find(this.project.getName());
	}

	private void execute() {
		setDefaultCharset();
		enhanceClasspath();
	}

	private void setDefaultCharset() {
		// Some of the Xentis projects have now set the encoding UTF-8 which is not the default.
		if (this.additionalConfiguration.encoding != null) {
			try {
				String aDefaultCharset = this.projectWrapper.getProject().getDefaultCharset();

				if (!this.additionalConfiguration.encoding.equals(aDefaultCharset)) {
					this.project.setDefaultCharset(this.additionalConfiguration.encoding, null);
				}
			}
			catch (CoreException theCause) {
				Activator.error("Access to default charset of project '" + this.project.getName() + "' failed:\n-> " + this.projectWrapper.getErrorMessage());
			}
		}
	}

	private void enhanceClasspath() {
		final IClasspathAttribute[] NO_EXTRA_ATTRIBUTES = {};

		this.additionalConfiguration.additionalClasspathEntries.forEach(theClasspathEntry -> {
			final IAccessRule[] someAccessRules = getAccessRules(theClasspathEntry.accessRules);

			switch (theClasspathEntry.type) {
				case Library:
					IPath aLibraryPath = this.project.getLocation().append(theClasspathEntry.path);
					this.projectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aLibraryPath, null, null,someAccessRules, NO_EXTRA_ATTRIBUTES, theClasspathEntry.exported));
					break;

				case Project:
					IPath aProjectPath = Path.fromPortableString("/" + theClasspathEntry.path);
					this.projectWrapper.addClasspathEntry(theProject -> JavaCore.newProjectEntry(aProjectPath, someAccessRules, false, NO_EXTRA_ATTRIBUTES, theClasspathEntry.exported));
					break;

				case Container:
					IPath aContainerPath = Path.fromPortableString(theClasspathEntry.path);
					this.projectWrapper.addClasspathEntry(theProject -> JavaCore.newContainerEntry(aContainerPath, someAccessRules, null, theClasspathEntry.exported));
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
}
