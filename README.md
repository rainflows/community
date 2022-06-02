# community
nowcoder.community
牛客社区项目
根据牛客网项目视频内容手打，项目中主要涉及发帖、评论、私信、转发、点赞、关注、通知、搜索、权限、统计等多个核心功能
项目利用SpringBoot进行构建，利用Redis实现点赞和关注功能，利用kafka实现异步的系统通知，利用Elasticsearch实现帖子搜索功能，利用Caffeine+Redis实现两级缓存优化热帖访问，利用Spring Security实现权限控制，利用Quartz实现任务调度功能并定时计算帖子分数赫尔清理垃圾文件
Elasticsearch实现搜索功能部分相比原视频做了较大改动
SpringBoot2.6.7 + MySQL8.0 + Redis3.2.1 + Kafka3.2.0 + Elasticsearch7.15.2
