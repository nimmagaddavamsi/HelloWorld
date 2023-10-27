dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-integration'
    implementation 'org.springframework.integration:spring-integration-sftp'
    // Add other dependencies if needed
}
---------------------

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageHandler;

@Configuration
public class SftpConfig {

    @Bean
    public DefaultSftpSessionFactory sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost("your.sftp.host");
        factory.setPort(22);
        factory.setUser("your_username");
        factory.setPassword("your_password");
        factory.setAllowUnknownKeys(true);
        return factory;
    }

    @Bean
    public MessageHandler sftpMessageHandler(DefaultSftpSessionFactory sftpSessionFactory) {
        FileWritingMessageHandler handler = new FileWritingMessageHandler(sftpSessionFactory);
        handler.setRemoteDirectoryExpressionString("/remote/directory/");
        handler.setFileNameGenerator(message -> message.getHeaders().get(FileHeaders.FILENAME).toString());
        return handler;
    }

    @Bean
    public IntegrationFlow sftpUploadFlow(MessageHandler sftpMessageHandler) {
        return IntegrationFlows.from(MessageChannels.direct())
                .handle(sftpMessageHandler)
                .get();
    }
}
----------------------------

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

public class SftpUploader {

    @Autowired
    private MessageHandler sftpMessageHandler;

    public void uploadByteArray(byte[] data, String remoteFileName) {
        Message<byte[]> message = MessageBuilder
                .withPayload(data)
                .setHeader(FileHeaders.FILENAME, remoteFileName)
                .build();
        sftpMessageHandler.handleMessage(message);
    }
}

--------------------------------

public class MainApplication {

    @Autowired
    private SftpUploader sftpUploader;

    public void uploadData() {
        byte[] data = "Hello, SFTP!".getBytes();
        String remoteFileName = "test.txt";
        sftpUploader.uploadByteArray(data, remoteFileName);
    }
}
