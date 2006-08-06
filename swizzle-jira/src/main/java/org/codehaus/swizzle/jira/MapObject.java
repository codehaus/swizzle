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
package org.codehaus.swizzle.jira;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;

/**
 * @version $Revision$ $Date$
 */
public class MapObject {

    /**
     * When Sending data of the following type, do not
     * send a Hashtable, instead send a single value
     * from the hashtable to act as a reference to the
     * object.
     */
    protected Map xmlrpcRefs = new HashMap();

    /**
     * A list of fields in this hashtable which should
     * not be sent on xml-rpc create or update calls.
     */
    protected List xmlrpcNoSend = new ArrayList();

    private final SimpleDateFormat[] formats;
    private final Map fields;

    protected MapObject(Map data) {
        fields = new HashMap(data);
        formats = new SimpleDateFormat[]{
                new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy"),
                new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z"),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"),
        };
    }

    protected String getString(String key) {
        return (String) fields.get(key);
    }

    protected boolean getBoolean(String key) {
        String value = getString(key);
        if (value == null) return false;
        return (value.equalsIgnoreCase("true") || value.equals("1") || value.equalsIgnoreCase("yes"));
    }

    protected int getInt(String key) {
        String value = getString(key);
        if (value == null) return 0;
        return Integer.parseInt(value);
    }

    protected void setString(String key, String value) {
        fields.put(key, value);
    }

    protected void setInt(String key, int value) {
        fields.put(key, Integer.toString(value));
    }

    protected void setBoolean(String key, boolean value) {
        fields.put(key, Boolean.toString(value));
    }

    protected void setDate(String key, Date value) {
        fields.put(key, formats[0].format(value));
    }

    protected Date getDate(String key) {
        String value = getString(key);
        if (value == null) return new Date();

        ParseException notParsable = null;
        for (int i = 0; i < formats.length; i++) {
            try {
                return formats[i].parse(value);
            } catch (ParseException e) {
                notParsable = e;
            }
        }

        notParsable.printStackTrace();
        return new Date();
    }

    protected Vector getVector(String key) {
        return (Vector) fields.get(key);
    }

    protected void setVector(String key, Vector value) {
        fields.put(key, value);
    }

    protected MapObject getMapObject(String key, Class type) {
        Object object = fields.get(key);
        if (object == null){
            return null;
        }
        if (object instanceof MapObject) {
            return (MapObject) object;
        }

        try {
            MapObject mapObject = createMapObject(type, object);
            fields.put(key, mapObject);
            return mapObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void setMapObject(String key, MapObject mapObject) {
        fields.put(key, mapObject);
    }

    protected boolean hasField(String key){
        return fields.containsKey(key);
    }
    
    protected List getMapObjects(String key, Class type) {
        List list;
        Object collection = fields.get(key);
        if (collection instanceof Vector) {
            Vector vector = (Vector) collection;
            try {
                list = toList(vector, type);
                fields.put(key, list);
            } catch (Exception e) {
                list = new ArrayList();
            }
        } else if (collection == null){
            list = new ArrayList();
            fields.put(key, list);
        } else {
            list = (List) collection;
        }

        return list;
    }

    protected void setMapObjects(String key, List objects) {
        fields.put(key, objects);
    }

    protected List toList(Vector vector, Class type) throws Exception {
        List list = new ArrayList(vector.size());

        for (int i = 0; i < vector.size(); i++) {
            Object object = createMapObject(type, vector.elementAt(i));
            list.add(object);
        }

        return list;
    }

    private MapObject createMapObject(Class type, Object value) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor constructor = type.getConstructor(new Class[]{Map.class});
        Map data;

        Object idField = xmlrpcRefs.get(type);
        if (idField != null && !(value instanceof Map)){
            data = new HashMap();
            data.put(idField, value);
        } else if (value instanceof Map) {
            data = (Map) value;
        } else {
            throw new RuntimeException("Cannot create a "+type.getName()+" from '" + value+"'");
        }

        Object object = constructor.newInstance(new Object[]{data});
        return (MapObject) object;
    }

    public Hashtable toHashtable() {
        Hashtable hashtable = new Hashtable(fields);

        // Remove anything marked as "no send"
        for (int i = 0; i < xmlrpcNoSend.size(); i++) {
            String fieldName = (String) xmlrpcNoSend.get(i);
            hashtable.remove(fieldName);
        }

        // Expand any MapObject values to be Hashtables
        // Where specified, use the appropriate Id Field instead of the Hashtable
        for (Iterator iterator = hashtable.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof MapObject) {

                MapObject mapObject = (MapObject) value;
                hashtable.put(key, toHashtableOrId(mapObject));

            } else if (value instanceof List && !(value instanceof Vector)) {

                List objects = (List) value;
                Vector vector = new Vector();

                for (int i = 0; i < objects.size(); i++) {
                    MapObject mapObject = (MapObject) objects.get(i);
                    vector.add(toHashtableOrId(mapObject));
                }

                hashtable.put(key, vector);
            }
        }
        return hashtable;
    }

    private Object toHashtableOrId(MapObject mapObject) {
        Hashtable child = mapObject.toHashtable();
        Object object;
        String idField = (String) xmlrpcRefs.get(mapObject.getClass());
        if (idField != null){
            object = child.get(idField);
        } else {
            object = child;
        }
        return object;
    }

}