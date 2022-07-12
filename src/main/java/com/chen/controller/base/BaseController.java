package com.chen.controller.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.chen.model.api.R;
import com.chen.model.vo.SearchVo;
import com.chen.service.impl.BaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author chenwh3
 */
@Slf4j
public class BaseController<T extends BaseServiceImpl<M , O>,M extends BaseMapper<O>, O> {

    @Autowired
    protected T service;

    protected Class<O> currentModelClass() {
        return (Class<O>) ReflectionKit.getSuperClassGenericType(getClass(), BaseController.class, 2);
    }


    public R batchSave(List<O> list){
        return R.data(service.saveBatch(list));
    }

    public R batchAdd(List<O> list){
        for (O o : list) {
            service.save(o);
        }
        return R.data(true);
    }


    public R update(O obj){
        return R.data(this.service.updateById(obj));
    }


    public R delete(String id){
        return R.data(this.service.removeById(id));
    }

    public R batchDelete(List<String> ids){
        return R.data(this.service.removeByIds(ids));
    }

    public R search(SearchVo searchVo){
        return R.data(service.searchPage(searchVo));
    }

}
