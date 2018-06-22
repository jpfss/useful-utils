package com.example.utils.config.webflux.errorhandler;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter4;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * RestTemplate配置
 *
 * @author chen.qian
 * @date 2018/6/13
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate template = new RestTemplate();
        SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory) template.getRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);

        //支持模拟表单提交
        FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
        formHttpMessageConverter.setSupportedMediaTypes(Arrays
                .asList(new MediaType("text", "html", Charset.forName("UTF-8")),
                        MediaType.APPLICATION_FORM_URLENCODED));

        //解决中文乱码
        StringHttpMessageConverter stringMessageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringMessageConverter.setSupportedMediaTypes(Collections
                .singletonList(new MediaType("text", "html", Charset.forName("UTF-8"))));

        FastJsonHttpMessageConverter4 jsonConverter = new FastJsonHttpMessageConverter4();
        //这里配置支持text/plain格式的解析，这样可以直接返回JSONObject对象
        jsonConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON_UTF8,
                MediaType.APPLICATION_JSON,
                MediaType.TEXT_PLAIN, MediaType.TEXT_HTML));
        List<HttpMessageConverter<?>> messageConverters = template.getMessageConverters();

        //解决中文乱码问题
        messageConverters.removeIf(httpMessageConverter -> httpMessageConverter instanceof StringHttpMessageConverter);
        messageConverters.add(stringMessageConverter);
        //由于默认存在jackson的converter所以这里设置fastJson的在前面，不然可能会由于顺序问题出错
        messageConverters.add(0, jsonConverter);
        messageConverters.add(formHttpMessageConverter);
        template.setMessageConverters(messageConverters);
        return template;
    }

}
