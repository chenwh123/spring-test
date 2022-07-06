package com.chen.controller;

import com.chen.controller.base.BaseController;
import com.chen.mapper.UmsAdminMapper;
import com.chen.model.api.R;
import com.chen.model.entity.UmsAdmin;
import com.chen.model.vo.SearchVo;
import com.chen.service.impl.UmsAdminServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenwh3
 */
@RestController
@RequestMapping("/ums/admin/")
@Api(value = "", tags = "")
public class UmsAdminController extends BaseController<UmsAdminServiceImpl, UmsAdminMapper, UmsAdmin> {


    @Override
    @ApiOperation(value = "批量保存", notes = "批量保存")
    @PostMapping("/batchAdd")
    public R batchAdd(@RequestBody List<UmsAdmin> list) {
        return super.batchAdd(list);
    }

    @Override
    @ApiOperation(value = "更新", notes = "删除")
    @PostMapping("/update")
    public R update(@RequestBody UmsAdmin obj) {
        return super.update(obj);
    }

    @Override
    @ApiOperation(value = "删除", notes = "删除")
    @PostMapping("/delete")
    public R delete(@RequestParam("id") String id) {
        return super.delete(id);
    }



    @Override
    @ApiOperation(value = "查询", notes = "更新")
    @PostMapping("/search")
    public R search(@RequestBody @Validated SearchVo obj) {
        return super.search(obj);
    }


    @Override
    @ApiOperation(value = "批量删除", notes = "批量删除")
    @PostMapping("/batchDelete")
    public R batchDelete(@RequestBody List<String> id) {
        return super.batchDelete(id);
    }
}
