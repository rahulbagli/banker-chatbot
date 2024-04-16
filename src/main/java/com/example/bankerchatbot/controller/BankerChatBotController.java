package com.example.bankerchatbot.controller;

import com.example.bankerchatbot.model.QueryResponse;
import com.example.bankerchatbot.service.BankerQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;

@RestController
@CrossOrigin(origins = "*")
public class BankerChatBotController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    @Autowired
    private BankerQueryService bankerQueryService;

    @RequestMapping(value = "/query",
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = { MediaType.APPLICATION_JSON_VALUE },
            method = RequestMethod.POST)
    public ResponseEntity<QueryResponse> productQueryPost(@RequestBody String query) {

        LOGGER.info("Query: "+query);
        return ResponseEntity.ok(bankerQueryService.processQuery(query));
    }
}
