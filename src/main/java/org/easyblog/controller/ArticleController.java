package org.easyblog.controller;

import org.easyblog.bean.Article;
import org.easyblog.bean.User;
import org.easyblog.bean.UserComment;
import org.easyblog.enumHelper.ArticleType;
import org.easyblog.service.impl.ArticleServiceImpl;
import org.easyblog.service.impl.CategoryServiceImpl;
import org.easyblog.service.impl.CommentServiceImpl;
import org.easyblog.service.impl.UserServiceImpl;
import org.easyblog.utils.HtmlParserUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/article")
public class ArticleController {


    private final UserServiceImpl userService;
    private final CategoryServiceImpl categoryServiceImpl;
    private final ArticleServiceImpl articleServiceImpl;
    private final CommentServiceImpl commentService;
    private final String PAGE404="/error/404";

    public ArticleController(CategoryServiceImpl categoryServiceImpl, UserServiceImpl userService, ArticleServiceImpl articleServiceImpl, CommentServiceImpl commentService) {
        this.categoryServiceImpl = categoryServiceImpl;
        this.userService = userService;
        this.articleServiceImpl = articleServiceImpl;
        this.commentService = commentService;
    }

    @RequestMapping(value = "/index/{userId}")
    public String index(@PathVariable("userId") int userId,
                        @RequestParam(value = "articleType", defaultValue = "3") int articleType,
                        Model model) {
        new ControllerUtils(categoryServiceImpl, articleServiceImpl).getArticleUserInfo(model, userId, articleType + "");
        final User user = userService.getUser(userId);
        List<Article> articles = articleServiceImpl.getUserArticles(userId, articleType + "");
        if (Objects.nonNull(articles)) {
                articles.forEach(article -> article.setArticleContent(HtmlParserUtil.HTML2Text(article.getArticleContent())));
            model.addAttribute("articles", articles);
            user.setUserPassword(null);
            model.addAttribute("user", user);
            if (ArticleType.Original.getArticleType().equals(articleType + "")) {
                model.addAttribute("displayOnlyOriginal", "1");
            } else if (ArticleType.Unlimited.getArticleType().equals(articleType + "")) {
                model.addAttribute("displayOnlyOriginal", "0");
            }
            return "user_home";
        }
        return PAGE404;
    }


    @RequestMapping(value = "/home/{userId}")
    public String homePage(@PathVariable("userId") int userId, Model model) {
        final User user = userService.getUser(userId);
        user.setUserPassword(null);
        List<Article> articles = articleServiceImpl.getUserArticles(userId, ArticleType.Unlimited.getArticleType());
        if(Objects.nonNull(articles)) {
            articles.forEach(article -> {
                article.setArticleContent(HtmlParserUtil.HTML2Text(article.getArticleContent()));
            });
            int articleSize=articles.size();
            if (articleSize < 15) {
                model.addAttribute("articles", articles);
            } else {
                model.addAttribute("articles", articles.subList(0, 15));
            }
            model.addAttribute("articleSize",articleSize);
            model.addAttribute("user", user);
            return "home";
        }
        return PAGE404;
    }


    @GetMapping(value = "/details/{articleId}")
    public String articleDetails(@PathVariable("articleId") int articleId, Model model) {
        final Article article = articleServiceImpl.getArticleById(articleId);
        model.addAttribute("article", article);
        if(Objects.nonNull(article)) {
            User user = userService.getUser(article.getArticleUser());
            model.addAttribute("user", user);
            List<UserComment> articleComments = commentService.getArticleComments(article.getArticleId());
            model.addAttribute("articleComments",articleComments);
            if (Objects.nonNull(user)) {
                User var0 = new User();
                var0.setUserId(user.getUserId());
                var0.setUserVisit(user.getUserVisit() + 1);
                userService.updateUserInfo(var0);
                user.setUserPassword(null);
                new ControllerUtils(categoryServiceImpl, articleServiceImpl).getArticleUserInfo(model, user.getUserId(), ArticleType.Original.getArticleType());
                return "blog";
            }
        }
        return PAGE404;
    }


}
