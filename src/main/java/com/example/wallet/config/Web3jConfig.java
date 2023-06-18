package com.example.wallet.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import com.example.wallet.config.properties.Web3jProperties;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EnableConfigurationProperties(Web3jProperties.class)
@Configuration
public class Web3jConfig {
    private static final Logger logger = LogManager.getLogger(Web3jConfig.class);
    private final Web3jProperties blockchainProperties;
    private final Map<String, Web3j> web3jMap = new HashMap<>();

    @Autowired
    public Web3jConfig(Web3jProperties blockchainProperties) {
        this.blockchainProperties = blockchainProperties;
        loadWeb3jInstances();
    }

    private void loadWeb3jInstances() {
        logger.info("Loading Web3j instances...");
        blockchainProperties.getBlockchains().forEach(blockchainInfo -> {
            logger.info("Loading Web3j instance for [{}]", blockchainInfo.getNetworkName());
            Web3j web3j = Web3j.build(new HttpService(blockchainInfo.getProviderUrl()));
            web3jMap.put(blockchainInfo.getNetworkName(), web3j);
            Runtime.getRuntime().addShutdownHook(new Thread(web3j::shutdown));
        });
    }

    @Bean
    public Map<String, Web3j> getWeb3jInstanceMap() {
        return web3jMap;
    }
}

