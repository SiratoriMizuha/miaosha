package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderDOMapper;
import com.miaoshaproject.dao.SequenceDoMapper;
import com.miaoshaproject.dataobject.OrderDO;
import com.miaoshaproject.dataobject.SequenceDo;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDoMapper sequenceDoMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId,Integer promoId, Integer amount) throws BusinessException {
        ItemModel itemModel= itemService.getItemById(itemId);
        if (itemModel==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }

        UserModel userModel=userService.getUserById(userId);
        if (userModel==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户不存在");
        }

        if (amount<0||amount>99){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"数量信息不正确");
        }

        if (promoId!=null){
            if (promoId.intValue()!=itemModel.getPromoModel().getId()){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动信息不正确");
            }else if (itemModel.getPromoModel().getStatus().intValue()!=2){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动信息还未开始");
            }
        }

        boolean result=itemService.decreaseStock(itemId,amount);
        if (!result){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        OrderModel orderModel=new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if (promoId!=null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoPrice());
        }else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        orderModel.setId(generateOrderNo());
        OrderDO orderDO=convertFromOrderModel(orderModel);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        orderDOMapper.insertSelective(orderDO);

        itemService.increaseSales(itemId,amount);

        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW )
    private String generateOrderNo(){
        StringBuilder stringBuilder=new StringBuilder();

        LocalDateTime now=LocalDateTime.now();
        String nowDate= now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);


        int sequence=0;
        SequenceDo sequenceDo= sequenceDoMapper.getSequenceByName("order_info");
        sequence= sequenceDo.getCurrentValue();
        sequenceDo.setCurrentValue(sequenceDo.getCurrentValue()+sequenceDo.getStep());
        sequenceDoMapper.updateByPrimaryKeySelective(sequenceDo);
        String sequenceStr=String.valueOf(sequence);
        for (int i=0;i<6-sequenceStr.length();i++){
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);

        stringBuilder.append("00");
        return stringBuilder.toString();
    }


    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if (orderModel==null){
            return null;
        }
        OrderDO orderDO=new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);


        return orderDO;

    }
}
