package com.chen.model.constant;


/**
 * 系统常量
 *
 * @author Chill
 */
public interface AppConstant {


    public static final String TOP = "TOP";

    public static final String SPACE = " ";

    public static final String EMPTY = "";

    public static final String AS = " as ";

    public static final int INT_0 = 0;

    public static final int INT_4 = 4;

    String LINE_CHAR = "\r";

    /**
     * 编码
     */
    String UTF_8 = "UTF-8";

    /**
     * contentType
     */
    String CONTENT_TYPE_NAME = "Content-type";

    /**
     * JSON 资源
     */
    String CONTENT_TYPE = "application/json;charset=utf-8";

    /**
     * 角色前缀
     */
    String SECURITY_ROLE_PREFIX = "ROLE_";

    /**
     * 主键字段名
     */
    String DB_PRIMARY_KEY = "id";

    /**
     * 业务状态[1:正常]
     */
    int DB_STATUS_NORMAL = 1;


    /**
     * 删除状态[0:正常,1:删除]
     */
    int DB_NOT_DELETED = 0;
    int DB_IS_DELETED = 1;

    /**
     * 用户锁定状态
     */
    int DB_ADMIN_NON_LOCKED = 0;
    int DB_ADMIN_LOCKED = 1;

    /**
     * 管理员对应的租户ID
     */
    String ADMIN_TENANT_ID = "000000";

    /**
     * 日志默认状态
     */
    String LOG_NORMAL_TYPE = "1";

    /**
     * 默认为空消息
     */
    String DEFAULT_NULL_MESSAGE = "暂无承载数据";
    /**
     * 默认成功消息
     */
    String DEFAULT_SUCCESS_MESSAGE = "操作成功";
    /**
     * 默认失败消息
     */
    String DEFAULT_FAILURE_MESSAGE = "操作失败";
    /**
     * 默认未授权消息
     */
    String DEFAULT_UNAUTHORIZED_MESSAGE = "签名认证失败";
    /**
     * 登录用户
     */
    String LOGIN_SYS_USER = "loginSysUser";

    /**
     * 登陆token
     */
    String JWT_DEFAULT_TOKEN_NAME = "token";

    /**
     * JWT用户名
     */
    String JWT_USERGID = "usergid";

    /**
     * JWT刷新新token响应状态码
     */
    int JWT_REFRESH_TOKEN_CODE = 460;

    /**
     * JWT刷新新token响应状态码，
     * Redis中不存在，但jwt未过期，不生成新的token，返回361状态码
     */
    int JWT_INVALID_TOKEN_CODE = 461;

    /**
     * JWT Token默认密钥
     */
    String JWT_DEFAULT_SECRET = "qmsSecret";

    /**
     * JWT 默认过期时间，3600L，单位秒
     */
    Long JWT_DEFAULT_EXPIRE_SECOND = 3600L;

    /**
     * 产品经理角色
     */
    String PRODUCTOR = "1";
    /**
     * 普通用户角色
     */
    String VISITOR = "2";

    /**
     * 盐
     */
    String SALT = "";
    /**
     * 工单状态创建
     */
    Integer ORDER_CREATE = 1;
    /**
     * 工单状态处理中
     */
    Integer ORDER_PROCESS = 2;
    /**
     * 工单状态已解决
     */
    Integer ORDER_COMPLETE = 3;
    /**
     * 工单状态关闭
     */
    Integer ORDER_CLOSE = 4;

    String UPMS_SYSTEM_ID = "40288fcb722b7518017234e294cb0000";

    String UPMS_ORG_ID = "40288fcb722b7518017234e7d60f0001";

    String UPMS_APP_ID = "40288fcb7331a73201734cb1e2a10000";

    /**
     * QMS的菜单最高目录
     */
    String QMS_TOP_ID = "1";
    /**
     * APP的菜单最高目录
     */
    String APP_TOP_ID = "2";

    /**
     * 通用查询的json key
     */
    String QRY_COND_KEY = "qry_cond_key";

    /**
     * grid的json行数据
     */
    String ROW_DATA = "row_data";

    /**
     * 表头数据
     */
    String HEAD_DATA = "head_data";

    /**
     * json的code键
     */
    String CODE = "code";

    /**
     * json的charg键
     */
    String CHARG = "charg";

    String CHARG2 = "charg2";

    /**
     * json的aufnr键
     */
    String AUFNR = "aufnr";

    String AUFNR2 = "aufnr2";


    /**
     * json的ebeln键
     */
    String EBELN = "ebeln";

    /**
     * json的prueflos键
     */
    String PRUEFLOS = "prueflos";

    /**
     * json的vornr键
     */
    String VORNR = "vornr";

    /**
     * json的name键
     */
    String NAME = "name";

    /**
     * json的werk键
     */
    String WERK = "werk";

    /**
     * json的ntype键
     */
    String NTYPE = "ntype";


    String N_TYPE = "nType";

    /**
     * 送样人
     */
    String C_Arrive_User = "cArriveUser";

    /**
     * 检验特性
     */
    String VERWMERKM = "verwmerkm";

    /**
     * 检验特性描述
     */
    String FEATURE_DESC = "featureDesc";

    /**
     * 实验室简码
     */
    String C_INI_SMP = "cIniSmp";

    String C_INI_SMP2 = "cIniSmp2";

    /**
     * 送样日期
     */
    String D_CRT_DATE = "dCrtDate";

    String D_CRT_DATE2 = "dCrtDate2";

    /**
     * 检验日期
     */
    String D_VT_DATE = "dVTDate";

    String D_VT_DATE2 = "dVTDate2";

    /**
     * 生产线
     */
    String C_SECTION = "cSection";

    /**
     * 样品名称
     */
    String KTEXTMAT = "ktextmat";

    /**
     * 样品编号
     */
    String CNO = "cNO";

    String CNO2 = "cNO2";

    /**
     * 近红外对数
     */
    String L_INFRARED = "lInfrared";



    /**
     * json的id键
     */
    String ID = "id";

    /**
     * json的PARA1键(适用敏感参数，如密码)
     */
    String PARA1 = "para1";
    /**
     * json的PARA2键(适用敏感参数，如密码)
     */
    String PARA2 = "para2";
    /**
     * json的PARA3键(适用敏感参数，如密码)
     */
    String PARA3 = "para3";

    /**
     * 文件Id
     */
    String FILE_ID = "fileId";

    /**
     * 文件数据
     */
    String FILE_DATA = "fileData";

    /**
     * 文件类型
     */
    String FILE_TYPE = "fileType";

    /**
     * 文件名
     */
    String FILE_NAME = "fileName";

    /**
     * 当前页的键
     */
    String CURR_PAGE = "currPage";

    /**
     * 当前页记录数的键
     */
    String PAGE_SIZE = "pageSize";

    /**
     * 当前页记录数的键
     */
    String TOTAL = "total";

    /**
     * json的parentId键
     */
    String PARENT_ID = "parentId";

    /**
     * json的ids键
     */
    String IDS = "ids";

    /**
     * json的flag键
     */
    String FLAG = "flag";

    /**
     * json的msg键
     */
    String MSG = "msg";

    /**
     * json的manual键
     */
    String MANUAL = "manual";

    /**
     * json的type键
     */
    String TYPE = "type";

    /**
     * json的cloneId
     */
    String CLONE_ID = "cloneId";

    /**
     * json的all值
     */
    String VAL_ALL = "all";

    /**
     * json的text键
     */
    String TEXT = "text";

    /**
     * json的供应商号
     */
    String LIFNR = "lifnr";

    /**
     * json的供应商号2
     */
    String LIFNR2 = "lifnr2";

    /**
     * json的物料号
     */
    String MATNR = "matnr";

    /**
     * json的物料号2
     */
    String MATNR2 = "matnr2";

    /**
     * json的工厂2
     */
    String WERK2 = "werk2";

    /**
     * json的开始
     */
    String ST = "st";

    /**
     * json的结束
     */
    String ET = "et";

    String PLNBEZ = "plnbez";


    /**
     * 物料组号
     */
    String MATKL = "matkl";

    /**
     * 操作成功
     */
    int OPT_SUCCESS = 1;

    /**
     * 操作失败
     */
    int OPT_FAIL = 2;

    /**
     * 操作异常
     */
    int OPT_EXCEPTION = 3;

    /**
     * 认证失败
     */
    int OPT_UNAUTHORIZED = 9;

    /**
     * 样品单默认的成本中心
     */
    String DEF_QC_KOSTL = "1206010000";

    /**
     * 样品单默认的部门
     */
    String DEF_QC_DEPT = "采购一大部";

    /**
     * 物料1开头对应原材F
     */
    String MATNR_TYPE_RAW = "F";
    int LAB_TYPE_RAW = 6;

    /**
     * 物料2开头对应包材H
     */
    String MATNR_TYPE_PACK = "H";
    int LAB_TYPE_PACK = 9;

    /**
     * QC样品单状态关闭
     */
    String SAMPLE_STATUS_CLOSE = "5";

    /**
     * QC样品单状态默认
     */
    String SAMPLE_STATUS_DEFAULT = "1";
}