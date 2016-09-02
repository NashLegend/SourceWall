package net.nashlegend.sourcewall.util;

import org.junit.Test;
import org.markdown4j.Markdown4jProcessor;

import java.io.IOException;

/**
 * Created by NashLegend on 16/9/1.
 */
public class ImageUtilsTest {
    @Test
    public void compressImage() throws Exception {
        try {
            String str = new Markdown4jProcessor().process("#test\n" +
                    "\n" +
                    "- 1\n" +
                    "- 2\n" +
                    "\n" +
                    "> sss\n\n" +
                    "ds\n" +
                    " [](http://jdjdjdjd) \n" +
                    "  ![](http://2.im.guokr.com/Ulll4wpKlonIkr6JIzHRoJVzSYwmmzbPR_gow_569TcACQAAwAYAAEpQ.jpg) ");
            System.out.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}