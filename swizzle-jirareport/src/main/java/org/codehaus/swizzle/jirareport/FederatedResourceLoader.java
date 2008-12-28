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
package org.codehaus.swizzle.jirareport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

public class FederatedResourceLoader extends ResourceLoader {

    private List loaders = new ArrayList();

    public FederatedResourceLoader() {
        loaders.add(new FileResourceLoader());
        loaders.add(new SimplerFileResourceLoader());
        loaders.add(new URLResourceLoader());
        loaders.add(new ClasspathResourceLoader());
    }

    public void commonInit(RuntimeServices runtimeServices, ExtendedProperties extendedProperties) {
        for (int i = 0; i < loaders.size(); i++) {
            ResourceLoader loader = (ResourceLoader) loaders.get(i);
            loader.commonInit(runtimeServices, extendedProperties);
        }
    }

    public InputStream getResourceStream(String string) throws ResourceNotFoundException {
        for (int i = 0; i < loaders.size(); i++) {
            try {
                ResourceLoader loader = (ResourceLoader) loaders.get(i);
                InputStream resourceStream = loader.getResourceStream(string);
                return resourceStream;
            } catch (ResourceNotFoundException e) {
            }
        }
        throw new ResourceNotFoundException(string);
    }

    public void init(ExtendedProperties extendedProperties) {
        for (int i = 0; i < loaders.size(); i++) {
            ResourceLoader loader = (ResourceLoader) loaders.get(i);
            loader.init(extendedProperties);
        }
    }

    public long getLastModified(Resource resource) {
        return System.currentTimeMillis();
    }

    public boolean isSourceModified(Resource resource) {
        return true;
    }

    public static class URLResourceLoader extends ResourceLoader {
        public long getLastModified(Resource resource) {
            return 0;
        }

        public InputStream getResourceStream(String string) throws ResourceNotFoundException {
            try {
                URL url = new URL(string);
                return url.openStream();
            } catch (IOException e) {
                throw (ResourceNotFoundException) new ResourceNotFoundException(string).initCause(e);
            }
        }

        public void init(ExtendedProperties extendedProperties) {
        }

        public boolean isSourceModified(Resource resource) {
            return false;
        }
    }

    public static class SimplerFileResourceLoader extends ResourceLoader {
        public long getLastModified(Resource resource) {
            return 0;
        }

        public InputStream getResourceStream(String string) throws ResourceNotFoundException {
            try {
                File file = new File(string);
                return new FileInputStream(file);
            } catch (IOException e) {
                throw (ResourceNotFoundException) new ResourceNotFoundException(string).initCause(e);
            }
        }

        public void init(ExtendedProperties extendedProperties) {
        }

        public boolean isSourceModified(Resource resource) {
            return false;
        }
    }
}
