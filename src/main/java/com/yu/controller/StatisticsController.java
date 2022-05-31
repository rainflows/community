package com.yu.controller;

import com.yu.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * 统计数据控制器
 *
 * @author yu
 * @date 2022/05/28
 */
@Controller
public class StatisticsController {

    /**
     * 统计服务
     */
    @Autowired
    private StatisticsService statisticsService;

    /**
     * 得到统计页面
     *
     * @return {@link String}
     */
    @RequestMapping(path = "/statistics", method = {RequestMethod.GET, RequestMethod.POST})
    public String getStatisticsPage() {
        return "/site/admin/data";
    }

    /**
     * 统计网站UV
     *
     * @param start 开始
     * @param end   结束
     * @param model 模型
     * @return {@link String}
     */
    @RequestMapping(path = "/statistics/uv", method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start, @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        long uv = statisticsService.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);
        return "forward:/statistics";
    }

    /**
     * 统计活跃用户
     *
     * @param start 开始
     * @param end   结束
     * @param model 模型
     * @return {@link String}
     */
    @RequestMapping(path = "/statistics/dau", method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start, @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        long dau = statisticsService.calculateDAU(start, end);
        model.addAttribute("dauResult", dau);
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);
        return "forward:/statistics";
    }
}
