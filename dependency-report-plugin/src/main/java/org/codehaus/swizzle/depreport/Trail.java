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

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class Trail {
    private final Trail parent;
    private final Dependency dependency;

    public Trail(Trail parent, Dependency dependency) {
        this.parent = parent;
        this.dependency = dependency;
    }

    public Trail getParent() {
        return parent;
    }

    public Dependency getDependency() {
        return dependency;
    }

    public List<Dependency> asList() {
        List<Dependency> dependencies;
        if (parent == null) {
            dependencies = new ArrayList<Dependency>();
        } else {
            dependencies = parent.asList();
        }
        dependencies.add(dependency);

        return dependencies;
    }

    public boolean contains(Dependency dep){
        if (dep.getId().equals(this.dependency.getId())) return true;
        if (parent == null) return false;
        return parent.contains(dep);
    }

}
