const addonload = (() => {
    let callbacks = [];
    window.onload = () => {
        callbacks.forEach(a => a());
    }
    return (f) => {
        if(f) callbacks.push(f);
    }
})();
addonload(() => {
    document.querySelectorAll("[form-action]").forEach(e => {
        let action = e.getAttribute("form-action");
        let method = e.getAttribute("form-method");
        let params = {};
        for(let attr of e.getAttributeNames()) {
            if(attr.startsWith("form-param-")) {
                params[attr.substring("form-param-".length)] = e.getAttribute(attr);
            }
        }
        if(!action || !method) {
            console.log("Element ", e, " is missing action or method", {action, method});
            return;
        }
        console.log("Added event listener to", e);
        e.addEventListener("click", () => {
            let form = document.createElement("form");
            console.log("clicked form element ", e);
            form.action = action;
            form.method = method;
            form.hidden = true;
            form.addEventListener("formdata", (e) => {
                let formdata = e.formData;
                for (let name in params) {
                    formdata.append(name, params[name]);
                }
            })
            document.body.append(form);
            form.submit();
        });
    });
})


