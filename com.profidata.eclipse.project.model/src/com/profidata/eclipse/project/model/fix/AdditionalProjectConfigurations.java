package com.profidata.eclipse.project.model.fix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdditionalProjectConfigurations {
    public final String executionEnvironment;
    public final Map<String, ProjectConfiguration> projectConfigurations = new HashMap<>();

    public static class ProjectConfiguration {
        public final String executionEnvironment;

        public final String encoding;
        public final Set<String> additionalPackageDependencies = new HashSet<>();
        public final Set<ClasspathEntry> additionalClasspathEntries = new HashSet<>();

        public final List<String> additionalBundles = new ArrayList<>();

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

        public final ClasspathEntryType type;
        public final String path;
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


    public AdditionalProjectConfigurations(String theExecutionEnvironment) {
        this.executionEnvironment = theExecutionEnvironment;
    }
}
