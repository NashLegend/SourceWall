package net.nashlegend.sourcewall.util;

/**
 * Created by NashLegend on 2014/9/23 0023
 * 固定不变的一些参数，比如intent、sharedPreference等的key值或者Action名
 */
public class Consts {

    public static final class Actions {
        public static final String Action_Start_Loading_Latest = "sourcewall.action.start.loading.latest";
        public static final String Action_Finish_Loading_Latest = "sourcewall.action.end.loading.latest";
        public static final String Action_Open_Articles_Fragment = "sourcewall.action.open.articles.fragment";
        public static final String Action_Open_Posts_Fragment = "sourcewall.action.open.posts.fragment";
        public static final String Action_Open_Questions_Fragment = "sourcewall.action.open.questions.fragment";
    }


    public static final class Extras {
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
        public static final String Extra_Redirect_Uri = "sourcewall.extra.redirect.url";//通知的跳转链接
        public static final String Extra_Notice_Id = "sourcewall.extra.notice.id";//通知id
        public static final String Extra_Image_String_Array = "sourcewall.extra.image.string.array";//图片Activity接收的图片地址们
        public static final String Extra_Image_Current_Position = "sourcewall.extra.image.current.position";//图片Activity刚进入时打开的图片
        public static final String Extra_Shared_Url = "sourcewall.extra.share.url";//分享的url地址
        public static final String Extra_Shared_Title = "sourcewall.extra.share.title";//分享的标题
        public static final String Extra_Shared_Summary = "sourcewall.extra.share.summary";//分享的标题
        public static final String Extra_Shared_Bitmap = "sourcewall.extra.share.bitmap";//分享的图片
        public static final String Extra_Activity_Hashcode = "sourcewall.extra.activity.hashcode";//分享的图片
    }

    public static final class Keys {
        // 保存SharedPreferences的Key
        public static final String Key_Cookie = "sourcewall.key.cookie";
        public static final String Key_Access_Token = "sourcewall.key.access.token";
        public static final String Key_Access_Token_2 = "sourcewall.key.access.token.2";
        public static final String Key_Ukey = "sourcewall.key.ukey";
        public static final String Key_User_Name = "sourcewall.key.user.name";
        public static final String Key_User_ID = "sourcewall.key.user.id";
        public static final String Key_User_Avatar = "sourcewall.key.user.avatar";
        public static final String Key_Is_Night_Mode = "sourcewall.key.is.night.mode";
        public static final String Key_Last_Post_Groups_Version = "sourcewall.key.last.post.groups.version";
        public static final String Key_Last_Ask_Tags_Version = "sourcewall.key.last.ask.tag.groups.version";
        public static final String Key_Last_Basket_Version = "sourcewall.key.last.basket.groups.version";
        public static final String Key_Image_Load_Mode = "sourcewall.key.image.load.mode";
        public static final String Key_Image_No_Load_Homepage = "sourcewall.key.image.no.load.homepage";
        public static final String Key_Reply_With_Html = "sourcewall.key.reply.with.html";
        public static final String Key_Use_Tail_Type = "sourcewall.key.use.tail.type";
        public static final String Key_Custom_Tail = "sourcewall.key.custom.tail";
        public static final String Key_Swipe_Any_Where = "sourcewall.key.swipe_any_where";
        public static final String Key_User_Has_Learned_Load_My_Groups = "sourcewall.key.user.has.learned.load.my.groups";
        public static final String Key_User_Has_Learned_Load_My_Tags = "sourcewall.key.user.has.learned.load.my.tags";
        public static final String Key_User_Has_Learned_Add_Image = "sourcewall.key.user.has.learned.add.image";

        // 保存SharedPreferences的Key，用于保存草稿
        public static final String Key_Sketch_Article_Reply = "sourcewall.key.sketch.article.reply";
        public static final String Key_Sketch_Post_Reply = "sourcewall.key.sketch.post.reply";
        public static final String Key_Sketch_Question_Answer = "sourcewall.key.sketch.question.answer";
        public static final String Key_Sketch_Publish_Post_Title = "sourcewall.key.sketch.publish.post.title";
        public static final String Key_Sketch_Publish_Post_Content = "sourcewall.key.sketch.publish.post.content";
    }

    public static final class RequestCode {
        // 保存startActivityForResult的RequestCode
        public static final int Code_Login = 1025;
        public static final int Code_Publish_Post = 1026;
        public static final int Code_Publish_Question = 1027;
        public static final int Code_Start_Shuffle_Groups = 1028;
        public static final int Code_Start_Shuffle_Ask_Tags = 1029;
        public static final int Code_Invoke_Image_Selector = 1030;
        public static final int Code_Invoke_Camera = 1031;
        public static final int Code_Message_Center = 1032;
        public static final int Code_Reply_Post = 1033;
        public static final int Code_Reply_Article = 1034;
        public static final int Code_Reply_Question = 1035;
        public static final int Code_Answer_Question = 1036;
    }

    public static final class Web {

        // 登录页保存Cookie
        public static final String LOGIN_URL = "https://account.guokr.com/sign_in/?display=mobile";
        public static final String SUCCESS_URL_1 = "http://m.guokr.com/";
        public static final String SUCCESS_URL_2 = "http://www.guokr.com/";
        public static final String Cookie_Token_Key = "_32353_access_token";
        public static final String Cookie_Ukey_Key = "_32353_ukey";
        public static final String Cookie_Token_Key_2 = "_32382_access_token";
        public static final String Cookie_Ukey_Key_2 = "_32382_ukey";

        //webview
        public static final String Base_Url = "http://www.guokr.com/";
        public static final String Base_Url2 = "file:///android_asset/";
    }

    public static final class ImageLoadMode {
        //图片加载模式
        public static final int MODE_ALWAYS_LOAD = 0;
        public static final int MODE_NEVER_LOAD = 1;
        public static final int MODE_LOAD_WHEN_WIFI = 2;
    }

    public static final class ZipMode {
        //图片压缩模式
        public static final int Low = 0;
        public static final int Medium = 1;
        public static final int High = 2;
        public static final int Original = 3;
    }

    public static final class TailType {
        //小尾巴类型
        public static final int Type_Use_Default_Tail = 0;
        public static final int Type_Use_Phone_Tail = 1;
        public static final int Type_Use_Custom_Tail = 2;
    }


}
