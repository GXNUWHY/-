package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            //生成随机验证码，保存到session
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            //发送短信（TODO）
//            SMSUtils.sendMessage("瑞吉外卖", "", phone, code);

//            session.setAttribute(phone, code);

            //将生成的验证码缓存到redis中，并设置有效期5分钟
            ValueOperations valueOperations = redisTemplate.opsForValue();
            valueOperations.set(phone, code, 5L, TimeUnit.MINUTES);

            log.info("【瑞吉外卖】您正在注册瑞吉外卖短信平台账号，验证码是："+ code +"，5分钟内有效，请及时输入。");
            return R.success("手机验证码短信发送成功");
        }
        return R.error("短信发送失败");
    }


    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

//        Object codeInSession = session.getAttribute(phone);

        //从redis中获取缓存的验证码
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object codeInSession = valueOperations.get(phone);

        if(null != codeInSession && codeInSession.equals(code)){//相同则登录成功
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper();
            lqw.eq(User::getPhone, phone);
            User user = userService.getOne(lqw);
            if(null == user){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
//            session.removeAttribute(phone);
            session.setAttribute("user", user.getId());

            //如果登录成功，删除redis缓存的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }

        return R.error("登录失败");
    }
}
