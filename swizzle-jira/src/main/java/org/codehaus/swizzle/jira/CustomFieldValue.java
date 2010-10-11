package org.codehaus.swizzle.jira;

import java.util.List;
import java.util.Map;

public class CustomFieldValue extends MapObject {
    /**
     * 
     */
    public CustomFieldValue() {
        super();
    }

    /**
     * @param data
     */
    public CustomFieldValue(Map data) {
        super(data);
    }

    /**
     * @return the customfieldId
     */
    public String getCustomfieldId() {
        return getString("customfieldId");
    }

    /**
     * @param customfieldId
     *            the customfieldId to set
     */
    public void setCustomfieldId(String customfieldId) {
        setString("customfieldId", customfieldId);
    }

    /**
     * @return the key
     */
    public String getKey() {
        return getString("key");
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        setString("key", key);
    }

    //
    // I'm not sure where the assumption is that custom fields contain a list of values, but I'm finding that in Greenhopper in
    // JIRA the Rank and Points custom fields have scalar String values. So if you use the getValues() method below you get a
    // ClassCastException. So I've added this method to grab a simple string value.
    //
    public String getValue() {
    	return getString("values");
    }
    
    /**
     * @return the values
     */
    public List getValues() {
        return getList("values");
    }

    /**
     * @param values
     *            the values to set
     */
    public void setValues(List values) {
        setList("values", values);
    }

    public String toString() {
        return (getCustomfieldId() != null) ? getCustomfieldId() : getKey();
    }
}
