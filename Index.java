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

---------------
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class JsonbType implements UserType {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.JAVA_OBJECT};
    }

    @Override
    public Class<JsonNode> returnedClass() {
        return JsonNode.class;
    }

    @Override
    public boolean equals(Object o, Object o1) throws HibernateException {
        if (o == o1) return true;
        if (o == null || o1 == null) return false;
        return o.equals(o1);
    }

    @Override
    public int hashCode(Object o) throws HibernateException {
        return o.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] strings, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException, SQLException {
        String json = resultSet.getString(strings[0]);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new HibernateException("Error parsing JSON", e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i, SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {
        if (o == null) {
            preparedStatement.setNull(i, Types.OTHER);
            return;
        }
        try {
            preparedStatement.setObject(i, objectMapper.writeValueAsString(o), Types.OTHER);
        } catch (Exception e) {
            throw new HibernateException("Error serializing JSON", e);
        }
    }

    @Override
    public Object deepCopy(Object o) throws HibernateException {
        if (o == null) return null;
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(o), returnedClass());
        } catch (Exception e) {
            throw new HibernateException("Error copying JSON", e);
        }
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object o) throws HibernateException {
        return (Serializable) o;
    }

    @Override
    public Object assemble(Serializable serializable, Object o) throws HibernateException {
        return serializable;
    }

    @Override
    public Object replace(Object o, Object o1, Object o2) throws HibernateException {
        return o;
    }
}


    
