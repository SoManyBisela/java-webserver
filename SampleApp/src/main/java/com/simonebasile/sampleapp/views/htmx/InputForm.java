package com.simonebasile.sampleapp.views.htmx;

import com.simonebasile.sampleapp.views.html.HtmlElement;

import java.util.Arrays;
import java.util.function.Consumer;

public class InputForm extends HtmlElement {

    public record LabelAndInput(HtmlElement label, HtmlElement input) {
    }
    private final HtmlElement inputTable;

    public InputForm() {
        super("form");
        inputTable = new HtmlElement("table");
        content(inputTable);
    }


    public InputForm button(Consumer<HtmlElement> editor) {
        HtmlElement submitButton = button();
        content(submitButton);
        editor.accept(submitButton);
        return this;
    }

    public InputForm input(String name, String type) {
        return input(name, type, null);
    }

    public record SelectOption(String value, String text) {
        public SelectOption(String value) {
            this(value, value);
        }
    }


    public InputForm select(String name, SelectOption... options) {
        return select(name, null, options);
    }
    public InputForm select(String name, Consumer<LabelAndInput> editor, SelectOption... options) {
        var label =  label().attr("for", name).text(name);
        var in =  select().attr("name", name)
                .content(Arrays.stream(options)
                        .map(a -> option().text(a.text).attr("value", a.value))
                        .toList());
        if(editor != null) {
            editor.accept(new LabelAndInput(label, in));
        }
        inputTable.content(HtmlElement.tr().content(
                td().content(label),
                td().content(in)
        ));
        return this;
    }

    public InputForm input(String name, String type, Consumer<LabelAndInput> editor) {
        var label =  label().attr("for", name).text(name);
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

    public InputForm attr(String name, String value, String ...more) {
        return (InputForm) super.attr(name, value, more);
    }

    @Override
    public InputForm hxGet(String url) {
        return (InputForm)super.hxGet(url);
    }

    @Override
    public InputForm hxPost(String url) {
        return (InputForm)super.hxPost(url);
    }

    @Override
    public InputForm hxPut(String url) {
        return (InputForm)super.hxPut(url);
    }

    @Override
    public InputForm hxDelete(String url) {
        return (InputForm)super.hxDelete(url);
    }

    @Override
    public InputForm hxTrigger(String value) {
        return (InputForm)super.hxTrigger(value);
    }

    @Override
    public InputForm hxTarget(String value) {
        return (InputForm)super.hxTarget(value);
    }

    @Override
    public InputForm hxSwap(String value) {
        return (InputForm)super.hxSwap(value);
    }


    @Override
    public InputForm hxVals(String... attrs) {
        return (InputForm)super.hxVals(attrs);
    }

}
