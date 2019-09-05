package biz.infocare;

import com.xero.api.*;
import com.xero.api.client.AccountingApi;
import com.xero.example.TokenStorage;
import com.xero.models.accounting.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class TestXero {

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private Config config = JsonConfig.getInstance();
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	@InjectMocks
	private AccountingApi accountingApi = new AccountingApi(config);

	/**
	 * Helper for random string
	 *
	 * @param count
	 * @return
	 */
	private String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

    @Before
    public void setUp() {     
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
		ApiClient apiClientForAccounting = new ApiClient(config.getApiUrl(),null,null,null);
		accountingApi.setApiClient(apiClientForAccounting);
		accountingApi.setOAuthToken("P45KW2BHZSCECYIJDWRTUG7PTSIHCH", "M2ECBHGDSHCVAVAQYEBDIZUIJQUKEX");
	}

	/**
	 * Get & Store token
	 */
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

	/**
	 * Create random invoice
	 */
	@Test
	public void createInvoice() {
		try {

			Accounts accounts = accountingApi.getAccounts(null, null, null);
			String accountCodeForInvoice = accounts.getAccounts().get(0).getCode();

			// Contacts contacts = accountingApi.getContacts(ifModifiedSince, where, order, ids, null, includeArchived);
			// UUID contactIDForInvoice = contacts.getContacts().get(0).getContactID();

			Contact useContact = new Contact();
			useContact.setName(randomAlphaNumeric(10));

			Invoice myInvoice = new Invoice();

			//useContact.setContactID(contactIDForInvoice);
			useContact.setName(randomAlphaNumeric(10));
			Invoices newInvoices = new Invoices();

			LineItem li = new LineItem();
			li.setAccountCode(accountCodeForInvoice);
			li.setDescription(randomAlphaNumeric(10));
			li.setQuantity(1.0);
			li.setUnitAmount(1.00);
			li.setLineAmount(1.00);
			li.setTaxType("NONE");

			myInvoice.addLineItemsItem(li);
			myInvoice.setContact(useContact);
			LocalDate dueDate =  LocalDate.of(2019, Month.SEPTEMBER,02);
			myInvoice.setDueDate(dueDate);
			LocalDate todayDate =  LocalDate.now();
			myInvoice.setDate(todayDate);
			myInvoice.setType(Invoice.TypeEnum.ACCREC);
			myInvoice.setReference(randomAlphaNumeric(10));
			myInvoice.setStatus(Invoice.StatusEnum.DRAFT);
			newInvoices.addInvoicesItem(myInvoice);

			Invoices newInvoice = accountingApi.createInvoice(newInvoices, null);
			myInvoice = newInvoice.getInvoices().get(0);
			System.out.println(newInvoice.toString());

			Invoices invoices = new Invoices();
			invoices.addInvoicesItem(myInvoice);
			accountingApi.createInvoice(invoices, false);

		} catch (XeroApiException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void createNewCreditNote() {

		try {

			Contacts contacts = accountingApi.getContacts(null, null, null, null, null, null);

			// Create Credit Note
			List<LineItem> lineItems = new ArrayList<>();
			LineItem li = new LineItem();
			li.setAccountCode("400");
			li.setDescription(randomAlphaNumeric(10));
			li.setQuantity(1.0);
			li.setUnitAmount(1.0);
			lineItems.add(li);

			CreditNotes newCNs = new CreditNotes();
			CreditNote cn = new CreditNote();
			cn.setContact(contacts.getContacts().get(0));
			cn.setLineItems(lineItems);
			cn.setType(CreditNote.TypeEnum.ACCPAYCREDIT);
			newCNs.addCreditNotesItem(cn);
			CreditNotes newCreditNote = accountingApi.createCreditNote(null, newCNs);
			UUID newCreditNoteId = newCreditNote.getCreditNotes().get(0).getCreditNoteID();

			System.out.println(newCreditNote.toString());
			System.out.println(newCreditNoteId);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getPayments() {
		try {
			Payments payments = accountingApi.getPayments(null, null, null);
			System.out.println(payments.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getPaymentDateRange() {
		try {
			String where = "Date >= DateTime(2019, 09, 02) && Date <= DateTime(2019, 09, 02)";
			Payments payments = accountingApi.getPayments(null, where, null);
			System.out.println(payments.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
