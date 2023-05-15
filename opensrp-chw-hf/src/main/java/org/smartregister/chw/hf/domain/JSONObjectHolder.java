package org.smartregister.chw.hf.domain;

import org.json.JSONObject;

/**
 * JSONObjectHolder is a Singleton class that holds a large JSONObject.
 * This class provides a way to pass a large JSONObject between Activities
 * without using Intent extras or temporary files.
 *
 * Usage:
 * To set the large JSONObject:
 * JSONObjectHolder.getInstance().setLargeJSONObject(largeJSONObject);
 *
 * To get the large JSONObject:
 * JSONObject largeJSONObject = JSONObjectHolder.getInstance().getLargeJSONObject();
 */
public class JSONObjectHolder {
    private static JSONObjectHolder instance;
    private JSONObject largeJSONObject;

    private JSONObjectHolder() {
    }

    /**
     * Returns the JSONObjectHolder instance, creating one if it doesn't exist.
     * This method is synchronized to ensure that only one instance is created.
     *
     * @return JSONObjectHolder instance
     */
    public static synchronized JSONObjectHolder getInstance() {
        if (instance == null) {
            instance = new JSONObjectHolder();
        }
        return instance;
    }

    /**
     * Returns the large JSONObject held by this JSONObjectHolder.
     *
     * @return large JSONObject
     */
    public JSONObject getLargeJSONObject() {
        return largeJSONObject;
    }

    /**
     * Sets the large JSONObject to be held by this JSONObjectHolder.
     *
     * @param largeJSONObject JSONObject to be held
     */
    public void setLargeJSONObject(JSONObject largeJSONObject) {
        this.largeJSONObject = largeJSONObject;
    }
}