/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;
import org.apache.maven.shared.dependency.tree.DependencyNode;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.net.URL;
import java.io.IOException;
import java.io.Serializable;

/**
 * @version $Rev$ $Date$
 */
public class Dependency {
    private final Artifact artifact;

    private final List<Dependency> children = new ArrayList<Dependency>();

    private final List<Trail> trails = new ArrayList<Trail>();

    private final Set usedClasses = new HashSet();

    private final ResourceFinder finder;

    private final Map data = new HashMap();

    private boolean duplicate;

    public Dependency() {
        this.artifact = null;
        this.finder = null;
    }
    public Dependency(Artifact artifact) {
        this.artifact = artifact;

        try {
            if (artifact.getFile() != null) {
                this.finder = new ResourceFinder(artifact.getFile().toURL());
            } else {
                this.finder = new ResourceFinder();
            }
        } catch (Exception e) {
            throw new IllegalStateException(artifact.getId(), e);
        }
    }

    public boolean provides(String className) {
        try {
            URL classUrl = finder.find(className + ".class");
            if (classUrl != null) {
                usedClasses.add(className);
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        } catch(Exception e){
            throw new IllegalStateException(getId(), e);
        }
    }

    public Dependency clone() {
        Dependency clone = new Dependency(artifact);
        clone.getTrails().addAll(getTrails());

        for (Dependency dependency : children) {
            clone.getChildren().add(dependency.clone());
        }

        return clone;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public <T> T get(Class<T> type) {
        return (T)data.get(type);
    }

    public <T> T remove(Class<T> type) {
        return (T)data.remove(type);
    }

    public <T> T set(Class<T> type, T value) {
        return (T)data.put(type, value);
    }

    public <T> boolean containsKey(Class<T> type) {
        return data.containsKey(type);
    }

    public boolean isUsed(){
        return usedClasses.size() > 0;
    }

    public Set getUsedClasses() {
        return usedClasses;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void addChild(Dependency dep){
        children.add(dep);
    }

    public List<Dependency> getChildren() {
        return children;
    }

    public List<Trail> getTrails() {
        return trails;
    }

    public boolean implies(String string){
        return getId().equals(string);
    }

    public String getId() {
        return artifact.getId();
    }

    public String toString() {
        return (artifact != null)? artifact.getId(): "null";
    }

    public void link(Trail trail) {
        if (trail == null) return;

        getTrails().add(trail);

        Dependency parent = trail.getDependency();

        parent.getChildren().add(this);
    }

    public void unlink(Trail trail) {
        if (trail == null) return;

        if (!getTrails().contains(trail)){
            throw new IllegalArgumentException("Not linked to trail" + trail);
        }

        getTrails().remove(trail);

        Dependency parent = trail.getDependency();

        parent.getChildren().remove(this);
    }

    public Trail getTrail(Dependency parent){
        for (Trail trail : trails) {
            if (trail.getDependency() == parent){
                return trail;
            }
        }
        return null;
    }

    public Set<Dependency> getDescendants() {
        Set<Dependency> decendants = new HashSet<Dependency>();

        for (Dependency child : children) {
            decendants.add(child);
            decendants.addAll(child.getDescendants());
        }

        return decendants;
    }

    public boolean accept(Visitor visitor) {
        if (visitor.visit(this)) {
            for (Dependency dependency : children) {
                if (!dependency.accept(visitor)) break;
            }
        }

        return visitor.endVisit(this);
    }

    public interface Visitor
    {
        /**
         * Starts the visit to the specified dependency node.
         *
         * @param node
         *            the dependency node to visit
         * @return <code>true</code> to visit the specified dependency node's children, <code>false</code> to skip the
         *         specified dependency node's children and proceed to its next sibling
         */
        boolean visit( Dependency node );

        /**
         * Ends the visit to to the specified dependency node.
         *
         * @param node
         *            the dependency node to visit
         * @return <code>true</code> to visit the specified dependency node's next sibling, <code>false</code> to skip
         *         the specified dependency node's next siblings and proceed to its parent
         */
        boolean endVisit( Dependency node );
    }

}
