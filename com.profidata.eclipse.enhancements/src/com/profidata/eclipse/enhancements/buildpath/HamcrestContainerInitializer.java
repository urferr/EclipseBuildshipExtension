package com.profidata.eclipse.enhancements.buildpath;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.junit.JUnitPreferencesConstants;
import org.eclipse.jdt.internal.junit.buildpath.BuildPathSupport.JUnitPluginDescription;
import org.eclipse.osgi.service.resolver.VersionRange;

public class HamcrestContainerInitializer extends ClasspathContainerInitializer {

	private static final JUnitPluginDescription HAMCREST_CORE_PLUGIN = new JUnitPluginDescription(
			"org.hamcrest.core",
			new VersionRange("[1.1.0,2.0.0)"),
			null,
			"org.hamcrest.core_1.*.jar",
			"org.hamcrest.core.source",
			"source-bundle/",
			JUnitPreferencesConstants.HAMCREST_CORE_JAVADOC);
	private static final JUnitPluginDescription HAMCREST_GENERATOR_PLUGIN = new JUnitPluginDescription(
			"org.hamcrest.generator",
			new VersionRange("[1.1.0,2.0.0)"),
			null,
			"org.hamcrest.generator_1.*.jar",
			"org.hamcrest.generator.source",
			"source-bundle/",
			"");
	private static final JUnitPluginDescription HAMCREST_LIBRARY_PLUGIN = new JUnitPluginDescription(
			"org.hamcrest.library",
			new VersionRange("[1.1.0,2.0.0)"),
			null,
			"org.hamcrest.library_1.*.jar",
			"org.hamcrest.library.source",
			"source-bundle/",
			"");
	private static final JUnitPluginDescription HAMCREST_TEXT_PLUGIN = new JUnitPluginDescription(
			"org.hamcrest.text",
			new VersionRange("[1.1.0,2.0.0)"),
			null,
			"org.hamcrest.text_1.*.jar",
			"org.hamcrest.text.source",
			"source-bundle/",
			"");

	private static class HamcrestContainer implements IClasspathContainer {

		private final IClasspathEntry[] entries;
		private final IPath path;

		public HamcrestContainer(IPath thePath, IClasspathEntry[] theEntries) {
			path = thePath;
			entries = theEntries;
		}

		@Override
		public IClasspathEntry[] getClasspathEntries() {
			return entries;
		}

		@Override
		public String getDescription() {
			return "Hamcrest";
		}

		@Override
		public int getKind() {
			return IClasspathContainer.K_APPLICATION;
		}

		@Override
		public IPath getPath() {
			return path;
		}

	}

	@Override
	public void initialize(IPath theContainerPath, IJavaProject theProject) throws CoreException {
		HamcrestContainer container = getNewContainer(theContainerPath);
		JavaCore.setClasspathContainer(
				theContainerPath,
				new IJavaProject[] {
						theProject },
				new IClasspathContainer[] {
						container },
				null);
	}

	private HamcrestContainer getNewContainer(IPath theContainerPath) {
		List<IClasspathEntry> allEntries = new ArrayList<>();

		allEntries.add(HAMCREST_CORE_PLUGIN.getLibraryEntry());
		allEntries.add(HAMCREST_GENERATOR_PLUGIN.getLibraryEntry());
		allEntries.add(HAMCREST_LIBRARY_PLUGIN.getLibraryEntry());
		allEntries.add(HAMCREST_TEXT_PLUGIN.getLibraryEntry());

		return new HamcrestContainer(theContainerPath, allEntries.toArray(new IClasspathEntry[allEntries.size()]));
	}
}
