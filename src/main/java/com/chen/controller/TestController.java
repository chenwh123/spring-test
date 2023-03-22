package com.chen.controller;

import com.chen.api.ApiInf;
import com.chen.controller.base.BaseController;
import com.chen.mapper.UmsAdminMapper;
import com.chen.model.api.R;
import com.chen.model.entity.UmsAdmin;
import com.chen.model.vo.SearchVo;
import com.chen.service.impl.UmsAdminServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author chenwh3
 */
@RestController
@RequestMapping("/test/")
@Api(value = "", tags = "")
public class TestController  {

    @Resource
    private ApiInf apiInf;

    @PostMapping("/test")
    public R test() {
        return R.data(apiInf.test(null));
    }


}
