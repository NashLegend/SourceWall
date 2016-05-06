package net.nashlegend.Tools;

import org.markdown4j.Markdown4jProcessor;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            String str = new Markdown4jProcessor().process("#uuu");
            System.out.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
