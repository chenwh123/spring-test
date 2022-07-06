package com.chen.model.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;

/**
 * @author chenwh3
 */
@Data
public class SearchVo {

    private String colunms = "";
    private String condition = "";
    private String order = "";

    @Min(1)
    private Integer pageIndex = 1;

    @Range(min = 1,max = 2000)
    private Integer pageSize = 200;
}
