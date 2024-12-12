package com.simonebasile.sampleapp.views.custom;

import com.simonebasile.sampleapp.model.Attachment;
import com.simonebasile.web.ssr.component.HtmlElement;
import com.simonebasile.web.ssr.component.IHtmlElement;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static com.simonebasile.web.ssr.component.HtmlElement.*;

/**
 * Represents a list of attachments in an HTML page.
 */
public class AttachmentList implements IHtmlElement {
    private final IHtmlElement content;

    public AttachmentList(List<Attachment> attachments, String ticketId) {
        HtmlElement container = table().attr("class", "attachments-table", "id", "attachmentlist");
        if(attachments != null && !attachments.isEmpty()) {
            container.content(
                            colgroup().content(
                                    col(),
                                    col().attr("style", "width: 40px")
                            )
                    );
            for (int i = 0; i < attachments.size(); i++) {
                Attachment attachment = attachments.get(i);
                container.content(tr().content(
                        td().text(attachment.getName()),
                        td().attr("class", "buttons-cell").content(a().attr(
                                        "href", "/attachment?ticketId=" + ticketId + "&ati=" + i,
                                        "target", "_blank"
                                ).content(button().attr("class", "button-icon")
                                        .content(new MaterialIcon("download")))
                        )
                ));
            }
        }
        content = container;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        content.write(out);
    }
}
