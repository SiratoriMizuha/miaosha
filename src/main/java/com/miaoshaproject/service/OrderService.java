package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.OrderModel;
import org.springframework.stereotype.Service;

public interface OrderService {
    OrderModel createOrder(Integer userId,Integer itemId,Integer promoId, Integer amount) throws BusinessException;

}
