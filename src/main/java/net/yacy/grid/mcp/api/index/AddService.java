/**
 *  AddService
 *  Copyright 04.03.2018 by Michael Peter Christen, @0rb1t3r
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package net.yacy.grid.mcp.api.index;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import net.yacy.grid.http.APIHandler;
import net.yacy.grid.http.ObjectAPIHandler;
import net.yacy.grid.http.Query;
import net.yacy.grid.http.ServiceResponse;
import net.yacy.grid.io.index.Index;
import net.yacy.grid.io.index.IndexFactory;
import net.yacy.grid.mcp.Data;

public class AddService extends ObjectAPIHandler implements APIHandler {
    
    private static final long serialVersionUID = 8578334562349879L;
    public static final String NAME = "add";
    private final static byte[] EMPTY_OBJECT = new byte[0];
    
    @Override
    public String getAPIPath() {
        return "/yacy/grid/mcp/index/" + NAME + ".json";
    }
    
    @Override
    public ServiceResponse serviceImpl(Query call, HttpServletResponse response) {
        //String indexName, String typeName, final String id, JSONObject object
        String indexName = call.get("index", "");
        String typeName = call.get("type", "_doc");
        String id = call.get("id", "");
        byte[] object = call.get("object", EMPTY_OBJECT); // this contains the JSON object
        JSONObject json = new JSONObject(true);
        if (indexName.length() > 0 && typeName.length() > 0 && id.length() > 0 && object.length > 0) {
            try {
                Index index = Data.gridIndex.getElasticIndex();
                JSONObject payload = new JSONObject(new JSONTokener(new String(object, StandardCharsets.UTF_8)));
                IndexFactory factory = index.add(indexName, typeName, id, payload);
                String url = factory.getConnectionURL();
                json.put(ObjectAPIHandler.SUCCESS_KEY, true);
                if (url != null) json.put(ObjectAPIHandler.SERVICE_KEY, url);
            } catch (IOException | JSONException e) {
                json.put(ObjectAPIHandler.SUCCESS_KEY, false);
                json.put(ObjectAPIHandler.COMMENT_KEY, e.getMessage());
            }
        } else {
            json.put(ObjectAPIHandler.SUCCESS_KEY, false);
            json.put(ObjectAPIHandler.COMMENT_KEY, "the request must contain an index, type, id and json object");
        }
        return new ServiceResponse(json);
    }
}
