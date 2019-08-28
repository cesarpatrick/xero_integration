package nz.com.neps.xero_integration.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import nz.com.neps.xero_integration.model.Config;
import nz.com.neps.xero_integration.model.JsonConfig;
import nz.com.neps.xero_integration.model.OAuthAuthorizeToken;
import nz.com.neps.xero_integration.model.OAuthRequestToken;
import nz.com.neps.xero_integration.model.TokenStorage;
import nz.com.neps.xero_integration.model.XeroApiException;

@RestController
@RequestMapping(value = "/api/controller")
public class IntegrationController {

	
	@RequestMapping(value = "/getToken", method = RequestMethod.GET)
	public void getToken(HttpServletResponse response) {
				
		try {
			Config config = JsonConfig.getInstance();			
			OAuthRequestToken requestToken = new OAuthRequestToken(config);
			requestToken.execute();
			
			TokenStorage storage = new TokenStorage();
			storage.save(response, requestToken.getAll());
			
			OAuthAuthorizeToken authToken = new OAuthAuthorizeToken(config, requestToken.getTempToken());
			response.sendRedirect(authToken.getAuthUrl());
			
		} catch (XeroApiException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	
	
}

