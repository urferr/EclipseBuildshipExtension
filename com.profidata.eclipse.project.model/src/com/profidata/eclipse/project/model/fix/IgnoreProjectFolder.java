package com.profidata.eclipse.project.model.fix;

import java.text.MessageFormat;

import com.profidata.eclipse.project.model.Activator;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.runtime.CoreException;

public class IgnoreProjectFolder {
	private final IProject project;
	private final String projectFolder;

	public static void run(IProject theProject, String theProjectFolder) {
		new IgnoreProjectFolder(theProject, theProjectFolder).execute();
	}

	private IgnoreProjectFolder(IProject theProject, String theProjectFolder) {
		this.project = theProject;
		this.projectFolder = theProjectFolder;
	}

	private void execute() {
		if (hasFolder(this.project, this.projectFolder)) {
			Activator.info(MessageFormat.format("ignore ''{0}'' in project ''{1}''", this.projectFolder, this.project.getName()));
			try {
				this.project.createFilter(
						IResourceFilterDescription.EXCLUDE_ALL | IResourceFilterDescription.FOLDERS | IResourceFilterDescription.INHERITABLE,
						new FileInfoMatcherDescription("org.eclipse.ui.ide.multiFilter", "1.0-name-matches-false-false-" + this.projectFolder),
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
