/**
 *
 * Copyright 2006 David Blevins
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.codehaus.swizzle.depreport;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.Artifact;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Do not specify a phase, so make it usable in a reactor environment where forking would create issues.
 *
 * @goal usage
 * @requiresDependencyResolution test
 */
public class UsageMojo
        extends AbstractMojo {

    /**
     * @parameter
     * @required
     */
    private String[] formats;

    /**
     * Base directory of the project.
     *
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File basedir;

    /**
     * The output directory of the assembled distribution file.
     *
     * @parameter expression="${project.build.directory}/"
     * @required
     */
    protected File outputDirectory;

    /**
     * The output directory of the assembled distribution file.
     *
     * @parameter expression="${project.build.directory}/classes"
     * @required
     */
    protected File classesDirectory;

    /**
     * The filename of the assembled distribution file.
     *
     * @parameter expression="${name}" default-value="used"
     * @required
     */
    protected String name;

    /**
     * The Maven Project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    protected MavenProject getExecutedProject()
    {
        return project;
    }

    private static void dir(File dir, DependencyVisitor dependencyVisitor) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                dir(file, dependencyVisitor);
            } else if (file.getName().endsWith(".class")) {
                file(file, dependencyVisitor);
            }
        }
    }

    private static void file(File file, DependencyVisitor dependencyVisitor) {
        try {
            FileInputStream in = new FileInputStream(file);
            try {
                ClassReader classReader = new ClassReader(in);
                classReader.accept(dependencyVisitor, true);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }


    /**
     * Create the binary distribution.
     *
     * @throws org.apache.maven.plugin.MojoExecutionException
     *
     */
    public void execute()
            throws MojoExecutionException, MojoFailureException {

        Map<String,Dependency> deps = getDependencyTree();

        RootDep rootDep = (RootDep) deps.remove("root");

        List children = rootDep.getChildren();
        if (children.size() == 0){
            getLog().debug("No dependencies to report");
            return;
        }

        DependencyVisitor visitor = new DependencyVisitor();

        dir(classesDirectory, visitor);

        for (String className : visitor.classes) {
            for (Dependency dep : deps.values()) {
                dep.provides(className);
            }
        }

        List<Dependency> declared = children;

        Set providedClasses = new HashSet();
        for (Dependency dep : declared) {
            providedClasses.addAll(dep.getUsedClasses());
        }

        for (int i = 0; i < children.size(); i++) {
            Dependency dep = (Dependency) children.get(i);
            checkDuplicates(dep.getChildren(), providedClasses);
        }

        for (int i = 0; i < children.size(); i++) {
            Dependency dep = (Dependency) children.get(i);
            if (!dep.isUsed() && !dep.getArtifact().isOptional()){
                getLog().info("Unused " + dep.getId());
            }
        }

        for (int i = 0; i < children.size(); i++) {
            Dependency dep = (Dependency) children.get(i);
            reportUsed(dep.getChildren());
        }


    }

    private void checkDuplicates(List<Dependency> children, Set<String> providedClasses) {

        for (Dependency dependency : children) {
            Set<String> usedClasses = dependency.getUsedClasses();
            dependency.setDuplicate(duplicates(usedClasses, providedClasses));

            checkDuplicates(dependency.getChildren(), providedClasses);
        }
    }

    private boolean duplicates(Set<String> usedClasses, Set<String> providedClasses) {
        if (usedClasses.size() == 0) return false;

        for (String className : usedClasses) {
            if (!providedClasses.contains(className)) {
                return false;
            }
        }
        return true;
    }

    private void reportUsed(List children) {
        for (int i = 0; i < children.size(); i++) {
            Dependency dep = (Dependency) children.get(i);
            if (dep.isDuplicate()){
                getLog().info("Duplicate " + dep.getId());
                Set<String> classes = dep.getUsedClasses();
                for (String className : classes) {
                    getLog().debug("Duplicated " + className);
                }
            } else if (dep.isUsed()){
                getLog().info("Undeclared " + dep.getId());
                Set<String> classes = dep.getUsedClasses();
                for (String className : classes) {
                    getLog().debug("Used " + className);
                }
            }

            reportUsed(dep.getChildren());
        }
    }

    private Map getDependencyTree() {
        Map deps = new HashMap();

        AndArtifactFilter filter = new AndArtifactFilter();
        filter.add(new ScopeArtifactFilter("runtime"));

        for (Iterator j = getDependencies().iterator(); j.hasNext();) {
            Artifact artifact = (Artifact) j.next();

            artifact.getDependencyFilter();
            if (filter.include(artifact)) {
                Dependency dep = new Dependency(artifact);
                deps.put(dep.getId(), dep);
            } else {
                getLog().debug("artifact: " + artifact + " not included");
            }
        }

        RootDep rootDep = asDependencyTree(deps);
        deps.put("root", rootDep);

        return deps;
    }

    private RootDep asDependencyTree(Map deps) {
        RootDep rootDep = new RootDep();

        String rootEntry = null;
        // Link
        for (Iterator iterator = deps.values().iterator(); iterator.hasNext();) {
            Dependency dep = (Dependency) iterator.next();
            try {
                List trail = dep.getArtifact().getDependencyTrail();
                if (trail == null){
                    rootDep.artifact = dep.getArtifact();
                    rootEntry = dep.getId();
                    continue;
                }

                int parent = trail.size() - 2;

                if (parent == 0) {
                    rootDep.addChild(dep);
                } else {
                    String parentId = (String) trail.get(parent);
                    Dependency parentDep = (Dependency) deps.get(parentId);

                    if (parentDep == null && parentId.endsWith("SNAPSHOT")){
                        for (Iterator iter = deps.entrySet().iterator(); iter.hasNext();) {
                            Map.Entry entry = (Map.Entry) iter.next();
                            String id = (String) entry.getKey();
                            id = id.substring(0, id.lastIndexOf(':'));
                            if (parentId.startsWith(id)){
                                parentDep = (Dependency) entry.getValue();
                                break;
                            }
                        }
                    }
                    if (parentDep == null){
                        rootDep.addChild(dep);
                        getLog().info("Couldn't find parent for "+dep.getId());
                    } else {
                        parentDep.addChild(dep);
                    }
                }
            } catch (Exception e) {
                getLog().error("Could not link dep "+dep.getId(), e);
            }
        }

        deps.remove(rootEntry);

        // sort
        for (Iterator iterator = deps.values().iterator(); iterator.hasNext();) {
            Dependency dep = (Dependency) iterator.next();
            Collections.sort(dep.getChildren(), new Comparator() {
                public int compare(Object o1, Object o2) {
                    String a = ((Dependency) o1).getArtifact().getFile().getName();
                    String b = ((Dependency) o2).getArtifact().getFile().getName();
                    return a.compareTo(b);
                }
            });
        }
        return rootDep;
    }

    public static class RootDep extends Dependency {

        private Artifact artifact;

        public RootDep() {
            super();
        }

        public Artifact getArtifact() {
            return artifact;
        }

        public String getId() {
            return (artifact == null)? "root": artifact.getId();
        }
    }


    /**
     * Retrieves all artifact dependencies.
     *
     * @return A HashSet of artifacts
     */
    private Set getDependencies() {
        MavenProject project = getExecutedProject();

        Set dependenciesSet = new HashSet();

        if (project.getArtifact() != null && project.getArtifact().getFile() != null) {
            dependenciesSet.add(project.getArtifact());
        }

        Set projectArtifacts = project.getArtifacts();
        if (projectArtifacts != null) {
            dependenciesSet.addAll(projectArtifacts);
        }

        return dependenciesSet;
    }


    public List getFormats() {
        return formats == null ? Collections.EMPTY_LIST : Arrays.asList(formats);
    }

}
