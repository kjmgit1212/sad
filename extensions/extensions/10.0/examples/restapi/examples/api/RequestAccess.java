package examples.api;

import java.util.List;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.json4j.JSONObject;

import examples.util.AuthenticationUtil;
import examples.util.Constants;
import examples.util.PropertiesManager;
import examples.util.ResourceBuilder;
import examples.util.RestClientException;
import examples.util.ResourceIdentifierUtil;

/**
 * Sample REST Client for requesting an access :
 * Request an access called "GSA" for user "Judith Hall". Creates an account "jhall" as part of request. 
 */
public class RequestAccess
{

    public static void main (String arg[]) throws RestClientException{
        // Step 1 : Authenticate with the ISIM server
        AuthenticationUtil authUtil = new AuthenticationUtil();
        List<String> authTokens  = authUtil.authenticate("itim manager", "secret");
                
        // Step 2 : form the people search REST URL
        String restURL = PropertiesManager.getPropertiesManager().getPropertyValue(Constants.BASE_URL) + "/access/assignments";
        
        // Step 3 : Create request body
        JSONObject requestBody = new JSONObject();
        try {
            // define attributes of the account which will be created as part of request
            JSONObject attributes = new JSONObject();
            // specify the account name
            attributes.put("eruid", "jhall");
            
            // create obligation with an id as "obligation1"
            JSONObject obligation = new JSONObject();
            obligation.put("id", "obligation1");
            // since a new account will be created as part of request, the action is CREATE_ACCOUNT
            obligation.put("action", "CREATE_ACCOUNT");
            obligation.put("_attributes", attributes);
            
            // create a reference of the above created obligation.
            JSONObject refObligation = new JSONObject();
            refObligation.put("$ref","obligation1");
            
            // specify the URI of the requested access GSA.
            JSONObject accessHref = new JSONObject();
            accessHref.put("href", ResourceIdentifierUtil.getAccessdentifier(authTokens, "GSA"));
            JSONObject access = new JSONObject();
            access.put("access", accessHref);
            
            JSONObject assignments = new JSONObject();
            assignments.put("_links", access);
            assignments.append("obligations", refObligation);
            
            JSONObject addOperation = new JSONObject();
            addOperation.append("assignments", assignments);
            addOperation.append("obligations",obligation);
            
            // Specify the person for whom access is being requested.
            JSONObject requesteeHref = new JSONObject();
            requesteeHref.put("href", ResourceIdentifierUtil.getPersonIdentifier(authTokens, "Judith%20Hall"));
            JSONObject _links = new JSONObject();
            _links.put("self", requesteeHref);
            
            JSONObject requstee = new JSONObject();
            requstee.put("_links", _links);
            requstee.put("add",addOperation);
            
            JSONObject request = new JSONObject();
            request.put("requestee", requstee);
            
            
            // add justification
            requestBody.put("justification", "Requesting access due to department change");
            requestBody.append("requests", request);
            
            Resource r = ResourceBuilder.buildResource(restURL, authTokens);
            
            // Set the CSRF token as its a POST request. This token is needed for HTTP PUT, POST, and DELETE calls.
            r.header("CSRFToken",authUtil.getCsrftoken());
            
            r.header("X-HTTP-Method-Override","submit-in-batch");
            r.contentType("application/json");
            r.accept("application/vnd.ibm.isim-v1+json");
            ClientResponse cr = r.post(requestBody.toString());
            System.out.println(cr.getEntity(String.class));
        } catch (Exception e ){
            e.printStackTrace();
        }
        
    }
}
