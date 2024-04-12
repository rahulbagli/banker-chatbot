package com.example.bankerchatbot.model.product;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlanType {
    private String planType;
    private List<PlanUse> planUseList;
}
