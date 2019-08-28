package nz.com.neps.xero_integration.model;
import java.util.HashMap;
import java.util.Map;

import com.xero.model.ApiException;


public class XeroApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final int responseCode;
    private String message;
    private Map<String, String> messageMap = new HashMap<String, String>();
    private ApiException apiException;
    private Error error;

    public XeroApiException(int responseCode) {
        super(responseCode + " response.");
        this.responseCode = responseCode;
    }

    public XeroApiException(int responseCode, String message) {
        super(responseCode + " response: " + message);
        this.responseCode = responseCode;
        this.message = message;
    }

    public XeroApiException(int responseCode, Map<String, String> map) {
        super(responseCode + "response");
        this.responseCode = responseCode;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (this.message == null) {
                this.message = entry.getValue() + " - ";
            } else {
                this.message = this.message + entry.getValue() + " ";
            }
        }
        this.messageMap = map;
    }

    // response type XML uses ApiException class to unmarshall
    public XeroApiException(int responseCode, String message, ApiException apiException) {
        super(responseCode + " response: " + message);
        this.responseCode = responseCode;
        this.message = message;
        this.apiException = apiException;
    }
    
    // response type JSON uses Error class
    public XeroApiException(int responseCode, String message, Error error) {
        super(responseCode + " response: " + message);
        this.responseCode = responseCode;
        this.message = message;
        this.error = error;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getMessages() {
        return messageMap;
    }

    public ApiException getApiException() {
        return apiException;
    }
    
    public Error getError() {
        return error;
    }

}
