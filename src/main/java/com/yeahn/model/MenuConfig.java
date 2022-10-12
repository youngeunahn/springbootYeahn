package com.yeahn.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MenuConfig {
    private String MENU_CODE;   // 메뉴코드
    private Integer MENU_SEQ;   // 메뉴 순서
    private String MENU_TITLE;  // 메뉴 이름 
    private String MENU_URL;    // 메뉴 URL
    private String MENU_GNB;    // 메뉴 GNB 이름
    private String MENU_PARENT; // 메뉴부모코드
    private Integer MENU_LEVEL; // 메뉴깊이
    private boolean MENU_LEVEL_VIEW;
    private String MENU_GROUP;
    private String DEL_YN;      // 삭제여부

    public MenuConfig() {} // 디폴트 생성자

    // 인스턴스 생성 메소드
    public static MenuConfig createInstance(String MENU_CODE, int MENU_SEQ, String MENU_TITLE, String MENU_URL, String MENU_GNB, String MENU_PARENT, int MENU_LEVEL, String DEL_YN) {
        MenuConfig MenuConfig = new MenuConfig();
        MenuConfig.MENU_CODE = MENU_CODE;
        MenuConfig.MENU_SEQ = MENU_SEQ;
        MenuConfig.MENU_TITLE = MENU_TITLE;
        MenuConfig.MENU_URL = MENU_URL;
        MenuConfig.MENU_GNB = MENU_GNB;
        MenuConfig.MENU_PARENT = MENU_PARENT;
        MenuConfig.MENU_LEVEL = MENU_LEVEL;
        MenuConfig.DEL_YN = DEL_YN;
        return MenuConfig;
    }
}
