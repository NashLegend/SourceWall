package net.nashlegend.router;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by NashLegend on 16/4/20.
 * 使用ActionRoute注解的都要实现此接口
 */
public interface Action {
    void execute(@NonNull Bundle bundle);
}
