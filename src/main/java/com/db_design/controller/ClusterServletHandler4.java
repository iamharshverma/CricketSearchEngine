package com.db_design.controller;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet implementation class com.searchengine.ServletHandler
 */
@WebServlet("//com.db_design.controller.ClusterServletHandler4")
public class ClusterServletHandler4 extends HttpServlet {
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		
		String query = request.getParameter("query");

		try {
			
			JSONArray clusterJSONObj = APICallHandler.clusterCall4(query);
			
			JSONObject jsonObject = new JSONObject();
			
			jsonObject.put("clust", clusterJSONObj);
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(jsonObject.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
