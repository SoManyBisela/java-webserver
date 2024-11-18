(() => {
    let _api;
    htmx.defineExtension('simple-loaded-event', {
        init: function(api) {
            _api = api;
        },
        onEvent: function(name, evt) {
            if (name === 'htmx:beforeProcessNode') {
                let el =  evt.target;
                let code = el.getAttribute("hx-sle-onload");
                new Function("event", code).call(el, evt);
            }
        },
    })
})();