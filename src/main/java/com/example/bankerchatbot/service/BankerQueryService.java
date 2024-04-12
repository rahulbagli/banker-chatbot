package com.example.bankerchatbot.service;

import com.example.bankerchatbot.model.QueryResponse;

public interface BankerQueryService {
    QueryResponse processQuery(String query);
}
