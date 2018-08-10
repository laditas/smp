package com.alipapa.smp.order.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipapa.smp.common.enums.*;
import com.alipapa.smp.common.request.UserInfo;
import com.alipapa.smp.common.request.UserStatus;
import com.alipapa.smp.consumer.controller.ConsumerController;
import com.alipapa.smp.consumer.pojo.Consumer;
import com.alipapa.smp.consumer.pojo.SysDict;
import com.alipapa.smp.consumer.pojo.UserConsumerRelation;
import com.alipapa.smp.consumer.service.ConsumerService;
import com.alipapa.smp.consumer.service.SysDictService;
import com.alipapa.smp.consumer.service.UserConsumerRelationService;
import com.alipapa.smp.consumer.vo.SysDictVo;
import com.alipapa.smp.order.pojo.AgentOrderDetail;
import com.alipapa.smp.order.pojo.Order;
import com.alipapa.smp.order.pojo.SelfOrderDetail;
import com.alipapa.smp.order.pojo.SubOrder;
import com.alipapa.smp.order.service.OrderServiceProxy;
import com.alipapa.smp.product.pojo.Product;
import com.alipapa.smp.product.pojo.ProductCategory;
import com.alipapa.smp.product.pojo.ProductPicture;
import com.alipapa.smp.product.service.ProductCategoryService;
import com.alipapa.smp.product.service.ProductPictureService;
import com.alipapa.smp.product.service.ProductService;
import com.alipapa.smp.user.pojo.User;
import com.alipapa.smp.user.service.UserService;
import com.alipapa.smp.utils.OrderNumberGenerator;
import com.alipapa.smp.utils.PriceUtil;
import com.alipapa.smp.utils.StringUtil;
import com.alipapa.smp.utils.WebApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.alipapa.smp.utils.WebApiResponse.error;

/**
 * 订单管理接口
 *
 * @author liuwei
 * @version 2.0
 * 2018-08-08
 */

@RestController
@CrossOrigin
@RequestMapping("/api/order")
public class OrderController {
    private static Logger logger = LoggerFactory.getLogger(ConsumerController.class);

    @Autowired
    private SysDictService sysDictService;


    @Autowired
    private UserService userService;


    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private UserConsumerRelationService userConsumerRelationService;

    @Autowired
    private OrderServiceProxy orderServiceProxy;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private ProductPictureService productPictureService;


    /**
     * 订单相关下拉列表
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/orderSelect", method = RequestMethod.GET)
    public WebApiResponse<List<SysDictVo>> orderSelect(@RequestParam("categoryCode") Integer categoryCode) {
        if (categoryCode == null || CategoryCode.valueOf(categoryCode) == null) {
            return WebApiResponse.error("参数有误！");
        }
        List<SysDictVo> sysDictVoList = new ArrayList<>();

        List<SysDict> sysDictList = sysDictService.listSysDictLikeText(OrderCategoryCode.valueOf(categoryCode).getCodeName(), null);
        if (!CollectionUtils.isEmpty(sysDictList)) {
            for (SysDict sysDict : sysDictList) {
                SysDictVo sysDictVo = new SysDictVo();
                sysDictVo.setId(sysDict.getId());
                sysDictVo.setCategoryCode(sysDict.getCategoryCode());
                sysDictVo.setDictText(sysDict.getDictText());
                sysDictVo.setDictValue(sysDict.getDictValue());
                sysDictVoList.add(sysDictVo);
            }
        }
        return WebApiResponse.success(sysDictVoList);
    }


    /**
     * 创建订单
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/createOrder", method = RequestMethod.POST)
    public WebApiResponse<String> createOrder(HttpServletRequest request) {
        UserInfo userInfo = UserStatus.getUserInfo();

        try {
            //不能为空
            String orderType = request.getParameter("orderType");
            String consumerNo = request.getParameter("consumerNo");
            String buyerUserNo = request.getParameter("buyerUserNo");
            String productionCycle = request.getParameter("productionCycle");
            String currency = request.getParameter("currency");
            String productAmount = request.getParameter("productAmount");
            String expectPurchaseAmount = request.getParameter("expectPurchaseAmount");
            String products = request.getParameter("products");

            //可为空
            String orderVolume = request.getParameter("orderVolume");
            String orderWeight = request.getParameter("orderWeight");
            String remark = request.getParameter("remark");

            if (StringUtil.isEmptyString(orderType) || StringUtil.isEmptyString(consumerNo) || StringUtil.isEmptyString(currency) || StringUtil.isEmptyString(productAmount)
                    || StringUtil.isEmptyString(expectPurchaseAmount) || StringUtil.isEmptyString(products)) {
                return error("缺少必填参数");
            }

            OrderTypeEnum orderTypeEnum = OrderTypeEnum.valueOf(orderType);
            if (orderTypeEnum == null) {
                return error("订单类型有误");
            }

            Order order = new Order();
            order.setActualPurchaseAmount(null);
            if (OrderTypeEnum.AGENT_ORDER == orderTypeEnum) {
                buyerUserNo = userInfo.getUserNo();
            }

            User buyer = userService.getUserByUserNo(buyerUserNo);
            if (buyer == null) {
                return error("采购员不存在");
            }
            order.setBuyerUserNo(buyer.getUserNo());
            order.setBuyerUserName(buyer.getName());
            order.setCnReceiptAmount(0L);

            Consumer consumer = consumerService.getConsumerByConsumerNo(consumerNo);
            if (consumer == null) {
                return error("客户不存在");
            }
            order.setConsumerCountry(consumer.getCountry());
            order.setConsumerName(consumer.getName());
            order.setConsumerNo(consumerNo);

            order.setCreatedTime(new Date());
            order.setCurrency(currency);
            order.setExpectPurchaseAmount(PriceUtil.convertToFen(expectPurchaseAmount));
            order.setOrderAmount(PriceUtil.convertToFen(productAmount)); //创建订单时订单金额暂等于产品总金额
            order.setOrderStatus(OrderStatusEnum.UN_SUBMIT.getCode());

            String orderNo = "M" + OrderNumberGenerator.get();
            order.setOrderNo(orderNo);
            order.setOrderType(orderTypeEnum.getCode());
            order.setOrderVolume(orderVolume);
            order.setOrderWeight(orderWeight);
            order.setPayStatus(OrderPayStatusEnum.UN_PAY.getCode());
            order.setProductAmount(PriceUtil.convertToFen(productAmount));
            order.setProductionCycle(productionCycle);
            order.setReceiptAmount(0L);
            order.setRemark(remark);

            User saler = userService.getUserByUserNo(userInfo.getUserNo());
            if (saler == null) {
                return error("业务员不存在");
            }
            order.setSalerUserNo(userInfo.getUserNo());
            order.setSalerUserName(saler.getName());
            //order.setSubmitTime();
            order.setUpdatedTime(new Date());

            List<SubOrder> subOrderList = new ArrayList<>();

            JSONArray productsArray = JSONArray.parseArray(products);
            for (int i = 0; i < productsArray.size(); i++) {
                JSONObject jsonObject = productsArray.getJSONObject(i);
                Long productCategoryId = jsonObject.getLong("productCategoryId");
                Long productId = jsonObject.getLong("productId");
                String subProductAmount = jsonObject.getString("productAmount");
                String subExpectPurchaseAmount = jsonObject.getString("expectPurchaseAmount");
                String productRemark = jsonObject.getString("productRemark");

                if (productCategoryId == null || productId == null) {
                    return error("产品缺少必填参数");
                }

                Product product = productService.getProductById(productId);
                ProductCategory productCategory = productCategoryService.getProductCategoryById(productCategoryId);

                List<ProductPicture> productPictureList = productPictureService.listProductPictureByProductId(productId);

                if (product == null || productCategory == null) {
                    return error("产品参数异常");
                }

                SubOrder subOrder = new SubOrder();
                subOrder.setActualPurchaseAmount(null);
                subOrder.setCreatedTime(new Date());
                subOrder.setExpectPurchaseAmount(PriceUtil.convertToFen(subExpectPurchaseAmount));
                subOrder.setPayedAmount(0L);
                subOrder.setOrderNo(orderNo);


                if (!CollectionUtils.isEmpty(productPictureList)) {
                    ProductPicture productPicture = productPictureList.get(0);
                    subOrder.setMiniPic(productPicture.getPicName());
                    subOrder.setPic(productPicture.getPicName());
                }

                subOrder.setProductAmount(PriceUtil.convertToFen(subProductAmount));
                subOrder.setProductCategoryId(productCategoryId);
                subOrder.setProductCategory(productCategory.getCategoryName());
                subOrder.setPayedAmount(0L);
                subOrder.setProductFrontAmount(null);
                subOrder.setProductId(productId);
                subOrder.setProductName(product.getProductName());
                subOrder.setProductRemark(productRemark);
                subOrder.setRemark(null);

                String subOrderNo = "S" + OrderNumberGenerator.get();
                subOrder.setSubOrderNo(subOrderNo);
                subOrder.setSubOrderStatus(0);
                subOrder.setSubPayStatus(0);
                subOrder.setUpdatedTime(new Date());

                if (OrderTypeEnum.SELF_ORDER == orderTypeEnum) {
                    String weight = jsonObject.getString("weight");
                    String material = jsonObject.getString("material");
                    String size = jsonObject.getString("size");
                    String color = jsonObject.getString("color");
                    String suturing = jsonObject.getString("suturing");
                    String printing = jsonObject.getString("printing");
                    Integer quantity = jsonObject.getInteger("quantity");
                    String saleAmount = jsonObject.getString("saleAmount");
                    String factoryAmount = jsonObject.getString("factoryAmount");

                    SelfOrderDetail selfOrderDetail = new SelfOrderDetail();
                    selfOrderDetail.setColor(color);
                    selfOrderDetail.setCreatedTime(new Date());
                    selfOrderDetail.setFactoryAmount(PriceUtil.convertToFen(factoryAmount));
                    selfOrderDetail.setMaterial(material);
                    selfOrderDetail.setPrinting(printing);
                    selfOrderDetail.setQuantity(quantity);
                    selfOrderDetail.setSaleAmount(PriceUtil.convertToFen(saleAmount));
                    selfOrderDetail.setRemark(null);
                    selfOrderDetail.setSubOrderNo(subOrderNo); //创建时补充
                    selfOrderDetail.setSize(size);
                    selfOrderDetail.setSuturing(suturing);
                    selfOrderDetail.setUpdatedTime(new Date());
                    selfOrderDetail.setWeight(weight);
                    subOrder.setSelfOrderDetail(selfOrderDetail);
                } else {
                    String saleAmount = jsonObject.getString("saleAmount");
                    String factoryAmount = jsonObject.getString("factoryAmount");
                    String unit = jsonObject.getString("unit");
                    Integer singlePackageCount = jsonObject.getInteger("singlePackageCount");
                    Integer packageNumber = jsonObject.getInteger("packageNumber");
                    String singleVolume = jsonObject.getString("singleVolume");
                    String totalVolume = jsonObject.getString("totalVolume");
                    String singleWeight = jsonObject.getString("singleWeight");
                    String totalWeight = jsonObject.getString("totalWeight");

                    AgentOrderDetail agentOrderDetail = new AgentOrderDetail();
                    agentOrderDetail.setCreatedTime(new Date());
                    agentOrderDetail.setFactoryAmount(PriceUtil.convertToFen(factoryAmount));
                    agentOrderDetail.setPackageNumber(packageNumber);
                    agentOrderDetail.setSaleAmount(PriceUtil.convertToFen(saleAmount));
                    agentOrderDetail.setSaleNo("SN" + product.getId() + consumer.getConsumerNo());
                    agentOrderDetail.setSinglePackageCount(singlePackageCount);
                    agentOrderDetail.setSingleVolume(singleVolume);
                    agentOrderDetail.setSingleWeight(singleWeight);
                    agentOrderDetail.setSubOrderNo(subOrderNo);
                    agentOrderDetail.setTotalVolume(totalVolume);
                    agentOrderDetail.setTotalWeight(totalWeight);
                    agentOrderDetail.setUnit(unit);
                    agentOrderDetail.setUpdatedTime(new Date());
                    subOrder.setAgentOrderDetail(agentOrderDetail);
                }

                subOrderList.add(subOrder);
            }

            boolean flag = orderServiceProxy.CreateOrder(order, subOrderList);
            if (flag) {
                //TODO 更新客户跟进时间及是否下单
                UserConsumerRelation userConsumerRelation = userConsumerRelationService.getRelationByConsumerIsDel(saler.getId(), consumer.getId(), null);
                userConsumerRelation.setFollowTime(new Date());
                userConsumerRelation.setHasOrder(1);
                userConsumerRelationService.updateUserConsumerRelation(userConsumerRelation);
                return WebApiResponse.success("success");
            }
        } catch (Exception ex) {
            logger.error("", ex);
            return error("创建订单异常");
        }
        return WebApiResponse.error("创建订单异常");
    }


}
