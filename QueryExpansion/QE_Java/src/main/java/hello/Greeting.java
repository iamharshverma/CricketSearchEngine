package hello;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.*;
import java.io.*;

public class Greeting {
    private  long id;
    private  String query;
    private String expanded_query;
    private String qe;
    private String content;

    public Greeting(long id, String query,String expanded_query,String qe) {
        this.id = id;
        this.qe = qe;
        this.query = query;
        this.expanded_query = expanded_query;
        this.content = "";
    }

    public long getId() {
        return id;
    }

    public String getQuery() {
        String[] query_to_solr = this.query.split(" ");
        String myUrl = "http://localhost:8983/solr/nutch/select?q=";
        for(int i=0;i<query_to_solr.length;i++){
            query_to_solr[i]="text:\""+query_to_solr[i]+"\"";
            myUrl=myUrl+query_to_solr[i];
        }
        try{
            URL url = new URL(myUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int status = con.getResponseCode();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            this.content=content.toString();
            ObjectMapper objectMapper = new ObjectMapper();
            MyResponse myres = objectMapper.readValue(content.toString(),MyResponse.class);
            switch (this.qe){
                case "association":
                    AssociationCluster asc = new AssociationCluster(this.query,myres);
                    this.expanded_query=asc.getExpandedQuery();
                    break;
                case "metric":
                    MetricCluster mt = new MetricCluster(this.query,myres);
                    this.expanded_query=mt.getExpandedQuery();
                    break;
                case "scalar":
                    ScalarCluster sc = new ScalarCluster(this.query,myres);
                    this.expanded_query=sc.getExpandedQuery();
                    break;
                case "rocchio":
                    RocchioCluster rc = new RocchioCluster(this.query,myres);
                    this.expanded_query=rc.getExpandedQuery();
                    break;
                default:
                    AssociationCluster asc1 = new AssociationCluster(this.query,myres);
                    this.expanded_query=asc1.getExpandedQuery();

            }
            return query;
        }
        catch (Exception e)
        {
            return e.getMessage();
        }

    }
    public String getExpanded_query(){
        return expanded_query;
    }
//
//    public String getContent(){
//        return this.content;
//    }

}
