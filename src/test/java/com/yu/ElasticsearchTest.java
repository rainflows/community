package com.yu;

import com.yu.dao.DiscussPostMapper;
import com.yu.dao.elasticsearch.DiscussPostRepository;
import com.yu.pojo.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * elasticsearch测试
 *
 * @author yu
 * @date 2022/05/24
 */
@SpringBootTest
public class ElasticsearchTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100));
    }

    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我是新人，我特么疯狂灌水");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete() {
        discussPostRepository.deleteById(231);
    }

    @Test
    public void testSearchByRestTemplate() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
//                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC))
//                .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
//                .withSorts(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>"))
                .build();
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        List<DiscussPost> posts = new ArrayList<>();
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            searchHit.getContent().setTitle(highlightFields.get("title") == null ? searchHit.getContent().getTitle() : highlightFields.get("title").get(0));
            searchHit.getContent().setContent(highlightFields.get("content") == null ? searchHit.getContent().getContent() : highlightFields.get("content").get(0));
            posts.add(searchHit.getContent());
        }
        for (DiscussPost post : posts) {
            System.out.println(post);
        }
    }
}
