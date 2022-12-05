package org.starcoin.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.starcoin.api.*;
import org.starcoin.utils.MoveScanClient;
import org.starcoin.utils.SwapApiClient;

import java.net.MalformedURLException;
import java.net.URL;

@SpringBootApplication(scanBasePackages = "org.starcoin")
@EntityScan("org.starcoin.bean")
public class SwapInfoApplication {
    private static final Logger logger = LoggerFactory.getLogger(SwapInfoApplication.class);
    @Value("${starcoin.swap.api.url}")
    private String swapAPIUrl;

    @Value("${starcoin.movescan.api.url}")
    private String moveScanAPIUrl;

    public static void main(String[] args){
        SpringApplication.run(SwapInfoApplication.class, args);
    }

    @Bean(name = "base_url")
    URL baseUrl(@Value("${starcoin.seeds}") String host) {
        try {
            return new URL("https://" + host + ":9850");
        } catch (MalformedURLException e) {
            logger.error("get base url error:", e);
        }
        return null;
    }

    @Bean
    TransactionRPCClient transactionRPCClient(URL baseUrl) {
        return new TransactionRPCClient(baseUrl);
    }

    @Bean
    BlockRPCClient blockRPCClient(URL baseUrl) {
        return new BlockRPCClient(baseUrl);
    }

    @Bean
    StateRPCClient stateRPCClient(URL baseUrl) {
        return new StateRPCClient(baseUrl);
    }

    @Bean
    TokenContractRPCClient tokenContractRPCClient(URL baseUrl) {
        return new TokenContractRPCClient(baseUrl);
    }

    @Bean
    ContractRPCClient contractRPCClient(URL baseUrl) {
        return new ContractRPCClient(baseUrl);
    }

    @Bean
    SwapApiClient swapApiClient() {
        try {
            URL swapUrl = new URL(swapAPIUrl);
            return new SwapApiClient(swapUrl.getProtocol(), swapUrl.getHost());
        } catch (MalformedURLException e) {
            logger.error("get swap api url error:", e);
        }
        return null;
    }

    @Bean
    MoveScanClient moveScanClient() {
        try {
            URL moveUrl = new URL(moveScanAPIUrl);
            return new MoveScanClient(moveUrl.getProtocol(), moveUrl.getHost(), moveUrl.getPort());
        } catch (MalformedURLException e) {
            logger.error("get move scan api url error:", e);
        }
        return null;
    }
}