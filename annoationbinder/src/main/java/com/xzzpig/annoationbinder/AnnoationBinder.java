package com.xzzpig.annoationbinder;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xzzpig.annoationbinder.annoation.BindContentView;
import com.xzzpig.annoationbinder.annoation.BindFragmentView;
import com.xzzpig.annoationbinder.annoation.BindInitField;
import com.xzzpig.annoationbinder.annoation.BindInitView;
import com.xzzpig.annoationbinder.annoation.BindView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xzzpig on 17-6-13.
 */

public class AnnoationBinder {
    public static void bindViews(Object target, View rootView) {
        for (Field field : target.getClass().getFields()) {
            if (field.isAnnotationPresent(BindView.class)) {
                int id = field.getAnnotation(BindView.class).id();
                if (id != 0) {
                    try {
                        field.set(target, rootView.findViewById(id));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void initAcitvity(Activity activity) {
        View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        bindContentView(activity);
        bindViews(activity, rootView);
        bindInitField(activity);
        bindInitView(activity, rootView);
    }

    public static View initFragment(Fragment fragment, LayoutInflater inflater, ViewGroup container,
                                    Bundle savedInstanceState) {
        View view = bindFragmentView(fragment, inflater, container, savedInstanceState);
        if (view == null)
            return null;
        bindViews(fragment, view);
        bindInitField(fragment);
        bindInitView(fragment, view);
        return view;
    }

    public static View bindFragmentView(Fragment target, LayoutInflater inflater, ViewGroup container,
                                        Bundle savedInstanceState) {
        if (target.getClass().isAnnotationPresent(BindFragmentView.class)) {
            int id = target.getClass().getAnnotation(BindFragmentView.class).layout();
            return inflater.inflate(id, container, false);
        }
        return null;
    }

    public static void bindContentView(Activity activity) {
        if (activity.getClass().isAnnotationPresent(BindContentView.class)) {
            int id = activity.getClass().getAnnotation(BindContentView.class).layout();
            activity.setContentView(id);
        }
    }

    public static void bindInitField(Object target) {
        Map<Method, BindInitField> map = new HashMap<>();
        for (Method method : target.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(BindInitField.class)) {
                BindInitField initField = method.getAnnotation(BindInitField.class);
                map.put(method, initField);
            }
        }
        List<Map.Entry<Method, BindInitField>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Method, BindInitField>>() {
            @Override
            public int compare(Map.Entry<Method, BindInitField> o1, Map.Entry<Method, BindInitField> o2) {
                return o1.getValue().level() - o2.getValue().level();
            }
        });
        for (Map.Entry<Method, BindInitField> entry : list) {
            BindInitField initField = entry.getValue();
            Method m = entry.getKey();
            Field field = null;
            try {
                field = target.getClass().getField(initField.target());
            } catch (NoSuchFieldException e) {
                continue;
            }
            try {
                m.invoke(target, field.get(target));
            } catch (Exception e) {
                try {
                    m.invoke(target);
                } catch (Exception e1) {
                    continue;
                }
                continue;
            }
        }
    }

    public static void bindInitView(Object target, View rootView) {
        Map<Method, BindInitView> map = new HashMap<>();
        for (Method method : target.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(BindInitView.class)) {
                BindInitView initField = method.getAnnotation(BindInitView.class);
                map.put(method, initField);
            }
        }
        List<Map.Entry<Method, BindInitView>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Method, BindInitView>>() {
            @Override
            public int compare(Map.Entry<Method, BindInitView> o1, Map.Entry<Method, BindInitView> o2) {
                return o1.getValue().level() - o2.getValue().level();
            }
        });
        for (Map.Entry<Method, BindInitView> entry : list) {
            BindInitView initField = entry.getValue();
            Method m = entry.getKey();
            try {
                m.invoke(target, rootView.findViewById(initField.target()));
            } catch (Exception e) {
                try {
                    m.invoke(target);
                } catch (Exception e1) {
                    continue;
                }
                continue;
            }
        }
    }
}
