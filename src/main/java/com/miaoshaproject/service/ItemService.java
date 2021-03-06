package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

public interface ItemService {

    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    List<ItemModel> lisItem();

    ItemModel getItemById(Integer id);

    boolean decreaseStock(Integer itemId,Integer amount)throws BusinessException;

    void increaseSales(Integer itemId,Integer amount)throws BusinessException;
}
