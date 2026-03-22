"use client";

import React from "react";

interface Props {
  componentUid: string;
  children: React.ReactNode;
}

interface State {
  hasError: boolean;
}

export default class CmsErrorBoundary extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error) {
    console.error(`[CMS] Component "${this.props.componentUid}" failed to render:`, error);
  }

  render() {
    if (this.state.hasError) {
      if (process.env.NODE_ENV === "development") {
        return (
          <div style={{ border: "2px dashed red", padding: 12, margin: 8, fontSize: 13 }}>
            Component <strong>{this.props.componentUid}</strong> failed to render.
          </div>
        );
      }
      return null;
    }
    return this.props.children;
  }
}
