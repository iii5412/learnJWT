package com.portfolio.main.domain.service.menu.menu;

import com.portfolio.main.application.login.exception.InvalidLoginId;
import com.portfolio.main.application.menu.dto.CreateMenu;
import com.portfolio.main.application.menu.dto.EditMenu;
import com.portfolio.main.application.menu.dto.MenuDto;
import com.portfolio.main.application.menu.dto.SearchMenu;
import com.portfolio.main.application.menu.exception.CannotDeleteMenuWithSubmenusException;
import com.portfolio.main.application.menu.exception.UpperMenuNotFoundException;
import com.portfolio.main.common.util.page.PageResult;
import com.portfolio.main.domain.model.account.Role;
import com.portfolio.main.domain.model.account.User;
import com.portfolio.main.domain.model.account.exception.RoleNotFoundException;
import com.portfolio.main.domain.model.account.type.RoleCode;
import com.portfolio.main.domain.model.menu.Menu;
import com.portfolio.main.domain.model.menu.MenuRole;
import com.portfolio.main.domain.model.menu.Program;
import com.portfolio.main.domain.model.menu.exception.MenuCannotBeOwnParentException;
import com.portfolio.main.domain.model.menu.exception.MenuNotFoundException;
import com.portfolio.main.domain.model.menu.exception.ProgramNotFoundException;
import com.portfolio.main.domain.repository.menu.MenuRepository;
import com.portfolio.main.domain.repository.menu.MenuRoleRepository;
import com.portfolio.main.domain.service.account.UserService;
import com.portfolio.main.domain.service.account.role.RoleService;
import com.portfolio.main.domain.service.menu.menurole.MenuRoleService;
import com.portfolio.main.domain.service.menu.program.ProgramService;
import com.portfolio.main.infrastructure.repository.mapper.menu.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 메뉴 작업을 위한 서비스 클래스입니다.
 */
@Service
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;
    private final UserService userService;
    private final ProgramService programService;
    private final RoleService roleService;
    private final MenuRoleService menuRoleService;

    @Autowired
    public MenuService(
            MenuRepository menuRepository
            , MenuMapper menuMapper
            , UserService userService
            , ProgramService programService
            , RoleService roleService
            , MenuRoleService menuRoleService
    ) {
        this.menuRepository = menuRepository;
        this.menuMapper = menuMapper;
        this.userService = userService;
        this.roleService = roleService;
        this.programService = programService;
        this.menuRoleService = menuRoleService;
    }

    public Menu findById(Long id) throws MenuNotFoundException {
        return menuRepository.findById(id).orElseThrow(MenuNotFoundException::new);
    }

    /**
     * 주어진 RoleCode에 따라 메뉴 목록을 검색합니다.
     *
     * @param roleCode 메뉴를 검색할 역할 코드
     * @return 역할이 액세스할 수 있는 최상위 메뉴 목록
     */
    public List<Menu> selectMenuByRoleCode(RoleCode roleCode) {
        final List<Menu> menus = menuRepository.selectMenuByRoleCode(roleCode);
        return getTopMenus(menus);
    }

    public PageResult<Menu> selectMenuWithPageable(SearchMenu searchMenu, PageRequest pageRequest) {
        return menuRepository.selectMenuWithPageable(searchMenu, pageRequest);
    }

    public List<Menu> selectMenu() {
        final List<Menu> menus = menuRepository.selectMenu();
        return getTopMenus(menus);
    }

    public List<Menu> selectMenuFlat() {
        final List<Menu> menus = selectMenu();

        List<Menu> flattenedMenus = new ArrayList<>();
        for (Menu menu : menus) {
            addMenuAndDescendants(flattenedMenus, menu);
        }

        return flattenedMenus;
    }

    /**
     * 제공된 정보를 기반으로 새 메뉴를 만듭니다.
     *
     * @param createMenu 생성할 메뉴의 세부정보가 포함된 객체입니다.
     * 상위 메뉴ID, 메뉴명, 메뉴종류, 주문번호, 역할코드, 작성자 로그인ID가 포함되어야 합니다.
     * @return 새로 생성된 메뉴의 ID입니다.
     * @throws UpperMenuNotFoundException createMenu에 지정된 상위 메뉴를 찾을 수 없는 경우.
     * @throws RoleNotFoundException createMenu에 지정된 역할을 찾을 수 없는 경우.
     */
    @Transactional
    public Long createMenu(CreateMenu createMenu) throws UpperMenuNotFoundException, RoleNotFoundException {
        final String createUserLoginId = createMenu.getCreateUserLoginId();
        final User createUser = userService.findByLoginId(createUserLoginId);

        final Menu newMenu = new Menu(
                createMenu.getMenuName(),
                createMenu.getMenuType(),
                createMenu.getOrderNum(),
                createUser
        );

        if (createMenu.hasUpperId()) {
            final Menu upperMenu = menuRepository.findById(createMenu.getUpperId()).orElseThrow(MenuNotFoundException::new);
            newMenu.setUpperMenu(upperMenu);
        }

        final Menu savedMenu = menuRepository.save(newMenu);

        final RoleCode roleCode = RoleCode.valueOf(createMenu.getRoleCode());
        final Role role = roleService.findByRoleCode(roleCode);

        menuRoleService.save(savedMenu, role);
        return savedMenu.getId();
    }

    @Transactional
    public Long edit(EditMenu editMenu) throws MenuNotFoundException, ProgramNotFoundException, InvalidLoginId {
        final Long targetMenuId = editMenu.getId();
        final Menu targetMenu = menuRepository.findById(targetMenuId).orElseThrow(MenuNotFoundException::new);

        if (Objects.equals(editMenu.getId(), editMenu.getUpperId())) {
            throw new MenuCannotBeOwnParentException();
        }

        Menu upperMenu = null;
        if (editMenu.hasUpperId()) {
            upperMenu = menuRepository.findById(editMenu.getUpperId()).orElseThrow(MenuNotFoundException::new);
        }

        Program program = null;
        if (editMenu.hasProgramId()) {
            program = programService.findById(editMenu.getProgramId());
        }

        final User editUser = userService.findByLoginId(editMenu.getEditUserLoginId());

        targetMenu.edit(editMenu.getMenuName(), editMenu.getMenuType(), editMenu.getOrderNum(), upperMenu, program, editUser);

        return targetMenu.getId();
    }

    public void deleteById(Long id) {

        final Menu targetMenu = menuRepository.findById(id).orElseThrow(MenuNotFoundException::new);

        if (!targetMenu.getSubMenus().isEmpty()) {
            throw new CannotDeleteMenuWithSubmenusException();
        }

        menuRoleService.deleteByMenuId(targetMenu.getId());

        menuRepository.deleteById(id);
    }

    private List<Menu> getTopMenus(List<Menu> menus) {
        return menus.stream().filter(menu -> !menu.hasUpperMenu()).toList();
    }

    private void addMenuAndDescendants(List<Menu> flattenedMenus, Menu menu) {
        flattenedMenus.add(menu);

        for (Menu subMenu : menu.getSubMenus()) {
            addMenuAndDescendants(flattenedMenus, subMenu);
        }
    }

//    private Menu convertToMenu(MenuDto menuDto) {
//        final Long userId = menuDto.getLastModifiedByUser().getId();
//        final User user = userService.findByUserId(userId);
//        Program program = null;
//
//        if (menuDto.hasProgram()) {
//            final Long programId = menuDto.getProgram().getId();
//            program = programService.findById(programId);
//        }
//
//        final Menu menu = new Menu(
//                menuDto.getId(),
//                menuDto.hasUpperMenu() ? convertToMenu(menuDto.getUpperMenu()) : null,
//                menuDto.getMenuName(),
//                menuDto.getMenuType(),
//                menuDto.getOrderNum(),
//                program,
//                user,
//                null
//        );
//
//        if (menuDto.hasSubMenus()) {
//            menuDto.getSubMenus().stream()
//                    .map(this::convertToMenu)
//                    .forEach(menu::addSubMenu);
//        }
//
//        return menu;
//    }

}
