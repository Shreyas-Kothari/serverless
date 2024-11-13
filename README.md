# Serverless for Email

## Overview

The `EmailHandler` class is an AWS Lambda function written in Java that processes incoming Amazon SNS (Simple Notification Service) events to send email notifications using the Mailgun API. The function leverages environment variables to configure the Mailgun domain and API key.

## Functionality

The Lambda function listens for SNS events, extracts the message payload, deserializes it to an `EmailRequest` object, and sends an email using the Mailgun service.

### Key Components

- **MailgunMessagesApi**: Interface used to interact with the Mailgun API.
- **SNS Event Handling**: Processes records from the SNS event and logs incoming messages.
- **ObjectMapper**: Used for deserializing the JSON message from SNS into an `EmailRequest` object.
- **Environment Variables**:
  - `MAIL_GUN_DOMAIN_NAME`: The domain name registered with Mailgun.
  - `MAIL_GUN_API_KEY`: The API key for authentication with Mailgun.

## Prerequisites

Ensure the following environment variables are set in your Lambda configuration:

- `MAIL_GUN_DOMAIN_NAME`
- `MAIL_GUN_API_KEY`

These values must be configured in the AWS Lambda environment for the function to work correctly.

## Installation and Deployment

1. **Clone the repository**:

    ```bash
    git clone https://github.com/Shreyas-Kothari/serverless.git
    cd serverless-fork/ServerlessEmailLambda
    ```

2. **Build the project**:

    ```bash
    mvn clean package
    ```

    This command compiles the code and creates a deployable JAR file in the target directory.

3. **Deploy to AWS Lambda**:
    - Upload the generated JAR file to your AWS Lambda function.
    - Ensure that the environment variables (`MAIL_GUN_DOMAIN_NAME` and `MAIL_GUN_API_KEY`) are configured in the Lambda function.

## Usage

Once deployed, the Lambda function listens for incoming SNS events. Each event should contain a JSON payload structured as follows:

```json
{
  "recipient": "example@domain.com",
  "subject": "Your Subject Here",
  "message": "Your message body here"
}
```
