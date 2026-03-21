"use client";

import { useEffect } from "react";

export default function BodyClassSetter({ pageUid }: { pageUid: string }) {
  useEffect(() => {
    const cls = `page-${pageUid}`;
    document.body.classList.add(cls);
    return () => {
      document.body.classList.remove(cls);
    };
  }, [pageUid]);

  return null;
}
