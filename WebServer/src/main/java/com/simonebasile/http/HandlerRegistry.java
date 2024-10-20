package com.simonebasile.http;

import java.util.*;

/**
 * Handler registry holds the registered handlers.
 */
class HandlerRegistry<T> {

    private final PrefixTreeNode<T> tree = new PrefixTreeNode<>();

    private static class PrefixTreeNode<T> {
        //context handlers handler
        T contextHandler;
        //exact handlers
        T exactHandler;
        HashMap<String, PrefixTreeNode<T>> children;

        PrefixTreeNode<T> computeIfAbsent(String path) {
            return children().computeIfAbsent(path, k -> new PrefixTreeNode<>());
        }

        PrefixTreeNode<T> get(String path) {
            if(children == null) return null;
            return children.get(path);
        }

        HashMap<String, PrefixTreeNode<T>> children() {
            if(children == null) {
                children = new HashMap<>();
            }
            return children;
        }

    }

    private PrefixTreeNode<T> getOrCreateNode(String path) {
        Objects.requireNonNull(path);
        final String[] parts = path.split("/");
        PrefixTreeNode<T> target = tree;
        for(String part : parts) {
            if(part.isEmpty()) continue;
            target = target.computeIfAbsent(part);
        }
        return target;
    }

    public boolean insertCtx(String path, T handler) {
        var node = getOrCreateNode(path);
        if(node.contextHandler != null) {
            return false;
        }
        node.contextHandler = handler;
        return true;
    }

    public boolean insertExact(String path, T handler) {
        var node = getOrCreateNode(path);
        if(node.exactHandler != null) {
            return false;
        }
        node.exactHandler = handler;
        return true;
    }

    public T getHandler(String path) {
        Objects.requireNonNull(path);
        final String[] parts = path.split("/");
        PrefixTreeNode<T> target = tree;
        T lastCtxHandler = null;
        for(String part : parts) {
            if(part.isEmpty()) continue;
            if(target.contextHandler != null) {
                lastCtxHandler = target.contextHandler;
            }
            var child = target.get(part);
            if(child == null) {
                return lastCtxHandler;
            }
            target = child;
        }
        if(target.exactHandler != null) {
            return target.exactHandler;
        } else if(target.contextHandler != null){
            return target.contextHandler;
        } else {
            return lastCtxHandler;
        }
    }

}
