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
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.swizzle.depreport.Dependency;
import org.codehaus.swizzle.depreport.ExcludesArtifactFilter;
import org.codehaus.swizzle.depreport.IncludesArtifactFilter;

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
 * @aggregator
 */
public class ReportMojo
        extends AbstractMojo {

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

    /**
     * Create the binary distribution.
     *
     * @throws org.apache.maven.plugin.MojoExecutionException
     *
     */
    public void execute()
            throws MojoExecutionException, MojoFailureException {

        if (!outputDirectory.exists()){
            outputDirectory.mkdirs();
        }

        Map deps = new HashMap();

        AndArtifactFilter filter = new AndArtifactFilter();
        filter.add(new ScopeArtifactFilter(getScope()));

        if (!getIncludes().isEmpty()) {
            filter.add(new IncludesArtifactFilter(getIncludes()));
        }
        if (!getExcludes().isEmpty()) {
            filter.add(new ExcludesArtifactFilter(getExcludes()));
        }

        for (Iterator j = getDependencies().iterator(); j.hasNext();) {
            Artifact artifact = (Artifact) j.next();

            if (filter.include(artifact)) {
                Dependency dep = new Dependency(artifact);
                deps.put(dep.getId(), dep);
            } else {
                getLog().debug("artifact: " + artifact + " not included");
            }
        }

        RootDep rootDep = asDependencyTree(deps);

        List formats = getFormats();
        for (int i = 0; i < formats.size(); i++) {
            try {
                String format = (String) formats.get(i);

                String filename = name + "." + format;

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
            List trail = dep.getArtifact().getDependencyTrail();
            int parent = trail.size() - 2;
            if (parent == 0) {
                rootDep.addChild(dep);
            } else {
                Dependency parentDep = (Dependency) deps.get(trail.get(parent));
                parentDep.addChild(dep);
            }
        }

        // sort
        deps.put(rootDep.getId(), rootDep);
        for (Iterator iterator = deps.values().iterator(); iterator.hasNext();) {
            Dependency dep = (Dependency) iterator.next();
            Collections.sort(dep.getChildern(), new Comparator() {
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
        public RootDep() {
            super(null);
        }

        public String getId() {
            return "root";
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
}
