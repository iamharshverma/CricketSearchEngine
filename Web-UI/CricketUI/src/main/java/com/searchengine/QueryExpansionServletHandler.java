package com.searchengine;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * Servlet implementation class com.searchengine.ServletHandler
 */
@WebServlet("/com.searchengine.QueryExpansionServletHandler")
public class QueryExpansionServletHandler extends HttpServlet {
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		
		String query = request.getParameter("query");

		try {
			
			JSONObject queryExpJSONObj = APICallHandler.queryExpCall(query);
			
			JSONObject jsonObject = new JSONObject();
			
			jsonObject.put("qexp", queryExpJSONObj);
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(jsonObject.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
