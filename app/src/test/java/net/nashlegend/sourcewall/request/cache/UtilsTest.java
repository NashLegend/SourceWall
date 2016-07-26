package net.nashlegend.sourcewall.request.cache;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by NashLegend on 16/7/26.
 */
public class UtilsTest {
    @Test
    public void sort(){
        ArrayList<Integer> integers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            integers.add((int) (100*Math.random()));
        }
        Collections.sort(integers, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return lhs-rhs;
            }
        });
        for (int i = 0; i < integers.size(); i++) {
            System.out.println(integers.get(i));
        }
    }

}