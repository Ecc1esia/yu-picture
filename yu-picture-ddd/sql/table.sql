-- 创建库
create database if not exists yu_picture;

-- 切换库
use yu_picture;

create table if not exists picture
(
    id             bigint auto_increment comment 'id'
        primary key,
    url            varchar(512)                       not null comment '图片 url',
    name           varchar(128)                       not null comment '图片名称',
    introduction   varchar(512)                       null comment '简介',
    category       varchar(64)                        null comment '分类',
    tags           varchar(512)                       null comment '标签（JSON 数组）',
    pic_size       bigint                             null comment '图片体积',
    pic_width      int                                null comment '图片宽度',
    pic_height     int                                null comment '图片高度',
    pic_scale      double                             null comment '图片宽高比例',
    pic_format     varchar(32)                        null comment '图片格式',
    user_id        bigint                             not null comment '创建用户 id',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    edit_time      datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    review_status  int      default 0                 not null comment '审核状态：0-待审核; 1-通过; 2-拒绝',
    review_message varchar(512)                       null comment '审核信息',
    reviewer_id    bigint                             null comment '审核人 ID',
    review_time    datetime                           null comment '审核时间',
    thumbnail_url  varchar(512)                       null comment '缩略图 url',
    space_id       bigint                             null comment '空间 id（为空表示公共空间）',
    pic_color      varchar(16)                        null comment '图片主色调',
    is_delete      tinyint  default 0                 not null comment '是否删除'
)
    comment '图片' collate = utf8mb4_unicode_ci;

create index idx_category
    on picture (category);

create index idx_introduction
    on picture (introduction);

create index idx_name
    on picture (name);

create index idx_review_status
    on picture (review_status);

create index idx_space_id
    on picture (space_id);

create index idx_tags
    on picture (tags);

create index idx_user_id
    on picture (user_id);

create table if not exists space
(
    id          bigint auto_increment comment 'id'
        primary key,
    space_name  varchar(128)                       null comment '空间名称',
    space_level int      default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
    max_size    bigint   default 0                 null comment '空间图片的最大总大小',
    max_count   bigint   default 0                 null comment '空间图片的最大数量',
    total_size  bigint   default 0                 null comment '当前空间下图片的总大小',
    total_count bigint   default 0                 null comment '当前空间下的图片数量',
    user_id     bigint                             not null comment '创建用户 id',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    edit_time   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '是否删除',
    space_type  int      default 0                 not null comment '空间类型：0-私有 1-团队'
)
    comment '空间' collate = utf8mb4_unicode_ci;

create index idx_space_level
    on space (space_level);

create index idx_space_name
    on space (space_name);

create index idx_space_type
    on space (space_type);

create index idx_user_id
    on space (user_id);

create table if not exists space_user
(
    id          bigint auto_increment comment 'id'
        primary key,
    space_id    bigint                                 not null comment '空间 id',
    user_id     bigint                                 not null comment '用户 id',
    space_role  varchar(128) default 'viewer'          null comment '空间角色：viewer/editor/admin',
    create_time datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted  tinyint      default 0                 not null,
    constraint uk_spaceId_userId
        unique (space_id, user_id)
)
    comment '空间用户关联' collate = utf8mb4_unicode_ci;

create index idx_spaceId
    on space_user (space_id);

create index idx_userId
    on space_user (user_id);

create table if not exists user
(
    id              bigint auto_increment comment 'id'
        primary key,
    user_account    varchar(256)                           not null comment '账号',
    user_password   varchar(512)                           not null comment '密码',
    user_name       varchar(256)                           null comment '用户昵称',
    user_avatar     varchar(1024)                          null comment '用户头像',
    user_profile    varchar(512)                           null comment '用户简介',
    user_role       varchar(256) default 'user'            not null comment '用户角色：user/admin',
    edit_time       datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    vip_expire_time datetime                               null comment '会员过期时间',
    vip_code        varchar(128)                           null comment '会员兑换码',
    vip_number      bigint                                 null comment '会员编号',
    is_delete       tinyint      default 0                 not null comment '是否删除',
    constraint uk_user_account
        unique (user_account)
)
    comment '用户' collate = utf8mb4_unicode_ci;

create index idx_user_name
    on user (user_name);
