package com.example.bankerchatbot.configuration;

import com.example.bankerchatbot.model.ProductAndPlan;
import com.example.bankerchatbot.model.QueryProductAttributes;
import com.example.bankerchatbot.model.QueryResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class BankerConfig {

//    @Bean
//    @Scope("prototype")
//    public QueryProductAttributes queryProductAttributesBean() {
//        return new QueryProductAttributes();
//    }
//
//    @Bean
//    @Scope("prototype")
//    public ProductAndPlan productAndPlanBean(){
//        return new ProductAndPlan();
//    }

    @Bean
    @Scope("prototype")
    public QueryResponse queryResponseBean(){
        return new QueryResponse();
    }
}
