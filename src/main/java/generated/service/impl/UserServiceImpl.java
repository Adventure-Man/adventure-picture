package generated.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import generated.entity.User;
import generated.mapper.UserMapper;
import generated.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author Administrator
 * @since 2025-07-06
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
