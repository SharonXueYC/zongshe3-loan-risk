/**
 * API 地址：本地开发默认 localhost:8080；云部署时自动使用当前域名（同源 /api）
 */
(function (global) {
  const KEY = 'apiBase';

  function getApiBase() {
    const stored = global.localStorage && global.localStorage.getItem(KEY);
    if (stored && stored.trim()) {
      return stored.trim().replace(/\/$/, '');
    }
    if (global.location && /^https?:$/i.test(global.location.protocol)) {
      return global.location.origin;
    }
    return 'http://localhost:8080';
  }

  function setApiBase(url) {
    if (global.localStorage && url) {
      global.localStorage.setItem(KEY, url.replace(/\/$/, ''));
    }
  }

  global.ZongsheApi = { getApiBase, setApiBase, KEY };
})(typeof window !== 'undefined' ? window : globalThis);
