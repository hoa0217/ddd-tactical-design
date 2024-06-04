package kitchenpos.products.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.List;
import kitchenpos.fake.FakePurgomalumClient;
import kitchenpos.fake.InMemoryMenuGroupRepository;
import kitchenpos.fake.InMemoryMenuRepository;
import kitchenpos.fake.InMemoryProductRepository;
import kitchenpos.fixture.MenuFixture;
import kitchenpos.fixture.MenuGroupFixture;
import kitchenpos.fixture.ProductFixture;
import kitchenpos.menus.domain.Menu;
import kitchenpos.menus.domain.MenuGroup;
import kitchenpos.menus.domain.MenuGroupRepository;
import kitchenpos.menus.domain.MenuRepository;
import kitchenpos.products.domain.Product;
import kitchenpos.products.domain.ProductRepository;
import kitchenpos.products.domain.PurgomalumClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ProductService")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProductServiceTest {

    private MenuGroupRepository menuGroupRepository = new InMemoryMenuGroupRepository();

    private ProductRepository productRepository = new InMemoryProductRepository();

    private MenuRepository menuRepository = new InMemoryMenuRepository();

    private PurgomalumClient purgomalumClient = new FakePurgomalumClient();

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, menuRepository, purgomalumClient);
    }

    @Test
    void 상품을_등록할_수_있다() {
        Product request = ProductFixture.createRequest("후라이드", 20_000L);
        Product actual = productService.create(request);

        assertThat(actual.getId()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"비속어", "욕설이 포함된 단어"})
    void 상품의_이름에_욕설이_포함되어있으면_예외를_던진다(final String name) {
        Product request = ProductFixture.createRequest(name);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> productService.create(request));
    }

    @Test
    void 상품가격이_0보다_작으면_예외를_던진다() {
        Product request = ProductFixture.createRequest(-10_000L);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> productService.create(request));
    }

    @Test
    void 상품의_가격을_변경한다() {
        Product saved = productService.create(ProductFixture.createRequest("후라이드", 20_000L));
        Menu menu = createFriedMenu(saved);
        Product changePriceRequest = ProductFixture.changePriceRequest(15_000L);

        Product actualProduct = productService.changePrice(saved.getId(), changePriceRequest);
        Menu actualMenu = menuRepository.findById(menu.getId()).get();

        assertAll(
                () -> assertThat(actualProduct.getPrice()).isEqualTo(BigDecimal.valueOf(15_000L)),
                () -> assertThat(actualMenu.isDisplayed()).isTrue()
        );
    }

    @Test
    void 상품가격변경시_수정한_상품이_속해있는_메뉴가격이_상품가격x상품갯수의_총합을_넘는다면_해당_메뉴는_손님들에게_숨긴다() {
        Product saved = productService.create(ProductFixture.createRequest("후라이드", 20_000L));
        Menu menu = createFriedMenu(saved);
        Product changePriceRequest = ProductFixture.changePriceRequest(10_000L);

        Product actualProduct = productService.changePrice(saved.getId(), changePriceRequest);
        Menu actualMenu = menuRepository.findById(menu.getId()).get();

        assertAll(
                () -> assertThat(actualProduct.getPrice()).isEqualTo(BigDecimal.valueOf(10_000L)),
                () -> assertThat(actualMenu.isDisplayed()).isFalse()
        );
    }

    private Menu createFriedMenu(Product product) {
        MenuGroup chickenMenuGroup = menuGroupRepository.save(MenuGroupFixture.createChicken());
        return menuRepository.save(
                MenuFixture.createFriedOnePlusOne(chickenMenuGroup, product));
    }

    @Test
    void 모든_상품목록을_볼_수_있다() {
        productService.create(ProductFixture.createRequest("후라이드", 20_000L));
        productService.create(ProductFixture.createRequest("양념", 20_000L));

        List<Product> actual = productService.findAll();

        assertThat(actual).hasSize(2);
    }
}