package com.jinyan.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jinyan.common.Constant;

import java.io.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class CheckSignUtils {

    /**
     * 方法调用入口示例
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        //模拟高德调用商家参数
        Map<String, String> params = new HashMap<String, String>();
        JSONObject bizContent = new JSONObject();
        bizContent.put("source", "example_api");
        bizContent.put("merchantId", "8888777711119999");
        bizContent.put("shopId", "123");
        bizContent.put("cpid", "456");
        bizContent.put("shopStatus", 1);
        params.put("biz_content", JSON.toJSONString(bizContent));

        params.put("app_id", "202210130194031937");
        params.put("utc_timestamp", "1666936656706");
        params.put("version", "1.0");
        params.put("charset", "UTF-8");
        params.put("sign_type", "RSA2");
        params.put("method", "amap.brand.createShop.audit.callback");
        params.put("sign", "W3p3dwa9udsLwoX79xlQ9aZu4nQltYth47poHhnzUEgVsv4cj8te04C2Awmc3+1a8Q/8M82J9YluyO1NrSQ2tB30qFOQiWwwgCDUpDD/SZ8gb0v168qGQfe3SdV1d2hnKQH1t+vqOiQfFs5c5+cFi7Kdr7L8OjHcIg7Oeg6aPk/EVhJqeXqpqaW/S6FBAHUp2xl5kxrXlK8rd//RDLYe+y5qkGzngcJisyH82d0E/83TJMHv86DX0frrpMjwLRc9RmzdSLXzaPU3gQaPq1DqMNLJRzqcOtPPVk929LW4TP0aDNsDP/wmWwuR1T7fQ3lwJ3kBfP8VD5Rx1ZJx35W4gA==");

        //使用高德公钥进行验签
        boolean checkSignResult = checkSign(params);

        //打印验签结果
        System.out.println(checkSignResult);
    }

    /**
     * 使用高德公钥验证签名
     *
     * @param paramFromAmap
     * @return
     * @throws Exception
     */
    public static boolean checkSign(Map<String, String> paramFromAmap) throws Exception {
        //使用高德公钥进行验签，请在高德云店「API对接信息」页面直接获取高德公钥
       // String amapPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAle9Evy4y/5EDQfz5hxJL00B+OMnFEl/HUqEqLpkh0da/keZBNuxpoX6MJiF3cSTj8AukoMCcj8ycSX4iqprahveT68Y2+j/D/3XCeJxbeISBDb9+8qT+ff/Y8xtLxXRJGkmNZPDodLlqcI5rtR78yT9a985gzKPOWesauPesxdgRwcxyPZqDfAuqVoRGFMgJhVIg/fMYDq3hXT75yO4tE6DTlvdZmb8iHoxZ6hXms6tOfQEdiiXpjhautnHJcAsdw55kSvms1zOsdv68tw3Y2ogqm8Wg0ukI6tBxaBtI65gyxwM4GLW5b4Z6DbGg9KJUdAGPcqPs+QzvUl0mqXm2CQIDAQAB";
        String signContent = getSignContent(paramFromAmap);


        return checkSign(signContent, paramFromAmap.get("sign"), Constant.PUBLIC_KEY);
    }
    /**
     * 使用高德公钥验证签名
     *
     * @param paramFromAmap
     * @return
     * @throws Exception
     */
    public static boolean checkSign(Map<String, String> paramFromAmap,String sign) throws Exception {
        //使用高德公钥进行验签，请在高德云店「API对接信息」页面直接获取高德公钥
        // String amapPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAle9Evy4y/5EDQfz5hxJL00B+OMnFEl/HUqEqLpkh0da/keZBNuxpoX6MJiF3cSTj8AukoMCcj8ycSX4iqprahveT68Y2+j/D/3XCeJxbeISBDb9+8qT+ff/Y8xtLxXRJGkmNZPDodLlqcI5rtR78yT9a985gzKPOWesauPesxdgRwcxyPZqDfAuqVoRGFMgJhVIg/fMYDq3hXT75yO4tE6DTlvdZmb8iHoxZ6hXms6tOfQEdiiXpjhautnHJcAsdw55kSvms1zOsdv68tw3Y2ogqm8Wg0ukI6tBxaBtI65gyxwM4GLW5b4Z6DbGg9KJUdAGPcqPs+QzvUl0mqXm2CQIDAQAB";
        String signContent = getSignContent(paramFromAmap);
        return checkSign(signContent, sign, Constant.PUBLIC_KEY);
    }
    /**
     * 参数转换为待验签字符串
     *
     * @param paramMap 待生成加密sign的参数集合
     */
    private static String getSignContent(Map<String, String> paramMap) {
        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<>(paramMap.keySet());
        // 将参数集合排序
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            //排除不需要参与签名的公共参数
            if ("sign_type".equals(key) || "sign".equals(key) || "need_encrypt".equals(key)) {
                continue;
            }
            String value = paramMap.get(key);
            // 拼装所有非空参数
            if (key != null && !"".equalsIgnoreCase(key) && value != null && !"".equalsIgnoreCase(value)) {
                content.append(i == 0 ? "" : "&").append(key).append("=").append(value);
            }
        }
        return content.toString();
    }

    /**
     * 对加密字符串进行验签
     *
     * @param content       待验签内容
     * @param sign          待验证签名
     * @param amapPublicKey 高德公钥
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private static boolean checkSign(String content, String sign, String amapPublicKey) throws IOException, GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        StringWriter writer = new StringWriter();
        io(new InputStreamReader(new ByteArrayInputStream(amapPublicKey.getBytes())), writer);
        byte[] encodedKey = writer.toString().getBytes();
        encodedKey = org.apache.commons.codec.binary.Base64.decodeBase64(encodedKey);
        PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(pubKey);
        signature.update(content.getBytes("UTF-8"));
        return signature.verify(Base64.getDecoder().decode(sign.getBytes()));
    }

    public static void io(Reader in, Writer out) throws IOException {
        int bufferSize = 4096;
        char[] buffer = new char[bufferSize];
        int amount;
        while ((amount = in.read(buffer)) >= 0) {
            out.write(buffer, 0, amount);
        }
    }
}

