package com.function;

import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    @FunctionName("HttpTrigger-Java")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            String date = request.getQueryParameters().get("date");

            // Daniel' s API Key
            URL url = new URL("https://api.nasa.gov/neo/rest/v1/feed?start_date=" + date + "&end_date=" + date + "&api_key=cRy6FaHeTsKlD36EVIH61isQM1obLhEjSmpXubx9");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            context.getLogger().info("GET Response Code :: " + responseCode);

            BufferedReader in;
            if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
            in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            }
            else {
            in = new BufferedReader(
                new InputStreamReader(con.getErrorStream()));
            }

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(in);

            JSONArray neo = (JSONArray) ((JSONObject) jsonObject.get("near_earth_objects")).get(date);

            Number max_diameter = 0;
            for(Object o : neo)
            {
                Number diameter = (Number) ((JSONObject) ((JSONObject) ((JSONObject) o).get("estimated_diameter")).get("meters")).get("estimated_diameter_max");
                if(diameter.doubleValue() > max_diameter.doubleValue())
                {
                    max_diameter = diameter;
                }
            }

            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return request.createResponseBuilder(HttpStatus.OK).body("The largest diameter object on " + date + " is " + max_diameter).build();
        }
        catch (Exception e)
        {
            context.getLogger().info(e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(e.getMessage()).build();
        }
    }
}
