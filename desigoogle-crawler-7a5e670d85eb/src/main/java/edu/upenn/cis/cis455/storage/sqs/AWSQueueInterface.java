package edu.upenn.cis.cis455.storage.sqs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.upenn.cis.cis455.utils.SHAHashGenerator;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AWSQueueInterface<T> {

    private String queueName;
    private String queueUrl;
    private AmazonSQS sqsClient;
    private final Class<T> clazz;

    private Gson gson = new Gson();


    public AWSQueueInterface(String queueName, Class<T> clazz) {
        this.clazz = clazz;
        this.queueName = queueName;
//        sqsClient = AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(new AWSCredentials() {
//            @Override
//            public String getAWSAccessKeyId() {
//                return "ASIAYGAO6WGPRTK7QGFA";
//            }
//
//            @Override
//            public String getAWSSecretKey() {
//                return "KLEYbeLhVlEgBA/4rnW4fBwEsf5wp19jLycjCSoT";
//            }
//        })).withRegion("us-east-1").build();
        sqsClient = AmazonSQSClientBuilder.standard().withRegion("us-east-1").build();
        queueUrl = sqsClient.getQueueUrl(queueName).getQueueUrl();
    }

    public int queueSize() {
        List<String> attributeNames = new ArrayList<>();
        attributeNames.add("All");
        GetQueueAttributesRequest request = new GetQueueAttributesRequest(queueUrl);
        request.setAttributeNames(attributeNames);
        Map<String, String> attributes = sqsClient.getQueueAttributes(request)
                .getAttributes();
        int messages = Integer.parseInt(attributes.get("ApproximateNumberOfMessages"));
        return messages;
    }

    public void addToQueue(T entry) {

        String jsonString = gson.toJson(entry);
        String hash = SHAHashGenerator.getHash(jsonString) + UUID.randomUUID().toString();
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageGroupId(hash)
                .withMessageDeduplicationId(hash)
                .withMessageBody(jsonString);
        sqsClient.sendMessage(send_msg_request);

    }

    public void addToQueue(List<T> entryList) {

        if(entryList.size() == 0) {
            return;
        }

        List<SendMessageBatchRequestEntry> sendMessageBatchRequestEntryList = new ArrayList<>();
        for(T entry : entryList) {
            String json = gson.toJson(entry);
            String hash = SHAHashGenerator.getHash(json) + UUID.randomUUID().toString();
            sendMessageBatchRequestEntryList.add(new SendMessageBatchRequestEntry()
                    .withMessageBody(json)
                    .withMessageDeduplicationId(hash)
                    .withMessageGroupId(hash)
                    .withId(UUID.randomUUID().toString()));
            if(sendMessageBatchRequestEntryList.size() == 10) {
                SendMessageBatchRequest send_batch_request = new SendMessageBatchRequest()
                        .withQueueUrl(queueUrl)
                        .withEntries(sendMessageBatchRequestEntryList);
                sqsClient.sendMessageBatch(send_batch_request);
                sendMessageBatchRequestEntryList.clear();
            }
        }

        if(sendMessageBatchRequestEntryList.size() > 0) {
            SendMessageBatchRequest send_batch_request = new SendMessageBatchRequest()
                    .withQueueUrl(queueUrl)
                    .withEntries(sendMessageBatchRequestEntryList);

            sqsClient.sendMessageBatch(send_batch_request);
        }

    }

    public T deQueue() {

        List<Message> messages = sqsClient.receiveMessage(queueUrl).getMessages();

        for (Message m : messages) {
            sqsClient.deleteMessage(queueUrl, m.getReceiptHandle());
        }

        if(messages.size() == 0) {
            return null;
        }
        String json = messages.get(0).getBody();
        return gson.fromJson(json, clazz);
    }

    public List<T> deQueue(int size) {

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest();
        receiveMessageRequest.setMaxNumberOfMessages(size);
        receiveMessageRequest.setQueueUrl(queueUrl);


        List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
        List<T> ans = new ArrayList<>();

        if(messages.size() == 0) {
            return new ArrayList<>();
        }

        for (Message m : messages) {
            String json = m.getBody();
            ans.add(gson.fromJson(json, clazz));
            sqsClient.deleteMessage(queueUrl, m.getReceiptHandle());
        }
        return ans;
    }

}
