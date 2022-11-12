package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义原数据对象处理器
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
/*
    在学习ThreadLocal之前，我们需要先确认-一个事情，就是客户端发送的每次http请求,对应的在服务端都会分配-一个新
    的线程来处理，在处理过程中涉及到下面类中的方法都属于相同的一-个线程:
            1、LoginCheckFilter的doFilter方 法
             2、EmployeeController的update方法
)           3、 MyMetaObjectHandler的updateFill方法
        可以在.上面的三个方法中分别加入下面代码(获取当前线程id) :
     long id = Thread. current Thread(). getId() ;
     log. info("线程id:{}", id);
    执行编辑员工功能进行验证,通过观察控制台输出可以发现，-次请求对应的线程id是相同的:
*/

/*    什么是ThreadLocal?
    ThreadLocal并不是- -个Thread, 而是Thread的局部变量。当使用ThreadLocal维护变量时，ThreadLocal为每个使用该
    变量的线程提供独立的变量副本,所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。
    ThreadLocal为每个线程提供单独一 份存储空间，具有线程隔离的效果，只有在线程内才能获取到对应的值,线程外则不
    能访问。
    ThreadLocal常用方法:
    public void set(T value)设置 当前线程的线程局部变量的值
●public T get()
    返回当前线程所对应的线程局部变量的值
            我们可以在LoginCheckFilter的doFilter方法中获取当前登录用户id,并调用ThreadLocal的set方法来设置当前线程的线
    程局部变量的值(用户id) ,然后在MyMetaObjectHandler的updateFill方法中调用ThreadLocal的get方法来获得当前
    线程所对应的线程局部变量的值(用户id)。*/

/*    公共字段自动填充，
    功能完善
    实现步骤:
            1、编写BaseContext工具类,基于ThreadLocal封装的工具类
2、在LoginCheckFilter的doFilter方法中调用BaseContext来设置当前登录用户的id
3、在MyMetaObjectHandler的方 法中调用BaseContext获取登录用户的id*/


    /**
     * 插入操作自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.warn("公共字段自动填充【insert】");
        log.warn(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());

        Long currentId = BaseContext.getCurrentId();
        metaObject.setValue("createUser", currentId);
        metaObject.setValue("updateUser", currentId);
    }

    /**
     * 更新操作自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.warn("公共字段自动填充【update】");
        log.warn(metaObject.toString());

        log.warn("线程id{}", Thread.currentThread().getId());

        metaObject.setValue("updateTime", LocalDateTime.now());

        Long currentId = BaseContext.getCurrentId();
        metaObject.setValue("updateUser", currentId);
    }
}
