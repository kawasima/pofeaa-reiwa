package pofeaa.original.datasource.tabledatagateway;

import org.jooq.DSLContext;
import pofeaa.original.base.money.Money;

// Transaction Script
public class PayAllowanceCommandHandler {
    private DSLContext context;
    public void execute(Long personId, Money amount) {
        PersonGateway personGateway = new PersonGateway(context);
        Person person = personGateway.findById(personId).orElseThrow();
        if (person.getAge() < 18) {

        }
    }
}
