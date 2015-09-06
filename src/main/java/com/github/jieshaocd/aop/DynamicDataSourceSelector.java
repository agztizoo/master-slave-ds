/**
 * DynamicDataSourceSelector.java
 */
package com.github.jieshaocd.aop;

import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.util.StringUtils;

import com.github.jieshaocd.datasource.DataSourceStrategyHolder;

/**
 * @author jieshao
 * @date Jun 10, 2015
 */
@Aspect
@Component
@Order(2)
public class DynamicDataSourceSelector {

    private static final Logger logger = LoggerFactory
            .getLogger(DynamicDataSourceSelector.class);

    @Resource
    private TransactionAttributeSource transactionAttributeSource;

    @Pointcut("execution(* com.github.jieshaocd.service..*.*(..))")
    public void pointCut() {}

    @Around("pointCut()")
    public Object aroundAdvice(ProceedingJoinPoint call) throws Throwable {
        String strategy = DataSourceStrategyHolder.get();
        boolean requireNew = !StringUtils.hasText(strategy);
        if (requireNew) {
            strategy = getStrategyByTransaction(call);
            DataSourceStrategyHolder.set(strategy);
        }
        if (logger.isDebugEnabled()) {
            String method = call.getSignature().getName();
            logger.debug(
                    "method: {0}, new strategy: {1}, current strategy: {2}",
                    method, requireNew, strategy);
        }
        try {
            Object result = call.proceed();
            return result;
        } finally {
            if (requireNew) {
                DataSourceStrategyHolder.clear();
            }
        }
    }

    private String getStrategyByMethodName(ProceedingJoinPoint call) {
        String methodName = call.getSignature().getName();
        if (methodName.startsWith("get") || methodName.startsWith("query")
                || methodName.startsWith("find") || methodName.startsWith("search")) {
            return DataSourceStrategyHolder.READ_ONLY;
        }
        return DataSourceStrategyHolder.READ_WRITE;
    }

    private String getStrategyByTransaction(ProceedingJoinPoint call) {
        Method method = ((MethodSignature) call.getSignature()).getMethod();
        TransactionAttribute arrt =
                transactionAttributeSource.getTransactionAttribute(method, call
                        .getTarget().getClass());
        if (arrt == null) {
            return DataSourceStrategyHolder.READ_ONLY;
        }
        int propagation = arrt.getPropagationBehavior();
        if (propagation > TransactionDefinition.PROPAGATION_REQUIRES_NEW
                || arrt.isReadOnly()) {
            return DataSourceStrategyHolder.READ_ONLY;
        }
        return DataSourceStrategyHolder.READ_WRITE;
    }

}
