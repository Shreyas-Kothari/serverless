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


public class EmailHandler implements RequestHandler<SNSEvent, String> {
    private static final String mailGunDomainName = System.getenv("MAIL_GUN_DOMAIN_NAME");
    private static final String mailGunApiKey = System.getenv("MAIL_GUN_API_KEY");

    public static MessageResponse sendEmail(String sender, String subject, String body) {
        MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(mailGunApiKey)
                .createApi(MailgunMessagesApi.class);
        Message message = Message.builder()
                .from("noreply@" + mailGunDomainName)
                .to(sender)
                .subject(subject)
                .html(body)
                .build();

        return mailgunMessagesApi.sendMessage(mailGunDomainName, message);
    }

    @Override
    public String handleRequest(SNSEvent event, Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        LambdaLogger log = context.getLogger();
        log.log("Event started/triggered "+ event);

        for(SNSEvent.SNSRecord record: event.getRecords()){
            String message = record.getSNS().getMessage();
            log.log("Message from SNS: " + message);
            try {
                EmailRequest o = objectMapper.readValue(message, EmailRequest.class);
                MessageResponse response = sendEmail(o.getRecipient(), o.getSubject(), o.getMessage());
                log.log("Email Sending service response is: " +response.getMessage());
                return response.getMessage();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return "No valid email request found in the input payload";
    }
}
