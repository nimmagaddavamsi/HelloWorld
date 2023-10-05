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

-----------

