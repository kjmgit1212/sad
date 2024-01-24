package examples.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;

import examples.util.AuthenticationUtil;
import examples.util.Constants;
import examples.util.PropertiesManager;
import examples.util.ResourceBuilder;
import examples.util.RestClientException;

/**
 * Sample REST Client for people search API : 
 * Searches for all the users in ISIM and returns there full name and last name.
 *
 */
public class SearchPeople
{
    public static final String peopleService = "people";
    
    public static void main(String [] args) throws RestClientException{
        
        // Step 1 : Authenticate with the ISIM server
        AuthenticationUtil authUtil = new AuthenticationUtil();
        List<String> authTokens  = authUtil.authenticate("itim manager", "secret");
        
        // Step 2 : form the people search REST URL
        String restURL = PropertiesManager.getPropertiesManager().getPropertyValue(Constants.BASE_URL) + "/" + peopleService;
        
        //Step 3 v1 : Define the queryParameters for the REST API
        String attributes = "cn,sn";
        Map<String,String> queryParameters = new HashMap<String, String>();
        queryParameters.put("attributes", attributes);
        
        // Step 4 Build Resource object with all the above information
        Resource r = ResourceBuilder.buildResource(restURL, queryParameters, authTokens);
        
        //Step 5 Call the REST API and print the response
        ClientResponse response = r.get();
        
        // Step 6 Process the response
        int responseCode = response.getStatusCode();
        if(responseCode != 404)
            System.out.println(response.getEntity(String.class));
        else
            throw new RestClientException("Invalid Base URL");
        
    }
}