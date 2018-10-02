package com.profidata.eclipse.project.model.fix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.profidata.eclipse.project.model.Activator;
import com.profidata.eclipse.project.model.ProjectConstants;
import com.profidata.eclipse.project.model.ProjectWrapper;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurationDefinitions.ProjectConfiguration;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class TestFragmentCreator {

    private final IProject project;
    private final List<String> testTypes;

    public static void run(IProject theProject, List<String> theTestTypes) {
        new TestFragmentCreator(theProject, theTestTypes).create();
    }

    public TestFragmentCreator(IProject theProject, List<String> theTestTypes) {
        this.project = theProject;
        this.testTypes = theTestTypes;
    }

    private void create() {
        IJavaProject aJavaProject = JavaCore.create(this.project);

        try {
            IClasspathEntry[] allClasspathEntries = aJavaProject.getRawClasspath();
            List<IClasspathEntry> allTestSourceClasspathEntries;

            // first we check for unit tests
            allTestSourceClasspathEntries = Arrays.stream(allClasspathEntries)
                    .filter(theEntry -> theEntry.getContentKind() == IPackageFragmentRoot.K_SOURCE && theEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
                    .filter(theEntry -> this.testTypes.contains(theEntry.getPath().removeFirstSegments(1).segment(0))
                            || (theEntry.getPath().removeFirstSegments(1).segmentCount() > 1 && theEntry.getPath().removeFirstSegments(1).segment(0).equals("src")
                                    && this.testTypes.contains(theEntry.getPath().removeFirstSegments(1).segment(1))))
                    .collect(Collectors.toList());

            if (!allTestSourceClasspathEntries.isEmpty()) {
                List<IClasspathEntry> allChangedClasspathEntries = new ArrayList<>(Arrays.asList(allClasspathEntries));

                allChangedClasspathEntries.removeAll(allTestSourceClasspathEntries);

                aJavaProject.setRawClasspath(allChangedClasspathEntries.toArray(new IClasspathEntry[allChangedClasspathEntries.size()]), null);

                createTestProject(this.project, allTestSourceClasspathEntries);
            }
        } catch (JavaModelException theCause) {
            Activator.error("Could not access class path of project '" + this.project.getName() + "': " + theCause.getMessage());
        }
    }

    private void createTestProject(IProject theProject, List<IClasspathEntry> theTestSourceClasspathEntries) {
        IWorkspace aWorkspace = theProject.getWorkspace();
        String aTestProjectName = theProject.getName() + ".test";
        ProjectWrapper aProjectWrapper = ProjectWrapper.of(aWorkspace, aTestProjectName);

        if (!aProjectWrapper.isExisting()) {
            Activator.info(" -> Create OSGi Test fragment project: " + aTestProjectName);
            ProjectConfiguration aAdditionalConfig = AdditionalProjectConfigurationDefinitions.find(aTestProjectName);
            ProjectWrapper.of(theProject).setSingletonPlugin(true);
            if (aProjectWrapper.hasProtocol()) {
                Activator.info(aProjectWrapper.getProtocolMessage());
            }

            String aExecutionEnvironment = AdditionalProjectConfigurationDefinitions.findExecutionEnvironment(aTestProjectName);
            IPath aWorkspaceLocation = theProject.getWorkspace().getRoot().getLocation();
            aProjectWrapper.createProject().open().toJavaProject().removeDefaultSourceFolder().setOutputFolder("bin").addNature(ProjectConstants.PLUGIN_NATURE_ID)
                    .addBuilder("org.eclipse.pde.ManifestBuilder").addBuilder("org.eclipse.pde.SchemaBuilder")
                    .addClasspathEntry(theTestProject -> JavaCore
                            .newContainerEntry(new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/"+aExecutionEnvironment)))
                    .addClasspathEntry(theTestProject -> JavaCore.newContainerEntry(ProjectConstants.PLUGIN_CLASSPATH));

            IPath aProjectLocation = theProject.getLocation();
            for (IClasspathEntry aTestSourceClasspathEntry : theTestSourceClasspathEntries) {
                IPath aRelativeProjectLocation = aProjectLocation.makeRelativeTo(aWorkspaceLocation);
                IPath aSourcePath = aTestSourceClasspathEntry.getPath();
                IPath aSourceLocation = new Path("WORKSPACE_LOC").append(aRelativeProjectLocation).append(aSourcePath.removeFirstSegments(1));
                String aSourceType = aSourcePath.lastSegment();
                String aTestType = aSourcePath.removeLastSegments(1).lastSegment();

                aProjectWrapper.addLinkedSourceFolder(aTestType + "-" + aSourceType, aSourceLocation);
            }

            aProjectWrapper
                    .createTestFragmentManifest(theProject, aExecutionEnvironment, () -> aAdditionalConfig.additionalPackageDependencies, () -> Collections.emptySet(), Collections
                            .emptyMap())
                    .createBuildProperties().refresh();

            // Some of the Xentis projects have now set the encoding UTF-8 which is not the default.
            // Therefore the corresponding test fragment should have the same encoding
            try {
                String aTestCharset = aProjectWrapper.getProject().getDefaultCharset();
                String aHostCharset = theProject.getDefaultCharset();

                if (!aHostCharset.equals(aTestCharset)) {
                    aProjectWrapper.getProject().setDefaultCharset(aHostCharset, null);
                }
            } catch (CoreException theCause) {
                Activator.error("Access to default charset of project '" + aTestProjectName + "' failed:\n-> " + aProjectWrapper.getErrorMessage());
            }

            if (aProjectWrapper.hasError()) {
                Activator.error("Create test project '" + aTestProjectName + "' failed:\n-> " + aProjectWrapper.getErrorMessage());
            } else if (aProjectWrapper.hasProtocol()) {
                Activator.info(aProjectWrapper.getProtocolMessage());
            }
        }
    }
}
