package net.nashlegend.sourcewall.util;

import net.nashlegend.sourcewall.AppApplication;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.model.SubItem;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by NashLegend on 2015/1/9 0009
 * 用户操作读取主题站、小组、问答的频道
 */
public class ChannelHelper {

    public static ArrayList<SubItem> getSections() {
        SubItem[] items = {new SubItem(SubItem.Section_Article, SubItem.Type_Collections, AppApplication.getApplication().getResources().getString(R.string.article), "Article"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Collections, AppApplication.getApplication().getResources().getString(R.string.post), "Post"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Collections, AppApplication.getApplication().getResources().getString(R.string.question), "Question")};
        ArrayList<SubItem> subItems = new ArrayList<>();
        Collections.addAll(subItems, items);
        return subItems;
    }

    public static ArrayList<SubItem> getArticles() {
        SubItem[] items = {new SubItem(SubItem.Section_Article, SubItem.Type_Collections, "科学人", ""),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "热点", "hot"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "前沿", "frontier"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "评论", "review"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "专访", "interview"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "视觉", "visual"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "速读", "brief"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "谣言粉碎机", "fact"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "商业科技", "techb"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "物理", "physics"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "生物", "biology"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "环境", "environment"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "天文", "astronomy"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "医学", "medicine"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "食物", "food"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "法证", "forensic"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "性情", "sex"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "地学", "earth"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "心理", "psychology"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "化学", "chemistry"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "科幻", "sci-fiction"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "数学", "math"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "DIY", "diy"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "农学", "agronomy"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "工程", "engineering"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "电子", "electronics"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "大气", "atmosphere"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "教育", "education"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "传播", "communication"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "社会", "society"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "互联网", "internet"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "航空航天", "aerospace"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Subject_Channel, "其他", "others"),
        };
        ArrayList<SubItem> subItems = new ArrayList<>();
        Collections.addAll(subItems, items);
        return subItems;
    }

    //new SubItem(SubItem.Section_Post, SubItem.Type_Private_Channel, "我的小组", "user_group"),
    public static ArrayList<SubItem> getPosts() {
        SubItem[] items = {
                new SubItem(SubItem.Section_Post, SubItem.Type_Collections, "小组热贴", "hot_posts"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "谣言粉碎机", "40"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "DIY", "27"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "自然控", "36"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "死理性派", "39"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "Geek笑点低", "63"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "吃货研究所", "69"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "谋杀 现场 法医", "31"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "美丽也是技术活", "73"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "情感夜夜话", "127"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "心事鉴定组", "33"),
        };
        ArrayList<SubItem> subItems = new ArrayList<>();
        Collections.addAll(subItems, items);
        return subItems;
    }

    public static ArrayList<SubItem> getQuestions() {
        SubItem[] items = {new SubItem(SubItem.Section_Question, SubItem.Type_Collections, "热门问答", "hottest"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Collections, "精彩回答", "highlight"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "生活", "生活"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "生物", "生物"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "健康", "健康"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "医学", "医学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "心理学", "心理学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "物理学", "物理学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "求真相", "求真相"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "化学", "化学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "生物学", "生物学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "社会科学", "社会科学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "互联网", "互联网"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "数学", "数学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "电子", "电子"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "食物", "食物"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "运动", "运动"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "计算机", "计算机"),
        };
        ArrayList<SubItem> subItems = new ArrayList<>();
        Collections.addAll(subItems, items);
        return subItems;
    }
}
