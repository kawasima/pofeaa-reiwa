package pofeaa.original.datasource.rowdatagateway;

import java.util.HashMap;
import java.util.Map;

public class Registry {
    private static final Map<Long, PersonGateway> personRegistry = new HashMap<>();

    public static PersonGateway getPerson(Long id) {
        return personRegistry.get(id);
    }

    public static void addPerson(PersonGateway person) {
        personRegistry.put(person.getId(), person);
    }

    public static void clear() {
        personRegistry.clear();
    }
}
