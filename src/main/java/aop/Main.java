package aop;

/**
 * @author 西城风雨楼
 */
public class Main {
    public static void main(String[] args) {
        CGLIBContainer.addAspect(LogAspect.class);
        CGLIBContainer.init();
        UserService userService = CGLIBContainer.getBean(UserService.class);
        userService.login("zhangsan", "123456");
    }
}
