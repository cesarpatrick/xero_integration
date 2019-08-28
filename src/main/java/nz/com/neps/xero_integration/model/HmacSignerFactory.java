package nz.com.neps.xero_integration.model;
import com.google.api.client.auth.oauth.OAuthHmacSigner;

public class HmacSignerFactory implements SignerFactory {

  private String consumerSecret;

  public HmacSignerFactory(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  @Override
  public OAuthHmacSigner createSigner(String tokenSecret) {
    OAuthHmacSigner signer = new OAuthHmacSigner();
    signer.tokenSharedSecret = tokenSecret;
    signer.clientSharedSecret = consumerSecret;
    return signer;
  }
}
