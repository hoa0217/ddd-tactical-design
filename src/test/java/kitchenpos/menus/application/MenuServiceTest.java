package kitchenpos.menus.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
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
import kitchenpos.products.domain.ProductRepository;
import kitchenpos.products.domain.PurgomalumClient;
import kitchenpos.products.domain.tobe.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayName("MenuService")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MenuServiceTest {

    private MenuRepository menuRepository = new InMemoryMenuRepository();
    private MenuGroupRepository menuGroupRepository = new InMemoryMenuGroupRepository();
    private ProductRepository productRepository = new InMemoryProductRepository();
    private PurgomalumClient purgomalumClient = new FakePurgomalumClient();

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuService(menuRepository, menuGroupRepository, productRepository,
                purgomalumClient);
    }

    @Test
    void 상품들을_조합하여_메뉴를_생성한다() {
        Menu createRequest = MenuFixture.createRequest(30_000L, createChickenMenuGroup(),
                createFriedProduct(), 2);

        Menu actual = menuService.create(createRequest);

        assertThat(actual.getId()).isNotNull();
    }

    @Test
    void 메뉴에_상품이_1개_이상_존재하지_않으면_예외를_던진다() {
        Menu createRequest = MenuFixture.createRequest(30_000L, createChickenMenuGroup(),
                createFriedProduct(), 0);

        assertThatIllegalArgumentException().isThrownBy(() -> menuService.create(createRequest));
    }


    @Test
    void 메뉴이름에_욕설이나_부적절한_언어를_사용하면_예외를_던진다() {
        Menu createRequest = MenuFixture.createRequest("욕설", 30_000L, createChickenMenuGroup(),
                createFriedProduct(), 2);

        assertThatIllegalArgumentException().isThrownBy(() -> menuService.create(createRequest));
    }

    @Test
    void 메뉴는_메뉴그룹에_속하지않으면_예외를_던진다() {
        Menu createRequest = MenuFixture.createRequest(30_000L, null, createFriedProduct(), 2);

        assertThatThrownBy(() -> menuService.create(createRequest)).isInstanceOf(
                NoSuchElementException.class);
    }

    @Test
    void 메뉴가격은_0보다_작으면_예외를_던진다() {
        Menu createRequest = MenuFixture.createRequest(-20_000L, createChickenMenuGroup(),
                createFriedProduct(), 2);

        assertThatThrownBy(() -> menuService.create(createRequest)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    void 메뉴가격은_상품가격x상품갯수의_총합을_넘으면_예외를_던진다() {
        Menu createRequest = MenuFixture.createRequest(50_000L, createChickenMenuGroup(),
                createFriedProduct(), 2);

        assertThatThrownBy(() -> menuService.create(createRequest)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    void 메뉴가격을_수정한다() {
        Menu saved = menuService.create(
                MenuFixture.createRequest(30_000L, createChickenMenuGroup(), createFriedProduct(),
                        2));

        Menu actual = menuService.changePrice(saved.getId(),
                MenuFixture.changePriceRequest(40_000L));

        assertThat(actual.getPrice()).isEqualTo(BigDecimal.valueOf(40_000L));
    }

    @Test
    void 메뉴가격_수정시_0보다_작으면_예외를_던진다() {
        Menu saved = menuService.create(
                MenuFixture.createRequest(30_000L, createChickenMenuGroup(), createFriedProduct(),
                        2));

        assertThatThrownBy(() -> menuService.changePrice(saved.getId(),
                MenuFixture.changePriceRequest(-40_000L))).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    void 메뉴가격_수정시_상품가격x상품갯수의_총합을_넘으면_예외를_던진다() {
        Menu saved = menuService.create(
                MenuFixture.createRequest(30_000L, createChickenMenuGroup(), createFriedProduct(),
                        2));

        assertThatThrownBy(() -> menuService.changePrice(saved.getId(),
                MenuFixture.changePriceRequest(50_000L))).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    void 메뉴를_손님들에게_노출한다() {
        Menu saved = menuService.create(
                MenuFixture.createRequest(30_000L, createChickenMenuGroup(), createFriedProduct(),
                        2));

        Menu actual = menuService.display(saved.getId());

        assertThat(actual.isDisplayed()).isTrue();
    }

    @Test
    void 메뉴를_손님들에게_숨긴다() {
        Menu saved = menuService.create(
                MenuFixture.createRequest(30_000L, createChickenMenuGroup(), createFriedProduct(),
                        2));

        Menu actual = menuService.hide(saved.getId());

        assertThat(actual.isDisplayed()).isFalse();
    }

    @Test
    void 모든_메뉴_목록을_볼_수_있다() {
        menuService.create(
                MenuFixture.createRequest("후라이드2마리", 30_000L, createChickenMenuGroup(),
                        createFriedProduct(), 2));
        menuService.create(
                MenuFixture.createRequest("후라이드1마리", 20_000L, createChickenMenuGroup(),
                        createFriedProduct(), 1));

        List<Menu> actual = menuService.findAll();

        assertThat(actual).hasSize(2);
    }

    private MenuGroup createChickenMenuGroup() {
        return menuGroupRepository.save(MenuGroupFixture.createChicken());

    }

    private Product createFriedProduct() {
        return productRepository.save(ProductFixture.createFired());
    }
}