package com.profidata.eclipse.project.model.fix;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.profidata.eclipse.project.model.Activator;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

public class AdditionalProjectConfigurationDefinitions {
    public final String executionEnvironment;
    public final Map<String, ProjectConfiguration> projectConfigurations = new HashMap<>();

    public static class ProjectConfiguration {

        private final String executionEnvironment;

        public final String encoding;
        public final Set<String> additionalPackageDependencies = new HashSet<>();
        public final Set<ClasspathEntry> additionalClasspathEntries = new HashSet<>();

        public ProjectConfiguration(String theEncoding) {
            this(theEncoding, null);
        }

        public ProjectConfiguration(String theEncoding, String theExecutionEnvironment) {
            this.encoding = theEncoding;
            this.executionEnvironment = theExecutionEnvironment;
        }
    }

    public static class ClasspathEntry {

        public enum ClasspathEntryType {
            Library, Project, Container
        }

        final ClasspathEntryType type;
        final String path;
        public final boolean exported;
        public final Set<AccessRule> accessRules = new HashSet<>();

        public ClasspathEntry(ClasspathEntryType theType, String thePath) {
            this(theType, thePath, false);
        }

        public ClasspathEntry(ClasspathEntryType theType, String thePath, boolean theExported) {
            this.type = theType;
            this.path = thePath;
            this.exported = theExported;
        }
    }

    public static class AccessRule {

        public final String pattern;
        public final int kind;

        public AccessRule(String thePattern, int theKind) {
            this.pattern = thePattern;
            this.kind = theKind;
        }
    }

    public static String findExecutionEnvironment(String theProjectName) {
        AdditionalProjectConfigurationDefinitions allConfigurations = load();
        String aExecutionEnvironment = find(allConfigurations, theProjectName).executionEnvironment;

        if (aExecutionEnvironment == null) {
            aExecutionEnvironment = allConfigurations.executionEnvironment;
        }

        return aExecutionEnvironment;
    }

    public static ProjectConfiguration find(String theProjectName) {
        return find(load(), theProjectName);
    }

    private static ProjectConfiguration find(AdditionalProjectConfigurationDefinitions theConfigurations, String theProjectName) {
        if (theConfigurations != null && theConfigurations.projectConfigurations.containsKey(theProjectName)) {
            return theConfigurations.projectConfigurations.get(theProjectName);
        }

        return new ProjectConfiguration(null);
    }

    private static AdditionalProjectConfigurationDefinitions load() {
        IPath aAdditionalProjectConfigurationPath = ResourcesPlugin.getWorkspace().getRoot().getLocation()
                .append(System.getProperty("additional.project.configuration.path", "URRExtensions/PDE-Targets & Launcher")).append("AdditionProjectConfiguration.json");

        try (Reader aReader = new FileReader(aAdditionalProjectConfigurationPath.toOSString())) {
            return new Gson().fromJson(aReader, AdditionalProjectConfigurationDefinitions.class);
        } catch (JsonIOException | IOException cause) {
            Activator.error("could not read json file: " + aAdditionalProjectConfigurationPath.toOSString());
        }

        return null;
    }

    public AdditionalProjectConfigurationDefinitions(String theExecutionEnvironment) {
        this.executionEnvironment = theExecutionEnvironment;
    }
}
