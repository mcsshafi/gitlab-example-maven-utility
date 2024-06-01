package org.example.utility.vault;

import com.christ.utility.lib.AppProperties;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class VaultDataManager {
    private final static String VaultServerUrl;
    private final static String VaultSecurityKey;
    private final static String VaultKeyStoreName;
    private final static String VaultProjectName;

    static {
        VaultSecurityKey = AppProperties.get("vault.security.key");
        VaultKeyStoreName = AppProperties.get("vault.keystore.name");
        VaultProjectName = AppProperties.get("vault.project.name");
        /*If URL is ending with / then remove the / with URL*/
        String url = AppProperties.get("vault.server.url");
        if(url != null && url.isEmpty() == false) {
            url = url.trim();
            if(url.endsWith("/") == true) {
                url = url.substring(0, url.length() - 1);
            }
        }
        VaultServerUrl = url;
    }

    public static List<String> getKeys() {
        List<String> keys = new ArrayList<String>();
        try {
            keys.addAll(getCommonKeys());
            keys.addAll(getProjectKeys());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return keys;
    }

    public static List<String> getProjectKeys() {
        return VaultDataManager.getKeys(VaultKeyStoreName + "/applications/" + VaultProjectName);
    }

    public static List<String> getCommonKeys() {
        return VaultDataManager.getKeys(VaultKeyStoreName);
    }

    public static List<String> getKeys(String path) {
        List<String> keys = new ArrayList<String>();
        OkHttpClient client = new OkHttpClient();
        String url = String.format("%s/metadata/%s/?list=true", VaultServerUrl, path);
        try {
            //System.out.println("Vault URL is---"+ url);
            Request request = new Request.Builder().url(url).get().addHeader("X-Vault-Token", VaultSecurityKey).build();
            Response response= client.newCall(request).execute();
            if(response != null) {
                String json = response.body().string();
                if(json != null && json.trim().length() > 0) {
                    JSONObject data = (JSONObject) new JSONParser().parse(json);
                    if(data != null) {
                        data = (JSONObject) data.get("data");
                        JSONArray items = (JSONArray) data.get("keys");
                        for(Object item : items) {
                            if(!item.toString().contains("/")) {
                                keys.add(path + "/" + item.toString());
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return keys;
    }

    public static HashMap<String, String> getSubKeys(String key) {
        HashMap<String, String> items = new HashMap<String, String>();
        try {
            //String url = String.format("%s/data/%s/%s/%s", VaultServerUrl, VaultKeyStoreName, VaultServerUrlProjectPath, key);
            String url = String.format("%s/data/%s", VaultServerUrl, key);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).get().addHeader("X-Vault-Token", VaultSecurityKey).build();
            try {
                Response response = client.newCall(request).execute();
                if(response != null) {
                    String json = response.body().string();
                    if(json != null && json.trim().length() > 0) {
                        JSONObject data = (JSONObject) new JSONParser().parse(json);
                        if(data != null) {
                            data = (JSONObject) data.get("data");
                            data = (JSONObject) data.get("data");
                            Iterator<?> keys = data.keySet().iterator();
                            while(keys.hasNext() ) {
                                String subKey = (String)keys.next();
                                items.put(subKey, data.get(subKey).toString());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}
