package kitchenpos.products.application;

import java.math.BigDecimal;
import java.util.UUID;

public interface MenuServiceClient {
    void hideMenuBasedOnProductPrice(UUID productId, BigDecimal productPrice);
}
