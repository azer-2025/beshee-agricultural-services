-- ----------------------------

SET NAMES utf8mb4;

-- 1、部门表

-- ----------------------------

drop table if exists sys_dept;

create table sys_dept (

  dept_id           bigint(20)      not null auto_increment    comment '部门id',
  parent_id         bigint(20)      default 0                  comment '父部门id',
  ancestors         varchar(50)     default ''                 comment '祖级列表',
  dept_name         varchar(30)     default ''                 comment '部门名称',
  order_num         int(4)          default 0                  comment '显示顺序',
  leader            varchar(20)     default null               comment '负责人',
  phone             varchar(11)     default null               comment '联系电话',
  email             varchar(50)     default null               comment '',
  status            char(1)         default '0'                comment '部门状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (dept_id)

) engine=innodb auto_increment=200 comment = '部门表';



-- ----------------------------

-- 初始化部门表数据-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.






-- ----------------------------

-- 2、用户信息

-- ----------------------------

drop table if exists sys_user;

create table sys_user (

  user_id           bigint(20)      not null auto_increment    comment '用户ID',

  dept_id           bigint(20)      default null               comment '部门ID',

  user_name         varchar(30)     not null                   comment '用户账号',

  nick_name         varchar(30)     not null                   comment '用户昵称',

  user_type         varchar(2)      default '00'               comment '用户类型（00系统用户）',

  email             varchar(50)     default ''                 comment '用户',

  phonenumber       varchar(11)     default ''                 comment '手机号码',

  sex               char(1)         default '0'                comment '用户性别（0男 1女 2未知）',

  avatar            varchar(100)    default ''                 comment '头像地址',

  password          varchar(100)    default ''                 comment '密码',

  status            char(1)         default '0'                comment '账号状态（0正常 1停用）',

  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',

  login_ip          varchar(128)    default ''                 comment '最后登录IP',

  login_date        datetime                                   comment '最后登录时间',

  pwd_update_date   datetime                                   comment '密码最最后更新时间',

  create_by         varchar(64)     default ''                 comment '创建者',

  create_time       datetime                                   comment '创建时间',

  update_by         varchar(64)     default ''                 comment '更新者',

  update_time       datetime                                   comment '更新时间',

  remark            varchar(500)    default null               comment '备注',

  primary key (user_id)

) engine=innodb auto_increment=100 comment = '用户信息表';



-- ----------------------------

-- 初始化用户信息表数据-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.






-- ----------------------------

-- 3、岗位信息

-- ----------------------------

drop table if exists sys_post;

create table sys_post

(

  post_id       bigint(20)      not null auto_increment    comment '岗位ID',

  post_code     varchar(64)     not null                   comment '岗位编码',

  post_name     varchar(50)     not null                   comment '岗位名称',

  post_sort     int(4)          not null                   comment '显示顺序',

  status        char(1)         not null                   comment '状态（0正常 1停用）',

  create_by     varchar(64)     default ''                 comment '创建者',

  create_time   datetime                                   comment '创建时间',

  update_by     varchar(64)     default ''			       comment '更新者',

  update_time   datetime                                   comment '更新时间',

  remark        varchar(500)    default null               comment '备注',

  primary key (post_id)

) engine=innodb comment = '岗位信息表';



-- ----------------------------

-- 初始化岗位信息表数据-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.






-- ----------------------------

-- 4、角色信息

-- ----------------------------

drop table if exists sys_role;

create table sys_role (

  role_id              bigint(20)      not null auto_increment    comment '角色ID',

  role_name            varchar(30)     not null                   comment '角色名称',

  role_key             varchar(100)    not null                   comment '角色权限字符串',

  role_sort            int(4)          not null                   comment '显示顺序',

  data_scope           char(1)         default '1'                comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',

  menu_check_strictly  tinyint(1)      default 1                  comment '菜单树选择项是否关联显示',

  dept_check_strictly  tinyint(1)      default 1                  comment '部门树选择项是否关联显示',

  status               char(1)         not null                   comment '角色状态（0正常 1停用）',

  del_flag             char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',

  create_by            varchar(64)     default ''                 comment '创建者',

  create_time          datetime                                   comment '创建时间',

  update_by            varchar(64)     default ''                 comment '更新者',

  update_time          datetime                                   comment '更新时间',

  remark               varchar(500)    default null               comment '备注',

  primary key (role_id)

) engine=innodb auto_increment=100 comment = '角色信息表';



-- ----------------------------

-- 初始化角色信息表数据-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.






-- ----------------------------

-- 5、菜单权限表

-- ----------------------------

drop table if exists sys_menu;

create table sys_menu (

  menu_id           bigint(20)      not null auto_increment    comment '菜单ID',

  menu_name         varchar(50)     not null                   comment '菜单名称',

  parent_id         bigint(20)      default 0                  comment '父菜单ID',

  order_num         int(4)          default 0                  comment '显示顺序',

  path              varchar(200)    default ''                 comment '路由地址',

  component         varchar(255)    default null               comment '组件路径',

  query             varchar(255)    default null               comment '路由参数',

  route_name        varchar(50)     default ''                 comment '路由名称',

  is_frame          int(1)          default 1                  comment '是否为外链（0是 1否）',

  is_cache          int(1)          default 0                  comment '是否缓存（0缓存 1不缓存）',

  menu_type         char(1)         default ''                 comment '菜单类型（M目录 C菜单 F按钮）',

  visible           char(1)         default 0                  comment '菜单状态（0显示 1隐藏）',

  status            char(1)         default 0                  comment '菜单状态（0正常 1停用）',

  perms             varchar(100)    default null               comment '权限标识',

  icon              varchar(100)    default '#'                comment '菜单图标',

  create_by         varchar(64)     default ''                 comment '创建者',

  create_time       datetime                                   comment '创建时间',

  update_by         varchar(64)     default ''                 comment '更新者',

  update_time       datetime                                   comment '更新时间',

  remark            varchar(500)    default ''                 comment '备注',

  primary key (menu_id)

) engine=innodb auto_increment=2000 comment = '菜单权限表';



-- ----------------------------

-- 初始化菜单信息表数据-- ----------------------------

-- 一级菜单

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


# insert into sys_menu values('3', '系统工具', '0', '3', 'tool',             null, '', '', 1, 0, 'M', '0', '0', '', 'tool',     'admin', sysdate(), '', null, '系统工具目录');


-- 二级菜单

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


# insert into sys_menu values('103',  '部门管理', '1',   '4', 'dept',       'system/dept/index',        '', '', 1, 0, 'C', '0', '0', 'system:dept:list',        'tree',          'admin', sysdate(), '', null, '部门管理菜单');
#
# insert into sys_menu values('104',  '岗位管理', '1',   '5', 'post',       'system/post/index',        '', '', 1, 0, 'C', '0', '0', 'system:post:list',        'post',          'admin', sysdate(), '', null, '岗位管理菜单');

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


# insert into sys_menu values('115',  '表单构建', '3',   '1', 'build',      'tool/build/index',         '', '', 1, 0, 'C', '0', '0', 'tool:build:list',         'build',         'admin', sysdate(), '', null, '表单构建菜单');
#
# insert into sys_menu values('116',  '代码生成', '3',   '2', 'gen',        'tool/gen/index',           '', '', 1, 0, 'C', '0', '0', 'tool:gen:list',           'code',          'admin', sysdate(), '', null, '代码生成菜单');

-- Data rows omitted from the public source snapshot.


-- 三级菜单

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 用户管理按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 角色管理按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 菜单管理按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 部门管理按钮

# insert into sys_menu values('1016', '部门查', '103', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 'admin', sysdate(), '', null, '');
#
# insert into sys_menu values('1017', '部门新增', '103', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 'admin', sysdate(), '', null, '');
#
# insert into sys_menu values('1018', '部门修改', '103', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 'admin', sysdate(), '', null, '');
#
# insert into sys_menu values('1019', '部门删除', '103', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 'admin', sysdate(), '', null, '');
#
# -- 岗位管理按钮
#
# insert into sys_menu values('1020', '岗位查', '104', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 'admin', sysdate(), '', null, '');
#
# insert into sys_menu values('1021', '岗位新', '104', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 'admin', sysdate(), '', null, '');
#
# insert into sys_menu values('1022', '岗位修改', '104', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 'admin', sysdate(), '', null, '');
#
# insert into sys_menu values('1023', '岗位删除', '104', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 'admin', sysdate(), '', null, '');
#
# insert into sys_menu values('1024', '岗位导出', '104', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 'admin', sysdate(), '', null, '');

-- 字典管理按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 参数设置按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 通知公告按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 操作日志按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 登录日志按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 在线用户按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 定时任务按钮

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 代码生成按钮

# insert into sys_menu values('1055', '生成查询', '116', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query',             '#', 'admin', sysdate(), '', null, '');
#
# insert into sys_menu values('1056', '生成修改', '116', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit',              '#', 'admin', sysdate(), '', null, '');
#
# insert into sys_menu values('1057', '生成删除', '116', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove',            '#', 'admin', sysdate(), '', null, '');
#
# insert into sys_menu values('1058', '导入代码', '116', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import',            '#', 'admin', sysdate(), '', null, '');
#
# insert into sys_menu values('1059', '预代码', '116', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview',           '#', 'admin', sysdate(), '', null, '');
#
# insert into sys_menu values('1060', '生成代码', '116', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code',              '#', 'admin', sysdate(), '', null, '');





-- ----------------------------

-- 6、用户和角色关联表 用户N-1角色

-- ----------------------------

drop table if exists sys_user_role;

create table sys_user_role (

  user_id   bigint(20) not null comment '用户ID',

  role_id   bigint(20) not null comment '角色ID',

  primary key(user_id, role_id)

) engine=innodb comment = '用户和色关联表';



-- ----------------------------

-- 初始化用户和色关联表数据

-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



drop table if exists sys_role_menu;

create table sys_role_menu (
  role_id   bigint(20) not null comment '角色ID',
  menu_id   bigint(20) not null comment '菜单ID',
  primary key(role_id, menu_id)
) engine=innodb comment = '角色和菜单关联表';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



drop table if exists sys_role_dept;

create table sys_role_dept (

  role_id   bigint(20) not null comment '角色ID',

  dept_id   bigint(20) not null comment '部门ID',

  primary key(role_id, dept_id)

) engine=innodb comment = '角色和部门关联表';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


drop table if exists sys_user_post;
create table sys_user_post
(
  user_id   bigint(20) not null comment '用户ID',
  post_id   bigint(20) not null comment '岗位ID',
  primary key (user_id, post_id)
) engine=innodb comment = '用户与岗位关联表';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



drop table if exists sys_oper_log;
create table sys_oper_log (
  oper_id           bigint(20)      not null auto_increment    comment '日志主键',
  title             varchar(50)     default ''                 comment '模块标题',
  business_type     int(2)          default 0                  comment '业务类型（0其它 1新增 2修改 3删除）',
  method            varchar(200)    default ''                 comment '方法名称',
  request_method    varchar(10)     default ''                 comment '请求方式',
  operator_type     int(1)          default 0                  comment '操作类别（0其它 1后台用户 2手机端用户）',
  oper_name         varchar(50)     default ''                 comment '操作人员',
  dept_name         varchar(50)     default ''                 comment '部门名称',
  oper_url          varchar(255)    default ''                 comment '请求URL',
  oper_ip           varchar(128)    default ''                 comment '主机地址',
  oper_location     varchar(255)    default ''                 comment '操作地点',
  oper_param        varchar(2000)   default ''                 comment '请求参数',
  json_result       varchar(2000)   default ''                 comment '返回参数',
  status            int(1)          default 0                  comment '操作状态（0正常 1异常）',
  error_msg         varchar(2000)   default ''                 comment '错误消息',
  oper_time         datetime                                   comment '操作时间',
  cost_time         bigint(20)      default 0                  comment '消耗时间',
  primary key (oper_id),
  key idx_sys_oper_log_bt (business_type),
  key idx_sys_oper_log_s  (status),
  key idx_sys_oper_log_ot (oper_time)
) engine=innodb auto_increment=100 comment = '操作日志记录';

-- ----------------------------

-- 11、字典类型表

-- ----------------------------

drop table if exists sys_dict_type;
create table sys_dict_type

(

  dict_id          bigint(20)      not null auto_increment    comment '字典主键',

  dict_name        varchar(100)    default ''                 comment '字典名称',

  dict_type        varchar(100)    default ''                 comment '字典类型',

  status           char(1)         default '0'                comment '状态（0正常 1停用）',

  create_by        varchar(64)     default ''                 comment '创建者',

  create_time      datetime                                   comment '创建时间',

  update_by        varchar(64)     default ''                 comment '更新者',

  update_time      datetime                                   comment '更新时间',

  remark           varchar(500)    default null               comment '备注',

  primary key (dict_id),

  unique (dict_type)

) engine=innodb auto_increment=100 comment = '字典类型表';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- ----------------------------

-- 12、字典数据表

-- ----------------------------

drop table if exists sys_dict_data;

create table sys_dict_data

(

  dict_code        bigint(20)      not null auto_increment    comment '字典编码',

  dict_sort        int(4)          default 0                  comment '字典排序',

  dict_label       varchar(100)    default ''                 comment '字典标',

  dict_value       varchar(100)    default ''                 comment '字典键值',

  dict_type        varchar(100)    default ''                 comment '字典类型',

  css_class        varchar(100)    default null               comment '样式属性（其他样式扩展）',

  list_class       varchar(100)    default null               comment '表格回显样式',

  is_default       char(1)         default 'N'                comment '是否默认（Y是 N否）',

  status           char(1)         default '0'                comment '状态（0正常 1停用）',

  create_by        varchar(64)     default ''                 comment '创建者',

  create_time      datetime                                   comment '创建时间',

  update_by        varchar(64)     default ''                 comment '更新者',

  update_time      datetime                                   comment '更新时间',

  remark           varchar(500)    default null               comment '备注',

  primary key (dict_code)

) engine=innodb auto_increment=100 comment = '字典数据表';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.






-- ----------------------------

-- 13、参数配置表

-- ----------------------------

drop table if exists sys_config;

create table sys_config (

  config_id         int(5)          not null auto_increment    comment '参数主键',

  config_name       varchar(100)    default ''                 comment '参数名称',

  config_key        varchar(100)    default ''                 comment '参数锐',

  config_value      varchar(500)    default ''                 comment '参数键值',

  config_type       char(1)         default 'N'                comment '系统内置（Y是 N否）',

  create_by         varchar(64)     default ''                 comment '创建者',

  create_time       datetime                                   comment '创建时间',

  update_by         varchar(64)     default ''                 comment '更新者',

  update_time       datetime                                   comment '更新时间',

  remark            varchar(500)    default null               comment '备注',

  primary key (config_id)

) engine=innodb auto_increment=100 comment = '参数配置表';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.






-- ----------------------------

-- 14、系统访问记录

drop table if exists sys_logininfor;

create table sys_logininfor (

  info_id        bigint(20)     not null auto_increment   comment '访问ID',

  user_name      varchar(50)    default ''                comment '用户账号',

  ipaddr         varchar(128)   default ''                comment '登录IP地址',

  login_location varchar(255)   default ''                comment '登录地点',

  browser        varchar(50)    default ''                comment '浏览器类型',

  os             varchar(50)    default ''                comment '操作系统',

  status         char(1)        default '0'               comment '登录状态（0成功 1失败）',

  msg            varchar(255)   default ''                comment '提示消息',

  login_time     datetime                                 comment '访问时间',

  primary key (info_id),

  key idx_sys_logininfor_s  (status),

  key idx_sys_logininfor_lt (login_time)

) engine=innodb auto_increment=100 comment = '系统访问记录';





-- ----------------------------

-- 15、定时任务调度表

-- ----------------------------

drop table if exists sys_job;

create table sys_job (

  job_id              bigint(20)    not null auto_increment    comment '任务ID',

  job_name            varchar(64)   default ''                 comment '任务名称',

  job_group           varchar(64)   default 'DEFAULT'          comment '任务组名',

  invoke_target       varchar(500)  not null                   comment '调用目标字符串',

  cron_expression     varchar(255)  default ''                 comment 'cron执行表达式',

  misfire_policy      varchar(20)   default '3'                comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',

  concurrent          char(1)       default '1'                comment '是否并发执行（0允许 1禁止）',

  status              char(1)       default '0'                comment '状态（0正常 1暂停）',

  create_by           varchar(64)   default ''                 comment '创建者',

  create_time         datetime                                 comment '创建时间',

  update_by           varchar(64)   default ''                 comment '更新者',

  update_time         datetime                                 comment '更新时间',

  remark              varchar(500)  default ''                 comment '备注信息',

  primary key (job_id, job_name, job_group)

) engine=innodb auto_increment=100 comment = '定时任务调度表';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.






-- ----------------------------

-- 16、定时任务调度日志表

-- ----------------------------

drop table if exists sys_job_log;

create table sys_job_log (

  job_log_id          bigint(20)     not null auto_increment    comment '任务日志ID',

  job_name            varchar(64)    not null                   comment '任务名称',

  job_group           varchar(64)    not null                   comment '任务组名',

  invoke_target       varchar(500)   not null                   comment '调用目标字符串',

  job_message         varchar(500)                              comment '日志信息',

  status              char(1)        default '0'                comment '执行状态（0正常 1失败）',

  exception_info      varchar(2000)  default ''                 comment '异常信息',

  create_time         datetime                                  comment '创建时间',

  primary key (job_log_id)

) engine=innodb comment = '定时任务调度日志表';





-- ----------------------------

-- 17、通知公告表

drop table if exists sys_notice;

create table sys_notice (

  notice_id         int(4)          not null auto_increment    comment '公告ID',

  notice_title      varchar(50)     not null                   comment '公告标题',

  notice_type       char(1)         not null                   comment '公告类型（1通知 2公告）',

  notice_content    longblob        default null               comment '公告内容',

  status            char(1)         default '0'                comment '公告状态（0正常 1关闭）',

  create_by         varchar(64)     default ''                 comment '创建者',

  create_time       datetime                                   comment '创建时间',

  update_by         varchar(64)     default ''                 comment '更新者',

  update_time       datetime                                   comment '更新时间',

  remark            varchar(255)    default null               comment '备注',

  primary key (notice_id)

) engine=innodb auto_increment=10 comment = '通知公告表';



-- ----------------------------

-- 初始化公告信息表数据-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.






-- ----------------------------

-- 18、代码生成业务表

-- ----------------------------

drop table if exists gen_table;

create table gen_table (

  table_id          bigint(20)      not null auto_increment    comment '编号',

  table_name        varchar(200)    default ''                 comment '表名称',

  table_comment     varchar(500)    default ''                 comment '表描述',

  sub_table_name    varchar(64)     default null               comment '关联子表的表名',

  sub_table_fk_name varchar(64)     default null               comment '子表关联的锐',

  class_name        varchar(100)    default ''                 comment '实体类名称',

  tpl_category      varchar(200)    default 'crud'             comment '使用的模板（crud单表操作 tree树表操作）',

  tpl_web_type      varchar(30)     default ''                 comment '前端模板类型（element-ui模版 element-plus模版）',

  package_name      varchar(100)                               comment '生成包路径',

  module_name       varchar(30)                                comment '生成模块名',

  business_name     varchar(30)                                comment '生成业务名',

  function_name     varchar(50)                                comment '生成功能名',

  function_author   varchar(50)                                comment '生成功能作者',

  gen_type          char(1)         default '0'                comment '生成代码方式（0zip压缩包 1自定义路径）',

  gen_path          varchar(200)    default '/'                comment '生成路径（不填默认项目路径）',

  options           varchar(1000)                              comment '其它生成选项',

  create_by         varchar(64)     default ''                 comment '创建者',

  create_time 	    datetime                                   comment '创建时间',

  update_by         varchar(64)     default ''                 comment '更新者',

  update_time       datetime                                   comment '更新时间',

  remark            varchar(500)    default null               comment '备注',

  primary key (table_id)

) engine=innodb auto_increment=1 comment = '代码生成业务表';





-- ----------------------------

-- 19、代码生成业务表字段

-- ----------------------------

drop table if exists gen_table_column;

create table gen_table_column (

  column_id         bigint(20)      not null auto_increment    comment '编号',

  table_id          bigint(20)                                 comment '归属表编号',

  column_name       varchar(200)                               comment '列名称',

  column_comment    varchar(500)                               comment '列描述',

  column_type       varchar(100)                               comment '列类型',

  java_type         varchar(500)                               comment 'JAVA类型',

  java_field        varchar(200)                               comment 'JAVA字段名',

  is_pk             char(1)                                    comment '是否主键（1是）',

  is_increment      char(1)                                    comment '是否自增（1是）',

  is_required       char(1)                                    comment '是否必填（1是）',

  is_insert         char(1)                                    comment '是否为插入字段（1是）',

  is_edit           char(1)                                    comment '是否编辑字段（1是）',

  is_list           char(1)                                    comment '是否列表字段（1是）',

  is_query          char(1)                                    comment '是否查询字段（1是）',

  query_type        varchar(200)    default 'EQ'               comment '查方式（等于不等于、大于小于范围）',

  html_type         varchar(200)                               comment '显示类型（文、文期、下拉、选、单选、日期控件）',

  dict_type         varchar(200)    default ''                 comment '字典类型',

  sort              int                                        comment '排序',

  create_by         varchar(64)     default ''                 comment '创建者',

  create_time 	    datetime                                   comment '创建时间',

  update_by         varchar(64)     default ''                 comment '更新者',

  update_time       datetime                                   comment '更新时间',

  primary key (column_id)

) engine=innodb auto_increment=1 comment = '代码生成业务表字段';

-- ----------------------------

-- 20. Agri Mall Business Tables

-- ----------------------------

drop table if exists agri_category;

create table agri_category (

            category_id        bigint(20)      not null auto_increment comment '分类ID',

            category_name      varchar(64)     not null                comment '分类名称',

            sort_num           int(4)          default 0               comment '排序号',

            status             char(1)         default '0'             comment '状态(0正常 1停用)',

            create_by          varchar(64)     default ''              comment '创建者',

            create_time        datetime                                 comment '创建时间',

            update_by          varchar(64)     default ''              comment '更新者',

            update_time        datetime                                 comment '更新时间',

            remark             varchar(500)    default null            comment '备注',

            primary key (category_id)

) engine=innodb auto_increment=100 comment='助农分类表';



drop table if exists agri_farmer_apply;

create table agri_farmer_apply (

                                   apply_id           bigint(20)      not null auto_increment comment '申请ID',

                                   user_id            bigint(20)      not null                comment '用户ID',

                                   real_name          varchar(64)     not null                comment '真实姓名',

                                   id_card            varchar(32)     default ''              comment '身份证号',

                                   phone              varchar(20)     default ''              comment '联系电话',

                                   farm_name          varchar(128)    not null                comment '农场名称',

                                   farm_address       varchar(255)    default ''              comment '农场地址',

                                   qualification_img  varchar(255)    default ''              comment '资质图片',

                                   status             char(1)         default '0'             comment '状态(0待审核 1已通过 2已驳回)',

                                   audit_by           varchar(64)     default ''              comment '审核人',

                                   audit_time         datetime                                 comment '审核时间',

                                   audit_remark       varchar(500)    default ''              comment '审核备注',

                                   create_time        datetime                                 comment '创建时间',

                                   update_time        datetime                                 comment '更新时间',

                                   primary key (apply_id),

                                   unique key uk_farmer_apply_user (user_id)

) engine=innodb auto_increment=100 comment='农户入驻申请表';



drop table if exists agri_product;

create table agri_product (

                              product_id         bigint(20)      not null auto_increment comment '商品ID',

                              category_id        bigint(20)      not null                comment '分类ID',

                              farmer_id          bigint(20)      not null                comment '农户ID',

                              product_name       varchar(128)    not null                comment '商品名称',

                              product_cover      varchar(255)    default ''              comment '封面图',

                              product_images     text                                     comment '图片列表',

                              product_desc       text                                     comment '商品描述',

                              price              decimal(10,2)   not null                comment '价格',

                              stock              int(11)         default 0               comment '库存',

                              sales_count        int(11)         default 0               comment '销量',

                              status             char(1)         default '0'             comment '状态(0待审核 1已上架 2已驳回 3已下架)',

                              audit_by           varchar(64)     default ''              comment '审核人',

                              audit_time         datetime                                 comment '审核时间',

                              audit_remark       varchar(500)    default ''              comment '审核备注',

                              create_time        datetime                                 comment '创建时间',

                              update_time        datetime                                 comment '更新时间',

                              primary key (product_id),

                              key idx_product_farmer (farmer_id),

                              key idx_product_category (category_id),

                              key idx_product_status (status)

) engine=innodb auto_increment=1000 comment='助农商品表';



drop table if exists agri_cart;

create table agri_cart (

                           cart_id            bigint(20)      not null auto_increment comment '购物车ID',

                           buyer_id           bigint(20)      not null                comment '买家ID',

                           product_id         bigint(20)      not null                comment '商品ID',

                           quantity           int(11)         default 1               comment '数量',

                           checked            char(1)         default '1'             comment '勾选状态(0否 1是)',

                           status             char(1)         default '0'             comment '状态(0有效 1失效)',

                           create_time        datetime                                 comment '创建时间',

                           update_time        datetime                                 comment '更新时间',

                           primary key (cart_id),

                           unique key uk_cart_user_product (buyer_id, product_id)

) engine=innodb auto_increment=1000 comment='购物车表';



drop table if exists agri_order;

create table agri_order (

                            order_id           bigint(20)      not null auto_increment comment '订单ID',

                            order_no           varchar(64)     not null                comment '订单编号',

                            buyer_id           bigint(20)      not null                comment '买家ID',

                            farmer_id          bigint(20)      not null                comment '农户ID',

                            total_amount       decimal(10,2)   not null                comment '订单总额',

                            freight_amount     decimal(10,2)   default 0.00            comment '运费',

                            pay_amount         decimal(10,2)   not null                comment '支付金额',

                            pay_status         char(1)         default '0'             comment '支付状态(0待支付 1已支付 2已退款)',

                            order_status       char(1)         default '0'             comment '订单状态(0待接单 1待发货 2运输中 3待收货 4已完成 5已取消 6退款中 7已退款)',

                            payment_channel    char(1)         default '1'             comment '支付渠道(1模拟SDK 2支付宝沙箱)',

                            pay_voucher_no     varchar(64)     default ''              comment '支付凭证号',

                            receiver_name      varchar(64)     default ''              comment '收货人',

                            receiver_phone     varchar(20)     default ''              comment '收货电话',

                            receiver_address   varchar(255)    default ''              comment '收货地址',

                            buyer_remark       varchar(500)    default ''              comment '买家备注',

                            farmer_remark      varchar(500)    default ''              comment '农户备注',

                            pay_time           datetime                                 comment '支付时间',

                            ship_time          datetime                                 comment '发货时间',

                            finish_time        datetime                                 comment '完成时间',

                            create_time        datetime                                 comment '创建时间',

                            update_time        datetime                                 comment '更新时间',

                            primary key (order_id),

                            unique key uk_agri_order_no (order_no),

                            key idx_agri_order_buyer (buyer_id),

                            key idx_agri_order_farmer (farmer_id)

) engine=innodb auto_increment=10000 comment='订单表';



drop table if exists agri_order_item;

create table agri_order_item (

                                 item_id            bigint(20)      not null auto_increment comment '订单项ID',

                                 order_id           bigint(20)      not null                comment '订单ID',

                                 product_id         bigint(20)      not null                comment '商品ID',

                                 product_name       varchar(128)    not null                comment '商品名称',

                                 product_cover      varchar(255)    default ''              comment '封面图',

                                 price              decimal(10,2)   not null                comment '价格',

                                 quantity           int(11)         not null                comment '数量',

                                 amount             decimal(10,2)   not null                comment '金额',

                                 review_status      char(1)         default '0'             comment '评价状态(0未评价 1已评价)',

                                 primary key (item_id),

                                 key idx_order_item_order (order_id)

) engine=innodb auto_increment=10000 comment='订单明细表';



drop table if exists agri_payment_voucher;

create table agri_payment_voucher (

                                      voucher_id         bigint(20)      not null auto_increment comment '凭证ID',

                                      order_id           bigint(20)      not null                comment '订单ID',

                                      pay_no             varchar(64)     not null                comment '支付流水号',

                                      channel            char(1)         default '1'             comment '支付渠道',

                                      voucher_img        varchar(255)    default ''              comment '支付凭证图',

                                      pay_amount         decimal(10,2)   not null                comment '支付金额',

                                      pay_time           datetime                                 comment '支付时间',

                                      status             char(1)         default '1'             comment '状态(0失败 1成功)',

                                      primary key (voucher_id),

                                      key idx_voucher_order (order_id)

) engine=innodb auto_increment=10000 comment='支付凭证表';



drop table if exists agri_logistics_trace;

create table agri_logistics_trace (

                                      trace_id           bigint(20)      not null auto_increment comment '物流轨迹ID',

                                      order_id           bigint(20)      not null                comment '订单ID',

                                      status_node        varchar(64)     not null                comment '物流节点',

                                      status_desc        varchar(500)    default ''              comment '节点描述',

                                      trace_time         datetime                                 comment '轨迹时间',

                                      primary key (trace_id),

                                      key idx_trace_order (order_id)

) engine=innodb auto_increment=10000 comment='物流轨迹表';



drop table if exists agri_review;

create table agri_review (

                             review_id          bigint(20)      not null auto_increment comment '评价ID',

                             order_id           bigint(20)      not null                comment '订单ID',

                             order_item_id      bigint(20)      not null                comment '订单项ID',

                             product_id         bigint(20)      not null                comment '商品ID',

                             buyer_id           bigint(20)      not null                comment '买家ID',

                             farmer_id          bigint(20)      not null                comment '农户ID',

                             quality_star       int(2)          default 5               comment '质量评分',

                             package_star       int(2)          default 5               comment '包装评分',

                             service_star       int(2)          default 5               comment '服务评分',

                             content            varchar(1000)   default ''              comment '评价内容',

                             create_time        datetime                                 comment '创建时间',

                             primary key (review_id),

                             key idx_review_product (product_id),

                             key idx_review_buyer (buyer_id)

) engine=innodb auto_increment=10000 comment='评价表';



drop table if exists agri_refund;

create table agri_refund (

                             refund_id          bigint(20)      not null auto_increment comment '退款ID',
                             order_id           bigint(20)      not null                comment '订单ID',
                             buyer_id           bigint(20)      not null                comment '买家ID',
                             farmer_id          bigint(20)      not null                comment '农户ID',

                             amount             decimal(10,2)   not null                comment '金额',

                             reason             varchar(500)    default ''              comment '退款原因',

                             status             char(1)         default '0'             comment '状态(0待处理 1已同意 2已拒绝 3已退款)',

                             farmer_remark      varchar(500)    default ''              comment '农户备注',

                             admin_remark       varchar(500)    default ''              comment '管理员备注',

                             create_time        datetime                                 comment '创建时间',

                             audit_time         datetime                                 comment '审核时间',

                             update_time        datetime                                 comment '更新时间',

                             primary key (refund_id),
                             key idx_refund_order (order_id)

) engine=innodb auto_increment=10000 comment='退款表';



drop table if exists agri_farmer_notice;

create table agri_farmer_notice (

                                    notice_id          bigint(20)      not null auto_increment comment '消息ID',
                                    farmer_id          bigint(20)      not null                comment '农户ID',
                                    notice_type        varchar(32)     not null                comment '消息类型(NEW_ORDER/REFUND_APPLY)',
                                    biz_id             bigint(20)      default null            comment '业务ID',
                                    title              varchar(128)    not null                comment '标题',
                                    content            varchar(1000)   default ''              comment '内容',
                                    read_flag          char(1)         default '0'             comment '已读标记(0未读 1已读)',
                                    create_time        datetime                                 comment '创建时间',
                                    read_time          datetime                                 comment '已读时间',
                                    primary key (notice_id),
                                    key idx_notice_farmer (farmer_id),
                                    key idx_notice_read (read_flag)
) engine=innodb auto_increment=10000 comment='农户消息表';



drop table if exists agri_admin_notice;

create table agri_admin_notice (
                                   notice_id          bigint(20)      not null auto_increment comment '消息ID',
                                   admin_id           bigint(20)      not null                comment '管理员ID',
                                   notice_type        varchar(32)     not null                comment '消息类型(FARMER_APPLY/PRODUCT_AUDIT/DISPUTE)',
                                   biz_id             bigint(20)      default null            comment '业务ID',
                                   title              varchar(128)    not null                comment '标题',
                                   content            varchar(1000)   default ''              comment '内容',
                                   read_flag          char(1)         default '0'             comment '已读标记(0未读 1已读)',
                                   create_time        datetime                                 comment '创建时间',
                                   read_time          datetime                                 comment '已读时间',
                                   primary key (notice_id),
                                   key idx_admin_notice_user (admin_id),
                                   key idx_admin_notice_read (read_flag)

) engine=innodb auto_increment=10000 comment='管理员消息表';



drop table if exists agri_dispute;

create table agri_dispute (
                              dispute_id         bigint(20)      not null auto_increment comment '纠纷ID',
                              order_id           bigint(20)      not null                comment '订单ID',
                              buyer_id           bigint(20)      not null                comment '买家ID',

                              farmer_id          bigint(20)      not null                comment '农户ID',

                              title              varchar(200)    not null                comment '标题',

                              content            varchar(1000)   default ''              comment '内容',

                              status             char(1)         default '0'             comment '状态(0待处理 1处理中 2已解决)',

                              handle_result      varchar(1000)   default ''              comment '处理结果',

                              handle_by          varchar(64)     default ''              comment '处理人',

                              handle_time        datetime                                 comment '处理时间',

                              create_time        datetime                                 comment '创建时间',

                              update_time        datetime                                 comment '更新时间',

                              primary key (dispute_id)
) engine=innodb auto_increment=1000 comment='纠纷表';



drop table if exists agri_freight_template;

create table agri_freight_template (
                                       template_id        bigint(20)      not null auto_increment comment '模板ID',

                                       template_name      varchar(128)    not null                comment '模板名称',

                                       region_code        varchar(64)     not null                comment '地区编码',

                                       first_fee          decimal(10,2)   not null                comment '首费',

                                       extra_fee          decimal(10,2)   default 0.00            comment '续费',

                                       status             char(1)         default '0'             comment '状态(0启用 1停用)',

                                       create_by          varchar(64)     default ''              comment '创建者',

                                       create_time        datetime                                 comment '创建时间',

                                       update_by          varchar(64)     default ''              comment '更新者',

                                       update_time        datetime                                 comment '更新时间',

                                       remark             varchar(500)    default null            comment '备注',

                                       primary key (template_id)

) engine=innodb auto_increment=100 comment='运费模板表';



drop table if exists agri_coupon;

create table agri_coupon (


                             coupon_id          bigint(20)      not null auto_increment comment '优惠券ID',

                             coupon_name        varchar(128)    not null                comment '优惠券名称',

                             coupon_type        char(1)         default '1'             comment '类型(1满减 2折扣)',

                             threshold_amount   decimal(10,2)   default 0.00            comment '门槛金额',

                             discount_amount    decimal(10,2)   default 0.00            comment '减免金额',

                             discount_rate      decimal(5,2)    default 0.00            comment '折扣率',

                             start_time         datetime                                 comment '开始时间',

                             end_time           datetime                                 comment '结束时间',

                             total_count        int(11)         default 0               comment '总量',

                             remain_count       int(11)         default 0               comment '剩余量',

                             status             char(1)         default '0'             comment '状态(0启用 1停用)',

                             create_by          varchar(64)     default ''              comment '创建者',

                             create_time        datetime                                 comment '创建时间',

                             update_by          varchar(64)     default ''              comment '更新者',

                             update_time        datetime                                 comment '更新时间',

                             remark             varchar(500)    default null            comment '备注',

                             primary key (coupon_id)

) engine=innodb auto_increment=100 comment='优惠券表';



drop table if exists agri_promotion;

drop table if exists agri_user_coupon;

create table agri_user_coupon (

                                  user_coupon_id      bigint(20)      not null auto_increment comment '用户优惠券ID',

                                  coupon_id           bigint(20)      not null                comment '优惠券ID',

                                  user_id             bigint(20)      not null                comment '用户ID',

                                  status              char(1)         default '0'             comment '状态(0未使用 1已使用 2已失效)',

                                  grant_by            varchar(64)     default ''              comment '发放人',

                                  grant_time          datetime                                 comment '发放时间',

                                  used_time           datetime                                 comment '使用时间',

                                  used_order_id       bigint(20)      default null            comment '使用订单ID',

                                  discount_amount     decimal(10,2)   default 0.00            comment '实扣金额',

                                  create_time         datetime                                 comment '创建时间',

                                  update_time         datetime                                 comment '更新时间',

                                  remark              varchar(500)    default null            comment '备注',

                                  primary key (user_coupon_id),

                                  key idx_coupon_user (coupon_id, user_id),

                                  key idx_user_status (user_id, status)

) engine=innodb auto_increment=100 comment='用户优惠券表';

create table agri_promotion (

                                promotion_id       bigint(20)      not null auto_increment comment '活动ID',

                                promotion_name     varchar(128)    not null                comment '活动名称',

                                promotion_type     char(1)         default '1'             comment '类型(1秒杀 2满减 3主题)',

                                start_time         datetime                                 comment '开始时间',

                                end_time           datetime                                 comment '结束时间',

                                content            varchar(1000)   default ''              comment '内容',

                                status             char(1)         default '0'             comment '状态(0启用 1停用)',

                                create_by          varchar(64)     default ''              comment '创建者',

                                create_time        datetime                                 comment '创建时间',

                                update_by          varchar(64)     default ''              comment '更新者',

                                update_time        datetime                                 comment '更新时间',

                                remark             varchar(500)    default null            comment '备注',

                                primary key (promotion_id)

) engine=innodb auto_increment=100 comment='促销活动表';



-- ----------------------------

-- 21. Dict Seed Data

-- ----------------------------

delete from sys_dict_data where dict_type in ('agri_apply_status','agri_product_status','agri_pay_status','agri_order_status','agri_refund_status','agri_coupon_type','agri_promotion_type');

delete from sys_dict_type where dict_type in ('agri_apply_status','agri_product_status','agri_pay_status','agri_order_status','agri_refund_status','agri_coupon_type','agri_promotion_type');

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- 22. 菜单
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.




-- ----------------------------

-- 23. 角色与演示用户
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- ----------------------------

-- 24. 演示业务数据
-- ----------------------------

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



-- =========================================================

-- 助农商城增量 SQL - 01 表结构

-- 执行顺序: 01 -> 02 -> 03

-- =========================================================

SET NAMES utf8mb4;



-- 20. 助农商城业务表

-- ----------------------------

create table if not exists agri_category (

                                             category_id        bigint(20)      not null auto_increment comment '分类ID',

                                             category_name      varchar(64)     not null                comment '分类名称',

                                             sort_num           int(4)          default 0               comment '排序号',

                                             status             char(1)         default '0'             comment '状态(0正常 1停用)',

                                             create_by          varchar(64)     default ''              comment '创建者',

                                             create_time        datetime                                 comment '创建时间',

                                             update_by          varchar(64)     default ''              comment '更新者',

                                             update_time        datetime                                 comment '更新时间',

                                             remark             varchar(500)    default null            comment '备注',

                                             primary key (category_id)

) engine=innodb auto_increment=100 comment='助农分类表';



create table if not exists agri_farmer_apply (

                                                 apply_id           bigint(20)      not null auto_increment comment '申请ID',

                                                 user_id            bigint(20)      not null                comment '用户ID',

                                                 real_name          varchar(64)     not null                comment '真实姓名',

                                                 id_card            varchar(32)     default ''              comment '身份证号',

                                                 phone              varchar(20)     default ''              comment '联系电话',

                                                 farm_name          varchar(128)    not null                comment '农场名称',

                                                 farm_address       varchar(255)    default ''              comment '农场地址',

                                                 qualification_img  varchar(255)    default ''              comment '资质图片',

                                                 status             char(1)         default '0'             comment '状态(0待审核 1已通过 2已驳回)',

                                                 audit_by           varchar(64)     default ''              comment '审核人',

                                                 audit_time         datetime                                 comment '审核时间',

                                                 audit_remark       varchar(500)    default ''              comment '审核备注',

                                                 create_time        datetime                                 comment '创建时间',

                                                 update_time        datetime                                 comment '更新时间',

                                                 primary key (apply_id),

                                                 unique key uk_farmer_apply_user (user_id)

) engine=innodb auto_increment=100 comment='农户入驻申请表';



create table if not exists agri_product (

                                            product_id         bigint(20)      not null auto_increment comment '商品ID',

                                            category_id        bigint(20)      not null                comment '分类ID',

                                            farmer_id          bigint(20)      not null                comment '农户ID',

                                            product_name       varchar(128)    not null                comment '商品名称',

                                            product_cover      varchar(255)    default ''              comment '封面图',

                                            product_images     text                                     comment '图片列表',

                                            product_desc       text                                     comment '商品描述',

                                            price              decimal(10,2)   not null                comment '价格',

                                            stock              int(11)         default 0               comment '库存',

                                            sales_count        int(11)         default 0               comment '销量',

                                            status             char(1)         default '0'             comment '状态(0待审核 1已上架 2已驳回 3已下架)',

                                            audit_by           varchar(64)     default ''              comment '审核人',

                                            audit_time         datetime                                 comment '审核时间',

                                            audit_remark       varchar(500)    default ''              comment '审核备注',

                                            create_time        datetime                                 comment '创建时间',

                                            update_time        datetime                                 comment '更新时间',

                                            primary key (product_id),

                                            key idx_product_farmer (farmer_id),

                                            key idx_product_category (category_id),

                                            key idx_product_status (status)

) engine=innodb auto_increment=1000 comment='助农商品表';



create table if not exists agri_cart (

                                         cart_id            bigint(20)      not null auto_increment comment '购物车ID',

                                         buyer_id           bigint(20)      not null                comment '买家ID',

                                         product_id         bigint(20)      not null                comment '商品ID',

                                         quantity           int(11)         default 1               comment '数量',

                                         checked            char(1)         default '1'             comment '勾选状态(0否 1是)',

                                         status             char(1)         default '0'             comment '状态(0有效 1失效)',

                                         create_time        datetime                                 comment '创建时间',

                                         update_time        datetime                                 comment '更新时间',

                                         primary key (cart_id),

                                         unique key uk_cart_user_product (buyer_id, product_id)

) engine=innodb auto_increment=1000 comment='购物车表';



create table if not exists agri_order (

                                          order_id           bigint(20)      not null auto_increment comment '订单ID',

                                          order_no           varchar(64)     not null                comment '订单编号',

                                          buyer_id           bigint(20)      not null                comment '买家ID',

                                          farmer_id          bigint(20)      not null                comment '农户ID',

                                          total_amount       decimal(10,2)   not null                comment '订单总额',

                                          freight_amount     decimal(10,2)   default 0.00            comment '运费',

                                          pay_amount         decimal(10,2)   not null                comment '支付金额',

                                          pay_status         char(1)         default '0'             comment '支付状态(0待支付 1已支付 2已退款)',

                                          order_status       char(1)         default '0'             comment '订单状态(0待接单 1待发货 2运输中 3待收货 4已完成 5已取消 6退款中 7已退款)',

                                          payment_channel    char(1)         default '1'             comment '支付渠道(1模拟SDK 2支付宝沙箱)',

                                          pay_voucher_no     varchar(64)     default ''              comment '支付凭证号',

                                          receiver_name      varchar(64)     default ''              comment '收货人',

                                          receiver_phone     varchar(20)     default ''              comment '收货电话',

                                          receiver_address   varchar(255)    default ''              comment '收货地址',

                                          buyer_remark       varchar(500)    default ''              comment '买家备注',

                                          farmer_remark      varchar(500)    default ''              comment '农户备注',

                                          pay_time           datetime                                 comment '支付时间',

                                          ship_time          datetime                                 comment '发货时间',

                                          finish_time        datetime                                 comment '完成时间',

                                          create_time        datetime                                 comment '创建时间',

                                          update_time        datetime                                 comment '更新时间',

                                          primary key (order_id),

                                          unique key uk_agri_order_no (order_no),

                                          key idx_agri_order_buyer (buyer_id),

                                          key idx_agri_order_farmer (farmer_id)

) engine=innodb auto_increment=10000 comment='订单表';



create table if not exists agri_order_item (

                                               item_id            bigint(20)      not null auto_increment comment '订单项ID',

                                               order_id           bigint(20)      not null                comment '订单ID',

                                               product_id         bigint(20)      not null                comment '商品ID',

                                               product_name       varchar(128)    not null                comment '商品名称',

                                               product_cover      varchar(255)    default ''              comment '封面图',

                                               price              decimal(10,2)   not null                comment '价格',

                                               quantity           int(11)         not null                comment '数量',

                                               amount             decimal(10,2)   not null                comment '金额',

                                               review_status      char(1)         default '0'             comment '评价状态(0未评价 1已评价)',

                                               primary key (item_id),

                                               key idx_order_item_order (order_id)

) engine=innodb auto_increment=10000 comment='订单明细表';



create table if not exists agri_payment_voucher (

                                                    voucher_id         bigint(20)      not null auto_increment comment '凭证ID',

                                                    order_id           bigint(20)      not null                comment '订单ID',

                                                    pay_no             varchar(64)     not null                comment '支付流水号',

                                                    channel            char(1)         default '1'             comment '支付渠道',

                                                    voucher_img        varchar(255)    default ''              comment '支付凭证图',

                                                    pay_amount         decimal(10,2)   not null                comment '支付金额',

                                                    pay_time           datetime                                 comment '支付时间',

                                                    status             char(1)         default '1'             comment '状态(0失败 1成功)',

                                                    primary key (voucher_id),

                                                    key idx_voucher_order (order_id)

) engine=innodb auto_increment=10000 comment='支付凭证表';



create table if not exists agri_logistics_trace (

                                                    trace_id           bigint(20)      not null auto_increment comment '物流轨迹ID',

                                                    order_id           bigint(20)      not null                comment '订单ID',

                                                    status_node        varchar(64)     not null                comment '物流节点',

                                                    status_desc        varchar(500)    default ''              comment '节点描述',

                                                    trace_time         datetime                                 comment '轨迹时间',

                                                    primary key (trace_id),

                                                    key idx_trace_order (order_id)

) engine=innodb auto_increment=10000 comment='物流轨迹表';



create table if not exists agri_review (

                                           review_id          bigint(20)      not null auto_increment comment '评价ID',

                                           order_id           bigint(20)      not null                comment '订单ID',

                                           order_item_id      bigint(20)      not null                comment '订单项ID',

                                           product_id         bigint(20)      not null                comment '商品ID',

                                           buyer_id           bigint(20)      not null                comment '买家ID',

                                           farmer_id          bigint(20)      not null                comment '农户ID',

                                           quality_star       int(2)          default 5               comment '质量评分',

                                           package_star       int(2)          default 5               comment '包装评分',

                                           service_star       int(2)          default 5               comment '服务评分',

                                           content            varchar(1000)   default ''              comment '评价内容',

                                           create_time        datetime                                 comment '创建时间',

                                           primary key (review_id),

                                           key idx_review_product (product_id),

                                           key idx_review_buyer (buyer_id)

) engine=innodb auto_increment=10000 comment='评价表';



create table if not exists agri_refund (
                                           refund_id          bigint(20)      not null auto_increment comment '退款ID',
                                           order_id           bigint(20)      not null                comment '订单ID',
                                           buyer_id           bigint(20)      not null                comment '买家ID',
                                           farmer_id          bigint(20)      not null                comment '农户ID',

                                           amount             decimal(10,2)   not null                comment '金额',

                                           reason             varchar(500)    default ''              comment '退款原因',

                                           status             char(1)         default '0'             comment '状态(0待处理 1已同意 2已拒绝 3已退款)',

                                           farmer_remark      varchar(500)    default ''              comment '农户备注',

                                           admin_remark       varchar(500)    default ''              comment '管理员备注',

                                           create_time        datetime                                 comment '创建时间',

                                           audit_time         datetime                                 comment '审核时间',

                                           update_time        datetime                                 comment '更新时间',

                                           primary key (refund_id),
                                           key idx_refund_order (order_id)
) engine=innodb auto_increment=10000 comment='退款表';

create table if not exists agri_farmer_notice (
                                                  notice_id          bigint(20)      not null auto_increment comment '消息ID',
                                                  farmer_id          bigint(20)      not null                comment '农户ID',
                                                  notice_type        varchar(32)     not null                comment '消息类型(NEW_ORDER/REFUND_APPLY)',
                                                  biz_id             bigint(20)      default null            comment '业务ID',
                                                  title              varchar(128)    not null                comment '标题',
                                                  content            varchar(1000)   default ''              comment '内容',
                                                  read_flag          char(1)         default '0'             comment '已读标记(0未读 1已读)',
                                                  create_time        datetime                                 comment '创建时间',
                                                  read_time          datetime                                 comment '已读时间',
                                                  primary key (notice_id),
                                                  key idx_notice_farmer (farmer_id),
                                                  key idx_notice_read (read_flag)
) engine=innodb auto_increment=10000 comment='农户消息表';

create table if not exists agri_admin_notice (
                                                 notice_id          bigint(20)      not null auto_increment comment '消息ID',
                                                 admin_id           bigint(20)      not null                comment '管理员ID',
                                                 notice_type        varchar(32)     not null                comment '消息类型(FARMER_APPLY/PRODUCT_AUDIT/DISPUTE)',
                                                 biz_id             bigint(20)      default null            comment '业务ID',
                                                 title              varchar(128)    not null                comment '标题',
                                                 content            varchar(1000)   default ''              comment '内容',
                                                 read_flag          char(1)         default '0'             comment '已读标记(0未读 1已读)',
                                                 create_time        datetime                                 comment '创建时间',
                                                 read_time          datetime                                 comment '已读时间',
                                                 primary key (notice_id),
                                                 key idx_admin_notice_user (admin_id),
                                                 key idx_admin_notice_read (read_flag)
) engine=innodb auto_increment=10000 comment='管理员消息表';

create table if not exists agri_dispute (
                                            dispute_id         bigint(20)      not null auto_increment comment '纠纷ID',
                                            order_id           bigint(20)      not null                comment '订单ID',
                                            buyer_id           bigint(20)      not null                comment '买家ID',

                                            farmer_id          bigint(20)      not null                comment '农户ID',

                                            title              varchar(200)    not null                comment '标题',

                                            content            varchar(1000)   default ''              comment '内容',

                                            status             char(1)         default '0'             comment '状态(0待处理 1处理中 2已解决)',

                                            handle_result      varchar(1000)   default ''              comment '处理结果',

                                            handle_by          varchar(64)     default ''              comment '处理人',

                                            handle_time        datetime                                 comment '处理时间',

                                            create_time        datetime                                 comment '创建时间',

                                            update_time        datetime                                 comment '更新时间',

                                            primary key (dispute_id)

) engine=innodb auto_increment=1000 comment='纠纷表';



create table if not exists agri_freight_template (

                                                     template_id        bigint(20)      not null auto_increment comment '模板ID',

                                                     template_name      varchar(128)    not null                comment '模板名称',

                                                     region_code        varchar(64)     not null                comment '地区编码',

                                                     first_fee          decimal(10,2)   not null                comment '首费',

                                                     extra_fee          decimal(10,2)   default 0.00            comment '续费',

                                                     status             char(1)         default '0'             comment '状态(0启用 1停用)',

                                                     create_by          varchar(64)     default ''              comment '创建者',

                                                     create_time        datetime                                 comment '创建时间',

                                                     update_by          varchar(64)     default ''              comment '更新者',

                                                     update_time        datetime                                 comment '更新时间',

                                                     remark             varchar(500)    default null            comment '备注',

                                                     primary key (template_id)

) engine=innodb auto_increment=100 comment='运费模板表';



create table if not exists agri_coupon (

                                           coupon_id          bigint(20)      not null auto_increment comment '优惠券ID',

                                           coupon_name        varchar(128)    not null                comment '优惠券名称',

                                           coupon_type        char(1)         default '1'             comment '类型(1满减 2折扣)',

                                           threshold_amount   decimal(10,2)   default 0.00            comment '门槛金额',

                                           discount_amount    decimal(10,2)   default 0.00            comment '减免金额',

                                           discount_rate      decimal(5,2)    default 0.00            comment '折扣率',

                                           start_time         datetime                                 comment '开始时间',

                                           end_time           datetime                                 comment '结束时间',

                                           total_count        int(11)         default 0               comment '总量',

                                           remain_count       int(11)         default 0               comment '剩余量',

                                           status             char(1)         default '0'             comment '状态(0启用 1停用)',

                                           create_by          varchar(64)     default ''              comment '创建者',

                                           create_time        datetime                                 comment '创建时间',

                                           update_by          varchar(64)     default ''              comment '更新者',

                                           update_time        datetime                                 comment '更新时间',

                                           remark             varchar(500)    default null            comment '备注',

                                           primary key (coupon_id)

) engine=innodb auto_increment=100 comment='优惠券表';



create table if not exists agri_user_coupon (

                                             user_coupon_id      bigint(20)      not null auto_increment comment '用户优惠券ID',

                                             coupon_id           bigint(20)      not null                comment '优惠券ID',

                                             user_id             bigint(20)      not null                comment '用户ID',

                                             status              char(1)         default '0'             comment '状态(0未使用 1已使用 2已失效)',

                                             grant_by            varchar(64)     default ''              comment '发放人',

                                             grant_time          datetime                                 comment '发放时间',

                                             used_time           datetime                                 comment '使用时间',

                                             used_order_id       bigint(20)      default null            comment '使用订单ID',

                                             discount_amount     decimal(10,2)   default 0.00            comment '实扣金额',

                                             create_time         datetime                                 comment '创建时间',

                                             update_time         datetime                                 comment '更新时间',

                                             remark              varchar(500)    default null            comment '备注',

                                             primary key (user_coupon_id),

                                             key idx_coupon_user (coupon_id, user_id),

                                             key idx_user_status (user_id, status)

) engine=innodb auto_increment=100 comment='用户优惠券表';

create table if not exists agri_promotion (

                                              promotion_id       bigint(20)      not null auto_increment comment '活动ID',
                                              promotion_name     varchar(128)    not null                comment '活动名称',
                                              promotion_type     char(1)         default '1'             comment '类型(1秒杀 2满减 3主题)',
                                              start_time         datetime                                comment '开始时间',
                                              end_time           datetime                                 comment '结束时间',
                                              content            varchar(1000)   default ''              comment '内容',
                                              status             char(1)         default '0'             comment '状态(0启用 1停用)',
                                              create_by          varchar(64)     default ''              comment '创建者',
                                              create_time        datetime                                 comment '创建时间',
                                              update_by          varchar(64)     default ''              comment '更新者',
                                              update_time        datetime                                 comment '更新时间',
                                              remark             varchar(500)    default null            comment '备注',
                                              primary key (promotion_id)
) engine=innodb auto_increment=100 comment='促销活动表';


SET NAMES utf8mb4;

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



SET NAMES utf8mb4;

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



CREATE TABLE IF NOT EXISTS agri_user_coupon (
                                                user_coupon_id  BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
                                                coupon_id       BIGINT(20) NOT NULL COMMENT '优惠券ID',
                                                user_id         BIGINT(20) NOT NULL COMMENT '用户ID',
                                                status          CHAR(1) DEFAULT '0' COMMENT '状态(0未使用 1已使用 2已失效)',
                                                grant_by        VARCHAR(64) DEFAULT '' COMMENT '发放人',
                                                grant_time      DATETIME COMMENT '发放时间',
                                                used_time       DATETIME COMMENT '使用时间',
                                                used_order_id   BIGINT(20) DEFAULT NULL COMMENT '使用订单ID',
                                                discount_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '实扣金额',
                                                create_time     DATETIME COMMENT '创建时间',
                                                update_time     DATETIME COMMENT '更新时间',
                                                remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
                                                PRIMARY KEY (user_coupon_id),
                                                KEY idx_coupon_user (coupon_id, user_id),
                                                KEY idx_user_status (user_id, status)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='用户优惠券表';

SET NAMES utf8mb4;

-- 1) 先删除角色-菜单关联（避免外键或脏关联）
DELETE FROM sys_role_menu
WHERE menu_id IN (
                  2000, -- 买家中心目录
                  2001, -- 商品浏览
                  2002, -- 购物车管理
                  2003, -- 订单跟踪
                  2004, -- 评价中心
                  2301, -- 评价查询（按钮）
                  2302  -- 评价新增（按钮）
    );

-- 2) 删除买家中心按钮菜单
DELETE FROM sys_menu
WHERE menu_id IN (2301, 2302);

-- 3) 删除买家中心子菜单
DELETE FROM sys_menu
WHERE menu_id IN (2001, 2002, 2003, 2004);

-- 4) 删除买家中心目录
DELETE FROM sys_menu
WHERE menu_id = 2000;

-- Data rows omitted from the public source snapshot.


UPDATE sys_menu
SET menu_name = '订单管理',
    parent_id = 2200,
    order_num = '6',
    path = 'orderManage',
    component = 'agri/admin/order/index',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'agri:admin:order:list',
    icon = 'list',
    update_by = 'admin',
    update_time = SYSDATE(),
    remark = '订单管理菜单'
WHERE menu_id = 2206;

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Add "评价管理" under 商城监管 and grant CRUD permissions to admin roles.

-- Data rows omitted from the public source snapshot.


UPDATE sys_menu
SET menu_name = '评价管理',
    parent_id = 2200,
    order_num = '7',
    path = 'reviewManage',
    component = 'agri/admin/review/index',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'agri:admin:review:list',
    icon = 'star',
    update_by = 'admin',
    update_time = SYSDATE(),
    remark = '评价管理菜单'
WHERE menu_id = 2207;

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.



DROP TABLE IF EXISTS `agri_buyer_address`;
     CREATE TABLE `agri_buyer_address` (
       `address_id`       bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '地址ID',
       `user_id`          bigint(20)      NOT NULL                   COMMENT '用户ID',
       `receiver_name`    varchar(64)     NOT NULL                   COMMENT '收货人姓名',
       `receiver_phone`   varchar(20)     NOT NULL                   COMMENT '收货联系电话',
       `receiver_address` varchar(255)    NOT NULL                   COMMENT '详细收货地址',
       `is_default`       char(1)         DEFAULT '0'                COMMENT '是否为默认地址（0-否，1-是）',
       `create_time`      datetime        DEFAULT NULL               COMMENT '创建时间',
       `update_time`      datetime        DEFAULT NULL               COMMENT '更新时间',
       PRIMARY KEY (`address_id`),
       KEY `idx_address_user` (`user_id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='买家收货地址表';

-- 1) 字典类型：模板名称

-- Data rows omitted from the public source snapshot.


-- 2) 字典类型：地区编码

-- Data rows omitted from the public source snapshot.


-- 3) 模板名称字典数据（dict_value 与 agri_freight_template.template_name 保持一致）

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- 4) 地区编码字典数据（dict_value 为标准区域编码）

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Supplemental SQL for AI customer service chat history.
-- Safe to run independently without touching the main table.sql.

CREATE TABLE IF NOT EXISTS agri_ai_chat_log (
                                                log_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'AI chat log ID',
                                                buyer_id bigint(20) NOT NULL COMMENT 'Buyer user ID',
                                                role varchar(16) NOT NULL COMMENT 'Role: user or assistant',
                                                content varchar(2000) DEFAULT '' COMMENT 'Message content',
                                                reply_mode varchar(32) DEFAULT '' COMMENT 'Reply mode: remote or fallback',
                                                provider varchar(64) DEFAULT '' COMMENT 'AI provider',
                                                model_name varchar(128) DEFAULT '' COMMENT 'Model name',
                                                create_time datetime DEFAULT NULL COMMENT 'Create time',
                                                PRIMARY KEY (log_id),
                                                KEY idx_ai_chat_buyer (buyer_id),
                                                KEY idx_ai_chat_time (create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT='AI customer service chat log';

-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.


-- Data rows omitted from the public source snapshot.

