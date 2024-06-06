package kitchenpos.products.ui.dto;

import java.math.BigDecimal;
import kitchenpos.products.domain.ProfanityValidator;
import kitchenpos.products.domain.tobe.ProductName;
import kitchenpos.products.domain.tobe.ProductPrice;
import kitchenpos.products.domain.tobe.Product;

public class ProductCreateRequest {

    private final ProductName name;

    private final ProductPrice price;

    public ProductCreateRequest(String name, BigDecimal price) {
        this(new ProductName(name), new ProductPrice(price));
    }

    public ProductCreateRequest(ProductName name, ProductPrice price) {
        this.name = name;
        this.price = price;
    }

    public void validateName(ProfanityValidator profanityValidator) {
        if (profanityValidator.containsProfanity(name.getName())) {
            throw new IllegalArgumentException();
        }
    }

    public Product to() {
        return new Product(name, price);
    }
}
