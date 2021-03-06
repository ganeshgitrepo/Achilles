package info.archinnov.achilles.test.parser.entity;

import info.archinnov.achilles.annotations.Order;
import javax.persistence.Column;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class CompoundKeyByConstructorMissingOrderAnnotation {

    @Column(name = "primaryKey")
    private Long id;

    private String name;

    @JsonCreator
    public CompoundKeyByConstructorMissingOrderAnnotation(@JsonProperty("name") String name,
            @Order(1) @JsonProperty("id") Long id) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
