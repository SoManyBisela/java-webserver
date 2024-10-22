addonload(() => {
    document.querySelectorAll("form").forEach(form => {
        let params = elementAttributes(form, "form-param-");
        if(Object.keys(params).length > 0) {
            addFormData(form, params);
        }
    })
});