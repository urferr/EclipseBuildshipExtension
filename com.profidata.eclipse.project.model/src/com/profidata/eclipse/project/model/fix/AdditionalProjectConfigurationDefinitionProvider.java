package com.profidata.eclipse.project.model.fix;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.profidata.eclipse.project.model.Activator;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurations.ProjectConfiguration;

public class AdditionalProjectConfigurationDefinitionProvider {

	private static final AdditionalProjectConfigurationDefinitionProvider INSTANCE = new AdditionalProjectConfigurationDefinitionProvider();

	public static AdditionalProjectConfigurationDefinitionProvider getInstance() {
		return INSTANCE;
	}

	private final IPath additionalProjectConfigurationPath;
	private volatile AdditionalProjectConfigurations projectConfigurationDefinitions;

	private AdditionalProjectConfigurationDefinitionProvider() {
		this.additionalProjectConfigurationPath = ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.append(System.getProperty("extension.buildship.additional.project.configuration.path", ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()))
				.append("AdditionalProjectConfiguration.json");

		ResourcesPlugin.getWorkspace().addResourceChangeListener(new ProjectConfigurationResourceChangeReporter(), IResourceChangeEvent.POST_CHANGE);

		reloadProjectConfigurationDefinition();
	}

	private void reloadProjectConfigurationDefinition() {
		if (additionalProjectConfigurationPath.toFile().exists()) {
			try (Reader aReader = new FileReader(additionalProjectConfigurationPath.toOSString())) {
				projectConfigurationDefinitions = new Gson().fromJson(aReader, AdditionalProjectConfigurations.class);
			}
			catch (JsonIOException | IOException cause) {
				projectConfigurationDefinitions = null;
				Activator.error("could not read json file: " + additionalProjectConfigurationPath.toOSString());
			}
		}
		else {
			this.projectConfigurationDefinitions = null;
			Activator.info("json file does not exist: " + this.additionalProjectConfigurationPath.toOSString());
		}
	}

	public String findExecutionEnvironment(String theProjectName) {
		AdditionalProjectConfigurations allConfigurations = projectConfigurationDefinitions;
		String aExecutionEnvironment = find(allConfigurations, theProjectName).executionEnvironment;

		if (aExecutionEnvironment == null && allConfigurations != null) {
			aExecutionEnvironment = allConfigurations.executionEnvironment;
		}

		return aExecutionEnvironment;
	}

	public String findJUnitLibraryPath(String theProjectName) {
		AdditionalProjectConfigurations allConfigurations = projectConfigurationDefinitions;

		return allConfigurations.junitLibraryPath;
	}

	public ProjectConfiguration find(String theProjectName) {
		return find(projectConfigurationDefinitions, theProjectName);
	}

	private ProjectConfiguration find(AdditionalProjectConfigurations theConfigurations, String theProjectName) {
		if (theConfigurations != null && theConfigurations.projectConfigurations.containsKey(theProjectName)) {
			return theConfigurations.projectConfigurations.get(theProjectName);
		}

		return new ProjectConfiguration(null);
	}

	private class ProjectConfigurationResourceChangeReporter implements IResourceChangeListener {

		@Override
		public void resourceChanged(IResourceChangeEvent theEvent) {
			switch (theEvent.getType()) {
				case IResourceChangeEvent.POST_CHANGE:
					try {
						theEvent.getDelta().accept(new ProjectConfigurationResourceVisitor());
					}
					catch (CoreException e) {
						Activator.error("could not read json file: " + e.getMessage());
					}
					break;
				default:
					break;
			}
		}
	}

	private class ProjectConfigurationResourceVisitor implements IResourceDeltaVisitor {
		@Override
		public boolean visit(IResourceDelta theResourceDelta) {
			switch (theResourceDelta.getKind()) {
				case IResourceDelta.CHANGED:
					IPath aChangeResourcePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(theResourceDelta.getResource().getFullPath());

					if (AdditionalProjectConfigurationDefinitionProvider.this.additionalProjectConfigurationPath.equals(aChangeResourcePath)) {
						Activator.info("json file: " + AdditionalProjectConfigurationDefinitionProvider.this.additionalProjectConfigurationPath.toOSString() + " modified -> reloading it");
						reloadProjectConfigurationDefinition();
						return false;
					}
					break;
				default:
					break;
			}
			return true; // visit the children
		}
	}

}
