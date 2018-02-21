package com.tipcrm.web.initializer;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tipcrm.cache.ConfigurationCache;
import com.tipcrm.cache.ListBoxCache;
import com.tipcrm.dao.entity.Configuration;
import com.tipcrm.dao.entity.ListBox;
import com.tipcrm.dao.repository.ConfigurationRepository;
import com.tipcrm.dao.repository.ListBoxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Order(value=1)
public class CacheInitializer implements CommandLineRunner {

    @Autowired
    private ListBoxRepository listBoxRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    private Logger logger = LoggerFactory.getLogger(CacheInitializer.class);

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing cache data...");
        initListBoxCache();
        initConfigurationCache();
    }

    private void initListBoxCache() {
        logger.info("Initializing list box cache data...");
        List<ListBox> listBoxes = listBoxRepository.findAll();
        ListBoxCache.addOrUpdateCache(listBoxes);
    }

    private void initConfigurationCache() {
        logger.info("Initializing configuration cache data...");
        Map<String, String> configurationMap = Maps.newHashMap();
        List<Configuration> configurations = configurationRepository.findAll();
        if (!CollectionUtils.isEmpty(configurations)) {
            for (Configuration configuration : configurations) {
                configurationMap.put(configuration.getKey(), configuration.getValue());
            }
        }
        ConfigurationCache.pushConfigurations(configurationMap);
    }

}