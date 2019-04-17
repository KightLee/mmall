package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service(value = "iUserService")
public class UserServiceimpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public ServiceResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount==0){
            return ServiceResponse.createByErrorMessage("用户名不存在");
        }
        //加校验,使用MD5校验MD5
        String Md5Password = MD5Util.MD5EncodeUtf8(password);
        //密码登录md
        User user = userMapper.selectLogin(username, Md5Password);
        if(user==null){
            return ServiceResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServiceResponse.createBySuccess(user,"登录成功");
    }

    @Override
    public ServiceResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)){
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if (resultCount>0){
                    return ServiceResponse.createByErrorMessage("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if (resultCount>0){
                    return ServiceResponse.createByErrorMessage("email已存在");
                }
            }

        }else {
            return ServiceResponse.createByErrorMessage("参数错误");
        }
        return ServiceResponse.createBySuccess("校验成功");
    }
    /*
    找回问题
     */
    @Override
    public ServiceResponse selectQuestion(String username) {
        ServiceResponse validResponse = this.checkValid(username, Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServiceResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServiceResponse.createBySuccess(question);
        }
        return ServiceResponse.createByErrorMessage("找回密码的问题是空的");
    }

    //注册
    @Override
    public ServiceResponse<String> register(User user) {
        ServiceResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        //校验的status没通过
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        //都通过则加密后传入数据库
        user.setRole(Const.Role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount==0){
            return ServiceResponse.createByErrorMessage("注册失败");
        }
        return ServiceResponse.createByErrorMessage("注册成功");
    }

    /*
    检查答案
     */
    public ServiceResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username,question,answer);
        //进了判断就是答案正确
        if(resultCount>0){
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServiceResponse.createBySuccess(forgetToken);
        }
        return ServiceResponse.createByErrorMessage("问题的答案错误");
    }
    public ServiceResponse<String> forgetResetPassword(String username,String password,String forgetToken){
        if(StringUtils.isNotBlank(forgetToken)){
            return ServiceResponse.createByErrorMessage("需要传递token");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(StringUtils.isNotBlank(token)){
            return ServiceResponse.createByErrorMessage("token无效或过期");
        }
        if(StringUtils.equals(forgetToken,token)){
            String MD5password = MD5Util.MD5EncodeUtf8(password);
            int rowResult = userMapper.updatePasswordByUsername(username,MD5password);
            if(rowResult>0){
                return ServiceResponse.createBySuccess("修改密码成功");
            }
        }else{
            return ServiceResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }
        return ServiceResponse.createByErrorMessage("修改密码失败");
    }
    public ServiceResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //不带id的话等下通过接口测试就能测到密码
        int resultCout = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCout==0){
            return ServiceResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCout = userMapper.updateByPrimaryKeySelective(user);
        if(updateCout>0){
            return ServiceResponse.createSuccessMessage("密码更新成功");
        }else{
            return ServiceResponse.createByErrorMessage("密码更新失败");
        }
    }
    public ServiceResponse<User> updateInformation(User user){
        //username不能更新
        int resultCout = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCout>0){
            return ServiceResponse.createByErrorMessage("Email已经存在，请更换email再更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateCout = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCout>0) {
            return ServiceResponse.createBySuccess(user, "更新个人信息成功");
        }
        return ServiceResponse.createByErrorMessage("更新个人信息失败");
    }
    public ServiceResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user==null){
            return ServiceResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServiceResponse.createBySuccess(user);
    }
    //检查是不是管理员
    public ServiceResponse checkAdminRole(User user){
        if(user!=null&&user.getRole().intValue()==Const.Role.ROLE_ADMIN){
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByError();
    }

}
