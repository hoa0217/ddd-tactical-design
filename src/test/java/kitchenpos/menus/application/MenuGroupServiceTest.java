package kitchenpos.menus.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import kitchenpos.fake.InMemoryMenuGroupRepository;
import kitchenpos.fixture.MenuGroupFixture;
import kitchenpos.menus.domain.MenuGroup;
import kitchenpos.menus.domain.MenuGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayName("MenuGroupService")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MenuGroupServiceTest {

    private MenuGroupRepository menuGroupRepository = new InMemoryMenuGroupRepository();

    private MenuGroupService menuGroupService;

    @BeforeEach
    void setUp() {
        menuGroupService = new MenuGroupService(menuGroupRepository);
    }

    @Test
    void 메뉴그룹을_생성한다() {
        MenuGroup request = MenuGroupFixture.createRequest("치킨");

        MenuGroup actual = menuGroupService.create(request);

        assertThat(actual.getId()).isNotNull();
    }

    @Test
    void 메뉴그룹이름이_비어있으면_예외를던진다() {
        MenuGroup request = MenuGroupFixture.createRequest("");

        assertThatIllegalArgumentException().isThrownBy(() -> menuGroupService.create(request));
    }

    @Test
    void 메뉴그룹을_조회한다() {
        MenuGroup request1 = MenuGroupFixture.createRequest("치킨");
        menuGroupService.create(request1);
        MenuGroup request2 = MenuGroupFixture.createRequest("피자");
        menuGroupService.create(request2);

        List<MenuGroup> actual = menuGroupService.findAll();

        assertThat(actual).hasSize(2);
    }
}