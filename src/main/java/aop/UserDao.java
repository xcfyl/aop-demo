package aop;

/**
 * @author 西城风雨楼
 */
public class UserDao {
    public String queryPasswdByUsername(String username) {
        return username + "123456";
    }
}
