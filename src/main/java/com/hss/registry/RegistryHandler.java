package com.hss.registry;

import com.hss.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryHandler extends ChannelInboundHandlerAdapter {

    //用保存所有可用的服务
    public static ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<String,Object>();

    //保存所有相关的服务类
    private List<String> classNames = new ArrayList<String>();

    public RegistryHandler(){
        //完成递归扫描
        scannerClass("com.hss.provider");
        doRegister();
    }

    /**
     * 客户端远程调用时触发
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        InvokerProtocol request = (InvokerProtocol)msg;

        //当客户端建立连接时，需要从自定义协议中获取信息，拿到具体的服务和实参
        //使用反射调用
        if(registryMap.containsKey(request.getClassName())){
            Object clazz = registryMap.get(request.getClassName());
            Method method = clazz.getClass().getMethod(request.getMethodName(), request.getParames());
            result = method.invoke(clazz, request.getValues());
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    /**
     * 调用异常时触发
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


    /*
     * 递归扫描
     */
    private void scannerClass(String packageName){
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        //获取物理路径
        try {
            String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
            File dir = new File(filePath);
            for (File file : dir.listFiles()) {
                //如果是一个文件夹，继续递归
                if(file.isDirectory()){
                    scannerClass(packageName + "." + file.getName());
                }else{
                    classNames.add(packageName + "." + file.getName().replace(".class", "").trim());
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 完成注册
     */
    private void doRegister(){
        if(classNames.size() == 0){ return; }
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if(clazz.isInterface()){
                    continue;
                }else {
                    Class<?> i = clazz.getInterfaces()[0];
                    registryMap.put(i.getName(), clazz.newInstance());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
