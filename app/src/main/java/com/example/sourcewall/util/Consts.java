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

    // 用于Intent传值时的Extra_Key
    public static final String Extra_Ace_Model = "sourcewall.extra.ace.model.id";
    public static final String Extra_Article = "sourcewall.extra.article.id";
    public static final String Extra_Simple_Comment = "sourcewall.extra.simple.comment.id";
    public static final String Extra_Post = "sourcewall.extra.post.id";
    public static final String Extra_Question = "sourcewall.extra.question.id";
    public static final String Extra_Answer = "sourcewall.extra.answer.id";
    public static final String Extra_SubItem = "sourcewall.extra.subitem.id";
    public static final String Extra_Should_Invalidate_Menu = "sourcewall.extra.should.invalidate.menu";
    public static final String Extra_Should_Load_Before_Shuffle = "sourcewall.extra.should.load.before.shuffle";

    // 保存SharedPreferences的Key
    public static final String Key_Cookie = "sourcewall.key.cookie";
    public static final String Key_Access_Token = "sourcewall.key.access.token";
    public static final String Key_Ukey = "sourcewall.key.ukey";
    public static final String Key_User_Name = "sourcewall.key.user.name";
    public static final String Key_User_ID = "sourcewall.key.user.id";
    public static final String Key_User_Avatar = "sourcewall.key.user.avatar";
    public static final String Key_Is_Night_Mode = "sourcewall.key.is.night.mode";
    public static final String Key_Last_Post_Groups_Version = "sourcewall.key.last.post.groups.version";
    public static final String Key_Image_Load_Mode = "sourcewall.key.image.load.mode";
    public static final String Key_Custom_Tail = "sourcewall.key.custom.tail";
    public static final String Key_Use_Post_Tail = "sourcewall.key.use.post.tail";
    public static final String key_Use_Default_Tail = "sourcewall.key.use.default.tail";

    // 保存SharedPreferences的Key，用于保存草稿
    public static final String Key_Sketch_Article_Reply = "sourcewall.key.sketch.article.reply";
    public static final String Key_Sketch_Post_Reply = "sourcewall.key.sketch.post.reply";
    public static final String Key_Sketch_Question_Answer = "sourcewall.key.sketch.question.answer";
    public static final String Key_Sketch_Publish_Post_Title = "sourcewall.key.sketch.publish.post.title";
    public static final String Key_Sketch_Publish_Post_Content = "sourcewall.key.sketch.publish.post.content";

    // 保存startActivityForResult的RequestCode
    public static final int Code_Login = 1025;
    public static final int Code_Publish_Post = 1026;
    public static final int Code_Publish_Question = 1027;
    public static final int Code_Start_Shuffle_Groups = 1028;
    public static final int Code_Invoke_Image_Selector = 1029;
    public static final int Code_Invoke_Camera = 1030;

    // 登录页保存Cookie
    public static final String LOGIN_URL = "https://account.guokr.com/sign_in/?display=mobile";
    public static final String SUCCESS_URL_1 = "http://m.guokr.com/";
    public static final String SUCCESS_URL_2 = "http://www.guokr.com/";
    public static final String Cookie_Token_Key = "_32353_access_token";
    public static final String Cookie_Ukey_Key = "_32353_ukey";

    //webview
    public static final String Base_Url = "http://www.guokr.com/";
    public static final String Base_Url2 = "file:///android_asset/";

    //图片加载模式
    public static final int MODE_ALWAYS_LOAD = 0;
    public static final int MODE_NEVER_LOAD = 1;
    public static final int MODE_LOAD_WHEN_WIFI = 2;

}
