package com.cage.eaglemq;

import java.util.*;

/**
 * ClassName: Test1
 * PackageName: com.cage.eaglemq
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/15 上午9:18
 * @Version: 1.0
 */
public class Test1 {
    public static void main(String[] args) {
        Map<String, List<String>> map = new HashMap<>();
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, "a", "b", "c");
        map.put("a", list);

        map.forEach((k, v) -> {
            System.out.println(k + ": " + v);
        });
        List<String> list1 = map.get("a");
        list1.add("d");

        map.forEach((k, v) -> {
            System.out.println(k + ": " + v);
        });
    }
}
