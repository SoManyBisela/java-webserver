addonload(() => {
    document.querySelectorAll("[form-action]").forEach(e => {
        let action = e.getAttribute("form-action");
        let method = e.getAttribute("form-method");
        let params = elementAttributes(e, "form-param-");
        if(!action || !method) {
            console.log("Element ", e, " is missing action or method", {action, method});
            return;
        }
        console.log("Added event listener to", e);
        e.addEventListener("click", (evt) => {
            evt.stopPropagation();
            let form = document.createElement("form");
            console.log("clicked form element ", evt);
            form.action = action;
            form.method = method;
            form.hidden = true;
            addFormData(form, params);
            document.body.append(form);
            form.submit();
        });
    });
})


