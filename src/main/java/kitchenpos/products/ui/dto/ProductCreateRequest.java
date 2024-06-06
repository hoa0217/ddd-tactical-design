package kitchenpos.products.ui.dto;

import java.math.BigDecimal;
import kitchenpos.products.domain.PurgomalumClient;
import kitchenpos.products.domain.tobe.Name;
import kitchenpos.products.domain.tobe.Price;
import kitchenpos.products.domain.tobe.Product;

public class ProductCreateRequest {

    private final Name name;

    private final Price price;

    public ProductCreateRequest(String name, BigDecimal price){
        this(new Name(name), new Price(price));
    }

    public ProductCreateRequest(Name name, Price price) {
        this.name = name;
        this.price = price;
    }

    public void validateName(PurgomalumClient purgomalumClient){
        if(purgomalumClient.containsProfanity(name.getName())){
            throw new IllegalArgumentException();
        }
    }

    public Product to() {
        return new Product(name, price);
    }

    public Name getName() {
        return name;
    }
}
