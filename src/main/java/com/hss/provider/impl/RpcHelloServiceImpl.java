package com.hss.provider.impl;

import com.hss.provider.RpcHelloService;

public class RpcHelloServiceImpl implements RpcHelloService {

    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
