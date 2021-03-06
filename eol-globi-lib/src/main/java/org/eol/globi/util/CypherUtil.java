package org.eol.globi.util;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class CypherUtil {
    public static String executeCypherQuery(CypherQuery query) throws IOException {
        HttpPost httpPost = new HttpPost("http://api.globalbioticinteractions.org:7474/db/data/cypher");
        HttpClient.addJsonHeaders(httpPost);
        httpPost.setEntity(new StringEntity(wrapQuery(query)));
        BasicResponseHandler responseHandler = new BasicResponseHandler();
        return HttpUtil.createHttpClient().execute(httpPost, responseHandler);
    }

    private static String wrapQuery(CypherQuery cypherQuery) {
        String query = "{\"query\":\"";
        query += cypherQuery.getQuery();
        query += " \", \"params\": {" + buildJSONParamList(cypherQuery.getParams()) + " } }";
        return query;
    }

    private static String buildJSONParamList(Map<String, String> paramMap) {
        StringBuilder builder = new StringBuilder();
        if (paramMap != null) {
            populateParams(paramMap, builder);
        }
        return builder.toString();
    }

    private static void populateParams(Map<String, String> paramMap, StringBuilder builder) {
        Iterator<Map.Entry<String, String>> iterator = paramMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            String jsonParam = "\"" + param.getKey() + "\" : \"" + param.getValue() + "\"";
            builder.append(jsonParam);
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
    }
}
