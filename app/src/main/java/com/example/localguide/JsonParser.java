package com.example.localguide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonParser {
    private HashMap<String, String > parseJsonObject(JSONObject object){
        //Initialize hash map
        HashMap<String, String> dataList= new HashMap<>();

        try {
            //get name from the object
            String name= object.getString("name");
            //get latitude from object
            String latitude=object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lat");
            //get longtitude from object
            String longtitude=object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lng");

            //put all the vaules in the hash map
            dataList.put("name",name);
            dataList.put("lat",latitude);
            dataList.put("lng",longtitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  dataList;
    }

    private List<HashMap<String,String>> parseJsonArray(JSONArray jsonArray){
        //initialize hash map
        List<HashMap<String, String >> dataList=new ArrayList<>();
        for (int i=0; i<jsonArray.length(); i++){

            try {
                //initialize hash map
                HashMap<String, String > data=parseJsonObject((JSONObject) jsonArray.get(i));
                //add data in hash map
                dataList.add(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return  dataList;
    }

    public List<HashMap<String, String >> parseResult(JSONObject object){
        //initialize json array
        JSONArray jsonArray=null;
        try {
            jsonArray=object.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return parseJsonArray(jsonArray);
    }
}
