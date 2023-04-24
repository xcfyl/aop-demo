package aop;

/**
 * @author 西城风雨楼
 */
public class UserService {
    @MyAutowire
    private UserDao userDao;

    public void login(String username, String passwd) {
        if (userDao.queryPasswdByUsername(username).equals(username + passwd)) {
            System.out.println("登录成功");
        } else {
            System.out.println("登录失败");
        }
    }
}
