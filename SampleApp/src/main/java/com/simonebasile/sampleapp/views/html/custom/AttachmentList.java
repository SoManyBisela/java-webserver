package com.simonebasile.sampleapp.views.html.custom;

import com.simonebasile.sampleapp.model.Attachment;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;
import com.simonebasile.sampleapp.views.html.NoElement;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class AttachmentList extends IHtmlElement {
    private final IHtmlElement content;

    public AttachmentList(List<Attachment> attachments, String id) {
        if(attachments == null || attachments.isEmpty()) {
            content = NoElement.instance;
            return;
        }
        HtmlElement container = table().attr("class", "attachments", "id", "attachmentlist");
        for (int i = 0; i < attachments.size(); i++) {
            Attachment attachment = attachments.get(i);
            container.content(tr().content(
                    td().text(attachment.getName()),
                    td().content(a().attr(
                            "href", "/attachment?ticketId=" + id + "&ati=" + i,
                            "target", "_blank"
                    ).text("Download"))
            ));
        }
        content = container;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        content.write(out);
    }
}
