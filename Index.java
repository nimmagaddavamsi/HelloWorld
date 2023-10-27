dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-integration'
    implementation 'org.springframework.integration:spring-integration-sftp'
    implementation 'com.jcraft:jsch:0.1.55'
    // Add other dependencies if needed
}
---------
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

public class SftpUploader {

    @Autowired
    private SftpRemoteFileTemplate sftpTemplate;

    public void uploadByteArray(byte[] data, String remoteFileName) {
        sftpTemplate.execute(session -> {
            try {
                session.write(new ByteArrayInputStream(data), remoteFileName);
            } catch (IOException e) {
                throw new RuntimeException("Error uploading file to SFTP server", e);
            }
            return null;
        });
    }
}

---------
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

@Configuration
public class SftpConfig {

    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost("your.sftp.host");
        factory.setPort(22);
        factory.setUser("your_username");
        factory.setPassword("your_password");
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory);
    }

    @Bean
    public SftpRemoteFileTemplate sftpTemplate(SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory) {
        return new SftpRemoteFileTemplate(sftpSessionFactory);
    }
}
---------

public class MainApplication {

    @Autowired
    private SftpUploader sftpUploader;

    public void uploadData() {
        byte[] data = "Hello, SFTP!".getBytes();
        String remoteFileName = "test.txt";
        sftpUploader.uploadByteArray(data, remoteFileName);
    }
}
