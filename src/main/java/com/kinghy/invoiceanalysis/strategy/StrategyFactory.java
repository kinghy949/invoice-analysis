package com.kinghy.invoiceanalysis.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 策略工厂
 * 负责策略的自动注册和获取
 */
@Slf4j
@Component
public class StrategyFactory {

    private final Map<String, ExtractionStrategy> strategies = new HashMap<>();

    /**
     * Spring自动注入所有ExtractionStrategy实现类
     */
    @Autowired
    private List<ExtractionStrategy> strategyList;

    /**
     * 初始化：自动注册所有策略
     */
    @PostConstruct
    public void init() {
        for (ExtractionStrategy strategy : strategyList) {
            registerStrategy(strategy);
        }
        log.info("策略工厂初始化完成，已注册 {} 个策略: {}", strategies.size(), strategies.keySet());
    }

    /**
     * 注册策略
     */
    public void registerStrategy(ExtractionStrategy strategy) {
        String name = strategy.getStrategyName();
        if (strategies.containsKey(name)) {
            log.warn("策略 {} 已存在，将被覆盖", name);
        }
        strategies.put(name, strategy);
        log.debug("注册策略: {}", name);
    }

    /**
     * 获取策略
     * @param strategyName 策略名称
     * @return 策略实例，如果不存在返回null
     */
    public ExtractionStrategy getStrategy(String strategyName) {
        ExtractionStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            log.error("未找到策略: {}，可用策略: {}", strategyName, strategies.keySet());
        }
        return strategy;
    }

    /**
     * 判断策略是否存在
     */
    public boolean hasStrategy(String strategyName) {
        return strategies.containsKey(strategyName);
    }

    /**
     * 获取所有已注册的策略名称
     */
    public Set<String> getAllStrategyNames() {
        return strategies.keySet();
    }
}
