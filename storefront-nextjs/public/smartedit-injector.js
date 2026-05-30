(function () {
  "use strict";

  if (window === window.parent) {
    // not running inside an iframe; nothing to do.
    return;
  }

  var url = new URL(window.location.href);
  if (!url.searchParams.has("preview")) {
    return;
  }

  var metaTag = document.querySelector('meta[name="smartedit-allowed-origins"]');
  var rawAllowed = metaTag ? metaTag.getAttribute("content") : "";
  var allowedOrigins = (rawAllowed || "")
    .split(",")
    .map(function (s) {
      return s.trim();
    })
    .filter(Boolean);
  if (allowedOrigins.length === 0) {
    return;
  }

  function isAllowedOrigin(origin) {
    return allowedOrigins.indexOf(origin) !== -1;
  }

  function postToParent(message) {
    for (var i = 0; i < allowedOrigins.length; i++) {
      try {
        window.parent.postMessage(message, allowedOrigins[i]);
      } catch {
        // swallow: cross-origin restrictions
      }
    }
  }

  function rectFor(el) {
    var r = el.getBoundingClientRect();
    return { x: r.left, y: r.top, width: r.width, height: r.height };
  }

  function findEditable(target) {
    var node = target;
    while (node && node !== document.body) {
      if (node.nodeType === 1) {
        var componentId = node.getAttribute && node.getAttribute("data-cms-component-id");
        if (componentId) {
          return {
            kind: "component",
            id: componentId,
            componentType: node.getAttribute("data-cms-component-type") || null,
            element: node,
          };
        }
        var slotId = node.getAttribute && node.getAttribute("data-cms-slot-id");
        if (slotId) {
          return {
            kind: "slot",
            id: slotId,
            slotName: node.getAttribute("data-slot-name") || null,
            position: node.getAttribute("data-cms-slot-position") || null,
            shared: node.getAttribute("data-cms-slot-shared") === "true",
            element: node,
          };
        }
      }
      node = node.parentNode;
    }
    return null;
  }

  function onClick(ev) {
    var hit = findEditable(ev.target);
    if (!hit) return;
    ev.preventDefault();
    ev.stopPropagation();
    var payload = Object.assign({}, hit, { rect: rectFor(hit.element) });
    delete payload.element;
    postToParent({ type: "smartedit:select", payload: payload });
  }

  function onMessage(ev) {
    if (!isAllowedOrigin(ev.origin)) {
      return;
    }
    var data = ev.data;
    if (!data || typeof data !== "object" || typeof data.type !== "string") {
      return;
    }
    if (data.type === "smartedit:reload") {
      var reloadUrl = new URL(window.location.href);
      reloadUrl.searchParams.set("_se", Date.now().toString());
      window.location.replace(reloadUrl.toString());
    }
  }

  document.addEventListener("click", onClick, true);
  window.addEventListener("message", onMessage, false);
  document.body.setAttribute("data-smartedit-mode", "preview");

  postToParent({
    type: "smartedit:ready",
    payload: {
      href: window.location.href,
      lang: document.documentElement.getAttribute("lang") || null,
    },
  });
})();
