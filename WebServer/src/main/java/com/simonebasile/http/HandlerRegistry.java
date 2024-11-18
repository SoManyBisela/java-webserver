package com.simonebasile.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Handler registry holds the registered handlers.
 */
class HandlerRegistry<T> {

    private static final Logger log = LoggerFactory.getLogger(HandlerRegistry.class);
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

    public record Match<T>(T handler, ResourceMatch match) {
        private Match(T handler, String matched, StringBuilder all) {
            this(handler, new ResourceMatch(matched, all.substring(matched.length())));
        }
    }

    public Match<T> getHandler(String path) {
        int qpStart = path.indexOf("?");
        if(qpStart != -1) {
            path = path.substring(0, qpStart);
        }
        Objects.requireNonNull(path);
        final String[] parts = path.split("/");
        StringBuilder matchedPath = new StringBuilder();
        PrefixTreeNode<T> target = tree;
        String lastMatchPath = null;
        T lastCtxHandler = null;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            if (target.contextHandler != null) {
                lastMatchPath = matchedPath.toString();
                lastCtxHandler = target.contextHandler;
            }
            var child = target.get(part);
            if (child == null) {
                if(lastCtxHandler != null) {
                    //Add remaining to result
                    for(; i < parts.length; i++) {
                        part = parts[i];
                        if (part.isEmpty()) continue;
                        matchedPath.append("/").append(part);
                    }
                    return new Match<>(lastCtxHandler, lastMatchPath, matchedPath);
                } else {
                    return null;
                }
            }
            matchedPath.append("/").append(part);
            target = child;
        }
        if(target.exactHandler != null) {
            return new Match<>(target.exactHandler, new ResourceMatch(matchedPath.toString(), ""));
        } else if(target.contextHandler != null){
            return new Match<>(target.contextHandler, new ResourceMatch(matchedPath.toString(), ""));
        } else if(lastCtxHandler != null){
            return new Match<>(lastCtxHandler, lastMatchPath, matchedPath);
        } else {
            return null;
        }
    }

}
