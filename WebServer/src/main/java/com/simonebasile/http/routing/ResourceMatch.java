package com.simonebasile.http.routing;

/**
 * This class represents a match between a handler path and a request path.
 *
 * @param matchedPath the path that was matched
 * @param remainingPath the remaining path that was not matched
 */
public record ResourceMatch(String matchedPath, String remainingPath) {
}
