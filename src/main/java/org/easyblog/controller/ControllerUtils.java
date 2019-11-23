package org.easyblog.controller;

import org.easyblog.bean.Article;
import org.easyblog.bean.ArticleCount;
import org.easyblog.bean.Category;
import org.easyblog.enumHelper.ArticleType;
import org.easyblog.service.impl.ArticleServiceImpl;
import org.easyblog.service.impl.CategoryServiceImpl;
import org.easyblog.service.impl.CommentServiceImpl;
import org.easyblog.service.impl.UserAttentionImpl;
import org.springframework.ui.Model;

import java.util.List;


class ControllerUtils {

    private final CategoryServiceImpl categoryService;
    private final ArticleServiceImpl articleService;
    private final CommentServiceImpl commentService;
    private final UserAttentionImpl userAttention;

    ControllerUtils(CategoryServiceImpl categoryService, ArticleServiceImpl articleService, CommentServiceImpl commentService, UserAttentionImpl userAttention) {
        this.categoryService = categoryService;
        this.articleService = articleService;
        this.commentService = commentService;
        this.userAttention = userAttention;
    }

    /***
     * 这是几个页面公共需要查询数据
     * @param model
     * @param userId
     * @param articleType
     */
    void getArticleUserInfo(Model model, int userId, String articleType) {
        try {
            //查作者的所有允许显示的分类
            List<Category> lists = categoryService.getUserAllViableCategory(userId);
            //查作者的所有归档
            List<ArticleCount> archives = articleService.getUserAllArticleArchives(userId);
            //查作者的最新文章
            List<Article> newestArticles = articleService.getUserNewestArticles(userId, 5);
            //查作者的原创文章数
            Article article = new Article();
            article.setArticleUser(userId);
            article.setArticleType(ArticleType.Original.getArticleType());
            int originalArticle = articleService.countSelective(article);
            //查作者的指定类型的所有文章
            List<Article> articles = articleService.getUserArticles(userId, articleType);
            //关于我的文章的评论数
            int receiveCommentNum = commentService.getReceiveCommentNum(userId);
            //我的关注数
            int attentionNumOfMe = userAttention.countAttentionNumOfMe(userId);
            model.addAttribute("attentionNumOfMe",attentionNumOfMe);
            model.addAttribute("receiveCommentNum",receiveCommentNum);
            model.addAttribute("categories", lists);
            model.addAttribute("archives", archives);
            model.addAttribute("newestArticles", newestArticles);
            model.addAttribute("articles", articles);
            model.addAttribute("articleNum", articles.size());
            model.addAttribute("originalArticleNum", originalArticle);
        }catch (Exception  e){
            return;
        }
    }


}
