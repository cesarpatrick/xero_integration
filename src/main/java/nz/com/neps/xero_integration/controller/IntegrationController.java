package nz.com.neps.xero_integration.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.OffsetDateTime;

import com.xero.api.ApiClient;
import com.xero.api.Config;
import com.xero.api.JsonConfig;
import com.xero.api.OAuthAccessToken;
import com.xero.api.OAuthAuthorizeToken;
import com.xero.api.OAuthRequestToken;
import com.xero.api.XeroApiException;
import com.xero.api.client.AccountingApi;
import com.xero.example.TokenStorage;
import com.xero.models.accounting.Accounts;
import com.xero.models.accounting.Contact;
import com.xero.models.accounting.Contacts;
import com.xero.models.accounting.CreditNote;
import com.xero.models.accounting.CreditNotes;
import com.xero.models.accounting.Invoice;
import com.xero.models.accounting.Invoices;
import com.xero.models.accounting.LineItem;
import com.xero.models.accounting.Payments;

@Controller
@RequestMapping(value = "/api/controller")
public class IntegrationController {
	
	OffsetDateTime ifModifiedSince = null;
	Config config = JsonConfig.getInstance();
	TokenStorage storage = new TokenStorage();
	OAuthAccessToken accessToken = new OAuthAccessToken(config);
	String order = null;
	boolean includeArchived = false;
	String ids = null;
	String where = null;
	boolean summarizeErrors = false;
	Invoice myInvoice = new Invoice();

	
	AccountingApi accountingApi = new AccountingApi(config);
	
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
	public String callBackToken(HttpServletResponse response, HttpServletRequest request) throws ServletException {

		try {

			// retrieve OAuth verifier code from callback URL param
			String verifier = request.getParameter("oauth_verifier");

			// Swap your temp token for 30 oauth token
			accessToken = new OAuthAccessToken(config);
			accessToken.build(verifier, storage.get(request, "tempToken"), storage.get(request, "tempTokenSecret"))
					.execute();
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
		return "index";
	}

	@RequestMapping(value = "/createNewInvoice", method = RequestMethod.GET)
	public String createNewInvoice(HttpServletResponse response, HttpServletRequest request) throws ServletException {

		try {		
		ApiClient apiClientForAccounting = new ApiClient(config.getApiUrl(),null,null,null);		
		accountingApi.setApiClient(apiClientForAccounting);
		accountingApi.setOAuthToken(accessToken.getToken(), accessToken.getTokenSecret());
		
		
		where = "Type==\"REVENUE\"";
		Accounts accounts = accountingApi.getAccounts(ifModifiedSince, where, order);
		
		String accountCodeForInvoice = accounts.getAccounts().get(0).getCode();
		where = null;
		
		//Contacts contacts = accountingApi.getContacts(ifModifiedSince, where, order, ids, null, includeArchived);
		//UUID contactIDForInvoice = contacts.getContacts().get(0).getContactID();
		
		Contact useContact = new Contact();
		//useContact.setContactID(contactIDForInvoice);
		useContact.setName("Adriano Neps 2");
		
		Invoices newInvoices = new Invoices();
		myInvoice = new Invoice();
		
		LineItem li = new LineItem();
		li.setAccountCode(accountCodeForInvoice);
		li.setDescription("Acme Tires");
		li.setQuantity(2.0);
		li.setUnitAmount(30.00);
		li.setLineAmount(60.00);
		li.setTaxType("NONE");
		
		myInvoice.addLineItemsItem(li);
		myInvoice.setContact(useContact);
		LocalDate dueDate =  LocalDate.of(2019,Month.SEPTEMBER,02);
		myInvoice.setDueDate(dueDate);
		LocalDate todayDate =  LocalDate.now();
		myInvoice.setDate(todayDate);
		myInvoice.setType(com.xero.models.accounting.Invoice.TypeEnum.ACCREC);
		myInvoice.setReference("test invoice");
		myInvoice.setStatus(com.xero.models.accounting.Invoice.StatusEnum.DRAFT);
		newInvoices.addInvoicesItem(myInvoice);
		
		Invoices newInvoice = accountingApi.createInvoice(newInvoices, summarizeErrors);
		myInvoice = newInvoice.getInvoices().get(0);		
		System.out.println(newInvoice.toString());		

		} catch (IOException e) {			
			e.printStackTrace();
		}			
		return "index";
	}
	
	@RequestMapping(value = "/deleteInvoice", method = RequestMethod.GET)
	public String deleteInvoice(HttpServletResponse response, HttpServletRequest request) throws ServletException {
		//You can delete a DRAFT or SUBMITTED invoice by updating the Status to DELETED.
		//If an invoice has been AUTHORISED it cannot be deleted but you can set it's status to VOIDED
		
		try {		
		ApiClient apiClientForAccounting = new ApiClient(config.getApiUrl(),null,null,null);		
		accountingApi.setApiClient(apiClientForAccounting);
		accountingApi.setOAuthToken(accessToken.getToken(), accessToken.getTokenSecret());
		
		
		where = "Type==\"REVENUE\"";
		Accounts accounts = accountingApi.getAccounts(ifModifiedSince, where, order);
		
		String accountCodeForInvoice = accounts.getAccounts().get(0).getCode();
		where = null;
		
		//Contacts contacts = accountingApi.getContacts(ifModifiedSince, where, order, ids, null, includeArchived);
		//UUID contactIDForInvoice = contacts.getContacts().get(0).getContactID();
		
		Contact useContact = new Contact();
		//useContact.setContactID(contactIDForInvoice);
		useContact.setName("Adriano Neps");
		
		Invoices invoices = new Invoices();
		myInvoice.setStatus(com.xero.models.accounting.Invoice.StatusEnum.DELETED);
		invoices.addInvoicesItem(myInvoice);
		
		Invoices newInvoice = accountingApi.updateInvoice(myInvoice.getInvoiceID(), invoices);
		
		System.out.println(newInvoice.toString());

		} catch (IOException e) {			
			e.printStackTrace();
		}			

		return "index";
	}
	
	
	@RequestMapping(value = "/getPaymentDateRange", method = RequestMethod.GET)
	public String getPaymentDateRange(HttpServletResponse response) {
		try {
			ApiClient apiClientForAccounting = new ApiClient(config.getApiUrl(),null,null,null);		
			accountingApi.setApiClient(apiClientForAccounting);
			accountingApi.setOAuthToken(accessToken.getToken(), accessToken.getTokenSecret());
			
			where = "Date >= DateTime(2019, 09, 02) && Date <= DateTime(2019, 09, 02)";
			Payments payments = accountingApi.getPayments(ifModifiedSince, where, order);
			
			System.out.println(payments.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "index";
	}
	
	@RequestMapping(value = "/getPayments", method = RequestMethod.GET)
	public String getPayments(HttpServletResponse response) {
		try {
			ApiClient apiClientForAccounting = new ApiClient(config.getApiUrl(),null,null,null);		
			accountingApi.setApiClient(apiClientForAccounting);
			accountingApi.setOAuthToken(accessToken.getToken(), accessToken.getTokenSecret());
						
			Payments payments = accountingApi.getPayments(ifModifiedSince, where, order);
			
			System.out.println(payments.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "index";
	}
	
	

	@RequestMapping(value = "/createNewCreditNote", method = RequestMethod.GET)
	public String createNewCreditNote() {		
	
		try {
		
		ApiClient apiClientForAccounting = new ApiClient(config.getApiUrl(),null,null,null);		
		accountingApi.setApiClient(apiClientForAccounting);
		accountingApi.setOAuthToken(accessToken.getToken(), accessToken.getTokenSecret());
			
		Contacts contacts = accountingApi.getContacts(ifModifiedSince, where, order, ids, null, includeArchived);
	
		// Create Credit Note
		List<LineItem> lineItems = new ArrayList<>();
		LineItem li = new LineItem();
		li.setAccountCode("400");
		li.setDescription("Foobar");
		li.setQuantity(2.0);
		li.setUnitAmount(20.0);
		lineItems.add(li);
		
		CreditNotes newCNs = new CreditNotes();
		CreditNote cn = new CreditNote();
		cn.setContact(contacts.getContacts().get(1));
		cn.setLineItems(lineItems);  
		cn.setType(com.xero.models.accounting.CreditNote.TypeEnum.ACCPAYCREDIT);
		newCNs.addCreditNotesItem(cn);
		CreditNotes newCreditNote = accountingApi.createCreditNote(summarizeErrors, newCNs);		
		UUID newCreditNoteId = newCreditNote.getCreditNotes().get(0).getCreditNoteID();
		
		System.out.println(newCreditNote.toString());
	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "index";
		
	}

}
