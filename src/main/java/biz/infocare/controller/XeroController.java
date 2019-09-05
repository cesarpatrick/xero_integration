package biz.infocare.controller;

import com.xero.api.*;
import com.xero.api.client.AccountingApi;
import com.xero.example.TokenStorage;
import com.xero.models.accounting.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.threeten.bp.OffsetDateTime;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping(value = "/api/controller")
public class XeroController {

	private Config config = JsonConfig.getInstance();
	private TokenStorage storage = new TokenStorage();
	private OAuthAccessToken accessToken = new OAuthAccessToken(config);
	private boolean summarizeErrors = false;
	private AccountingApi accountingApi = new AccountingApi(config);

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

			// retrieve OAuth verifier code from callback URL param
			String verifier = request.getParameter("oauth_verifier");

			// Swap your temp token for 30 oauth token
			accessToken = new OAuthAccessToken(config);
			accessToken.build(verifier, storage.get(request, "tempToken"),
					storage.get(request, "tempTokenSecret")).execute();

			System.out.printf(accessToken.getToken());
			System.out.printf(accessToken.getTokenSecret());

			if (!accessToken.isSuccess()) {
				storage.clear(response);
			} else {
				// DEMONSTRATION ONLY - Store in Cookie - you can extend TokenStorage
				// and implement the save() method for your database
				storage.save(response, accessToken.getAll());
			}

		} catch (XeroApiException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/createNewInvoice", method = RequestMethod.GET)
	public String createNewInvoice(Invoice invoice) throws ServletException {

		try {
			ApiClient apiClientForAccounting = new ApiClient(config.getApiUrl(),null,null,null);
			accountingApi.setApiClient(apiClientForAccounting);
			accountingApi.setOAuthToken(accessToken.getToken(), accessToken.getTokenSecret());

			Invoices invoices = new Invoices();
			invoices.addInvoicesItem(invoice);

			Invoices newInvoice = accountingApi.createInvoice(invoices, summarizeErrors);

			//TODO what next? save in DB?

			System.out.println(newInvoice.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "index";
	}

	@RequestMapping(value = "/createNewCreditNote", method = RequestMethod.GET)
	public void createNewCreditNote(CreditNote creditNote) {
		try {

			ApiClient apiClientForAccounting = new ApiClient(config.getApiUrl(),null,null,null);
			accountingApi.setApiClient(apiClientForAccounting);
			accountingApi.setOAuthToken(accessToken.getToken(), accessToken.getTokenSecret());

			CreditNotes creditNotes = new CreditNotes();
			creditNotes.addCreditNotesItem(creditNote);
			CreditNotes newCreditNote = accountingApi.createCreditNote(summarizeErrors, creditNotes);
			UUID newCreditNoteId = newCreditNote.getCreditNotes().get(0).getCreditNoteID();

			// TODO what next? save in DB?

			System.out.println(newCreditNote.toString());
			System.out.println(newCreditNoteId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/getPayments", method = RequestMethod.GET)
	public void getPayments(HttpServletResponse response) {
		try {
			ApiClient apiClientForAccounting = new ApiClient(config.getApiUrl(),null,null,null);
			accountingApi.setApiClient(apiClientForAccounting);
			accountingApi.setOAuthToken(accessToken.getToken(), accessToken.getTokenSecret());

			Payments payments = accountingApi.getPayments(null, null, null);

			// TODO what next? save in DB?

			System.out.println(payments.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/getPaymentDateRange", method = RequestMethod.GET)
	public void getPaymentDateRange(String where) {
		try {
			ApiClient apiClientForAccounting = new ApiClient(config.getApiUrl(),null,null,null);
			accountingApi.setApiClient(apiClientForAccounting);
			accountingApi.setOAuthToken(accessToken.getToken(), accessToken.getTokenSecret());

//			String where = "Date >= DateTime(2019, 09, 02) && Date <= DateTime(2019, 09, 02)";
			Payments payments = accountingApi.getPayments(null, where, null);

			// TODO what next? save in DB?

			System.out.println(payments.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}