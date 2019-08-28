package nz.com.neps.xero_integration.jaxb;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.xero.api.XeroClientException;

/**
 * Marshall and unmarshall from schema derived class and/or java
 * to schema (JAXB-annotated) mapped classes found in com.xero.model
 * Will throw {@link XeroClientException} if {@link JAXBContext}
 * cannot create the marshaller or unmarshaller
 */
public class XeroJAXBMarshaller {
    static final JAXBContext context = initContext();

    private static JAXBContext initContext() {
        try { 
            return JAXBContext.newInstance("com.xero.model", XeroJAXBMarshaller.class.getClassLoader());
        } catch (Exception e) {
            throw new XeroClientException(e.getMessage(), e);
        }        
    }

    public XeroJAXBMarshaller() {
    }

    
    public String marshall(JAXBElement<?> object) {
        try {
            StringWriter writer = new StringWriter();
            context.createMarshaller().marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new IllegalStateException("Error marshalling request object " + object.getClass(), e);
        }
    }

    public <T> T unmarshall(String responseBody, Class<T> clazz) throws UnsupportedEncodingException {
        try {
            Source source = new StreamSource(new StringReader(responseBody));
            return context.createUnmarshaller().unmarshal(source, clazz).getValue();
        } catch (JAXBException e) {
            throw new IllegalStateException("Error unmarshalling response: " + responseBody, e);
        }
    }

}
