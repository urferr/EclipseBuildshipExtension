package com.profidata.eclipse.project.model;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ProjectConstants {
	public static final String PLUGIN_NATURE_ID = "org.eclipse.pde.PluginNature";
	public static final String PLUGIN_CLASSPATH_ID = "org.eclipse.pde.core.requiredPlugins";
	public static final String FEATURE_NATURE_ID = "org.eclipse.pde.FeatureNature";
	public static final String GRADLE_NATURE_ID = "org.eclipse.buildship.core.gradleprojectnature";
	public static final String GRADLE_CLASSPATH_ID = "org.eclipse.buildship.core.gradleclasspathcontainer";

	public static final IPath PLUGIN_CLASSPATH = new Path(ProjectConstants.PLUGIN_CLASSPATH_ID);
    public static final IPath GRADLE_CLASSPATH = new Path(ProjectConstants.GRADLE_CLASSPATH_ID);

	private ProjectConstants() {
		// prevent from being instantiated
	}
}
