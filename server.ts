import express from 'express';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  DemoCatalogSource,
  LicensedCatalogConfig,
} from './src/domain/licensedCatalogRepository.ts';
import { CatalogAccess } from './src/domain/licensedCatalogRights.ts';
import {
  YouTubeSourceBadge,
  type YouTubeVideoCard,
  isVideoPolicyCompliant,
} from './src/model/mornicaModels.ts';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = 3000;
const HOST = '0.0.0.0';

const catalogSource = new DemoCatalogSource();
const catalogRights = LicensedCatalogConfig.demoRights();

async function createApp() {
  const app = express();
  app.use(express.json());

  /**
   * Server-side gated media proxy for offline Cache API saves of verified
   * demo/licensed tracks. Never an open proxy: resolves only by catalog ID and
   * enforces LicensedCatalogRights + exact host allowlist before fetching.
   */
  app.get('/api/catalog/media', async (req, res) => {
    const trackId = typeof req.query.id === 'string' ? req.query.id : '';
    const territory =
      typeof req.query.territory === 'string' && req.query.territory
        ? req.query.territory
        : process.env.MORNICA_DEFAULT_REGION || 'NG';

    const track = catalogSource.byId(trackId);
    if (!track) {
      res.status(404).json({ error: 'Catalog track not found' });
      return;
    }

    const decision = catalogRights.evaluate(
      track,
      CatalogAccess.SAVE_OFFLINE,
      territory,
    );
    if (!decision.allowed) {
      res.status(403).json({
        error: 'Offline save not authorized for this track',
        denial: decision.denial,
      });
      return;
    }

    try {
      const upstream = await fetch(track.mediaUrl);
      if (!upstream.ok || !upstream.body) {
        res.status(502).json({ error: 'Upstream audio unavailable' });
        return;
      }

      const contentType = upstream.headers.get('content-type') || 'audio/mpeg';
      const contentLength = upstream.headers.get('content-length');
      res.setHeader('Content-Type', contentType);
      if (contentLength) {
        res.setHeader('Content-Length', contentLength);
      }
      res.setHeader('Cache-Control', 'public, max-age=86400');

      const arrayBuffer = await upstream.arrayBuffer();
      res.send(Buffer.from(arrayBuffer));
    } catch {
      res.status(502).json({ error: 'Failed to fetch authorized media' });
    }
  });

  /**
   * Optional live YouTube Data API v3 proxy (keeps YOUTUBE_API_KEY strictly on server).
   */
  app.get('/api/youtube/search', async (req, res) => {
    const apiKey = process.env.YOUTUBE_API_KEY?.trim() || '';
    const query = typeof req.query.q === 'string' ? req.query.q.trim() : '';
    if (!apiKey || !query) {
      res.json({ configured: false, items: [] });
      return;
    }

    try {
      const encoded = encodeURIComponent(`${query} official music video`);
      const url = `https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&videoEmbeddable=true&maxResults=8&regionCode=NG&q=${encoded}&key=${encodeURIComponent(apiKey)}`;
      const upstream = await fetch(url);
      if (!upstream.ok) {
        res.json({ configured: true, items: [] });
        return;
      }
      const data = (await upstream.json()) as {
        items?: Array<{
          id?: { videoId?: string };
          snippet?: {
            title?: string;
            channelTitle?: string;
            channelId?: string;
            publishedAt?: string;
          };
        }>;
      };
      const items: YouTubeVideoCard[] = [];
      for (const entry of data.items ?? []) {
        const videoId = entry.id?.videoId ?? '';
        const snippet = entry.snippet;
        if (!videoId || !snippet) continue;
        const card: YouTubeVideoCard = {
          videoId,
          title: snippet.title || 'Official Music Video',
          channelTitle: snippet.channelTitle || query,
          channelId: snippet.channelId || '',
          category: query,
          publishedAt: snippet.publishedAt || '2026',
          sourceBadge: YouTubeSourceBadge.LIVE_YOUTUBE_API,
          isFreeUnpaywalled: true,
        };
        if (isVideoPolicyCompliant(card)) {
          items.push(card);
        }
      }
      res.json({ configured: true, items });
    } catch {
      res.json({ configured: true, items: [] });
    }
  });

  if (process.env.NODE_ENV === 'production') {
    const distDir = path.join(__dirname, 'dist');
    app.use(express.static(distDir));
    app.get('*splat', (_req, res) => {
      res.sendFile(path.join(distDir, 'index.html'));
    });
  } else {
    const { createServer: createViteServer } = await import('vite');
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  }

  app.listen(PORT, HOST, () => {
    console.log(`Moronto Mornica running on http://${HOST}:${PORT}`);
  });
}

createApp();
