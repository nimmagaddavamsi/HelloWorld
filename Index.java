import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceBusMessageSender {

    private final ServiceBusSenderClient senderClient;

    public ServiceBusMessageSender(@Value("${azure.servicebus.connection-string}") String connectionString,
                                   @Value("${azure.servicebus.queue-name}") String queueName) {
        this.senderClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(queueName)
                .buildSenderClient();
    }

    public void sendMessage(String message) {
        ServiceBusMessage serviceBusMessage = new ServiceBusMessage(message);
        senderClient.sendMessage(serviceBusMessage);
    }
}

---------------


import org.springframework.stereotype.Service;

@Service
public class YourService {

    private final ServiceBusMessageSender messageSender;

    public YourService(ServiceBusMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void sendToServiceBus(String message) {
        messageSender.sendMessage(message);
    }
}

----------------

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class YourController {

    private final YourService yourService;

    public YourController(YourService yourService) {
        this.yourService = yourService;
    }

    @PostMapping("/send-message")
    public void sendMessage(@RequestBody String message) {
        yourService.sendToServiceBus(message);
    }
}

implementation group: 'com.azure', name: 'azure-messaging-servicebus', version: '7.2.0' // Check for the latest version


import com.azure.messaging.servicebus.*;

public class ServiceBusSubscriber {
    public static void main(String[] args) {
        // Define your Service Bus connection string and queue/topic name
        String connectionString = "<your_connection_string>";
        String queueName = "<your_queue_name>"; // Or topic name if you're working with a topic

        // Initialize a ServiceBusClient
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
                .connectionString(connectionString);

        ServiceBusReceiverClient receiverClient = builder.receiver()
                .queueName(queueName)
                .buildReceiverClient();

        // Start receiving messages
        while (true) {
            Iterable<ServiceBusReceivedMessage> receivedMessages = receiverClient.receiveMessages(1);

            for (ServiceBusReceivedMessage receivedMessage : receivedMessages) {
                System.out.println("Received message: " + receivedMessage.getBody().toString());
            }
        }
    }
}
