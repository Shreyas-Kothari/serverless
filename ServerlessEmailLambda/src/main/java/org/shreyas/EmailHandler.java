package org.shreyas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import org.shreyas.model.EmailRequest;

import java.net.HttpURLConnection;
import java.net.URL;

public class EmailHandler implements RequestHandler<SNSEvent, String> {
    private static final String mailGunDomainName = System.getenv("MAIL_GUN_DOMAIN_NAME");
    private static final String mailGunApiKey = System.getenv("MAIL_GUN_API_KEY");
    private static LambdaLogger log;

    public static MessageResponse sendEmail(String sender, String subject, String body) {

        MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(mailGunApiKey)
                .createApi(MailgunMessagesApi.class);

        Message message = Message.builder()
                .from("noreply@" + mailGunDomainName)
                .to(sender)
                .subject(subject)
                .html(body)
                .build();

        log.log("The message build is: " + message.toString());

        return mailgunMessagesApi.sendMessage(mailGunDomainName, message);
    }

    @Override
    public String handleRequest(SNSEvent event, Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        log = context.getLogger();

        log.log("bing google");
        bingMethod();
        for (SNSEvent.SNSRecord record : event.getRecords()) {
            String message = record.getSNS().getMessage();
            log.log("Message from SNS: " + message);
            try {
                EmailRequest o = objectMapper.readValue(message, EmailRequest.class);
                log.log("Received email request: " + o);
                MessageResponse response = sendEmail(o.getRecipient(), o.getSubject(), o.getMessage());
                log.log("Email Sending service response is: " + response.getMessage());
                return response.getMessage();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return "No valid email request found in the input payload";
    }

    public void bingMethod() {
        try {
            // Replace with a known Mailgun endpoint or a simple public site
            URL url = new URL("https://google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                log.log("Connection to Google successful!");
            } else {
                log.log("Connection to Google failed with response code: " + responseCode);
            }
        } catch (Exception e) {
            log.log("Failed to connect: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
