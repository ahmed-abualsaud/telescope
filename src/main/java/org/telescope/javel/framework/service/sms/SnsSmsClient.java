
package org.telescope.javel.framework.service.sms;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;

import com.amazonaws.services.sns.model.PublishResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.config.Keys;
import org.telescope.server.Context;

import java.util.HashMap;
import java.util.Map;

public class SnsSmsClient implements SmsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnsSmsClient.class);

    private final AmazonSNSAsync snsClient;

    public SnsSmsClient() {
        if (Context.getConfig().hasKey(Keys.SMS_AWS_REGION)
                && Context.getConfig().hasKey(Keys.SMS_AWS_ACCESS)
                && Context.getConfig().hasKey(Keys.SMS_AWS_SECRET)) {
            BasicAWSCredentials awsCredentials =
                    new BasicAWSCredentials(Context.getConfig().getString(Keys.SMS_AWS_ACCESS),
                    Context.getConfig().getString(Keys.SMS_AWS_SECRET));
            snsClient = AmazonSNSAsyncClientBuilder.standard()
                    .withRegion(Context.getConfig().getString(Keys.SMS_AWS_REGION))
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
        } else {
            throw new RuntimeException("SNS Not Configured Properly. Please provide valid config.");
        }
    }

    @Override
    public void sendMessageSync(String destAddress, String message, boolean command) {
        Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
        smsAttributes.put("AWS.SNS.SMS.SenderID",
                new MessageAttributeValue().withStringValue("SNS").withDataType("String"));
        smsAttributes.put("AWS.SNS.SMS.SMSType",
                new MessageAttributeValue().withStringValue("Transactional").withDataType("String"));

        PublishRequest publishRequest = new PublishRequest().withMessage(message)
                .withPhoneNumber(destAddress).withMessageAttributes(smsAttributes);

        snsClient.publishAsync(publishRequest, new AsyncHandler<PublishRequest, PublishResult>() {
            @Override
            public void onError(Exception exception) {
                LOGGER.error("SMS send failed", exception);
            }
            @Override
            public void onSuccess(PublishRequest request, PublishResult result) {
            }
        });
    }

    @Override
    public void sendMessageAsync(String destAddress, String message, boolean command) {
        sendMessageSync(destAddress, message, command);
    }
}
