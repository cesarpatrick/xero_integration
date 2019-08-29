package nz.com.neps.xero_integration.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.xero.api.OAuthAccessToken;

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

	
	@RequestMapping(value = "/callBackToken", method = RequestMethod.GET)
	public void callBackToken(HttpServletResponse response, HttpServletRequest request) throws ServletException {
				
		try {
			Config config = JsonConfig.getInstance();
			
			// DEMONSTRATION ONLY - retrieve TempToken from Cookie
			TokenStorage storage = new TokenStorage();

			// retrieve OAuth verifier code from callback URL param
			String verifier = request.getParameter("oauth_verifier");

			// Swap your temp token for 30 oauth token
			OAuthAccessToken accessToken = new OAuthAccessToken((com.xero.api.Config) config);
			accessToken.build(verifier,storage.get(request,"tempToken"),storage.get(request,"tempTokenSecret")).execute();
			if(!accessToken.isSuccess())
			{
				storage.clear(response);
				request.getRequestDispatcher("index.html").forward(request, response);
			}
			else 
			{
				// DEMONSTRATION ONLY - Store in Cookie - you can extend TokenStorage
				// and implement the save() method for your database
				storage.save(response,accessToken.getAll());			
				request.getRequestDispatcher("callback.html").forward(request, response);			
			}
			
		} catch (XeroApiException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
}

