package biz.infocare.service;

import com.xero.api.ApiClient;
import com.xero.api.Config;
import com.xero.api.JsonConfig;
import com.xero.api.OAuthAccessToken;
import com.xero.api.client.AccountingApi;
import com.xero.example.TokenStorage;
import com.xero.models.accounting.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.OffsetDateTime;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class XeroService {

    private AccountingApi accountingApi;

    public XeroService() {
        Config config = JsonConfig.getInstance();
        OAuthAccessToken accessToken = new OAuthAccessToken(config);
        ApiClient apiClientForAccounting = new ApiClient(config.getApiUrl(),null,null,null);
        accountingApi = new AccountingApi(config);
        accountingApi.setApiClient(apiClientForAccounting);
        accountingApi.setOAuthToken(accessToken.getToken(), accessToken.getTokenSecret());
    }

    /**
     * Get Accounts
     *
     * @param where
     * @param order
     * @return
     * @throws IOException
     */
    public Accounts getAccounts(String where, String order) throws IOException {
        return accountingApi.getAccounts(null, where, order);
    }

    /**
     * Sends provided Invoice information to Xero.
     * I.E. we should have an Invoice object which Infocare can generate, and your class sends it to Xero.
     * @param invoices
     * @throws ServletException
     */
    public void createNewInvoices(Invoices invoices) throws IOException {
        try {
            Invoices newInvoice = accountingApi.createInvoice(invoices, false);
            // TODO what to do with response? save in DB?
            System.out.println(newInvoice.toString());
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Sends provided Invoice information to Xero.
     * I.E. we should have an Invoice object which Infocare can generate, and your class sends it to Xero.
     * @param invoice
     * @throws ServletException
     */
    public void createNewInvoice(Invoice invoice) throws IOException {
        try {
            Invoices invoices = new Invoices();
            invoices.addInvoicesItem(invoice);
            createNewInvoices(invoices);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
