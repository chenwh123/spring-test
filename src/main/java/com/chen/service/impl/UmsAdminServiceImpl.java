package com.chen.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.service.UmsAdminService;
import com.chen.mapper.UmsAdminMapper;
import com.chen.model.entity.UmsAdmin;
import org.springframework.stereotype.Service;

/**
* @author chenwh3
*/
@Service
public class UmsAdminServiceImpl extends ServiceImpl<UmsAdminMapper, UmsAdmin>
    implements UmsAdminService {

}

