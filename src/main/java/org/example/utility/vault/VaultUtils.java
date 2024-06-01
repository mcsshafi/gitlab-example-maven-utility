package org.example.utility.vault;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VaultUtils {
    public final static String CONFIG_SECTION_APP_CACHING_CONFIG = "app.caching.config";
    public final static String CONFIG_SECTION_APP_DB_CONFIG = "app.db.config";
    public final static String CONFIG_SECTION_APP_LOGGING_CONFIG = "app.logging.config";
    public final static String CONFIG_SECTION_APP_PAYMENT_GATEWAY_CONFIG = "app.payment.gateway.config";
    public final static String CONFIG_SECTION_APP_CONFIG = "app.config";
    public final static String CONFIG_SECTION_APP_MESSAGING_CONFIG = "app.messaging.config";

    public final static VaultUtils instance = new VaultUtils();

    private Map<String, Map<String, String>> _data;

    public VaultUtils() {
        this.init();
    }

    private void init() {
        this.load();
    }
    private boolean load() {
        this._data = new HashMap<String, Map<String,String>>();
        try {
            List<String> keys = VaultDataManager.getKeys();
            if(keys != null) {
                for(String key : keys) {
                    try {
                        String refinedKey = key;
                        if(refinedKey.contains("/")) {
                            refinedKey = refinedKey.substring(refinedKey.lastIndexOf('/') + 1);
                        }
                        HashMap<String, String> subKeys = VaultDataManager.getSubKeys(key);
                        if(!this._data.containsKey(refinedKey)) {
                            this._data.put(refinedKey, subKeys);
                        }
                        else {
                            for(String subKey : subKeys.keySet()) {
                                this._data.get(refinedKey).put(subKey, subKeys.get(subKey));
                            }
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return (this._data != null && this._data.size() > 0);
    }

    public VaultDataItem get(String key) {
        return get(CONFIG_SECTION_APP_CONFIG, key);
    }
    public VaultDataItem get(String configSection, String key) {
        String value = "";
        try {
            value = this._data.get(configSection).get(key);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return new VaultDataItem(value);
    }
}
