package com.profidata.eclipse.project.model.fix;

import java.text.MessageFormat;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.runtime.CoreException;

import com.profidata.eclipse.project.model.Activator;

public class IgnoreProjectFolder {
	private final IProject project;
	private final String projectFolder;

	public static void run(IProject theProject, String theProjectFolder) {
		new IgnoreProjectFolder(theProject, theProjectFolder).execute();
	}

	private IgnoreProjectFolder(IProject theProject, String theProjectFolder) {
		project = theProject;
		projectFolder = theProjectFolder;
	}

	private void execute() {
		if (hasFolder(project, projectFolder)) {
			Activator.info(MessageFormat.format("ignore ''{0}'' in project ''{1}''", projectFolder, project.getName()));
			try {
				project.createFilter(
						IResourceFilterDescription.EXCLUDE_ALL | IResourceFilterDescription.FOLDERS | IResourceFilterDescription.INHERITABLE,
						new FileInfoMatcherDescription("org.eclipse.ui.ide.multiFilter", "1.0-name-matches-false-false-" + projectFolder),
						0,
						null);
			}
			catch (CoreException theCause) {
				Activator.error(" -> failed: " + theCause.getMessage());
			}
		}
	}

	private boolean hasFolder(IProject theProject, String theFolder) {
		return theProject.getFolder(theFolder).exists();
	}
}
