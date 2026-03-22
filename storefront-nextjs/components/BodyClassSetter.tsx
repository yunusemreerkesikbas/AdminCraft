"use client";

import { useEffect } from "react";

export default function BodyClassSetter({ pageUid }: { pageUid: string }) {
  useEffect(() => {
    const safeUid = pageUid.replace(/[^a-zA-Z0-9_-]/g, "");
    if (!safeUid) return;
    const cls = `page-${safeUid}`;
    document.body.classList.add(cls);
    return () => {
      document.body.classList.remove(cls);
    };
  }, [pageUid]);

  return null;
}
