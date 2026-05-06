/* SmartEdit storefront-side bridge.
 *
 * Loaded by the Next.js storefront layout. Activates only when the URL
 * carries a `?preview=<ticket>` query parameter (the admin SmartEdit shell
 * adds this when embedding the storefront in its iframe).
 *
 * Contract:
 * - Posts {type:"smartedit:ready",payload:{pageUid,lang,href}} on load.
 * - Listens for clicks bubbling up from elements that carry
 *   `data-cms-component-id` or `data-cms-slot-id` and posts
 *   {type:"smartedit:select",payload:{kind,id,...rect}}.
 * - Listens for parent → iframe messages of type "smartedit:reload"
 *   and reloads the current page so the iframe re-fetches DRAFT content.
 *
 * Allowed parent origins are read from the script tag's
 * `data-allowed-origins` attribute (comma-separated). Messages from / to
 * other origins are ignored.
 */
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

  var script = document.currentScript || (function () {
    var scripts = document.getElementsByTagName("script");
    for (var i = 0; i < scripts.length; i++) {
      if (scripts[i].src && scripts[i].src.indexOf("smartedit-injector.js") !== -1) {
        return scripts[i];
      }
    }
    return null;
  })();
  var rawAllowed = script && script.getAttribute("data-allowed-origins");
  var allowedOrigins = (rawAllowed || "")
    .split(",")
    .map(function (s) { return s.trim(); })
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
      } catch (e) {
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
      window.location.reload();
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
