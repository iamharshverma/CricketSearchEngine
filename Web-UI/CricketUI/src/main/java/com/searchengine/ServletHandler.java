package com.searchengine;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet implementation class com.searchengine.ServletHandler
 */
@WebServlet("/com.searchengine.ServletHandler")
public class ServletHandler extends HttpServlet {
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		
		String query = request.getParameter("query");

		try {
			
			/*JSONParser parser = new JSONParser();
			parser.doGetCluster("{}");*/
			
			JSONArray googleJSONObj = APICallHandler.googleCall(query);
			JSONArray bingJSONObj = APICallHandler.bingCall(query);
			JSONArray solrJSONObj = APICallHandler.solrCall(query, 15);
			JSONArray hitsJSONObj = APICallHandler.hitsCall(solrJSONObj);
						
			
			JSONObject jsonObject = new JSONObject();

			jsonObject.put("google", googleJSONObj);
			jsonObject.put("bing", bingJSONObj);
			jsonObject.put("solr", solrJSONObj);
			jsonObject.put("hits", hitsJSONObj);
			
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(jsonObject.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
