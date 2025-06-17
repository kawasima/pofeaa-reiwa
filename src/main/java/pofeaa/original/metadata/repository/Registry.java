package pofeaa.original.metadata.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class Registry {
    private static Registry INSTANCE;
    private final PersonRepository personRepository;

    public Registry(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @PostConstruct
    public void init() {
        Registry.INSTANCE = this;
    }
    public static PersonRepository getPersonRepository() {
        return Registry.INSTANCE.personRepository;
    }
}
