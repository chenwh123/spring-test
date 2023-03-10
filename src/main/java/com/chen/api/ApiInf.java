package com.chen.api;

import com.chen.anno.DoRequest;
import org.springframework.stereotype.Component;

/**
 * @author chenwh3
 */
public interface ApiInf {

    @DoRequest("${base.temp}")
    String test();
}
