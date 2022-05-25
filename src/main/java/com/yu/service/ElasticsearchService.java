package com.yu.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.yu.dao.elasticsearch.DiscussPostRepository;
import com.yu.pojo.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * elasticsearch服务
 *
 * @author yu
 * @date 2022/05/24
 */
@Service
public class ElasticsearchService {

    /**
     * 讨论发布库
     */
    @Autowired
    private DiscussPostRepository discussPostRepository;

    /**
     * elasticsearch其他模板
     */
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 保存讨论帖
     *
     * @param post 帖子
     */
    public void saveDiscussPost(DiscussPost post) {
        discussPostRepository.save(post);
    }

    /**
     * 删除讨论帖
     *
     * @param id id
     */
    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    /**
     * 搜索讨论帖
     *
     * @param keyword 关键字
//     * @param current 当前
//     * @param limit   限制
     * @return {@link List}<{@link DiscussPost}>
     */
    public List<DiscussPost> searchDiscussPost(String keyword) {
        // 搜索
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>"))
                .build();
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        // 得到查询结果列表
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        // 待返回集合
        List<DiscussPost> posts = new ArrayList<>();
        // 将遍历结果进行处理
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            // 高亮显示的内容
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            // 将高亮内容填充到content中
            searchHit.getContent().setTitle(highlightFields.get("title") == null ? searchHit.getContent().getTitle() : highlightFields.get("title").get(0));
            searchHit.getContent().setContent(highlightFields.get("content") == null ? searchHit.getContent().getContent() : highlightFields.get("content").get(0));
            // 传入返回集合
            posts.add(searchHit.getContent());
        }
        return posts;
    }

    /**
     * 查询页面信息,分页查询
     *
     * @param list        列表
     * @param currentPage 当前页面
     * @param pageSize    页面大小
     * @return {@link PageInfo}<{@link T}>
     */
    public <T> PageInfo<T> queryPageInfo(List<T> list, int currentPage, int pageSize) {
        int total = list.size();
        if (total > pageSize) {
            int toIndex = pageSize * currentPage;
            if (toIndex > total) {
                toIndex = total;
            }
            list = list.subList(pageSize * (currentPage - 1), toIndex);
        }

        Page<T> page = new Page<>(currentPage, pageSize);
        page.addAll(list);
        page.setPages((total + pageSize - 1) / pageSize);
        page.setTotal(total);

        PageInfo<T> pageInfo = new PageInfo<>(page);
        return pageInfo;
    }
}
