package pofeaa.original.base.servicestub;

import pofeaa.original.base.money.Money;
import pofeaa.original.base.plugin.PluginFactory;

public interface TaxService {
    TaxService INSTANCE = (TaxService) PluginFactory.getPlugin(TaxService.class);
    
    TaxInfo getSalesTaxInfo(String productCode, Address addr, Money saleAmount);
}
