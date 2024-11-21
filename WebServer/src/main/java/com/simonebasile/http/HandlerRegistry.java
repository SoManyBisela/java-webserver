package com.simonebasile.http;


import java.util.*;

/**
 * A registry for handlers that can be matched against a path.
 *
 * @param <T> the type of the handlers
 */
class HandlerRegistry<T> {

    private final PrefixTreeNode<T> tree = new PrefixTreeNode<>();

    /**
     * The node of the data structure that holds registered handlers.
     */
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

    /**
     * Gets or creates a node for the given path.
     * @param path the path to get or create the node for
     * @return the node for the given path
     */
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

    /**
     * Inserts a context handler for the given path.
     * @param path the path to insert the handler for
     * @param handler the handler to insert
     * @return true if the handler was inserted, false if a handler was already present
     */
    public boolean insertCtx(String path, T handler) {
        var node = getOrCreateNode(path);
        if(node.contextHandler != null) {
            return false;
        }
        node.contextHandler = handler;
        return true;
    }

    /**
     * Inserts an exact handler for the given path.
     * @param path the path to insert the handler for
     * @param handler the handler to insert
     * @return true if the handler was inserted, false if a handler was already present
     */
    public boolean insertExact(String path, T handler) {
        var node = getOrCreateNode(path);
        if(node.exactHandler != null) {
            return false;
        }
        node.exactHandler = handler;
        return true;
    }

    /**
     * A match between a path and a handler.<br>
     * returning the match is necessary to give information to the handler about the path that was matched
     * and the remaining path that may be useful to context handlers
     */
    public record Match<T>(T handler, ResourceMatch match) {
        private Match(T handler, String matched, StringBuilder all) {
            this(handler, new ResourceMatch(matched, all.substring(matched.length())));
        }
    }

    /**
     * Gets the handler for the given path.<br>
     * Ignores query parameters.<br>
     * Finds and returns the most specific valid handler for the path<br>
     * Exact handlers are only valid if the path is an exact match<br>
     * Context handlers are valid for all subpaths of the path they are registered for<br>
     * If matching the same path, the exact handler is returned.
     *
     * @param path the path to get the handler for
     * @return a match to the handler for or null if no handler was found
     */
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
