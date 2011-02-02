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

import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class TreeToGraphConverter {

    private final ArtifactFilter filter;

    public TreeToGraphConverter(ArtifactFilter filter) {
        this.filter = filter;
    }

    public Dependency convert(DependencyNode node) {

        // construct
        Map<String,Dependency> deps = create(node, new HashMap<String, Dependency>());

        // link
        Dependency root = link(node, deps);

        // sort
        for (Dependency dep : deps.values()) {
            Collections.sort(dep.getChildren(), new Comparator() {
                public int compare(Object o1, Object o2) {
                    String a = ((Dependency) o1).getArtifact().getFile().getName();
                    String b = ((Dependency) o2).getArtifact().getFile().getName();
                    return a.compareTo(b);
                }
            });
        }

        return root;
    }

    private Map<String, Dependency> create(DependencyNode node, HashMap<String, Dependency> dependencies) {
        if (!filter.include(node.getArtifact())) return dependencies;

        State state = State.values()[node.getState()];
        Dependency dep = new Dependency(node.getArtifact());

        if (state == State.INCLUDED) dependencies.put(dep.getId(), dep);

        List<DependencyNode> children = node.getChildren();

        for (DependencyNode child : children) {
            create(child, dependencies);
        }

        return dependencies;
    }

    public Dependency link(DependencyNode node, Map<String, Dependency> deps) {
        return link(null, node, deps);
    }

    public Dependency link(Trail trail, DependencyNode node, Map<String, Dependency> deps) {
        if (!filter.include(node.getArtifact())) return null;

        Dependency dep = deps.get(node.getArtifact().getId());

        dep.link(trail);


        List<DependencyNode> children = node.getChildren();

        for (DependencyNode child : children) {
            link(new Trail(trail, dep), child,  deps);
        }

        return dep;
    }

    public static enum State {
        INCLUDED,
        OMITTED_FOR_DUPLICATE,
        OMITTED_FOR_CONFLICT,
        OMITTED_FOR_CYCLE;
    }
}
