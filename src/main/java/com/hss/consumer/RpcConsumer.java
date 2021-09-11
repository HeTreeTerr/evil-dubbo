package com.hss.consumer;

import com.hss.consumer.proxy.RpcProxy;
import com.hss.provider.RpcHelloService;

public class RpcConsumer {

    //消费者
    public static void main(String[] args) {
        //  IRpcHelloService rpcHello = new RpcHelloServiceImpl(); 不能直接new
        RpcHelloService rpcHello = RpcProxy.create(RpcHelloService.class); //用rpc的方式，调用到远程的服务
        System.out.println(rpcHello.hello("wei"));
    }
}
