package nz.com.neps.xero_integration.model;

import com.google.api.client.auth.oauth.OAuthSigner;

public interface SignerFactory {
	OAuthSigner createSigner(String tokenSharedSecret);
}
