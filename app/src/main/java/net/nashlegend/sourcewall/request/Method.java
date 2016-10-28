package net.nashlegend.sourcewall.request;

/**
 * Created by NashLegend on 2016/10/28.
 */

public enum Method {
    GET("GET"),//must no body
    POST("POST"),//must body
    DELETE("DELETE"),//may body
    PUT("PUT"),//must body
    HEAD("HEAD"),//must no body
    PATCH("PATCH"),;//must body

    private String mtd;

    Method(String method) {
        this.mtd = method;
    }

    public String value() {
        return mtd;
    }
}
