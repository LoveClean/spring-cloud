package cn.itcast.order.service;

import cn.itcast.feign.clients.UserClient;
import cn.itcast.feign.pojo.User;
import cn.itcast.order.mapper.OrderMapper;
import cn.itcast.order.pojo.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Service
public class OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private UserClient userClient;

    public Order queryOrderById(Long orderId) {
        // 1.查询订单
        Order order = orderMapper.findById(orderId);
        // 2.Feign远程调用
        User user = userClient.findById(order.getUserId());
        // 3.封装到order对象
        order.setUser(user);
        // 4.返回
        return order;
    }


//    @Autowired
//    private RestTemplate restTemplate;
//
//    public Order queryOrderById(Long orderId) {
//        // 1.查询订单
//        Order order = orderMapper.findById(orderId);
//        // 2.利用RestTemplate，发起HTTP请求，查询用户
//        String url = "http://userservice/user/" + order.getUserId();
//        User user = restTemplate.getForObject(url, User.class);
//        // 3.封装到order对象
//        order.setUser(user);
//        // 4.返回
//        return order;
//    }
}
