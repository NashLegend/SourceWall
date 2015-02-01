package net.nashlegend.sourcewall.util;

/**
 * Created by NashLegend on 2014/12/9 0009
 * 果壳的样式变来变去，要不要加一个检查样式变化的类，发现变化后重新请求
 * 这是一个 TODO 的需求，有可能不需要，最后再说……
 */
public class StyleChecker {

    public static String getArticleHtml(String content) {
        boolean isNight = SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false);
        String style;
        if (isNight) {
            style = "  <link rel=\"stylesheet\" href=\"file:///android_asset/night.static.guokr.com/apps/minisite/styles/f79e35f9.main.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/night.static.guokr.com/apps/minisite/styles/e8ff5a9c.gbbcode.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/night.static.guokr.com/apps/minisite/styles/e263077d.article.css\" /> \n";
        } else {
            style = "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/minisite/styles/f79e35f9.main.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/minisite/styles/e8ff5a9c.gbbcode.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/minisite/styles/e263077d.article.css\" /> \n";
        }
        String prefix = "<html class=\"no-js screen-scroll\">\n" +
                " <head> \n" +
                "  <meta charset=\"UTF-8\" /> \n" +
                "  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1,user-scalable=no\" /> \n" +
                "  <meta name=\"format-detection\" content=\"telephone=no\" /> \n" +
                style +
                " </head> \n" +
                " <body> \n" +
                "  <div class=\"container article-page\"> \n" +
                "   <div class=\"main\"> \n" +
                "    <div class=\"content\"> ";
        String suffix = "</div> \n" +
                "   </div> \n" +
                "  </div> \n" +
                " </body>\n" +
                "</html>";
        return prefix + content + suffix;
    }

    public static String getPostHtml(String content) {
        boolean isNight = SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false);
        String style;
        if (isNight) {
            style = "  <link rel=\"stylesheet\" href=\"file:///android_asset/night.static.guokr.com/apps/msite/styles/755794f4.m.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/night.static.guokr.com/apps/group/styles/e8ff5a9c.gbbcode.css\" type=\"text/css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/night.static.guokr.com/apps/msite/styles/81e10205.group.css\" type=\"text/css\" /> \n";
        } else {
            style = "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/msite/styles/755794f4.m.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/group/styles/e8ff5a9c.gbbcode.css\" type=\"text/css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/msite/styles/81e10205.group.css\" type=\"text/css\" /> \n";
        }
        String prefix = "<html>\n" +
                " <head> \n" +
                "  <meta charset=\"UTF-8\" /> \n" +
                "  <meta content=\"width=device-width,initial-scale=1.0,maximum-scale=1,minimum-scale=1,user-scalable=no\" name=\"viewport\" /> \n" +
                style +
                " </head> \n" +
                " <body> \n" +
                "  <div class=\"msite-container \"> \n" +
                "   <article id=\"contentMain\" class=\"content-main post\"> ";
        String suffix = "</article> \n" +
                "  </div> \n" +
                " </body>\n" +
                "</html>";
        return prefix + content + suffix;
    }

    public static String getQuestionHtml(String content) {
        boolean isNight = SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false);
        String style;
        if (isNight) {
            style = "  <link rel=\"stylesheet\" href=\"file:///android_asset/night.static.guokr.com/apps/ask/styles/3192ac2b.main.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/night.static.guokr.com/apps/ask/styles/e8ff5a9c.gbbcode.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/night.static.guokr.com/apps/ask/styles/b3b2bdee.contentPage.css\" /> \n";
        } else {
            style = "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/ask/styles/3192ac2b.main.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/ask/styles/e8ff5a9c.gbbcode.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/ask/styles/b3b2bdee.contentPage.css\" /> \n";
        }

        String prefix = "<html class=\"no-js screen-scroll\">\n" +
                " <head>\n" +
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /> \n" +
                "  <meta charset=\"UTF-8\" /> \n" +
                "  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1,user-scalable=no\" /> \n" +
                "  <meta name=\"format-detection\" content=\"telephone=no\" /> \n" +
                style +
                " </head> \n" +
                " <body> \n" +
                "  <div class=\"gwrap ask-content-page\"> \n" +
                "   <div class=\"gmain\"> \n" +
                "    <div class=\"post\"> \n" +
                "     <div class=\"post-detail gbbcode-content\" id=\"articleContent\"> \n" +
                "      <div id=\"questionDesc\">";
        String suffix = "</div> \n" +
                "     </div> \n" +
                "    </div> \n" +
                "   </div> \n" +
                "  </div>  \n" +
                " </body>\n" +
                "</html>";
        return prefix + content + suffix;
    }

    public static String getAnswerHtml(String content) {
        boolean isNight = SharedUtil.readBoolean(Consts.Key_Is_Night_Mode, false);
        String style;
        if (isNight) {
            style = "  <link rel=\"stylesheet\" href=\"file:///android_asset/night.static.guokr.com/apps/ask/styles/3192ac2b.main.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/night.static.guokr.com/apps/ask/styles/e8ff5a9c.gbbcode.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/night.static.guokr.com/apps/ask/styles/b3b2bdee.contentPage.css\" /> \n";
        } else {
            style = "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/ask/styles/3192ac2b.main.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/ask/styles/e8ff5a9c.gbbcode.css\" /> \n" +
                    "  <link rel=\"stylesheet\" href=\"file:///android_asset/static.guokr.com/apps/ask/styles/b3b2bdee.contentPage.css\" /> \n";
        }
        String prefix = "<html class=\"no-js screen-scroll\">\n" +
                " <head> \n" +
                "  <meta charset=\"UTF-8\" /> \n" +
                "  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1,user-scalable=no\" /> \n" +
                "  <meta name=\"format-detection\" content=\"telephone=no\" /> \n" +
                style +
                " </head> \n" +
                " <body> \n" +
                "  <div class=\"gwrap ask-content-page\"> \n" +
                "   <div class=\"gmain\"> \n" +
                "    <div class=\"answers\" id=\"answers\"> \n" +
                "     <div class=\"answer gclear  \"> \n" +
                "      <div class=\"answer-r\"> \n" +
                "       <div class=\"answer-txt answerTxt gbbcode-content\">";
        String suffix = "</div> \n" +
                "      </div> \n" +
                "     </div> \n" +
                "    </div> \n" +
                "   </div> \n" +
                "  </div>  \n" +
                " </body>\n" +
                "</html>";
        return prefix + content + suffix;
    }

}
