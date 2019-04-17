package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Product;

public interface IProductService {
    ServiceResponse saveOrupdateProduct(Product product);
    ServiceResponse<String> setSaleStatus(Integer productId,Integer status);
    ServiceResponse<Object> manageProductDetail(Integer productId);
    ServiceResponse getProductList(int pageNum,int pageSize);
    ServiceResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);
}
