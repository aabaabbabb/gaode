package com.jinyan.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jinyan.common.Constant;

public class GenerateSignUtils {

    public static void main(String[] args) throws Exception {
        Map<String, String> paramMap = new HashMap<String, String>();

        // 构造业务参数
        JSONObject bizContent = new JSONObject();
        bizContent.put("source", "example_api");
        bizContent.put("merchantId", "8888777711119999");
        bizContent.put("shopInfos", new String[]{});
        paramMap.put("biz_content", JSON.toJSONString(bizContent));

        // 构造参与加签的公共参数
        paramMap.put("app_id", "202210130194031937");
        paramMap.put("utc_timestamp", String.valueOf(System.currentTimeMillis()));
        paramMap.put("version", "1.0");
        paramMap.put("charset", "UTF-8");
        paramMap.put("method", "amap.brand.merchant.createShop");
        paramMap.put("sign", generateSign(paramMap));
        paramMap.put("sign_type", "RSA2");

        //打印最终请求高德参数
        System.out.println(paramMap);
    }

    /**
     * 使用商家私钥生成签名
     *
     * @param paramMap
     * @return
     * @throws Exception
     */
    public static String generateSign(Map<String, String> paramMap) throws Exception {
        //使用商家私钥进行加签，请在高德云店「接入准备及配置」页面生成并获取商家私钥
       // String merchantPrivateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCiAmPyV51mk91V97bpfPIyTLFuESm+b6d/yu2gZ7UbBdvKdo32j+hDSFeche8MPiV2yDReb7aKyXUnmi1pciUr6aVTwZnUJTF9lxNg3QgD7wMSRiZZNGJnHRB8TJd9NWnwpMS7/GA+SnQtsNsuJvZqxxo1uulCfropoJH7WHdUHoj65v65lUOi/4/2HxQinW8BmFuchSp2JUwho2SYNIoIqNk+e+BWl0kWhrt2wiCoXgTJcDYeb+imGNsuhi+SG8tSEHqXNm4d5SH2NcM8zxsvbnZ+DiCXIYb/FYX2UKsmSowq6ofWpSCNCL7tXzeYqSIQa2nWgiz6bCtpMz6LB6ibAgMBAAECggEAKaa1rWtrEyE2yVBLXVH7LoVbJHITKluoU4mHeDLRc+YYsL+0Fcy/qPSjWaFxaNNdbGFiXVkZNCtBDHJt9GsdNWH3WEYw10LMq344xDFvSxuazYrtdwepuNrXDy3d0Jn3559k2PO0hq87mTeb0gzAU4lmqqOoLqlIEUWv4k/HNf/N1i9Lk81Znfi0/ER5UOOgYkhv+8yPUHTmp0iFGEVAm0uOxERadLBV/3W6O681ZNjPHwKLYh/ObY7fY3nE2a8ZrIk6/ClOs7U6BVvh7udZOasoCsjk32qcpwk70zxrNBjEzBJ8Ig2Gt7BjzqY6Uk/MpeVp80S3xkJl0Eg6b5gY2QKBgQDsQl3o+FdH8mkhWK6HlILZ+5crCVZnugPlF4jc2/EWCVrTa3wEEV4RcIoNFBdOW3BX33hjPKs12PNJT3nRe2dx2mF1NVjbisPcJzLHraIXTKrE1IzLcrBeDrEpYLZ7dBiUm89vkYPnr46BFP/86UHfKovfCp6AQIdysYh0TV0CdQKBgQCvi82RJF4Q6R5m4bqMkJTP2ur1Csejhs/uiOiFkN1iFzegTVKgCqeW+i4QzfGfkVAClEe8sFcRIk2m3SGqDPsxUN1x/yWnZ7jw7WKvD9UeAO74Y9uehgodWf2T7Z28J5z2lsojpFq4CKgWwEJJGIrgS4DjI03g6sjPOWvgeNp8zwKBgQDZa23u0nPIEy5qcKkUvjf5EI4aHdq1VDr3XcQmtAVWaT97ZuPW/oCZ/f4dcQrNolptk4q/kHpi8IdbRNdvT2hwHy8m5PQj/bym4i/aEFElrflNb+kt5RqnRFAHKyhjzgA86nJoUvqXgTjcU44MmkBzdj0IAkbA8iF0DCruFK/ubQKBgQCCPZgLek7r/vkWXZ0ZUt+FgydVHGVXttqoYB0bk4ocj5surI+Du8PxKLZqs1D0EUyqTTcYEEDYrnmsNl4Wm0D2qsdRH3rhQ/wbjBPuTDHS6+Bgmz3C1DHG6xBO4zlm8oDrFp7saLEB3zJOht/m+XV3yazbWEMcfzTzQef+h8uaUwKBgQC5hdPE4A+reEsagP+DSQOwX6+KPoR5Ory6KeButrFX03MEIOg6FZ2/ir5CbEc1DbOIAtgYxqWAGIRx9GFbCGiqvDZ4TYQt2xJU7JMndepFBCuN4vABdm1eWMRcZ7a8XllJQOgTx3gkTLjNjqIx7uzMRvQN8b5dUGGRz3wf1OBRRA==";
        String signContent = getSignContent(paramMap);
        return getSign(signContent, Constant.PRIVATE_KEY);
    }

    /**
     * 参数转换为待加签字符串
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
     * 字符串加签
     *
     * @param signContent        待加密的参数字符串
     * @param merchantPrivateKey 商家应用私钥
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private static String getSign(String signContent, String merchantPrivateKey) throws IOException, GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] encodedKey = readText(new ByteArrayInputStream(merchantPrivateKey.getBytes())).getBytes();
        encodedKey = Base64.getDecoder().decode(encodedKey);
        PrivateKey priKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));

        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(priKey);
        signature.update(signContent.getBytes("UTF-8"));
        byte[] signed = signature.sign();
        return new String(Base64.getEncoder().encode(signed));
    }

    private static String readText(InputStream in) throws IOException {
        Reader reader = new InputStreamReader(in);
        StringWriter writer = new StringWriter();

        int bufferSize = 4096;
        char[] buffer = new char[bufferSize];
        int amount;
        while ((amount = reader.read(buffer)) >= 0) {
            writer.write(buffer, 0, amount);
        }
        return writer.toString();
    }
}


