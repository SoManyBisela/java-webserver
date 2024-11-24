package com.simonebasile.sampleapp.views.html.custom;

import com.simonebasile.sampleapp.model.Comment;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;
import com.simonebasile.sampleapp.views.html.NoElement;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.div;

/**
 * Represents the commment section of a ticket in an HTML page.
 */
public class CommentSection implements IHtmlElement{
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    private final IHtmlElement content;

    public CommentSection(List<Comment> comments, String username) {
        if (comments == null || comments.isEmpty()) {
            content = NoElement.instance;
            return;
        }
        HtmlElement commentSection = div().attr("class", "comments stack-vertical");
        for (int i = comments.size() - 1; i >= 0; i--) {
            Comment comment = comments.get(i);
            commentSection.content(div().attr("class", "comment").content(
                    div().attr("class", "stack-horizontal").content(
                            div().attr("class", "comment-author")
                                    .text(comment.getAuthor().equals(username) ? "You" : comment.getAuthor()),
                            div().attr("class", "comment-time")
                                    .text(formatTime(comment.getCreationDate()))
                    ),
                    div().attr("class", "comment-content")
                            .text(comment.getContent())
            ));
        }
        content = commentSection;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        content.write(out);
    }

    private String formatTime(LocalDateTime creationDate) {
        if(creationDate == null) return "";
        long seconds = ChronoUnit.SECONDS.between(creationDate, LocalDateTime.now());
        if(seconds < 60) return "less than a minute ago";
        if(seconds < 3600) return (seconds / 60) + " minutes ago";
        if(seconds < 86400) return (seconds / 3600) + " hours and " + (seconds % 3600 / 60) + " minutes ago";
        else return dateTimeFormatter.format(creationDate);
    }
}
