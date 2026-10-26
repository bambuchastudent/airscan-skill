/*
 * Paste into browser DevTools Console while viewing the public Aena VLC
 * destinations page. This is a manual, user-triggered export of the rendered
 * main DOM. No HTTP requests, timers, browser automation, or source rehosting.
 * The downloaded file stays on your machine; never commit it to GitHub.
 */
(() => {
  const url = new URL(window.location.href);
  if (url.hostname !== "www.aena.es" ||
      !url.pathname.includes("valencia") ||
      !(url.pathname.includes("destinations") || url.pathname.includes("destinos"))) {
    throw new Error("Open the official Aena Valencia destinations page first.");
  }
  const main = document.querySelector("main");
  if (!main) {
    throw new Error("The rendered page has no <main> element; save the HTML manually.");
  }
  const clone = main.cloneNode(true);
  clone.querySelectorAll("script,style,noscript").forEach(el => el.remove());
  const html = "<!doctype html><html lang='" + document.documentElement.lang +
      "'><head><meta charset='utf-8'><title>Aena VLC manual DOM capture</title></head><body>" +
      clone.outerHTML + "</body></html>";
  const link = document.createElement("a");
  const blobUrl = URL.createObjectURL(new Blob([html], {type: "text/html;charset=utf-8"}));
  try {
    link.href = blobUrl;
    link.download = "aena-vlc-rendered-dom.html";
    link.click();
  } finally {
    setTimeout(() => URL.revokeObjectURL(blobUrl), 2000);
  }
})();
