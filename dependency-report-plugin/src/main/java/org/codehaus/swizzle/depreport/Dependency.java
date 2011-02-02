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

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class Dependency {
    private final Artifact artifact;

    private final List childern = new ArrayList();

    private final Set usedClasses = new HashSet();

    private final ResourceFinder finder;

    private boolean duplicate;

    public Dependency() {
        this.artifact = null;
        this.finder = null;
    }
    public Dependency(Artifact artifact) {
        this.artifact = artifact;

        try {
            this.finder = new ResourceFinder(artifact.getFile().toURL());
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

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
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
        childern.add(dep);
    }

    public List getChildren() {
        return childern;
    }

    public boolean implies(String string){
        return getId().equals(string);
    }

    public String getId() {
        return artifact.getId();
    }
}
