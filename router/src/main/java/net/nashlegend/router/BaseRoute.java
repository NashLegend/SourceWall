package net.nashlegend.router;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.net.URLDecoder;
import java.util.Set;

/**
 * Created by NashLegend on 16/4/20.
 */
public abstract class BaseRoute {

    private String format;
    private ExtraTypes extraTypes;
    private Path formatPath;

    public BaseRoute(String format, ExtraTypes extraTypes) {
        if (format == null) {
            throw new NullPointerException("format can not be null");
        }
        this.format = format;
        this.extraTypes = extraTypes;
        if (format.toLowerCase().startsWith("http://") || format.toLowerCase().startsWith("https://")) {
            this.formatPath = Path.create(Uri.parse(format));
        } else {
            this.formatPath = Path.create(Uri.parse("helper://".concat(format)));
        }
    }

    public String getFormat() {
        return format;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof BaseRoute) {
            BaseRoute that = (BaseRoute) o;
            return that.format.equals(((BaseRoute) o).format);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return format.hashCode();
    }

    public boolean match(Path fullLink, Uri uri) {
        boolean flag;
        if (formatPath.isHttp()) {
            flag = Path.match(formatPath, fullLink);
        } else {
            flag = Path.match(formatPath.next(), fullLink.next());
        }
        return flag && (extraTypes == null || extraTypes.matchRequired(uri));
    }

    @NonNull
    public Bundle parseExtras(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putString(Router.RAW_URI, uri.toString());
        // path segments // ignore scheme
        Path p = formatPath.next();
        Path y = Path.create(uri).next();
        while (p != null) {
            if (p.isArgument()) {
                put(bundle, p.argument(), y.value());
            }
            p = p.next();
            y = y.next();
        }
        // parameter
        Set<String> names = uri.getQueryParameterNames();
        for (String name : names) {
            String value = uri.getQueryParameter(name);
            put(bundle, name, value);
        }
        return bundle;
    }

    private void put(Bundle bundle, String name, String value) {
        try {
            int type = extraTypes.getType(name);
            name = extraTypes.transfer(name);
            if (type == ExtraTypes.STRING) {
                type = extraTypes.getType(name);
            }
            switch (type) {
                case ExtraTypes.INT:
                    bundle.putInt(name, Integer.parseInt(value));
                    break;
                case ExtraTypes.LONG:
                    bundle.putLong(name, Long.parseLong(value));
                    break;
                case ExtraTypes.BOOL:
                    bundle.putBoolean(name, Boolean.parseBoolean(value));
                    break;
                case ExtraTypes.SHORT:
                    bundle.putShort(name, Short.parseShort(value));
                    break;
                case ExtraTypes.FLOAT:
                    bundle.putFloat(name, Float.parseFloat(value));
                    break;
                case ExtraTypes.DOUBLE:
                    bundle.putDouble(name, Double.parseDouble(value));
                    break;
                default:
                    //默认是urlEncoded
                    bundle.putString(name, URLDecoder.decode(value, "UTF-8"));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
