const addonload = (() => {
    let callbacks = [];
    window.onload = () => {
        callbacks.forEach(a => a());
    }
    return (f) => {
        if(f) callbacks.push(f);
    }
})();

const elementAttributes = (el, prefix = "") => {
    let params = {};
    for(let name of el.getAttributeNames()) {
        if(name.startsWith(prefix)) {
            params[name.substring(prefix.length)] = el.getAttribute(name);
        }
    }
    return params;
};

const addFormData = (el, params) => {
    el.addEventListener("formdata", (e) => {
        let formdata = e.formData;
        for (let name in params) {
            formdata.append(name, params[name]);
        }
    });
};

