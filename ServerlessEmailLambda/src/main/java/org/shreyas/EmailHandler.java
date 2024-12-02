package org.shreyas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import org.shreyas.model.EmailRequest;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import javax.management.ServiceNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class EmailHandler implements RequestHandler<SNSEvent, String> {
    private static String mailGunDomainName;
    private static String mailGunApiKey;
    private static LambdaLogger log;
    private static final String region = System.getenv("REGION");
    private static final String secretManagerName = System.getenv("SECRET_MANAGER_NAME");

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
        log.log("ping google");
        bingMethod();
        try {
            getSecretsFromSecretManager();
        } catch (ServiceNotFoundException e) {
            throw new RuntimeException(e);
        }
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
            } catch (Exception e) {
                log.log("Exception occurred: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return "No valid email request found in the input payload";
    }

    private void getSecretsFromSecretManager() throws ServiceNotFoundException {
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        GetSecretValueRequest request = GetSecretValueRequest.builder()
               .secretId(secretManagerName)
               .build();

        try{
            log.log("Setting up the secrets from the secret manager");
            GetSecretValueResponse response = client.getSecretValue(request);
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            Map<String, String> secrets = mapper.readValue(response.secretString(), Map.class);
            mailGunDomainName=secrets.get("MAIL_GUN_DOMAIN_NAME");
            mailGunApiKey=secrets.get("MAIL_GUN_API_KEY");
            log.log("Retrieved secrets from secret manager");
        }catch (ResourceNotFoundException e) {
            throw new ServiceNotFoundException("Could not find secret manager with name: " + secretManagerName);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to get secret", e);
        }
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
