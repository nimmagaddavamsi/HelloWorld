import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.PostgreSQL10Dialect;
import org.hibernate.service.ServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.SpringPhysicalNamingStrategy;

@Configuration
public class HibernateConfiguration {

    @Bean
    public HibernatePhysicalNamingStrategy physicalNamingStrategy() {
        return new HibernatePhysicalNamingStrategy();
    }

    @Bean
    public PostgreSQL10Dialect postgreSQL10Dialect() {
        return new PostgreSQL10Dialect() {
            @Override
            public TypeContributions getTypeContributions() {
                return super.getTypeContributions()
                        .addType(JsonbUserType.JSONB_TYPE, JsonbUserType.SQL_TYPES);
            }
        };
    }

    private static class HibernatePhysicalNamingStrategy extends SpringPhysicalNamingStrategy {
        // You don't need to add anything here if you don't have any custom naming strategy.
    }
}
