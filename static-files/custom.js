htmx.defineExtension('raw-body', {
    onEvent: function(name, evt) {
        if (name === 'htmx:configRequest') {
            evt.detail.headers['Content-Type'] = 'application/octet-stream'
            let fileParam = evt.detail.elt.getAttribute("hx-raw-file-param");
            let filenameParam = evt.detail.elt.getAttribute("hx-raw-filename-param");
            let file = evt.detail.parameters[fileParam];
            evt.detail.parameters[filenameParam] = file.name;
            delete evt.detail.parameters[fileParam];
            //replace path
            let finalPath = evt.detail.path;
            let values = evt.detail.parameters;
            const hasValues = !values.keys().next().done
            if (hasValues) {
                if (finalPath.indexOf('?') < 0) {
                    finalPath += '?'
                } else {
                    finalPath += '&'
                }
                let additionalParams = '';
                values.forEach(function(realValue, name) {
                    if (additionalParams !== '') {
                        additionalParams += '&'
                    }
                    if (String(realValue) === '[object Object]') {
                        realValue = JSON.stringify(realValue)
                    }
                    additionalParams += encodeURIComponent(name) + '=' + encodeURIComponent(realValue)
                })
                finalPath += additionalParams;
            }
            evt.detail.path = finalPath;

            //replace parameters
            evt.detail.parameters = {file};
            evt.detail.useUrlParams = false;
        } else if (name === 'htmx:validateUrl') {
        }
    },

    encodeParameters: function(xhr, parameters, elt) {
        return parameters.get("file");
    }
})