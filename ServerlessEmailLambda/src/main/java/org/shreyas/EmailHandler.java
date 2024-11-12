package org.shreyas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import org.shreyas.model.EmailRequest;


public class EmailHandler implements RequestHandler<EmailRequest, String> {
    private static final String mailGunDomainName = System.getenv("MAIL_GUN_DOMAIN_NAME");
    private static final String mailGunApiKey = System.getenv("MAIL_GUN_API_KEY");

    public static void sendEmail(String sender, String subject, String body) {
        MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(mailGunApiKey)
                .createApi(MailgunMessagesApi.class);
        Message message = Message.builder()
                .from("noreply@shreyaskothari.me")
                .to(sender)
                .subject(subject)
                .text(body)
                .build();

        mailgunMessagesApi.sendMessage(mailGunDomainName, message);
    }

    @Override
    public String handleRequest(EmailRequest o, Context context) {

        LambdaLogger log = context.getLogger();
        log.log("Payload is: " + o);
        log.log("Environment variables are: MAIL_GUN_DOMAIN_NAME"+mailGunDomainName);
        log.log("Environment variables are: MAIL_GUN_API_KEY"+mailGunApiKey);
        return o.toString();
    }
}
