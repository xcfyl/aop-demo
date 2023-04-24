package aop;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author 西城风雨楼
 */
@Aspect({UserService.class, UserDao.class})
public class LogAspect {
    public static void before(Object obj, Method method, Object[] args) {
        System.out.println(Arrays.toString(args));
    }

    public static void after(Object obj, Method method, Object[] args) {
        System.out.println("后置通知: " + method.getName());
    }

    public static void exception(Object obj, Method method, Object[] args, Throwable e) {
        System.out.println("发生了异常: " + e.getMessage());
    }
}
