package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
   @Autowired
   private ShoppingCartService shoppingCartService;

   @PostMapping("/add")
   public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

       //设置用户id
      Long currentId = BaseContext.getCurrentId();
      shoppingCart.setUserId(currentId);

      LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper();
      lqw.eq(ShoppingCart::getUserId, currentId);
      if(shoppingCart.getDishId() != null){
         //添加到购物车的是菜品
         lqw.eq(ShoppingCart::getDishId, shoppingCart.getDishId());

      }
      else {
         //添加到购物车的是套餐
         lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

      }
      
      //查询当前菜品或套餐是否在购物车
      // select * from shopping_cart where user_id = ?  and dish_id/setmeal_id = ?
      ShoppingCart cartServiceOne = shoppingCartService.getOne(lqw);
      if(cartServiceOne != null){
         //若存在，原数量+1
         Integer number = cartServiceOne.getNumber();
         cartServiceOne.setNumber(number+1);
         shoppingCartService.updateById(cartServiceOne);
      }
      else {
         //不存在则添加,默认为1
         shoppingCart.setNumber(1);
         shoppingCart.setCreateTime(LocalDateTime.now());
         shoppingCartService.save(shoppingCart);
         cartServiceOne = shoppingCart;
      }
       return R.success(cartServiceOne);
   }

   /**
    * 查看购物车
    * @return
    */
   @GetMapping("/list")
   public R<List<ShoppingCart>> list(){
      LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
      lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
      lqw.orderByDesc(ShoppingCart::getCreateTime);
      List<ShoppingCart> list = shoppingCartService.list(lqw);

      return R.success(list);
   }

   /**
    * 清空购物车
    * @return
    */
   @DeleteMapping("/clean")
   public R<String> clean(){
      //delete from shopping_cart where user_id
      LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
      lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
      shoppingCartService.remove(lqw);

      return R.success("清空购物车");
   }
}