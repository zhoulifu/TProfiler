/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.config;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 包名过滤器,过滤注入或者不注入的Package
 *
 * @author luqi
 * @author zlf
 * @since 2010-6-23
 */
public class ProfFilter {
    public static FilterStrategy DEFAULT_STRATEGY = FilterStrategy.UNDEFINED;

    static Node root;

    static {
        root = new Node("", DEFAULT_STRATEGY, null);

        // Default settings
        addQualifiedName("java", FilterStrategy.EXCLUDE);
        addQualifiedName("sun", FilterStrategy.EXCLUDE);
        addQualifiedName("com.sun", FilterStrategy.EXCLUDE);
        addQualifiedName("org", FilterStrategy.EXCLUDE);
        // Self-exclude
        addQualifiedName("com.taobao.profile", FilterStrategy.EXCLUDE);
    }

    public static void addIncludeClass(String qualifiedName) {
        addQualifiedName(qualifiedName, FilterStrategy.INCLUDE);
    }

    public static void addExcludeClass(String qualifiedName) {
        addQualifiedName(qualifiedName, FilterStrategy.EXCLUDE);
    }

    public static void addExcludeClassLoader(String qualifiedName) {
        addQualifiedName(qualifiedName, FilterStrategy.EXCLUDE);
    }

    private static void addQualifiedName(String qualifiedName, FilterStrategy strategy) {
        Node curr = root;
        String[] segments = qualifiedName.split("\\.");
        synchronized (root) {
            for (String segment : segments) {
                Node child = curr.getChildByName(segment);
                if (child == null) {
                    child = curr.createChildByName(segment);
                }
                curr = child;
            }
        }

        curr.setStrategy(strategy);
    }

    public static boolean needsTransform(ClassLoader classLoader, String className) {
        boolean need = false;
        if (classLoader != null) {
            Node classLoadNode = getNodeByQualified(classLoader.getClass().getName());
            need = classLoadNode.strategy == FilterStrategy.INCLUDE;
        }

        Node classNode = getNodeByQualified(className);

        return need | (classNode.strategy == FilterStrategy.INCLUDE);
    }

    private static Node getNodeByQualified(String qualified) {
        String[] segments = qualified.split("\\.");
        Node curr = root, pre = root;
        int index = 0, len = segments.length;
        while (curr != null) {
            pre = curr;
            curr = curr.getChildByName(segments[index]);
            if (++index == len) {
                break;
            }
        }

        return curr == null ? pre : curr;
    }

    static void reset() {
        root.recursiveReset();
    }

    private static class Node {
        private String name;
        private FilterStrategy strategy;
        private Node parent;
        private List<Node> childrenList;

        Node(String name, Node parent) {
            this(name, parent.strategy, parent);
        }

        Node(String name, FilterStrategy strategy, Node parent) {
            this.name = name;
            this.strategy = strategy;
            this.parent = parent;
        }

        void setStrategy(FilterStrategy strategy) {
            this.strategy = strategy;
        }

        Node createChildByName(String name) {
            if (childrenList == null) {
                childrenList = new CopyOnWriteArrayList<Node>();
            }

            Node child = new Node(name, this);
            childrenList.add(child);
            return child;
        }

        Node getChildByName(String name) {
            if (childrenList == null) {
                return null;
            }

            for (Node node : childrenList) {
                if (node.name.equals(name)) {
                    return node;
                }
            }

            return null;
        }

        void recursiveReset() {
            filterStrategyReset();
            if (childrenList == null) {
                return;
            }

            for (Node node : childrenList) {
                node.recursiveReset();
            }
        }

        private void filterStrategyReset() {
            strategy = (parent == null) ? DEFAULT_STRATEGY : parent.strategy;
        }
    }
}
