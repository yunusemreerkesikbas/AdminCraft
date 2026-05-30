import ExecutionEnvironment from '@docusaurus/ExecutionEnvironment';

declare global {
  interface Window {
    gtag?: (...args: unknown[]) => void;
  }
}

const CRAFTIVE_HOSTNAME = 'craftive.io';

function trackCraftiveOutboundClick(event: MouseEvent): void {
  const target = event.target;

  if (!(target instanceof Element)) {
    return;
  }

  const link = target.closest<HTMLAnchorElement>('a[href]');

  if (!link) {
    return;
  }

  let url: URL;

  try {
    url = new URL(link.href);
  } catch {
    return;
  }

  if (url.hostname !== CRAFTIVE_HOSTNAME || !window.gtag) {
    return;
  }

  window.gtag('event', 'outbound_click', {
    event_category: 'docs_referral',
    event_label: link.textContent?.trim() || url.href,
    link_domain: url.hostname,
    link_url: url.href,
  });
}

if (ExecutionEnvironment.canUseDOM) {
  document.addEventListener('click', trackCraftiveOutboundClick);
}

export {};
