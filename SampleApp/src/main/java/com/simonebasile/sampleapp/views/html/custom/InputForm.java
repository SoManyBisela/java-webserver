package com.simonebasile.sampleapp.views.html.custom;

import com.simonebasile.sampleapp.views.html.HtmlElement;

import java.util.function.Consumer;

public class InputForm extends HtmlElement {

    public record LabelAndInput(HtmlElement label, HtmlElement input) {
    }
    private final HtmlElement inputTable;
    private final HtmlElement submitButton;
    public InputForm() {
        super("form");
        inputTable = new HtmlElement("table");
        submitButton = new HtmlElement("button").attr("type", "submit");
        content(inputTable, submitButton);
    }

    public InputForm editSubmit(Consumer<HtmlElement> editSubmit) {
        editSubmit.accept(submitButton);
        return this;
    }

    public InputForm input(String name, String type) {
        return input(name, type, null);
    }

    public InputForm input(String name, String type, Consumer<LabelAndInput> editor) {
        var label =  label().attr("type", type, "for", name).text(name);
        var in =  input().attr("type", type, "name", name);
        if(editor != null) {
            editor.accept(new LabelAndInput(label, in));
        }
        inputTable.content(HtmlElement.tr().content(
                td().content(label),
                td().content(in)
        ));
        return this;
    }

    public InputForm action(String action, String method) {
        attr("action", action, "method", method);
        return this;
    }

    public InputForm buttonText(String text) {
        submitButton.text(text);
        return this;
    }
}
