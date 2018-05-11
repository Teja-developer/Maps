package com.notes.map.map;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Teja on 3/11/2018.
 */

public class DirectionsParser {

    /**
     * Returns a list of lists containing latitude and longitude from a JSONObject
     */
    public static String[] dir;
    public static int[] dist;
    public static ArrayList<LatLng> latLngs;
    
    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            // Loop for all routes
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();
                Log.v("Map", jLegs.toString());

                //Loop for all legs
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    Log.v("Map", jSteps.toString());

                    //Loop for all steps
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List list = decodePolyline(polyline);

                        //Bringing the instructions(directions) from the json file!
                        String string = (String) ((JSONObject) jSteps.get(k)).get("html_instructions");

//  Trail 1:              try {
//                            // Convert from Unicode to UTF-8
//                            byte[] utf8 = string.getBytes("UTF-8");
//
//                            // Convert from UTF-8 to Unicode
//                            string = new String(utf8, "UTF-8");
//
//                        } catch (UnsupportedEncodingException e) {}
                        string.replaceAll("\\/<.*?>","");
                        string = string.replace("<b>","");
                        string = string.replace("</b>","");

                        //Log.i("Maps",""+string);
                        dir[k] = string; //Assigning it to the string array

                        dist[k] = ((JSONObject) jSteps.get(k)).getJSONObject("distance").getInt("value");

                        //Loop for all points
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            double lat = ((LatLng) list.get(l)).latitude;
                            double lon = ((LatLng) list.get(l)).longitude;

                            LatLng temp = new LatLng(lat,lon);
                            hm.put("lon", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                            if(l==list.size()-1)
                                latLngs.add(temp);
                        }
                        Log.i("Map",""+path+"(DirectionParser)\n");
                    }
                    routes.add(path);
                    for(String s: dir)
                        Log.i("Map","(DirectionParser)"+s);
                    for(int l: dist)
                        Log.i("Map",""+l);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

        return routes;
    }

    /**
     * Method to decode polyline
     * Source : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List decodePolyline(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
