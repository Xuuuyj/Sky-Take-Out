package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.xml.stream.events.EndElement;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/*
*
* 自定义切面类，实现公共字段自动填充，insert，update方法的公共填充*/
@Aspect//切面类
@Component//bean
@Slf4j//日志
public class AutoFillAspect {
    //AutoFile再去看一下，已经全然忘记了
    /*切入点*/
    //拦截这个方法上以及annotation
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {

    }

    /*定义通知，前置通知*/
    @Before("autoFillPointCut()")
    public void bautoFill(JoinPoint joinPoint)  {
        log.info("开始公共字段自动填充");
        //具体的内容
        //获取被拦截的方法的数据库的类型是update还是insert
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); //获得签名
        AutoFill autoFill =  signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库的操作类型


        //获取当前被拦截到的方法参数-实体对象
        Object[] args = joinPoint.getArgs();//获得所有的参数
        if(args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];//获取第0个实体
        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();


        //根据当前不同的操作类型，为对应的属性通过反射赋值
        if(operationType == OperationType.INSERT) {
            //为4个公共字段赋值

            try {
                Method setCreatTime = entity.getClass().getDeclaredMethod(
                        AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(
                        AutoFillConstant.SET_CREATE_USER,Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(
                        AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(
                        AutoFillConstant.SET_UPDATE_USER,Long.class);

                //通过反射为属性赋值
                setCreatTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }else if(operationType == OperationType.UPDATE) {
            //为两个公共字段
            try {
                Method setUpdateUser = entity.getClass().getDeclaredMethod(
                        AutoFillConstant.SET_UPDATE_USER,Long.class
                );
                Method setUpdateTime = entity.getClass().getDeclaredMethod(
                        AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class
                );

                //通过反射为两个字段赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}

















