package io.daniel.controller;

import io.daniel.Main;
import io.daniel.dto.Transfer;
import io.daniel.model.Account;
import io.daniel.utility.JsonUtility;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;
import spark.utils.IOUtils;

import java.io.IOException;
import java.math.BigDecimal;


public class BankControllerIntegrationTest {
    private static final String BASE_PATH = "http://localhost:4567";

    @BeforeClass
    public static void beforeClass() {
        Main.main(null);
    }

    @AfterClass
    public static void afterClass() {
        Spark.stop();
    }

    @Test
    public void aNewAccountShouldBeCreated() throws IOException {
        HttpUriRequest request = new HttpPost(BASE_PATH + "/account?amount=123");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        String body = IOUtils.toString(httpResponse.getEntity().getContent());

        System.out.println(body);
    }

    @Test
    public void getTheSameAccountAsWasCreated() throws IOException {
        HttpUriRequest request = new HttpPost(BASE_PATH + "/account?amount=123");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        String body = IOUtils.toString(httpResponse.getEntity().getContent());

        HttpUriRequest request2 = new HttpGet(BASE_PATH + "/account/" + body);
        HttpResponse httpResponse2 = HttpClientBuilder.create().build().execute(request2);
        Assert.assertEquals(httpResponse2.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        String body2 = IOUtils.toString(httpResponse2.getEntity().getContent());

        Account account = JsonUtility.convertFromJson(body2, Account.class);

        Assert.assertNotNull(account);
        Assert.assertEquals(account.getId(), new Integer(body));
        Assert.assertEquals(account.getBalance(), new BigDecimal(123));
    }

    @Test
    public void shouldTransferMoney() throws IOException {
        Integer accountFrom = createNewAccount(new BigDecimal(100));
        Integer accountTo = createNewAccount(new BigDecimal(10));

        Transfer transfer = Transfer.builder()
                .idAccountFrom(accountFrom)
                .idAccountTo(accountTo)
                .amount(new BigDecimal(100))
                .build();

        HttpUriRequest request = RequestBuilder.create("POST")
                .setUri(BASE_PATH + "/bank/transfer")
                .setEntity(new StringEntity(JsonUtility.convertToJson(transfer), ContentType.APPLICATION_JSON))
                .build();
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        Account from = getAccount(accountFrom);
        Account to = getAccount(accountTo);

        Assert.assertEquals(from.getBalance(), new BigDecimal(0));
        Assert.assertEquals(to.getBalance(), new BigDecimal(110));
    }

    @Test
    public void transferMoney_ThrowException_WhenAccountFromHaveEnoughBalance() throws IOException {
        Integer accountFrom = createNewAccount(new BigDecimal(100));
        Integer accountTo = createNewAccount(new BigDecimal(10));

        Transfer transfer = Transfer.builder()
                .idAccountFrom(accountFrom)
                .idAccountTo(accountTo)
                .amount(new BigDecimal(1000))
                .build();

        HttpUriRequest request = RequestBuilder.create("POST")
                .setUri(BASE_PATH + "/bank/transfer")
                .setEntity(new StringEntity(JsonUtility.convertToJson(transfer), ContentType.APPLICATION_JSON))
                .build();
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    private Integer createNewAccount(BigDecimal amount) throws IOException {
        HttpUriRequest request = new HttpPost(BASE_PATH + "/account?amount=" + amount);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        return Integer.parseInt(IOUtils.toString(httpResponse.getEntity().getContent()));
    }

    private Account getAccount(Integer id) throws IOException {
        HttpUriRequest request = new HttpGet(BASE_PATH + "/account/" + id);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        String body = IOUtils.toString(httpResponse.getEntity().getContent());
        return JsonUtility.convertFromJson(body, Account.class);
    }
}
