package org.codehaus.swizzle.depreport;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.*;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DefaultDependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.codehaus.swizzle.depreport.Dependency;
import org.codehaus.swizzle.depreport.ExcludesArtifactFilter;
import org.codehaus.swizzle.depreport.IncludesArtifactFilter;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Do not specify a phase, so make it usable in a reactor environment where forking would create issues.
 *
 * @goal report
 * @requiresDependencyResolution test
 */
public class ReportMojo
        extends AbstractMojo {

    /**
     * The Maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The artifact repository to use.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * The artifact factory to use.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;

    /**
     * The artifact metadata source to use.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * The artifact collector to use.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactCollector artifactCollector;

    /**
     * @parameter
     */
    private String[] includes;

    /**
     * @parameter
     */
    private String[] excludes;

    /**
     * @parameter
     * @required
     */
    private String[] formats;

    /**
     * @parameter expression="${scope}" default-value="runtime"
     */
    private String scope;

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
     * @parameter expression="${project.build.directory}/classes/META-INF/"
     * @required
     */
    protected File outputDirectory;

    /**
     * The filename of the assembled distribution file.
     *
     * @parameter expression="${name}" default-value="dependencies"
     * @required
     */
    protected String name;

    protected MavenProject getExecutedProject()
    {
        return project;
    }

    /**
     * Create the binary distribution.
     *
     * @throws org.apache.maven.plugin.MojoExecutionException
     *
     */
    public void execute()
            throws MojoExecutionException, MojoFailureException {

        AndArtifactFilter filter = new AndArtifactFilter();
        filter.add(new ScopeArtifactFilter(getScope()));

        if (!getIncludes().isEmpty()) {
            filter.add(new IncludesArtifactFilter(getIncludes()));
        }
        if (!getExcludes().isEmpty()) {
            filter.add(new ExcludesArtifactFilter(getExcludes()));
        }

        Set<Artifact> artifacts = getDependencies();

        Map<String, Dependency> deps = new HashMap();

        for (Artifact artifact : artifacts) {
            Dependency dep = new Dependency(artifact);
            deps.put(dep.getId(), dep);
        }

        TreeToGraphConverter converter = new TreeToGraphConverter(filter);

        DefaultDependencyTreeBuilder dependencyTreeBuilder = new DefaultDependencyTreeBuilder();

        dependencyTreeBuilder.enableLogging(new LogWrapper(getLog()));


        Dependency rootDep;

        try {
            DependencyNode node = dependencyTreeBuilder.buildDependencyTree(project, localRepository, artifactFactory, artifactMetadataSource, filter, artifactCollector);

            rootDep = converter.link(node, deps);
        } catch (DependencyTreeBuilderException e) {
            getLog().error(e);
            return;
        }

        if (rootDep.getChildren().size() == 0){
            getLog().info("No dependencies to report");
            return;
        }

//        rootDep.accept(new AbbreviationGraphVisitior());
//        rootDep.accept(new RollupVisitior());

        List formats = getFormats();
        for (int i = 0; i < formats.size(); i++) {
            try {
                String format = (String) formats.get(i);

                String filename = name + "." + format;

                if (!outputDirectory.exists()){
                    outputDirectory.mkdirs();
                }

                File destFile = new File(outputDirectory, filename);

                FileWriter fileWriter = new FileWriter(destFile);
                PrintWriter out = new PrintWriter(fileWriter);

                try {
                    getFormatter(out, format).format(rootDep);
                } finally {
                    out.close();
                }
            }
            catch (IOException e) {
                throw new MojoExecutionException("Error creating report: " + e.getMessage(), e);
            }
        }
    }

    private Formatter getFormatter(PrintWriter out, String format) throws MojoExecutionException {

        if (format.equals("txt")) {

            return new PlainTextFormatter(out);

        } else if (format.equals("xml")) {

            return new XmlFormatter(out);

        } else if (format.equals("wiki")) {

            return new WikiFormatter(out);

        } else throw new MojoExecutionException("No such format '" + format + "'");
    }

    private RootDep asDependencyTree(Map deps) {
        RootDep rootDep = new RootDep();

        // Link
        for (Iterator iterator = deps.values().iterator(); iterator.hasNext();) {
            Dependency dep = (Dependency) iterator.next();
            try {
                List trail = dep.getArtifact().getDependencyTrail();
                if (trail == null){
                    rootDep.artifact = dep.getArtifact();
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

        // sort
        deps.put(rootDep.getId(), rootDep);
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

        if (project.getArtifact() != null) {
            dependenciesSet.add(project.getArtifact());
        }

        Set projectArtifacts = project.getArtifacts();
        if (projectArtifacts != null) {
            dependenciesSet.addAll(projectArtifacts);
        }

        return dependenciesSet;
    }


    public List getIncludes() {
        return includes == null ? Collections.EMPTY_LIST : Arrays.asList(includes);
    }

    public List getExcludes() {
        return excludes == null ? Collections.EMPTY_LIST : Arrays.asList(excludes);
    }

    public List getFormats() {
        return formats == null ? Collections.EMPTY_LIST : Arrays.asList(formats);
    }

    public String getScope() {
        return scope;
    }

    private static class LogWrapper implements Logger {
        private final Log log;

        public LogWrapper(Log log) {
            this.log = log;
        }

        public boolean isDebugEnabled() {
            return log.isDebugEnabled();
        }

        public void debug(String content) {
            log.debug(content);
        }

        public void debug(String content, Throwable error) {
            log.debug(content, error);
        }

        public void debug(Throwable error) {
            log.debug(error);
        }

        public boolean isInfoEnabled() {
            return log.isInfoEnabled();
        }

        public void info(String content) {
            log.info(content);
        }

        public void info(String content, Throwable error) {
            log.info(content, error);
        }

        public void info(Throwable error) {
            log.info(error);
        }

        public boolean isWarnEnabled() {
            return log.isWarnEnabled();
        }

        public void warn(String content) {
            log.warn(content);
        }

        public void warn(String content, Throwable error) {
            log.warn(content, error);
        }

        public void warn(Throwable error) {
            log.warn(error);
        }

        public boolean isErrorEnabled() {
            return log.isErrorEnabled();
        }

        public void error(String content) {
            log.error(content);
        }

        public void error(String content, Throwable error) {
            log.error(content, error);
        }

        public void error(Throwable error) {
            log.error(error);
        }

        public void fatalError(String message) {
        }

        public void fatalError(String message, Throwable throwable) {
        }

        public boolean isFatalErrorEnabled() {
            return false;
        }

        public Logger getChildLogger(String name) {
            return null;
        }

        public int getThreshold() {
            return 0;
        }

        public String getName() {
            return null;
        }
    }

}
