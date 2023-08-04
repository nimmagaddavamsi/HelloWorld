
  To create a customized strategy for generating primary keys in Hibernate, you need to implement the `org.hibernate.id.IdentifierGenerator` interface. This interface has one method called `generate`, which you should override to provide your custom logic for generating primary key values.

Here's a step-by-step guide on how to create a custom identifier generator in Hibernate:

Step 1: Implement the IdentifierGenerator interface
Create a class that implements the `IdentifierGenerator` interface. This class will define your custom logic to generate primary key values. The `generate` method should return a unique Serializable value that will be used as the primary key.

```java
import java.io.Serializable;
import java.util.Random;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class CustomPrimaryKeyGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        // Your custom logic to generate the primary key value
        // For example, you can generate a random integer as the primary key
        int primaryKeyValue = generateRandomInt();
        return primaryKeyValue;
    }

    private int generateRandomInt() {
        Random random = new Random();
        return random.nextInt(10000); // Change this as per your requirement
    }
}
```

Step 2: Annotate the entity class with your custom generator
In your entity class, annotate the primary key field with the `@GeneratedValue` annotation, specifying your custom generator class in the `generator` attribute.

```java
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class YourEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-generator")
    @org.hibernate.annotations.GenericGenerator(name = "custom-generator", strategy = "your.package.CustomPrimaryKeyGenerator")
    private Long id;

    // Other fields, constructors, getters, setters, etc.
}
```

Ensure to replace `your.package` with the actual package name where your `CustomPrimaryKeyGenerator` class is located.

That's it! Now, when you save a new instance of `YourEntity`, Hibernate will invoke your custom identifier generator to generate a primary key value for the `id` field. In this example, we generated a random integer, but you can implement any custom logic based on your requirements, such as using a combination of the current timestamp, a sequence, or any other unique identifier generator logic.
