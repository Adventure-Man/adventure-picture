package com.adventure.picturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.adventure.picturebackend.common.utils.ErrorCode;
import com.adventure.picturebackend.common.exception.BusinessException;
import com.adventure.picturebackend.manager.StpKits;
import com.adventure.picturebackend.mapper.UserMapper;
import com.adventure.picturebackend.model.dto.user.UserQueryRequest;
import com.adventure.picturebackend.model.entity.User;
import com.adventure.picturebackend.model.enums.UserRoleEnum;
import com.adventure.picturebackend.model.vo.LoginUserVO;
import com.adventure.picturebackend.model.vo.UserVO;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryOrderBy;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.CacheableServiceImpl;
import com.adventure.picturebackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.adventure.picturebackend.common.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author Administrator
 * @since 2025-07-06
 */
@Service
@Slf4j
public class UserServiceImpl extends CacheableServiceImpl<UserMapper, User> implements UserService {
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            userObj = StpKits.SPACE.getSession().get(USER_LOGIN_STATE);
        }
        if (userObj == null) {
            throw new BusinessException(ErrorCode.LOGIN_AUTH_ERROR, "未登录");
        }
        // 移除登录态
        HttpSession session = request.getSession();
        if (session != null) {
            session.removeAttribute(USER_LOGIN_STATE);
        }
        StpKits.SPACE.logout();
        return true;
    }


    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }


    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            userObj = StpKits.SPACE.getTokenSession().get(USER_LOGIN_STATE);
        }
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.LOGIN_AUTH_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.LOGIN_AUTH_ERROR);
        }
        return currentUser;
    }


    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.getMapper().selectOneByQuery(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        // 先踢出之前所有登录设备 清除用户之前的所有 Token 和 Session：
        StpKits.SPACE.logoutByTokenValue(StpKits.SPACE.getTokenValue());
        // 记录登录态到Redis中，与springSession过期过期信息一致
        StpKits.SPACE.login(user.getId());
        StpKits.SPACE.getTokenSession().set(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 检查是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.getMapper().selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "tu_ku";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        LocalDateTime createTime = user.getCreateTime();
        userVO.setCreateTime(createTime);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            return new QueryWrapper();
        }
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("*")
                .eq("userRole", userRole, StrUtil.isNotBlank(userRole))
                .like("userAccount", userAccount, StrUtil.isNotBlank(userAccount))
                .like("userName", userName, StrUtil.isNotBlank(userName))
                .like("userProfile", userProfile, StrUtil.isNotBlank(userProfile));
        //指定排序字段
        if (StrUtil.isNotBlank(sortField)){
            queryWrapper.orderBy(new QueryOrderBy(new QueryColumn(sortField),sortOrder));
        }
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        // 判断用户是否是管理员
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }


}
