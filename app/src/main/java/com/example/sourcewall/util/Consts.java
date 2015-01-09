package com.example.sourcewall.util;

/**
 * Created by NashLegend on 2014/9/23 0023
 * 固定不变的一些参数，比如intent、sharedPreference等的key值或者Action名
 */
public class Consts {

    public static final String Action_Open_Content_Fragment = "sourcewall.action.open.content.fragment";

    public static final String Action_Open_Articles_Fragment = "sourcewall.action.open.articles.fragment";
    public static final String Action_Open_Posts_Fragment = "sourcewall.action.open.posts.fragment";
    public static final String Action_Open_Questions_Fragment = "sourcewall.action.open.questions.fragment";

    // usually used in intent.put
    public static final String Extra_Ace_Model = "sourcewall.extra.ace.model.id";
    public static final String Extra_Article = "sourcewall.extra.article.id";
    public static final String Extra_Simple_Comment = "sourcewall.extra.simple.comment.id";
    public static final String Extra_Post = "sourcewall.extra.post.id";
    public static final String Extra_Question = "sourcewall.extra.question.id";
    public static final String Extra_Answer = "sourcewall.extra.answer.id";
    public static final String Extra_SubItem = "sourcewall.extra.subitem.id";

    // usually used in SharedPreferences
    public static final String Key_Cookie = "sourcewall.key.cookie";
    public static final String Key_Access_Token = "sourcewall.key.access.token";
    public static final String Key_Ukey = "sourcewall.key.ukey";
    public static final String Key_Is_Night_Mode = "sourcewall.key.is.night.mode";

    // login webpage
    public static final String LOGIN_URL = "https://account.guokr.com/sign_in/?display=mobile";
    public static final String SUCCESS_URL_1 = "http://m.guokr.com/";
    public static final String SUCCESS_URL_2 = "http://www.guokr.com/";
    public static final String Cookie_Token_Key = "_32353_access_token";
    public static final String Cookie_Ukey_Key = "_32353_ukey";

    //webview
    public static final String Base_Url = "http://www.guokr.com/";
    public static final String Base_Url2 = "file:///android_asset/";

}
