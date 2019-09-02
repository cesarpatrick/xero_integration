package xero_integration;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.threeten.bp.OffsetDateTime;

import com.xero.api.Config;
import com.xero.api.JsonConfig;
import com.xero.api.OAuthAccessToken;
import com.xero.api.OAuthAuthorizeToken;
import com.xero.api.OAuthRequestToken;
import com.xero.api.XeroApiException;
import com.xero.api.client.AccountingApi;
import com.xero.example.TokenStorage;

public class TestXero {
	
	OffsetDateTime ifModifiedSince = null;
	Config config = JsonConfig.getInstance();
	TokenStorage storage = new TokenStorage();
	OAuthAccessToken accessToken = new OAuthAccessToken(config);
	String order = null;
	boolean includeArchived = false;
	String ids = null;
	String where = null;
	
	AccountingApi accountingApi = new AccountingApi(config);
	
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Before
    public void setUp() {     
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }
	
	@Test
	public void getToken() {

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
