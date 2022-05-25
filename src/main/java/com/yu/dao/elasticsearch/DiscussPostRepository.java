package com.yu.dao.elasticsearch;

import com.yu.pojo.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 讨论发布库
 *
 * @author yu
 * @date 2022/05/24
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

}
