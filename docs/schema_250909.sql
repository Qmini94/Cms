create table board
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                             null comment '게시판 식별자',
    depart_name       varchar(250)                            null comment '부서명',
    reg_pin           varchar(250)                            null comment '등록자 PIN',
    reg_id            varchar(250)                            null comment '등록자 ID',
    reg_name          varchar(30)                             null comment '등록자 이름',
    created_date      datetime                                null comment '등록일',
    updated_date      datetime                                null comment '수정일',
    reg_ip            varchar(15)                             null comment '등록자 IP',
    is_deleted        tinyint(1)             default 0        null comment '삭제 여부',
    view_count        int                    default 0        null,
    search_tag        varchar(100)                            null comment '검색 태그',
    is_top_fixed      tinyint(1)             default 0        null comment '상단 고정 여부',
    top_start         datetime                                null comment '상단고정시작일시',
    top_end           datetime                                null comment '상단고정종료일시',
    pidx              int unsigned           default '0'      null comment '상위고유번호',
    level             int unsigned           default '0'      null comment '레벨',
    seq               tinyint unsigned       default '0'      null comment '정렬 시퀀스',
    sort              decimal(10, 2)                          null comment '정렬',
    admin_comment     longtext                                null comment '관리자메모',
    admin_comment_to  enum ('all', 'writer') default 'writer' null comment '관리자메모 노출범위(전체/작성자)',
    open_status       enum ('y', 'a', 'n')   default 'y'      null comment '공개 여부',
    is_approved       tinyint(1)             default 1        null comment '승인 여부',
    category_1        varchar(50)                             null comment '카테고리1',
    category_2        varchar(50)                             null comment '카테고리2',
    process_1         varchar(50)                             null comment '처리 구분',
    title             varchar(255)                            null comment '제목',
    content           longtext                                null comment '내용',
    mainimage_idx     bigint                                  null comment '대표 이미지 고유값',
    period_start      varchar(10)                             null comment '게시 시작일',
    period_end        varchar(10)                             null comment '게시 종료일',
    contents_original longtext                                null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_activity
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_apply
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_articles
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_autonomy
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_banner
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_bidding_announcement
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_budget
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_card_news
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_company_request
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_customer_opinion
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_data
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_freeboard
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_hj_inform
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_hope
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_human_rights
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_inconvenience
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_info_list
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_information
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_job_new
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_master
(
    idx                     bigint auto_increment comment 'PK'
        primary key,
    board_id                varchar(50)                          not null comment '게시판 식별용 ID',
    board_name              varchar(100)                         not null comment '게시판 이름',
    description             text                                 null comment '게시판 설명',
    is_use                  tinyint(1) default 1                 null comment '사용 여부',
    board_type              varchar(10)                          null comment '게시판 유형',
    is_admin_approval       tinyint(1) default 0                 not null comment '관리자 승인 후 노출 여부',
    is_privacy_option       tinyint(1) default 0                 not null comment '공개/비공개 옵션 사용 여부',
    max_file_upload         int        default 0                 null comment '첨부파일 최대 개수',
    max_total_file_size     int        default 0                 null comment '전체 업로드 파일 최대 크기(KB)',
    restricted_files        varchar(255)                         null comment '업로드 제한 파일 확장자(쉼표로 구분)',
    max_file_size           int        default 0                 null comment '업로드 파일 최대 크기(KB)',
    allowed_images          varchar(255)                         null comment '업로드 허용 사진 확장자(쉼표로 구분)',
    max_image_size          int        default 0                 null comment '업로드 사진 최대 크기(KB)',
    is_sms_alert            tinyint(1) default 0                 not null comment '게시글 등록 시 SMS 알림 사용 여부',
    is_required_fields      tinyint(1) default 0                 not null comment '필수 입력 필드 사용 여부',
    is_comment              tinyint(1) default 0                 not null comment '댓글 사용 여부',
    is_use_period           tinyint(1) default 0                 not null comment '게시 기간 사용 여부',
    is_author_posts_view    tinyint(1) default 0                 not null comment '작성자가 쓴 글만 보기',
    is_admin_deleted_view   tinyint(1) default 0                 not null comment '관리자 삭제된 글 보기',
    list_count              int        default 10                null comment '목록 수',
    is_show_author          tinyint(1) default 1                 not null comment '등록자 표시',
    is_show_date            tinyint(1) default 1                 not null comment '등록일 표시',
    is_search_field_control tinyint(1) default 0                 not null comment '검색 필드 설정 관리',
    is_top_post             tinyint(1) default 0                 not null comment '게시물(top) 설정',
    created_date            datetime   default CURRENT_TIMESTAMP not null comment '생성일시',
    updated_date            datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정일시',
    constraint board_id
        unique (board_id)
)
    comment '게시판 정의 테이블' collate = utf8mb4_general_ci;

create table board_field_definition
(
    id               bigint auto_increment comment '필드 정의 고유 ID'
        primary key,
    board_master_idx bigint                               not null comment '게시판 IDX (FK to board_master.idx)',
    field_name       varchar(100)                         not null comment '컬럼명 (DB 실제 컬럼명)',
    display_name     varchar(100)                         not null comment '화면 출력용 필드명',
    field_type       varchar(50)                          not null comment '필드 타입 (VARCHAR, TEXT, DATE, INT 등)',
    is_required      tinyint(1) default 0                 null comment '필수 여부',
    is_searchable    tinyint(1) default 0                 null comment '검색 조건에서 사용 여부',
    field_order      int        default 0                 null comment '출력 순서',
    default_value    varchar(255)                         null comment '기본값 (선택)',
    placeholder      varchar(255)                         null comment '입력 힌트 (프론트용)',
    created_date     datetime   default CURRENT_TIMESTAMP null comment '생성일시',
    updated_date     datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '수정일시',
    constraint fk_board_field_definition_board_idx
        foreign key (board_master_idx) references board_master (idx)
            on delete cascade
)
    comment '게시판별 필드 정의 테이블' collate = utf8mb4_general_ci;

create index idx_board_idx
    on board_field_definition (board_master_idx);

create table board_monitoring
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_news
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_news_a
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_news_b
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_newsletter
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_notice
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    depart_name       varchar(250)                 null,
    reg_pin           varchar(250)                 null,
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null,
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null,
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null,
    top_end           datetime                     null,
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null,
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null,
    category_2        varchar(50)                  null,
    process_1         varchar(50)                  null,
    title             varchar(255)                 null,
    content           longtext                     null,
    mainimage_idx     bigint                       null,
    period_start      varchar(10)                  null,
    period_end        varchar(10)                  null,
    contents_original longtext                     null,
    test              varchar(255)                 null
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_notice_yumcorp
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_photo
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_popup
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_praise_relay
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_private_contract
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_promote
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_question
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_resident_participation
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_safety_health
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_satisfaction
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_singo
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_situation
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_social
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_suggestion
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_suggestion_page
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                             null comment '게시판 식별자',
    depart_name       varchar(250)                            null comment '부서명',
    reg_pin           varchar(250)                            null comment '등록자 PIN',
    reg_id            varchar(250)                            null comment '등록자 ID',
    reg_name          varchar(30)                             null comment '등록자 이름',
    created_date      datetime                                null comment '등록일',
    updated_date      datetime                                null comment '수정일',
    reg_ip            varchar(15)                             null comment '등록자 IP',
    is_deleted        tinyint(1)             default 0        null comment '삭제 여부',
    view_count        int                    default 0        null,
    search_tag        varchar(100)                            null comment '검색 태그',
    is_top_fixed      tinyint(1)             default 0        null comment '상단 고정 여부',
    top_start         datetime                                null comment '상단고정시작일시',
    top_end           datetime                                null comment '상단고정종료일시',
    pidx              int unsigned           default '0'      null comment '상위고유번호',
    level             int unsigned           default '0'      null comment '레벨',
    seq               tinyint unsigned       default '0'      null comment '정렬 시퀀스',
    sort              decimal(10, 2)                          null comment '정렬',
    admin_comment     longtext                                null comment '관리자메모',
    admin_comment_to  enum ('all', 'writer') default 'writer' null comment '관리자메모 노출범위(전체/작성자)',
    open_status       enum ('y', 'a', 'n')   default 'y'      null comment '공개 여부',
    is_approved       tinyint(1)             default 1        null comment '승인 여부',
    category_1        varchar(50)                             null comment '카테고리1',
    category_2        varchar(50)                             null comment '카테고리2',
    process_1         varchar(50)                             null comment '처리 구분',
    title             varchar(255)                            null comment '제목',
    content           longtext                                null comment '내용',
    mainimage_idx     bigint                                  null comment '대표 이미지 고유값',
    period_start      varchar(10)                             null comment '게시 시작일',
    period_end        varchar(10)                             null comment '게시 종료일',
    contents_original longtext                                null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table board_testyang
(
    idx          bigint auto_increment
        primary key,
    title        varchar(255)                         not null,
    content      longtext                             null,
    is_deleted   tinyint(1) default 0                 not null,
    view_count   int        default 0                 not null,
    reg_id       varchar(50)                          null,
    reg_name     varchar(100)                         null,
    created_date datetime   default CURRENT_TIMESTAMP not null,
    updated_date datetime                             null,
    test         varchar(32)                          null
)
    collate = utf8mb4_general_ci;

create index idx_board_testyang__created
    on board_testyang (created_date);

create index idx_board_testyang__isdel_created
    on board_testyang (is_deleted, created_date);

create table board_types
(
    idx          int auto_increment comment '게시판 타입 고유번호 (PK)'
        primary key,
    type         varchar(50)  not null comment '게시판 타입 식별 ID (예: list, gallery, form)',
    type_name    varchar(100) not null comment '게시판 타입 이름 (예: 일반형 게시판)',
    description  text         null comment '게시판 타입 설명',
    created_date datetime(6)  not null,
    updated_date datetime(6)  not null,
    type_id      varchar(50)  not null,
    constraint type_id
        unique (type)
)
    comment '게시판 동작 타입 정의 테이블' collate = utf8mb4_general_ci;

create table board_video
(
    idx               bigint auto_increment comment '고유번호'
        primary key,
    board_id          varchar(50)                  null comment '게시판 식별자',
    depart_name       varchar(250)                 null comment '부서명',
    reg_pin           varchar(250)                 null comment '등록자 PIN',
    reg_id            varchar(250)                 null comment '등록자 ID',
    reg_name          varchar(30)                  null comment '등록자 이름',
    created_date      datetime                     null comment '등록일',
    updated_date      datetime                     null comment '수정일',
    reg_ip            varchar(15)                  null comment '등록자 IP',
    is_deleted        tinyint(1)       default 0   null comment '삭제 여부',
    view_count        int              default 0   null,
    search_tag        varchar(100)                 null comment '검색 태그',
    is_top_fixed      tinyint(1)       default 0   null comment '상단 고정 여부',
    top_start         datetime                     null comment '상단고정시작일시',
    top_end           datetime                     null comment '상단고정종료일시',
    pidx              int unsigned     default '0' null comment '상위고유번호',
    level             int unsigned     default '0' null comment '레벨',
    seq               tinyint unsigned default '0' null comment '정렬 시퀀스',
    admin_comment     longtext                     null comment '관리자메모',
    is_approved       tinyint(1)       default 1   null comment '승인 여부',
    category_1        varchar(50)                  null comment '카테고리1',
    category_2        varchar(50)                  null comment '카테고리2',
    process_1         varchar(50)                  null comment '처리 구분',
    title             varchar(255)                 null comment '제목',
    content           longtext                     null comment '내용',
    mainimage_idx     bigint                       null comment '대표 이미지 고유값',
    period_start      varchar(10)                  null comment '게시 시작일',
    period_end        varchar(10)                  null comment '게시 종료일',
    contents_original longtext                     null comment '원본 콘텐츠'
)
    comment '게시글 테이블' collate = utf8mb4_general_ci
                      row_format = DYNAMIC;

create table cms_menu
(
    id          bigint unsigned auto_increment comment '고유번호'
        primary key,
    parent_id   bigint unsigned                         null comment '상속메뉴',
    position    int                                     not null comment '트리 - 포지션',
    level       bigint unsigned                         not null comment '트리 - 레벨',
    title       varchar(255) collate utf8mb4_unicode_ci null comment '메뉴명',
    name        varchar(255)                            null comment '메뉴 이름',
    type        varchar(255) collate utf8mb4_unicode_ci null comment '메뉴 타입',
    value       varchar(255)                            null comment '구분값',
    is_show     tinyint(1) default 1                    not null comment '디스플레이 여부',
    path_url    varchar(255)                            null comment '경로 - url',
    path_string varchar(255)                            null comment '경로 - 경로명',
    path_id     varchar(255)                            null comment '경로 - 아이디',
    constraint uq_type_name
        unique (type, name)
)
    comment 'yubi-cs 사이트 메뉴' collate = utf8mb4_general_ci;

create index idx_path_id
    on cms_menu (path_id);

create index type
    on cms_menu (type);

create index value
    on cms_menu (value);

create table cms_permission
(
    idx      int auto_increment comment '고유번호'
        primary key,
    menu_id  int                         not null comment '메뉴번호',
    type     enum ('id', 'level')        not null comment '권한유형(''id'',''level'')',
    value    varchar(30)                 null comment '값',
    manage   enum ('y', 'n') default 'n' not null comment '담당구분',
    admin    enum ('n', 'y') default 'n' null comment '관리자 유무',
    access   enum ('y', 'n') default 'y' null comment '접근권한',
    view     enum ('y', 'n') default 'y' null comment '보기권한',
    `write`  enum ('y', 'n') default 'n' not null comment '쓰기권한',
    modify   enum ('y', 'n') default 'n' not null comment '수정권한',
    reply    enum ('y', 'n') default 'n' not null comment '답변권한',
    remove   enum ('y', 'n') default 'n' not null comment '삭제권한',
    sort     int                         null comment '우선순위',
    reg_user varchar(30)                 null comment '등록자',
    reg_date datetime                    null comment '등록일',
    mod_user varchar(30)                 null comment '수정자',
    mod_date datetime                    null comment '수정일',
    del      enum ('y', 'n') default 'n' null comment '삭제여부',
    del_user varchar(30)                 null comment '삭제한 유저',
    del_date datetime                    null comment '삭제일자'
)
    comment 'yubi-cs 사용권한(메뉴별)' collate = utf8mb4_general_ci;

create index menu
    on cms_permission (menu_id);

create table cms_permission_1
(
    idx      int auto_increment comment '고유번호'
        primary key,
    menu_id  int                         not null comment '메뉴번호',
    type     enum ('id', 'level')        not null comment '권한유형(''id'',''name'',''department'',''position'',''ip'',''login'')',
    value    varchar(30)                 null comment '값',
    manage   enum ('y', 'n') default 'n' not null comment '담당구분',
    admin    enum ('n', 'y') default 'n' null comment '관리자 유무',
    access   enum ('y', 'n') default 'y' null comment '접근권한',
    view     enum ('y', 'n') default 'y' null comment '보기권한',
    `write`  enum ('y', 'n') default 'n' not null comment '쓰기권한',
    modify   enum ('y', 'n') default 'n' not null comment '수정권한',
    reply    enum ('y', 'n') default 'n' not null comment '답변권한',
    remove   enum ('y', 'n') default 'n' not null comment '삭제권한',
    sort     int                         null comment '우선순위',
    reg_user varchar(30)                 null comment '등록자',
    reg_date datetime                    null comment '등록일',
    mod_user varchar(30)                 null comment '수정자',
    mod_date datetime                    null comment '수정일',
    del      enum ('y', 'n') default 'n' null comment '삭제여부',
    del_user varchar(30)                 null comment '삭제한 유저',
    del_date datetime                    null comment '삭제일자'
)
    comment 'yubi-cs 사용권한(메뉴별)' collate = utf8mb4_general_ci;

create index menu
    on cms_permission_1 (menu_id);

create table content
(
    idx          int auto_increment comment '고유번호'
        primary key,
    parent_id    int               null comment '컨텐츠 그룹 대표 ID',
    hostname     varchar(30)       not null comment '등록된 사이트 도메인명',
    is_use       tinyint default 0 not null comment '컨텐츠 사용 유무',
    is_main      tinyint default 0 null,
    title        varchar(200)      null comment '제목',
    content      mediumtext        not null comment '컨텐츠 내용',
    updated_by   varchar(30)       null comment '수정자 아이디',
    created_by   varchar(30)       null comment '등록자 아이디',
    updated_date datetime          null comment '수정일',
    created_date datetime          null comment '등록일'
)
    comment '콘텐츠 관리' collate = utf8mb4_general_ci;

create index section_idx
    on content (is_use);

create table department
(
    id         bigint                    not null comment '고유 아이디'
        primary key,
    parent_id  bigint                    null comment '부모 아이디',
    position   bigint                    not null comment '위치',
    path       varchar(255)              not null comment '경로 (Dot Notation)',
    depth      int                       not null comment '계층 깊이',
    title      varchar(255)              null comment '부서명',
    full_title varchar(255)              null comment '전체 부서명 (조직 전체 포함)',
    type       enum ('part', 'position') null comment '부서 유형',
    pcode      varchar(100)              null comment '부서 코드',
    tel        varchar(50)               null comment '전화번호',
    fax        varchar(50)               null comment '팩스번호',
    details    varchar(500)              null comment '상세 설명',
    reg_date   datetime                  null comment '등록일'
)
    comment '조직도 부서 테이블' collate = utf8mb4_general_ci;

create table member
(
    idx                int auto_increment comment '기본 키 (자동 증가)'
        primary key,
    user_name          varchar(255)                null comment '사용자 이름',
    user_nick          varchar(255)                null comment '사용자 닉네임',
    user_id            varchar(255)                not null comment '사용자 ID (고유)',
    user_level         int                         null comment '사용자 등급 (권한 레벨)',
    user_pin           varchar(255)                not null comment '사용자 PIN 코드',
    user_password      varchar(255)                null comment '비밀번호 (해시값)',
    group_idx          int                         null comment '그룹 ID',
    dept_code          varchar(255)                null comment '부서 코드',
    dept_id            int                         null comment '부서 ID',
    dept_position      varchar(255)                null comment '부서 내 직책',
    dept_sort          int                         null comment '부서 정렬 순서',
    dept_work          text                        null comment '부서 업무 설명',
    dept_tel           varbinary(50)               null comment '부서 전화번호',
    dept_fax           varbinary(50)               null comment '부서 팩스번호',
    dept_class         varchar(100)                null comment '부서 유형 (예: 인사, 개발 등)',
    email              varbinary(255)              null comment '사용자 이메일',
    tel                varbinary(50)               null comment '사용자 전화번호',
    phone              varbinary(50)               null comment '사용자 휴대폰 번호',
    zipcode            varbinary(20)               null comment '우편번호',
    address1           varbinary(255)              null comment '기본 주소',
    address2           varbinary(255)              null comment '상세 주소',
    pass_hint_question varchar(255)                null comment '비밀번호 힌트 질문',
    pass_hint_answer   varbinary(255)              null comment '비밀번호 힌트 답변',
    reg_date           datetime                    null comment '회원 가입일',
    last_login_date    datetime                    null comment '마지막 로그인 날짜',
    last_login_ip      varchar(45)                 null comment '마지막 로그인 IP',
    recv_sms           enum ('n', 'y') default 'n' null comment 'SMS 수신 여부 (n: 미수신, y: 수신)',
    recv_mail          enum ('n', 'y') default 'n' null comment '이메일 수신 여부 (n: 미수신, y: 수신)',
    foreigner          enum ('n', 'y') default 'n' null comment '외국인 여부 (n: 내국인, y: 외국인)',
    staff_id           varchar(255)                null comment '동기화시 고유 식별값',
    agree_date         datetime                    null comment '약관 동의 날짜',
    birthday           date                        null comment '생년월일',
    info_update_date   datetime                    null comment '정보 업데이트 날짜',
    device_id          varchar(255)                null comment '사용자 기기 ID',
    auth_token         varchar(255)                null comment '인증 토큰',
    simple_pw          varchar(100)                null comment '간편 비밀번호 (PIN 방식)',
    bookmark           varchar(255)                null comment '즐겨찾기 정보',
    is_sync            enum ('0', '1')             null comment '동기화 여부'
)
    comment '사용자 정보 테이블' collate = utf8mb4_general_ci;

create table site
(
    idx           int auto_increment comment '고유번호'
        primary key,
    site_name     varchar(60) default '' not null comment '사이트 이름',
    site_hostname varchar(20)            null comment '사이트 호스트명(서브도메인)',
    site_domain   varchar(40) default '' not null comment '사이트 도메인',
    is_open       tinyint(1)  default 0  not null comment '활성화 여부',
    is_deleted    tinyint(1)  default 0  not null comment '삭제 여부',
    allow_ip      text                   null comment '허용 아이피',
    deny_ip       text                   null comment '차단 아이피',
    constraint uq_site_hostname
        unique (site_hostname)
)
    comment '사이트정보(설정)' collate = utf8mb4_general_ci;

create table cms_site_layout
(
    idx            bigint auto_increment
        primary key,
    site_idx       int                                  not null,
    kind           enum ('MAIN', 'SUB')                 not null,
    html           longtext                             not null,
    version        int        default 1                 not null,
    is_published   tinyint(1) default 1                 not null,
    updated_at     datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    extra_css_urls longtext                             null,
    extra_js_urls  longtext                             null,
    constraint uk_site_kind
        unique (site_idx, kind, is_published),
    constraint cms_site_layout_ibfk_1
        foreign key (site_idx) references site (idx)
)
    collate = utf8mb4_unicode_ci;

create table visit_site
(
    idx        bigint auto_increment comment '고유 번호'
        primary key,
    visit_date date                   not null comment '날짜',
    web_cnt    int         default 0  null comment '웹 방문자 수',
    mobile_cnt int         default 0  null comment '모바일 방문자 수',
    hostname   varchar(50) default '' null comment '호스트 이름',
    constraint uq_visit_date_hostname
        unique (visit_date, hostname)
)
    comment '사이트 방문자수' collate = utf8mb4_general_ci;

