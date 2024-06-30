package com.geekbank.bank.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class PayPalService {

    private String clientId = "YOUR_PAYPAL_CLIENT_ID";
    private String clientSecret = "YOUR_PAYPAL_CLIENT_SECRET";
    private String accessToken = "YOUR_ACCESS_TOKEN"; // Deberás implementar la lógica para obtener este token.
    private String apiUrl = "https://api-m.sandbox.paypal.com"; // Cambia a "https://api-m.paypal.com" para producción.

    public String searchInvoices(String phoneNumber) throws IOException {
        URL url = new URL(apiUrl + "/v2/invoicing/search-invoices?page=1&page_size=1&total_required=true");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");
        httpConn.setRequestProperty("Authorization", "Bearer " + accessToken);
        httpConn.setRequestProperty("Content-Type", "application/json");
        httpConn.setDoOutput(true);

        String requestBody = "{ \"total_amount_range\": { \"lower_amount\": { \"currency_code\": \"USD\", \"value\": \"50.00\" }, \"upper_amount\": { \"currency_code\": \"USD\", \"value\": \"50.00\" } }, \"invoice_date_range\": { \"start\": \"2018-06-01\", \"end\": \"2018-06-21\" } }";

        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
        writer.write(requestBody);
        writer.flush();
        writer.close();
        httpConn.getOutputStream().close();

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
