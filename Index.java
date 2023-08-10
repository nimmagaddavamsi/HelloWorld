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

public class JsonbUserType implements UserType {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.JAVA_OBJECT};
    }

    @Override
    public Class returnedClass() {
        return JsonNode.class;
    }

    @Override
    public Object nullSafeGet(
            ResultSet resultSet,
            String[] names,
            SharedSessionContractImplementor session,
            Object owner) throws HibernateException, SQLException {
        String json = resultSet.getString(names[0]);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new HibernateException("Error converting JSONB column to JsonNode", e);
        }
    }

    @Override
    public void nullSafeSet(
            PreparedStatement preparedStatement,
            Object value,
            int index,
            SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            preparedStatement.setNull(index, Types.OTHER);
            return;
        }
        try {
            preparedStatement.setObject(index, objectMapper.writeValueAsString(value), Types.OTHER);
        } catch (Exception e) {
            throw new HibernateException("Error converting JsonNode to JSONB column", e);
        }
    }

    // Other methods (equals, hashCode, deepCopy, etc.) can be left as default or implemented as needed.
}
