package aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author 西城风雨楼
 */
public class CGLIBContainer {
    private static final Map<Class<?>, Map<InterceptPoint, List<Method>>> INTERCEPT_METHODS_MAP = new HashMap<>();

    private static final List<Class<?>> ASPECTS = new ArrayList<>();

    public static void addAspect(Class<?> clazz) {
        ASPECTS.add(clazz);
    }

    public static <T> T getBean(Class<T> origin) {
        try {
            T bean = doGetBean(origin);
            Field[] fields = origin.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(MyAutowire.class)) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    Class<?> type = field.getType();
                    field.set(bean, getBean(type));
                }
            }
            return bean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
        try {
            for (Class<?> clazz : ASPECTS) {
                Aspect aspect = clazz.getAnnotation(Aspect.class);
                if (aspect != null) {
                    Method before = getMethod(clazz, "before");
                    Method after = getMethod(clazz, "after");
                    Method exception = getMethod(clazz, "exception");
                    // before和after要作用的类
                    Class<?>[] targets = aspect.value();
                    for (Class<?> target : targets) {
                        addInterceptMethod(target, InterceptPoint.BEFORE, before);
                        addInterceptMethod(target, InterceptPoint.AFTER, after);
                        addInterceptMethod(target, InterceptPoint.EXCEPTION, exception);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addInterceptMethod(Class<?> clazz, InterceptPoint point, Method method) {
        if (method == null) {
            return;
        }

        Map<InterceptPoint, List<Method>> map = INTERCEPT_METHODS_MAP.getOrDefault(clazz, new HashMap<>());
        List<Method> methods = map.getOrDefault(point, new ArrayList<>());
        methods.add(method);
        map.put(point, methods);
        INTERCEPT_METHODS_MAP.put(clazz, map);
    }

    @SuppressWarnings("unchecked")
    private static <T> T doGetBean(Class<T> bean) {
        try {
            if (!INTERCEPT_METHODS_MAP.containsKey(bean)) {
                return bean.newInstance();
            }

            return (T) Enhancer.create(bean, new MethodInterceptor() {
                @Override
                public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                    // 在这里对该bean进行动态代理
                    Class<?> superclass = o.getClass().getSuperclass();
                    List<Method> beforeMethods = getInterceptorMethods(superclass, InterceptPoint.BEFORE);
                    invokeAllStaticMethod(beforeMethods, o, method, args);
                    try {
                        Object result = methodProxy.invokeSuper(o, args);
                        // 获取后置方法
                        List<Method> afterMethods = getInterceptorMethods(superclass, InterceptPoint.AFTER);
                        invokeAllStaticMethod(afterMethods, o, method, args);
                        return result;
                    } catch (Exception e) {
                        // 在这里选择什么都不处理，其实可以再定义一个异常方法
                        List<Method> exceptionMethods = getInterceptorMethods(superclass, InterceptPoint.EXCEPTION);
                        invokeAllStaticMethod(exceptionMethods, o, method, args, e);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void invokeAllStaticMethod(List<Method> methods, Object... args) throws InvocationTargetException, IllegalAccessException {
        for (Method method : methods) {
            method.invoke(null, args);
        }
    }

    private static List<Method> getInterceptorMethods(Class<?> clazz, InterceptPoint point) {
        Map<InterceptPoint, List<Method>> map = INTERCEPT_METHODS_MAP.get(clazz);
        if (map == null) {
            return Collections.emptyList();
        }

        List<Method> methods = map.get(point);
        if (methods == null) {
            return Collections.emptyList();
        }

        return methods;
    }

    private static Method getMethod(Class<?> clazz, String name) throws NoSuchMethodException {
        if ("exception".equals(name)) {
            return clazz.getMethod(name, Object.class, Method.class, Object[].class, Throwable.class);
        }
        return clazz.getMethod(name, Object.class, Method.class, Object[].class);
    }
}
